package com.example.mockproject.entities;

import java.util.List;

public class CreditResponse {
    private int id;
    private List<Person> cast;
    private List<Person> crew;

    public int getId() {
        return id;
    }

    public List<Person> getCrew() {
        return crew;
    }

    public List<Person> getCast() {
        return cast;
    }
}
