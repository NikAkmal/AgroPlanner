package com.example.agroplanner;

public class Crops {
    private String plot;
    private String plants;
    private String plotid;
    private String imageUrl;
    private String id;


    public Crops() {

    }

    public String getPlants() {return plants;}

    public void setPlants(String plants) {
        this.plants = plants;
    }

    public String getPlot() {
        return plot;
    }

    public void setPlot(String plot) {
        this.plot = plot;
    }

    public String getPlotid() {
        return plotid;
    }

    public void setPlotid(String plotid) {
        this.plotid = plotid;
    }

    public String getImageUrl() {return imageUrl;}

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getId() {return id;}

    public void setId(String plants) {
        this.id = id;
    }
}
