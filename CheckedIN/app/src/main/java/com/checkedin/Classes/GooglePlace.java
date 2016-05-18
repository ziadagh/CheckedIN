package com.checkedin.Classes;

public class GooglePlace {
    private String name;
    private String category;

    public GooglePlace() {
        this.name = "";
        this.setCategory("");
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
