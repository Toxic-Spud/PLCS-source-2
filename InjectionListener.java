package com.securebuild;

import java.util.ArrayList;
import java.util.Set;

public class InjectionListener implements EventListener<Events.FileEvent> {
    private final Set<String> fileExtensions;
    EventBus bus;

    public InjectionListener(EventBus bus, Set<String> fileExtensions) {
        this.bus = bus;
        this.bus.subscribe("FileEvent", this);
        this.fileExtensions = fileExtensions;
    }

    @Override
    public void onEvent(Events.FileEvent e) {
        if (this.fileExtensions.contains(e.fileName.split("[.]")[e.fileName.split("[.]").length-1].toLowerCase()) || this.fileExtensions.contains("*")) {
            ArrayList<String> privilegedProcesses =  SeDebugPrivilegeChecker.getPrivilegedProcesses();
            Events.AlertEvent alert = new Events.AlertEvent(e.fileName.toLowerCase(), privilegedProcesses);
            this.bus.publish(alert);
        }
    }
}


