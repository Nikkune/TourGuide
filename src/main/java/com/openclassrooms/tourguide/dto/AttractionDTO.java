package com.openclassrooms.tourguide.dto;

import gpsUtil.location.Location;

/**
 * Data Transfer Object for attraction information
 * Contains information about an attraction, its location, the user's location,
 * the distance between them, and the reward points for visiting the attraction.
 */
public class AttractionDTO {
    private String attractionName;
    private Location attractionLocation;
    private Location userLocation;
    private double distance;
    private int rewardPoints;

    public AttractionDTO() {
    }

    public AttractionDTO(String attractionName, Location actrctionLocation, Location userLocation, double distance, int rewardPoints) {
        this.attractionName = attractionName;
        this.attractionLocation = actrctionLocation;
        this.userLocation = userLocation;
        this.distance = distance;
        this.rewardPoints = rewardPoints;
    }

    public String getAttractionName() {
        return attractionName;
    }

    public void setAttractionName(String attractionName) {
        this.attractionName = attractionName;
    }

    public Location getAttractionLocation() {
        return attractionLocation;
    }

    public void setAttractionLocation(Location atractionLocation) {
        this.attractionLocation = atractionLocation;
    }

    public Location getUserLocation() {
        return userLocation;
    }

    public void setUserLocation(Location userLocation) {
        this.userLocation = userLocation;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getRewardPoints() {
        return rewardPoints;
    }

    public void setRewardPoints(int rewardPoints) {
        this.rewardPoints = rewardPoints;
    }
}