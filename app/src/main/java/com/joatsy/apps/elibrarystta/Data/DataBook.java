package com.joatsy.apps.elibrarystta.Data;

public class DataBook {
    private String id;
    private String value;
    private String id_root;
    private String file_location;
    private int status;

    public DataBook(String id_root, String id, String value, int status,String file_location) {
        this.id_root = id_root;
        this.id = id;
        this.value = value;
        this.status = status;
        this.file_location = file_location;
    }

    public String getIdRoot() {
        return this.id_root;
    }

    public void setIdRoot(String id_root) {
        this.id_root = id_root;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getStatus() {
        return this.status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getLocation() {
        return this.file_location;
    }

    public void setLocation(String file_location) {
        this.file_location = file_location;
    }
}
