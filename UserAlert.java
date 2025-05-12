package com.securebuild;

import javax.swing.*;
public class UserAlert implements EventListener<Events.AlertEvent> {

    public UserAlert(EventBus bus) {
        bus.subscribe("AlertEvent", this);
    }

    public void createAlert(String fileName) {
        JOptionPane.showMessageDialog(null, "File "+fileName+" was altered during compilation", "Alert", JOptionPane.INFORMATION_MESSAGE);

    }
    public void onEvent(Events.AlertEvent e) {
        this.createAlert(e.alteredFile);
    }
}


