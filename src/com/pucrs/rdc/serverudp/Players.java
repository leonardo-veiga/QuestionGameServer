package com.pucrs.rdc.serverudp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Players {

    private Map<String,UserInfo> players;

    public Players() {
        this.players = new HashMap<>();
    }

    public Map<String, UserInfo> getPlayers() {
        return players;
    }

    public void setPlayers(Map<String, UserInfo> users) {
        this.players = users;
    }

    public void addPlayer(UserInfo userInfo) {
        this.players.put(userInfo.getName(), userInfo);
    }

    public UserInfo findUserByName(String userName) {
        return this.players.get(userName);
    }

    public List<UserInfo> getPlayersWithNoVote() {
        List<UserInfo> playersList = new ArrayList<>();
        for (Map.Entry<String, UserInfo> player : players.entrySet()) {
            if(player.getValue().getChosenDifficulty() == 0) {
                playersList.add(player.getValue());
            }
        }

        return playersList;
    }
}
