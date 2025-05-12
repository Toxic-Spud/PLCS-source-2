package com.securebuild;

import com.sun.jna.platform.win32.*;

public class PrivilegeEnabler {
    public static void enableSeDebugPrivilege() {
        WinNT.HANDLEByReference hToken = new WinNT.HANDLEByReference();
        // Open the current process token
        if (!Advapi32.INSTANCE.OpenProcessToken(
                Kernel32.INSTANCE.GetCurrentProcess(),
                WinNT.TOKEN_ADJUST_PRIVILEGES | WinNT.TOKEN_QUERY,
                hToken)) {
            System.err.println("Failed to open process token.");

        }
        // Lookup LUID for SeDebugPrivilege
        WinNT.LUID luid = new WinNT.LUID();
        if (!Advapi32.INSTANCE.LookupPrivilegeValue(null, WinNT.SE_DEBUG_NAME, luid)) {
            System.err.println("Failed to lookup privilege value.");
        }
        // Enable the seDebug
        WinNT.TOKEN_PRIVILEGES tp = new WinNT.TOKEN_PRIVILEGES(1);
        tp.Privileges[0] = new WinNT.LUID_AND_ATTRIBUTES(luid, new WinDef.DWORD(WinNT.SE_PRIVILEGE_ENABLED));
        tp.write();
        if (!Advapi32.INSTANCE.AdjustTokenPrivileges(
                hToken.getValue(),
                false,
                tp,
                0,
                null,
                null)) {
            System.err.println("Failed to adjust token privileges.");
        }
        int error = Kernel32.INSTANCE.GetLastError();
        if (error == WinError.ERROR_NOT_ALL_ASSIGNED) {
            System.err.println("SeDebugPrivilege was not assigned. You may need to run as administrator.");
        }
        else{
            System.out.println("SeDebugPrivilege successfully enabled.");
        }
    }
}



