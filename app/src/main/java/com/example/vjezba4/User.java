package com.example.vjezba4;

public class User {
    private final String username;
    private final String userId;
    private final int perfectScoreCount;

    public User(String username, String userId, int perfectScoreCount) {
        this.username = username;
        this.userId = userId;
        this.perfectScoreCount = perfectScoreCount;
    }

    public String getUsername() {
        return username;
    }

    public String getUserId() {
        return userId;
    }

    public int getPerfectScoreCount() {
        return perfectScoreCount;
    }

    @Override
    public String toString() {
        return username + " - " + perfectScoreCount + " perfect scores";
    }
}
