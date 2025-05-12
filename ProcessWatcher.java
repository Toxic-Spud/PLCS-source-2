package com.securebuild;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.*;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ProcessWatcher {
    private final EventBus bus;
    private Set<String> buildProcesses;
    private Set<String> runningBuildProcesses;

    public ProcessWatcher(EventBus bus, Set<String> buildProcesses) {
        this.bus = bus;
        this.buildProcesses = buildProcesses;
        this.runningBuildProcesses = new HashSet<String>();
    }

    public void start() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            var running = getRunningBuildProcesses();
            for (var proc: running.toArray()) {
                if( !this.runningBuildProcesses.contains(proc.toString())){
                    this.bus.publish(new Events.BuildEvent("BuildStart", proc.toString(),Integer.valueOf(proc.toString().split("/")[1])));
                }
            }
            for (var proc: this.runningBuildProcesses.toArray()) {
                if( !running.contains(proc.toString())){
                    this.bus.publish(new Events.BuildEvent( "BuildStopped", proc.toString(),Integer.valueOf(proc.toString().split("/")[1])));
                }
            }
            this.runningBuildProcesses = running;
        }, 0, 10, TimeUnit.MILLISECONDS);
    }


    private Set<String> getRunningBuildProcesses() {
        Set<String> runningBuildProcessList = new HashSet<>();
        WinNT.HANDLE snapshot = Kernel32.INSTANCE.CreateToolhelp32Snapshot(
                new WinDef.DWORD(2), new WinDef.DWORD(0));
        if (WinBase.INVALID_HANDLE_VALUE.equals(snapshot)) {
            System.err.println("Failed to take process snapshot");
            return new HashSet<>();
        }
        Tlhelp32.PROCESSENTRY32 entry = new Tlhelp32.PROCESSENTRY32();
        if (!Kernel32.INSTANCE.Process32First(snapshot, entry)) {
            System.err.println("Failed to get first process");
            Kernel32.INSTANCE.CloseHandle(snapshot);
            return new HashSet<>();
        }
        do {
            if (this.buildProcesses.contains(Native.toString(entry.szExeFile).toLowerCase(Locale.getDefault()))){
                runningBuildProcessList.add(Native.toString(entry.szExeFile).toLowerCase(Locale.getDefault())+"/"+entry.th32ProcessID.toString());
            }
        } while (Kernel32.INSTANCE.Process32Next(snapshot, entry));
        Kernel32.INSTANCE.CloseHandle(snapshot);
        return runningBuildProcessList;
    }
}
