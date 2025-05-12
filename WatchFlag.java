package com.securebuild;

import java.util.concurrent.atomic.AtomicBoolean;

public final class WatchFlag{
    public AtomicBoolean flag;
    public int processesWatched;
    private static WatchFlag instance;
    private WatchFlag(){
        this.processesWatched = 0;
        this.flag = new AtomicBoolean(false);
    }

    static public synchronized WatchFlag getInstace(){
        if (instance == null){
            instance = new WatchFlag();
        }
        return instance;
    }

}
