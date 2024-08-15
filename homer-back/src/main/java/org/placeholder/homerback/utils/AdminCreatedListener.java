package org.placeholder.homerback.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AdminCreatedListener implements ApplicationListener<OnAdminCreatedEvent> {

    @Autowired
    private SendGridMailService mailService;

    @Override
    public void onApplicationEvent(OnAdminCreatedEvent event) {
        this.sendPassword(event);
    }

    private void sendPassword(OnAdminCreatedEvent event) {
        String password = event.getPassword();
        mailService.sendAdminPassword(password);
    }
}