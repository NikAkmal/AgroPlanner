package com.example.agroplanner;

public class Calendar {
    private int day;
    private int year;
    private int month;

    private String description;
    private String fertilizer;
    private String imageUrl;

    private String id;
    private String postid;
    private String type;


    public Calendar() {

    }

    public int getDay() {return day;}

    public void setDay(int day) {
        this.day = day;
    }

    public int getMonth() {return month;}

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {return year;}

    public void setYear(int year) {
        this.year = year;
    }

    public String getDescription() {return description;}

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFertilizer() {return fertilizer;}

    public void setFertilizer(String fertilizer) {
        this.fertilizer = fertilizer;
    }

    public String getId() {return id;}

    public void setId(String plants) {
        this.id = id;
    }

    public String getImageUrl() {return imageUrl;}

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getPostid() {return postid;}

    public void setPostid(String postid) {
        this.postid = postid;
    }

    public String getType() {return type;}

    public void setType(String type) {
        this.type = type;
    }

}



