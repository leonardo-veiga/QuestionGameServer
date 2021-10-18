package com.pucrs.rdc.serverudp;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class UserReceiverThread extends Thread {

    private DatagramSocket udpSocket;
    private boolean stopped = false;

    private static Players players;

    private static Game game;

    ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    public UserReceiverThread(DatagramSocket ds, Players players, Game game) throws SocketException {
        this.udpSocket = ds;
        ds.setBroadcast(true);
        this.players = players;
        this.game = game;
    }

    public void disconnect() {
        this.stopped = true;
    }

    Runnable gameRunnable = new Runnable() {
        public void run() {
            if(!game.isVoteStart()) {
                if (players.getPlayers().size() < 2) {
                    sendMessagetoAll("Esperando por pelo menos 2 jogadores");
                } else {
                    game.setVoteStart(true);
                    sendMessagetoAll("2 jogadores presentes! O jogo irá começar.");
                }
            } else {
                if(game.getDifficulty() == 0) {
                    List<UserInfo> playersWithNoVote = players.getPlayersWithNoVote();
                    for (UserInfo player: playersWithNoVote) {
                        sendPrivateMessage("Escolha uma dificuldade para o jogo utilizando o comando \"D n\", onde n é a dificuldade! A maioria decide." +
                                        "\n1 - Fácil" +
                                        "\n2 - Médio" +
                                        "\n3 - Difícil",
                                player.getName());
                    }
                } else {
                    if(!game.isGameStart()) {
                        String message = "A dificuldade escolhida pelos jogadores foi ";
                        switch (game.getDifficulty()) {
                            case 1:
                                message += game.getDifficulty() + " - Fácil!";
                                break;
                            case 2:
                                message += game.getDifficulty() + " - Médio!";
                                break;
                            case 3:
                                message += game.getDifficulty() + " - Difícil!";
                                break;
                        }
                        game.setGameStart(true);
                        sendMessagetoAll(message);
                    } else {
                        sendMessagetoAll("agora vai!");

                    }
                }

            }
        }
    };

    public void run() {
        executor.scheduleAtFixedRate(gameRunnable, 0, 5, TimeUnit.SECONDS);

        // Create a byte buffer/array for the receive Datagram packet
        byte[] receiveData = new byte[1024];

        while (true) {
            if (stopped)
                return;

            // Set up a DatagramPacket to receive the data into
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            try {
                // Receive a packet from the server (blocks until the packets are received)
                udpSocket.receive(receivePacket);

                // Extract the reply from the DatagramPacket
                String clientMessage =  new String(receivePacket.getData(), 0, receivePacket.getLength());

                System.out.println("Client Server Connected - Socket Address: " + receivePacket.getSocketAddress());
                System.out.println("Client Server message: \"" + clientMessage + "\"");


                this.processCommand(clientMessage, receivePacket);

                Thread.yield();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void processCommand(String command, DatagramPacket receivePacket) {
        String messageCommand = command.split(" ")[0];

        String clientUser = this.findClientUser(receivePacket);

        String response = "";

        if(messageCommand != null) {
            messageCommand = messageCommand.toUpperCase();

            switch (messageCommand) {
                case "REG":
                    UserInfo userInfo = this.createUser(command, receivePacket);
                    this.players.addPlayer(userInfo);
                    response += "Jogador Registrado! Você é o " + userInfo.getName() + " - " + userInfo.toString() + "\n\nBem vindo ao Jogo " + userInfo.getName() + "!";
                    break;
                case "LISTP":
                    String users = this.listOnlineUsers();
                    response += "Lista de Jogadores - " + users;
                    break;
                case "D":
                    String returnMessage = this.setGameDifficulty(messageCommand,command, clientUser);
                    response += returnMessage;
                    break;
            }

            this.sendPrivateMessage(response, clientUser);
        }
    }

    private void sendMessage(DatagramSocket udpSocket, String clientMessage, UserInfo address) {
        // print da mensagem de status
        System.out.println("Client IP Address, Hostname & Port: " + address.getClientIP() + ", " + address.getClientIP().getHostName() + ", " + address.getPort() + "\n");

        // mensagem de resposta
        String returnMessage = clientMessage.toUpperCase();

        // criando um buffer de bytes vazio para enviar
        byte[] sendData  = new byte[1024];

        // aplicando a mensagem ao buffer para envio
        sendData = returnMessage.getBytes();

        // criando um DatagramPacket com o buffer de dados, endereço de IP e a porta
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address.getClientIP(), address.getPort()-1);

        try {
            // enviando a mensagem
            udpSocket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private List<UserInfo> listAllBroadcastAddresses() {
        List<UserInfo> broadcastList = new ArrayList<>();
        for (Map.Entry<String, UserInfo> entry : this.players.getPlayers().entrySet()) {
            broadcastList.add(entry.getValue());
        }
        return broadcastList;
    }

    private UserInfo findBroadcastAddressByUserName(String name) {
        return this.players.getPlayers().get(name);
    }

    private String findUserByHostAndPort(String receivedHostName, int receivedPort) {
        for (Map.Entry<String, UserInfo> entry : this.players.getPlayers().entrySet()) {
            if(receivedHostName.equals(entry.getValue().getClientIP().getHostName())
                    && receivedPort == entry.getValue().getPort()-1) {
                return entry.getKey();
            }
        }

        return null;
    }

    private String findClientUser(DatagramPacket receivePacket) {
        String receivedHostName = receivePacket.getAddress().getHostName();
        int receivedPort = receivePacket.getPort();

        return this.findUserByHostAndPort(receivedHostName, receivedPort);
    }

    private UserInfo createUser(String command, DatagramPacket receivePacket) {

        String userName = command.split(" ")[1];

        UserInfo userInfo = new UserInfo();

        if (userName != null) {
            int playerNum = this.players.getPlayers().size()+1;
            userInfo.setName(userName + " " + playerNum);
            userInfo.setClientIP(receivePacket.getAddress());
            userInfo.setPort(receivePacket.getPort());
        }

        return userInfo;
    }

    private String listOnlineUsers() {
        String response = "";
        if(!this.players.getPlayers().isEmpty()) {
            for (Map.Entry<String, UserInfo> entry : this.players.getPlayers().entrySet()) {
                response += entry.getValue() + ",\n";
            }
        } else {
            response += "Sem usuários cadastrados!";
        }

        return response;
    }

    private void sendMessagetoAll(String message) {
        message = "\n" + message;

        List<UserInfo> broadcastAddresses = this.listAllBroadcastAddresses();
        for (UserInfo address : broadcastAddresses) {
            this.sendMessage(udpSocket, message, address);
        }
    }

    private void sendPrivateMessage(String message, String clientUser) {
        message = "\n" + message;

        UserInfo broadcastAddress = this.findBroadcastAddressByUserName(clientUser);
        this.sendMessage(udpSocket, message, broadcastAddress);
    }

    private String setGameDifficulty(String messageCommand, String command, String clientUser) {
        String returnMessage = "";
        int commandValue = Integer.valueOf(command.split(" ")[1]);

        if(this.game.isVoteStart()) {
            if (commandValue > 0 && commandValue < 4) {
                UserInfo player = this.players.findUserByName(clientUser);
                player.setChosenDifficulty(commandValue);
                returnMessage += "Voto de dificuldade registrado!";

                this.game.getMajorityDifficulty(this.players);
            } else {
                returnMessage += "Dificuldade inválida!";
            }
        } else {
            returnMessage += "Quantidade de jogadores insuficiente! Aguarde outros jogadores";
        }

        return returnMessage;
    }
}
