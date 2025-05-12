package com.securebuild;

import java.util.ArrayList;

public class BuildListener implements EventListener<Events.BuildEvent> {
    private final ArrayList<String> dirs;
    private final EventBus bus;


    public BuildListener(ArrayList<String> dirs, EventBus bus) {
        this.dirs = dirs;
        this.bus = bus;
        this.bus.subscribe("BuildEvent", this);
    }
    @Override
    public void onEvent(Events.BuildEvent e) {
        if ("BuildStart".equals(e.action)) {startWatching(e);}
        else if ("BuildStopped".equals(e.action)) {stopWatching();}
    }

    private void startWatching(Events.BuildEvent buildProcess) {
        WatchFlag watching = WatchFlag.getInstace();
        ++watching.processesWatched;
        if(watching.flag.getAndSet(true)) {
            return;
        }
        Thread watchThread = new Thread(() -> {
            try {
                DirectoryWatcher dw = new DirectoryWatcher(this.dirs, bus, watching);
                dw.watch(); // Watch while flag is true
            } catch (Exception e) {
                System.out.println("Watcher error: " + e.getMessage());
            }
        });
        watchThread.start();
        System.out.println("Build Process Started: " + buildProcess.buildProcess);
    }

    private void stopWatching() {
        WatchFlag watching = WatchFlag.getInstace();
        --watching.processesWatched;
        if (watching.processesWatched == 0){
            watching.flag.set(false);
            System.out.println("All build processes stopped\n");
        }
    }
}



