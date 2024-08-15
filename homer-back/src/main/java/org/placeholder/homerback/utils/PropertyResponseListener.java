package org.placeholder.homerback.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class PropertyResponseListener implements ApplicationListener<OnPropertyResponseEvent> {

    @Autowired private SendGridMailService mailService;

    @Override
    public void onApplicationEvent(OnPropertyResponseEvent event) {
        mailService.sendPropertyResponse(event);
    }
}
