package com.securebuild;

import com.sun.jna.Native;
import com.sun.jna.platform.win32.*;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinNT.HANDLEByReference;
import com.sun.jna.ptr.IntByReference;

import java.util.ArrayList;

public class SeDebugPrivilegeChecker {
    public static boolean processHasSeDebugPrivilege(HANDLE hProcess) {
        HANDLEByReference hToken = new HANDLEByReference();
        if (!Advapi32.INSTANCE.OpenProcessToken(hProcess, WinNT.TOKEN_QUERY, hToken)) {
            return false;
        }
        IntByReference size = new IntByReference();
        Advapi32.INSTANCE.GetTokenInformation(
                hToken.getValue(),
                WinNT.TOKEN_INFORMATION_CLASS.TokenPrivileges,
                null,
                0,
                size
        );
        WinNT.TOKEN_PRIVILEGES privileges = new WinNT.TOKEN_PRIVILEGES((size.getValue()-4)/12);
        boolean result = Advapi32.INSTANCE.GetTokenInformation(
                hToken.getValue(),
                WinNT.TOKEN_INFORMATION_CLASS.TokenPrivileges,
                privileges,
                size.getValue(),
                size
        );
        if (!result) {
            Kernel32.INSTANCE.CloseHandle(hToken.getValue());
            return false;
        }
        int privilegeCount = (size.getValue()-4)/12;
        WinNT.LUID luid = new WinNT.LUID();
        if (!Advapi32.INSTANCE.LookupPrivilegeValue(null, WinNT.SE_DEBUG_NAME, luid)) {
            Kernel32.INSTANCE.CloseHandle(hToken.getValue());
            return false;
        }
        for (int i = 0; i < privilegeCount; i++) {
            WinNT.LUID_AND_ATTRIBUTES entry = privileges.Privileges[i];
            entry.read();
            if (entry.Luid.HighPart == luid.HighPart && entry.Luid.LowPart == luid.LowPart && entry.Attributes.intValue() == 2) {
                Kernel32.INSTANCE.CloseHandle(hToken.getValue());
                return true;
            }
        }
        Kernel32.INSTANCE.CloseHandle(hToken.getValue());
        return false;
    }

    public static ArrayList<String> getPrivilegedProcesses() {
        HANDLE snapshot = Kernel32.INSTANCE.CreateToolhelp32Snapshot(
                new WinDef.DWORD(2), new WinDef.DWORD(0));
        if (WinBase.INVALID_HANDLE_VALUE.equals(snapshot)) {
            System.err.println("Failed to take process snapshot");
            return new ArrayList<>();
        }
        Tlhelp32.PROCESSENTRY32 entry = new Tlhelp32.PROCESSENTRY32();
        if (!Kernel32.INSTANCE.Process32First(snapshot, entry)) {
            System.err.println("Failed to get first process");
            Kernel32.INSTANCE.CloseHandle(snapshot);
            return new ArrayList<>();
        }
        ArrayList<String> privilegedProcesses = new ArrayList<>();
        do {
            HANDLE hProcess = Kernel32.INSTANCE.OpenProcess(
                    WinNT.PROCESS_QUERY_INFORMATION,
                    false,
                    entry.th32ProcessID.intValue()
            );
            if (hProcess != null) {
                boolean hasDebug = processHasSeDebugPrivilege(hProcess);
                if (hasDebug) {
                    privilegedProcesses.add("Process [" + entry.th32ProcessID.intValue() + "] " + Native.toString(entry.szExeFile));
                }
                Kernel32.INSTANCE.CloseHandle(hProcess);
            }
        } while (Kernel32.INSTANCE.Process32Next(snapshot, entry));
        Kernel32.INSTANCE.CloseHandle(snapshot);
        return privilegedProcesses;
    }
}


