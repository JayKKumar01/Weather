package com.example.weather.interfaces;

public interface NotificationListener {
    void onToogleMic();
    void onHangUp();
    void onToogleDeafen();
    void onUpdateTime(int sec);
}
