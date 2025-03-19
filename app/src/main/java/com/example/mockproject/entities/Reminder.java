package com.example.mockproject.entities;

public class Reminder {
    private int id;
    private String time;
    private Movie movie;

    public Reminder(int id, String time, Movie movie) {
        this.id = id;
        this.time = time;
        this.movie = movie;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Movie getMovie() {
        return movie;
    }

    public void setMovie(Movie movie) {
        this.movie = movie;
    }
}
