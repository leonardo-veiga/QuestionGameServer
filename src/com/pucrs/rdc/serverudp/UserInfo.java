package com.pucrs.rdc.serverudp;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

public class UserInfo {
    private String name;
    private InetAddress clientIP;
    private int port;
    private int points;
    private int chosenDifficulty;

    public UserInfo(String name, InetAddress clientIP, int port) {
        this.name = name;
        this.clientIP = clientIP;
        this.port = port;
        this.points = 0;
        this.chosenDifficulty = 0;
    }

    public UserInfo() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public InetAddress getClientIP() {
        return clientIP;
    }

    public void setClientIP(InetAddress clientIP) {
        this.clientIP = clientIP;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public int getChosenDifficulty() {
        return chosenDifficulty;
    }

    public void setChosenDifficulty(int chosenDifficulty) {
        this.chosenDifficulty = chosenDifficulty;
    }

    public void addPoint() {
        this.points++;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "name='" + name + '\'' +
                ", clientIP=" + clientIP +
                ", port=" + port +
                ", points=" + points +
                ", chosenDifficulty=" + chosenDifficulty +
                '}';
    }
}
