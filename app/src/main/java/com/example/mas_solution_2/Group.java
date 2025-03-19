package com.example.mas_solution_2;

import java.util.ArrayList;
import java.util.List;

public class Group {
    private String id;
    private String name;
    private String imageUrl;
    private List<String> members;

    public Group() {
        // Required empty constructor for Firestore
        members = new ArrayList<>();
    }

    public Group(String id, String name, String imageUrl) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.members = new ArrayList<>();
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<String> getMembers() {
        return members;
    }

    public void setMembers(List<String> members) {
        this.members = members;
    }

    public void addMember(String memberId) {
        if (!members.contains(memberId)) {
            members.add(memberId);
        }
    }

    public void removeMember(String memberId) {
        members.remove(memberId);
    }
}