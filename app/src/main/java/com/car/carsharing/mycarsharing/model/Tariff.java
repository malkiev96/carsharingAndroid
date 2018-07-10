package com.car.carsharing.mycarsharing.model;

public class Tariff {

    private int id;

    private String name;

    private Float payRent;

    private Float payWaiting;

    private Float payBooking;

    private int freeBookingMin;

    private boolean enabled;

    private Float payCrash;

    private String freeWaitingStart;

    private String freeWaitingEnd;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Float getPayRent() {
        return payRent;
    }

    public void setPayRent(Float payRent) {
        this.payRent = payRent;
    }

    public Float getPayWaiting() {
        return payWaiting;
    }

    public void setPayWaiting(Float payWaiting) {
        this.payWaiting = payWaiting;
    }

    public Float getPayBooking() {
        return payBooking;
    }

    public void setPayBooking(Float payBooking) {
        this.payBooking = payBooking;
    }

    public int getFreeBookingMin() {
        return freeBookingMin;
    }

    public void setFreeBookingMin(int freeBookingMin) {
        this.freeBookingMin = freeBookingMin;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Float getPayCrash() {
        return payCrash;
    }

    public void setPayCrash(Float payCrash) {
        this.payCrash = payCrash;
    }

    public String getFreeWaitingStart() {
        return freeWaitingStart;
    }

    public void setFreeWaitingStart(String freeWaitingStart) {
        this.freeWaitingStart = freeWaitingStart;
    }

    public String getFreeWaitingEnd() {
        return freeWaitingEnd;
    }

    public void setFreeWaitingEnd(String freeWaitingEnd) {
        this.freeWaitingEnd = freeWaitingEnd;
    }
}
