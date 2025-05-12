package com.securebuild;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.WString;
import com.sun.jna.ptr.IntByReference;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;


public class DirectoryWatcher {
    private final ArrayList<String> directories;
    private final EventBus bus;
    private final WatchFlag watching;
    public DirectoryWatcher(ArrayList<String> directories, EventBus bus, WatchFlag watching) {
        this.directories = directories;
        this.bus = bus;
        this.watching = watching;
    }

    public void watch() {
        ArrayList<WinAPI.HANDLE> handles = new ArrayList<>();
        for (String directory : directories) {
            WinAPI.HANDLE hDir = WinAPI.INSTANCE.CreateFileW(
                    new WString(directory),
                    WinAPI.FILE_LIST_DIRECTORY,
                    WinAPI.FILE_SHARE_READ | WinAPI.FILE_SHARE_WRITE | WinAPI.FILE_SHARE_DELETE,
                    null,
                    WinAPI.OPEN_EXISTING,
                    WinAPI.FILE_FLAG_BACKUP_SEMANTICS,
                    null
            );
            if (hDir == null || hDir.getPointer() == Pointer.NULL) {
                System.out.println("Failed to open directory handle.");
            }
            else{
                handles.add(hDir);
            }
        }
        Pointer bufferPtr = new Memory(1024);
        System.out.println("Monitoring for file write events in: " + this.directories);

        while (this.watching.flag.get()) {
            IntByReference bytesReturned = new IntByReference();

            for (WinAPI.HANDLE hDir : handles) {
                boolean success = WinAPI.INSTANCE.ReadDirectoryChangesW(
                        hDir,
                        bufferPtr,
                        1024,
                        true,
                        WinAPI.FILE_NOTIFY_CHANGE_LAST_WRITE | WinAPI.FILE_NOTIFY_CHANGE_FILE_NAME,
                        bytesReturned,
                        null,
                        null
                );
                if (!this.watching.flag.get()){
                    break;
                }
                if (success) {
                    byte[] bytes = bufferPtr.getByteArray(0,bytesReturned.getValue());
                    int fileNameLength = bufferPtr.getInt(8);
                    byte[] fileNameBytes = new byte[fileNameLength];
                    System.arraycopy(bytes,12,fileNameBytes,1,fileNameLength-1);
                    String fileName = new String(fileNameBytes, StandardCharsets.UTF_16);
                    this.bus.publish(new Events.FileEvent(fileName, "FileAltered"));
                } else {
                    System.out.println("Error reading directory changes.");
                }
            }
        }
        for (WinAPI.HANDLE hDir : handles) {
            WinAPI.INSTANCE.CloseHandle(hDir);
        }
        bufferPtr = null;
    }


}