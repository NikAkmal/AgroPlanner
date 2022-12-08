package com.example.agroplanner;

public class User {

    public String fullname, username, email;
    public String imageurl;

    public User () {

    }

    public User(String fullname, String username, String email, String imageurl) {
        this.fullname = fullname;
        this.username = username;
        this.email = email;
        this.imageurl = imageurl;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getImageurl() {
        return imageurl;
    }

    public void setImageurl(String imageurl) {
        this.imageurl = imageurl;
    }
}
