package org.placeholder.homerback.dtos;

import java.time.LocalDateTime;

public class ActionDTO {
    private Integer userId;
    private String username;
    private String email;
    private String actionType;
    private LocalDateTime dateTime;
    private Object value;

    public ActionDTO(Integer userId, String username, String email, String actionType, LocalDateTime dateTime, Object value) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.actionType = actionType;
        this.dateTime = dateTime;
        this.value = value;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
