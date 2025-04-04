package com.example.vk10;

import java.util.ArrayList;

public class CarDataStorage {
    private static CarDataStorage instance;
    private volatile String city;
    private volatile int year;
    private static ArrayList<CarData> carData = new ArrayList<>();

    private CarDataStorage() { }

    public static CarDataStorage getInstance() {
        if (instance == null) {
            instance = new CarDataStorage();
        }
        return instance;
    }

    public ArrayList<CarData> getCarData() {
        return carData;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public void clearData() {
        carData.clear();
    }

    public String getCity() {
        return city;
    }

    public int getYear() {
        return year;
    }

    public static void addCarData(CarData newCarData) {
        carData.add(newCarData);
    }

}
