package com.pucrs.rdc.serverudp;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class ControlReceiverThread extends Thread {

    private DatagramSocket udpSocket;
    private boolean stopped = false;

    private static Players players;

    public ControlReceiverThread(DatagramSocket ds, Players players) throws SocketException {
        this.udpSocket = ds;
        this.players = players;
    }

    public void disconnect() {
        this.stopped = true;
    }

    public void run() {

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

                String response = this.processCommand(clientMessage, receivePacket);

                this.sendMessage(udpSocket, receivePacket, response);

                Thread.yield();
            } catch (IOException ex) {
                System.err.println(ex);
            }
        }
    }

    private void sendMessage(DatagramSocket udpSocket, DatagramPacket receivePacket, String clientMessage) throws IOException {
        // Get the IP address and the the port number which the received connection came from
        InetAddress clientIP = receivePacket.getAddress();

        // Print out status message
        System.out.println("Client IP Address & Hostname: " + clientIP + ", " + clientIP.getHostName() + "\n");

        // Get the port number which the recieved connection came from
        int clientPort = receivePacket.getPort();

        // Response message
        String returnMessage = clientMessage.toUpperCase();

        // Create an empty buffer/array of bytes to send back
        byte[] sendData  = new byte[1024];

        // Assign the message to the send buffer
        sendData = returnMessage.getBytes();

        // Create a DatagramPacket to send, using the buffer, the clients IP address, and the clients port
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientIP, clientPort);

        // Send the echoed message
        udpSocket.send(sendPacket);
    }

    private String processCommand(String command, DatagramPacket receivePacket) {
        String messageCommand = command.split(" ")[0];
        UserInfo userInfo = null;
        String response = command + " - ";

        if(messageCommand != null) {
            messageCommand = messageCommand.toUpperCase();

            try {
                switch (messageCommand) {
                    case "REG":
                        userInfo = this.createUser(command, receivePacket);
                        this.players.addPlayer(userInfo);
                        response += "Jogador Registrado! Você é o " + userInfo.getName() + " - " + userInfo.toString() + "\n\nBem vindo ao Jogo " + userInfo.getName() + "!";
                        break;
                    case "LISTP":
                        String users = this.listOnlineUsers();
                        response += "Lista de Jogadores - " + users;
                        break;
                }
            } catch (Exception e) {
                response += "Erro registrando comando";
            }
        }

        return response;

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

    private List<UserInfo> listAllBroadcastAddresses() {
        List<UserInfo> broadcastList = new ArrayList<>();
        for (Map.Entry<String, UserInfo> entry : this.players.getPlayers().entrySet()) {
            broadcastList.add(entry.getValue());
        }
        return broadcastList;
    }

//    private void sendMessagetoAll(String message) {
//        List<UserInfo> broadcastAddresses = this.listAllBroadcastAddresses();
//        for (UserInfo address : broadcastAddresses) {
//            this.sendSysMessage(udpSocket, message, address);
//        }
//    }
}
