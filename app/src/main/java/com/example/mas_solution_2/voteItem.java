package com.example.mas_solution_2;

public class voteItem {

    private String location;
    int vote = 0;

    public voteItem(String location, int vote){
        this.location = location;
        this.vote = vote;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getVote() {
        return vote;
    }

    public void setVote(int vote) {
        this.vote = vote;
    }

    public void voteThis(){
        this.setVote(this.vote + 1);
    }

}
