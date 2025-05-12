package com.securebuild;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.PointerType;
import com.sun.jna.WString;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;
import com.sun.jna.win32.W32APIOptions;

public interface WinAPI extends StdCallLibrary {
    WinAPI INSTANCE = Native.load("Kernel32", WinAPI.class, W32APIOptions.DEFAULT_OPTIONS);

    int FILE_LIST_DIRECTORY = 0x0001;
    int FILE_SHARE_READ = 0x00000001;
    int FILE_SHARE_WRITE = 0x00000002;
    int FILE_SHARE_DELETE = 0x00000004;
    int FILE_NOTIFY_CHANGE_CREATION = 0x00000040;
    int OPEN_EXISTING = 3;
    int FILE_FLAG_BACKUP_SEMANTICS = 0x02000000;
    int FILE_NOTIFY_CHANGE_LAST_WRITE = 0x00000010;
    int FILE_NOTIFY_CHANGE_FILE_NAME = 0x00000001;
    HANDLE CreateFileW(WString lpFileName, int dwDesiredAccess, int dwShareMode,
                       Pointer lpSecurityAttributes, int dwCreationDisposition,
                       int dwFlagsAndAttributes, HANDLE hTemplateFile);

    boolean ReadDirectoryChangesW(HANDLE hDirectory,
                                  Pointer lpBuffer,
                                  int nBufferLength,
                                  boolean bWatchSubtree,
                                  int dwNotifyFilter,
                                  IntByReference lpBytesReturned,
                                  Pointer lpOverlapped,
                                  Pointer lpCompletionRoutine);

    boolean CloseHandle(HANDLE hObject);

    class HANDLE extends PointerType {}
}