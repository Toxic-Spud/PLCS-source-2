package com.securebuild;


public interface EventListener<T extends Events.Event> {
    void onEvent(T e);//on event takes any object that is a subclass of the event class
}


