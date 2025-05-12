package com.securebuild;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventBus {
    private final Map<String, List<EventListener>> listeners = new HashMap<>();

    public void subscribe(String eventType, EventListener listener) {
        listeners.computeIfAbsent(eventType, k -> new ArrayList<>()).add(listener);
    }
     public synchronized void publish(Events.Event e) {
        List<EventListener> eventListeners = listeners.getOrDefault(e.eventType, List.of());
        for (EventListener listener : eventListeners) {
            listener.onEvent(e);
        }
    }
    public void unsubscribe(String eventType, EventListener listener) {
        List<EventListener> eventListeners = listeners.getOrDefault(eventType, List.of());
        eventListeners.remove(listener);
    }
    public List<String> getAllEventTypes() {
        return new ArrayList<>(listeners.keySet());
    }
}

