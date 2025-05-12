package com.securebuild;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.message.MapMessage;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Events {
    public static class Event {
        final Date timestamp;
        final String eventType;
        public Event(String eventType){
            this.timestamp = new Date();
            this.eventType = eventType;
        }
        public String getEventType() {
            return eventType;
        }


        public MapMessage toMapMessage() {
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                return new MapMessage(objectMapper.convertValue(this, new TypeReference<Map<String, Object>>() {}));
            } catch (Exception e) {
                return new MapMessage(new HashMap<String, Object>());
            }
        }
    }

    public static class BuildEvent extends Event {
        final String action;
        final String buildProcess;
        final int processID;
        public BuildEvent( String action, String buildProcess, int processID){
            super("BuildEvent");
            this.action = action;
            this.buildProcess = buildProcess;
            this.processID = processID;
        }
        public String getAction() {
            return action;
        }
        public String getBuildProcess() {
            return buildProcess;
        }
    }

    public static class FileEvent extends Event {
        final String fileName;
        final String action;
        public FileEvent(String fileName, String action){
            super("FileEvent");
            this.fileName = fileName;
            this.action = action;
        }
        public String getFileName() {
            return fileName;
        }

    }

    public static class AlertEvent extends Event {
        final String alteredFile;
        final ArrayList<String> runningPrivilegedProcesses;
        public AlertEvent(String fileName, ArrayList<String> privilegedProcesses) {
            super("AlertEvent");
            this.alteredFile = fileName;
            this.runningPrivilegedProcesses = privilegedProcesses;
        }
        public String getAlteredFile() {
            return alteredFile;
        }
        public ArrayList<String> getRunningPrivilegedProcesses() {
            return runningPrivilegedProcesses;
        }
    }
}
