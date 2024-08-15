package org.placeholder.homerback.utils;

import org.placeholder.homerback.entities.User;
import org.springframework.context.ApplicationEvent;

public class OnAdminCreatedEvent extends ApplicationEvent {
    private String password;

    public OnAdminCreatedEvent(String password) {
        super(password);
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}