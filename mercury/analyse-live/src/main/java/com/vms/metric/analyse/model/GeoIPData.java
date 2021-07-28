package com.vms.metric.analyse.model;

/**
 */
public class GeoIPData {
    private String countryCode;
    private String stateCode;
    private String cityName;
    private double latitude;
    private double longitude;
    private String geohash;
    private Integer autonomousSystemNumber;
    private String autonomousSystemOrganization;

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getGeohash() {
        return geohash;
    }

    public void setGeohash(String geohash) {
        this.geohash = geohash;
    }

    public Integer getAutonomousSystemNumber() {
        return autonomousSystemNumber;
    }

    public GeoIPData setAutonomousSystemNumber(Integer autonomousSystemNumber) {
        this.autonomousSystemNumber = autonomousSystemNumber;
        return this;
    }

    public String getAutonomousSystemOrganization() {
        return autonomousSystemOrganization;
    }

    public void setAutonomousSystemOrganization(String autonomousSystemOrganization) {
        this.autonomousSystemOrganization = autonomousSystemOrganization;
    }

    @Override
    public String toString() {
        return "GeoIPData{" +
                "countryCode='" + countryCode + '\'' +
                ", stateCode='" + stateCode + '\'' +
                ", cityName='" + cityName + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", geohash='" + geohash + '\'' +
                ", autonomousSystemNumber=" + autonomousSystemNumber +
                ", autonomousSystemOrganization='" + autonomousSystemOrganization + '\'' +
                '}';
    }
}
