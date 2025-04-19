package com.example.watch_gatherers;

import com.google.firebase.Timestamp;
import java.util.Date;

public class Hangout {
    private String id;
    private String name;
    private String location;
    private Timestamp date;
    private String groupId;

    // Required empty constructor
    public Hangout() {}

    public Hangout(String id, String name, String location, Timestamp date, String groupId) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.date = date;
        this.groupId = groupId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public boolean isUpcoming() {
        return date != null && date.toDate().after(new Date());
    }
}