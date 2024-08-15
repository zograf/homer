package org.placeholder.homerback.dtos;

import org.placeholder.homerback.entities.User;

public class UserDTO {
    private String name;
    private String email;

    public UserDTO() {

    }

    public UserDTO(User user) {
        this.name = user.getName();
        this.email = user.getEmail();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
