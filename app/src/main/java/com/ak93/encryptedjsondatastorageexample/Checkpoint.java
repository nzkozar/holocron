package com.ak93.encryptedjsondatastorageexample;

/**
 * Created by Anže Kožar on 11.2.2017.
 */

public class Checkpoint{

    private long id;

    private String name;
    private double longitude,latitude;

    public Checkpoint(long id, String name, double longitude, double latitude){
        this.id = id;
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }
}
