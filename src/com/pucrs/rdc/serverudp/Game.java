package com.pucrs.rdc.serverudp;

import java.util.*;

public class Game {

    private boolean gameStart;
    private boolean voteStart;
    private int difficulty;
    private List<Question> questions;
    private List<Integer> usedQuestions;

    public Game() {
        this.gameStart = false;
        this.voteStart = false;
        this.difficulty = 0;
        this.questions = new ArrayList<>();
        this.usedQuestions = new ArrayList<>();
    }

    public boolean isGameStart() {
        return gameStart;
    }

    public void setGameStart(boolean gameStart) {
        this.gameStart = gameStart;
    }

    public boolean isVoteStart() {
        return voteStart;
    }

    public void setVoteStart(boolean voteStart) {
        this.voteStart = voteStart;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(int difficulty) {
        this.difficulty = difficulty;
    }

    public int getMajorityDifficulty(Players players) {
        int oneCount = 0;
        int twoCount = 0;
        int threeCount = 0;
        int result = 0;

        if(players.getPlayersWithNoVote().size() == 0) {
            for (Map.Entry<String, UserInfo> player : players.getPlayers().entrySet()) {
                int vote = player.getValue().getChosenDifficulty();

                if(vote == 1) {
                    oneCount++;
                } else if(vote == 2) {
                    twoCount++;
                } else if(vote == 3) {
                    threeCount++;
                }
            }

            if(oneCount > twoCount || oneCount > threeCount) {
                result = 1;
            } else if(twoCount > oneCount || twoCount > threeCount) {
                result =  2;
            } else if(threeCount > oneCount || threeCount > twoCount) {
                result =  3;
            } else {
                result =  new Random().nextInt(3) + 1;
            }
        }

        this.difficulty = result;

        return result;
    }
}
