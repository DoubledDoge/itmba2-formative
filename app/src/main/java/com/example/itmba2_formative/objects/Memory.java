package com.example.itmba2_formative.objects;

import android.net.Uri;

public class Memory {
    private final int id;
    private final String title;
    private final String description;
    private final Uri photoUri;
    private final Uri musicUri;
    private final String location;

    public Memory(int id, String title, String description, Uri photoUri, Uri musicUri, String location) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.photoUri = photoUri;
        this.musicUri = musicUri;
        this.location = location;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Uri getPhotoUri() { return photoUri; }
    public Uri getMusicUri() { return musicUri; }
    public String getLocation() { return location; }
}
