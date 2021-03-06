package com.example.finish1m.Domain.Models;

import java.util.ArrayList;

// метка

public class Locate {

    private String id;

    // координаты
    private double longitude;
    private double latitude;
    private String address;
    private String title; // заголовок
    private String message; // описание
    private String chatId; // ссылка на чат

    private ArrayList<String> imageRefs; // ссылки на изображеия

    public Locate(String id, double longitude, double latitude, String address, String title, String message, String chatId, ArrayList<String> imageRefs) {
        this.id = id;
        this.longitude = longitude;
        this.latitude = latitude;
        this.address = address;
        this.title = title;
        this.message = message;
        this.chatId = chatId;
        this.imageRefs = imageRefs;
    }

    public Locate() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public ArrayList<String> getImageRefs() {
        return imageRefs;
    }

    public void setImageRefs(ArrayList<String> imageRefs) {
        this.imageRefs = imageRefs;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public String toString() {
        return "Locate{" +
                "id='" + id + '\'' +
                ", longitude=" + longitude +
                ", latitude=" + latitude +
                ", title='" + title + '\'' +
                ", message='" + message + '\'' +
                ", chatId='" + chatId + '\'' +
                ", imageRefs=" + imageRefs +
                '}';
    }

}
