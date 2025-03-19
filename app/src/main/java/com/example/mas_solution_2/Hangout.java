package com.example.mas_solution_2;

import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Hangout {
    private String id;
    private String name;
    private String location;
    private String imageUrl;
    private Timestamp date;
    private String groupId;
    private boolean isPast;
    private List<String> participants;
    private List<String> photoUrls;

    public Hangout() {
        // Required empty constructor for Firestore
        participants = new ArrayList<>();
        photoUrls = new ArrayList<>();
    }

    public Hangout(String id, String name, String location, String imageUrl, Timestamp date, String groupId) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.imageUrl = imageUrl;
        this.date = date;
        this.groupId = groupId;
        this.isPast = date.toDate().before(new Date());
        this.participants = new ArrayList<>();
        this.photoUrls = new ArrayList<>();
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Timestamp getDate() {
        return date;
    }

    public void setDate(Timestamp date) {
        this.date = date;
        this.isPast = date.toDate().before(new Date());
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public boolean isPast() {
        return isPast;
    }

    public void setPast(boolean past) {
        isPast = past;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }

    public void addParticipant(String participantId) {
        if (!participants.contains(participantId)) {
            participants.add(participantId);
        }
    }

    public void removeParticipant(String participantId) {
        participants.remove(participantId);
    }

    public List<String> getPhotoUrls() {
        return photoUrls;
    }

    public void setPhotoUrls(List<String> photoUrls) {
        this.photoUrls = photoUrls;
    }

    public void addPhotoUrl(String photoUrl) {
        if (!photoUrls.contains(photoUrl)) {
            photoUrls.add(photoUrl);
        }
    }
}