package org.placeholder.homerback.dtos;

public class DenyPropertyDTO {
    private Integer id;
    private String reason;

    public DenyPropertyDTO() {}

    public DenyPropertyDTO(Integer id, String reason) {
        this.id = id;
        this.reason = reason;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
