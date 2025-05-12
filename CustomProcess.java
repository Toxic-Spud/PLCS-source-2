package com.securebuild;

public class CustomProcess {
    final String processName;
    final String filePath;
    final int pid;
    final boolean hasSeDebug;
    public CustomProcess(String processName, String filePath, int pid, boolean hasSeDebug) {
        this.processName = processName;
        this.filePath = filePath;
        this.pid = pid;
        this.hasSeDebug = hasSeDebug;
    }
}
