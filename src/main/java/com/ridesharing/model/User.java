package com.ridesharing.model;

public class User {

    private int userId;
    private String phoneNo;
    private String name;
    private String gender;
    private String email;
    private String govtId;
    private String behaviour;
    private String passwordHash;

    public User(){
    }

    public User(String phoneNo, String name, String gender, String email, String govtId,
                String behaviour, String passwordHash){
        this.phoneNo = phoneNo;
        this.name = name;
        this.gender = gender;
        this.email = email;
        this.govtId = govtId;
        this.behaviour = behaviour;
        this. passwordHash = passwordHash;
    }

    // Getter
    public int getUserId(){return userId;}

    public String getPhoneNo() {
        return phoneNo;
    }

    public String getName() {
        return name;
    }

    public String getGender() {
        return gender;
    }

    public String getEmail() {
        return email;
    }

    public String getGovtId() {
        return govtId;
    }

    public String getBehaviour() {
        return behaviour;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    // Setter
    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setPhoneNo(String phoneNo) {
        this.phoneNo = phoneNo;
    }

    public void setName(String name) {
        this.name = name;
    }


    public void setGender(String gender) {
        this.gender = gender;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setGovtId(String govtId) {
        this.govtId = govtId;
    }

    public void setBehaviour(String behaviour) {
        this.behaviour = behaviour;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }
}
