package com.securebuild;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        try{
            InetAddress localHost = InetAddress.getLocalHost();
            System.setProperty("hostName", localHost.getHostName());
            System.setProperty("hostAddress", localHost.getHostAddress());

        }catch(Exception e){
            System.setProperty("hostName", "unknown");
            System.setProperty("hostAddress", "0.0.0.0");
        }
        System.setProperty("log4j2.configurationFile", "./log4j2.xml");
        try{
            Logger log = LogManager.getLogger("root");
            log.info("SecureBuildStarting");
        }catch(AppenderLoggingException e){
            System.out.println("Could not connect to logstash only logging locally");
        }

        Properties properties = new Properties();
        String fileName = "./SecureBuild.config";
        try (FileInputStream configFile = new FileInputStream(fileName)) {
            properties.load(configFile);
            System.out.println("Config File found at path: " + fileName);
        } catch (FileNotFoundException ex) {
            System.out.println("Config File not found at path: " + fileName);
        } catch (IOException ex) {
            System.out.println("Error reading config file");
        }
        String buildProcesses = properties.getProperty("build_processes").toLowerCase(Locale.ENGLISH);
        List<String> buildProcessesList = Arrays.asList(buildProcesses.split(","));
        buildProcessesList.replaceAll(s -> s.strip().replaceAll("^\"|\"$", ""));
        Set<String> buildProcessSet = new HashSet<>(buildProcessesList);
        System.out.println("Monitoring Build processes: " + buildProcesses);
        PrivilegeEnabler.enableSeDebugPrivilege();
        EventBus bus = new EventBus();
        String dirsProperty = properties.getProperty("dirs");
        List<String> dirsList = Arrays.asList(dirsProperty.split(","));
        dirsList.replaceAll(s -> s.strip().replaceAll("^\"|\"$", "").replaceAll("/","\\\\"));
        Set<String> dirsSet = new HashSet<>(dirsList);
        ArrayList<String> dirs = new ArrayList<>(dirsSet);
        dirs.replaceAll(s -> s.strip().replaceAll("^\"|\"$", ""));
        System.out.println("Monitoring Dirs: " + dirs);
        String fileTypes = properties.getProperty("fileTypes");
        List<String> extList = Arrays.asList(fileTypes.split(","));
        extList.replaceAll(s -> s.strip().replaceAll("^\"|\"$", "").replaceAll("/","\\\\"));
        Set<String> extSet = new HashSet<>(extList);
        System.out.println("Monitoring File Extensions: " + extSet);
        new InjectionListener(bus, extSet);
        new BuildListener(dirs, bus);
        new UserAlert(bus);
        new logger(bus);
        new ProcessWatcher(bus, buildProcessSet).start();
    }
}