package org.placeholder.homer.simulation.dto;

public class Plate {

    private Integer id;
    private String text;

    public Plate() {
        this.text = "";
    }
    public Plate(Integer id, String text) {
        this.id = id;
        this.text = text;
    }
    public Plate(String text) {
        this.text = text;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
