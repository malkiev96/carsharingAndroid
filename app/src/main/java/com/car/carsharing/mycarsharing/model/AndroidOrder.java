package com.car.carsharing.mycarsharing.model;


public class AndroidOrder {
    private int currentAction;
    private float price;
    private String time;
    private Client client;
    private AndroidCar androidCar;

    public AndroidCar getAndroidCar() {
        return androidCar;
    }

    public void setAndroidCar(AndroidCar androidCar) {
        this.androidCar = androidCar;
    }

    public int getCurrentAction() {
        return currentAction;
    }

    public void setCurrentAction(int currentAction) {
        this.currentAction = currentAction;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }


}
