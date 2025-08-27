package com.example.itmba2_formative.objects;

public class User {
    private final int userId;
    private final String email;
    private final String fullName;

    public User(int userId, String email, String fullName) {
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
    }

    public int getUserId() { return userId; }
    public String getEmail() { return email; }
    public String getFullName() { return fullName; }
}