package com.securebuild;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
public class logger implements EventListener<Events.Event> {

    public logger(EventBus bus) {
        List<String> eventTypes =  bus.getAllEventTypes();
        eventTypes.forEach(eventType -> {bus.subscribe(eventType, this);});

    }
    public void onEvent(Events.Event e) {
        Logger log = LogManager.getLogger("root");
        try {
            if ("BuildEvent".equals(e.eventType)) {
                if (log.isInfoEnabled()){
                    log.info(e.toMapMessage());
                }

            } else {
                if (log.isWarnEnabled()){
                    log.warn(e.toMapMessage());
                }
            }
        }catch (org.apache.logging.log4j.core.appender.AppenderLoggingException err) {
            System.err.println("Could not connect to logstash only logging locally");
        }
    }
}
