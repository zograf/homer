package org.placeholder.homerback.utils;

import org.springframework.context.ApplicationEvent;

public class OnPropertyResponseEvent extends ApplicationEvent {
    private String email;
    private String title;
    private String body;
    private String heroText;
    private String reason;

    public OnPropertyResponseEvent(String email, String title, String heroText, String body, String reason) {
        super(email);
        this.email = email;
        this.title = title;
        this.body = body;
        this.heroText = heroText;
        this.reason = reason;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getHeroText() {
        return heroText;
    }

    public void setHeroText(String heroText) {
        this.heroText = heroText;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
