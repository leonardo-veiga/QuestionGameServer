package com.pucrs.rdc.serverudp;

import java.net.*; // Importando por causa da classe Socket


class Server {

    public final static int USER_PORT = 8005;
    public final static int SERVER_PORT = 9005;
    private static Players players = new Players();
    private static Game game = new Game();

    public static void main(String args[]) throws Exception {

        System.out.println("Utilização: UDPServer Usuario - Agora utilizando a porta # = " + USER_PORT);
        System.out.println("Utilização: UDPServer Servidor - Agora utilizando a porta # = " + SERVER_PORT);

        // Abrindo um novo datagram socket para a comunicação com usuarios na porta especificada
        DatagramSocket udpUserSocket = new DatagramSocket(USER_PORT);

        // Abrindo um novo datagram socket para a comunicação com o servidor na porta especificada
        DatagramSocket udpControlSocket = new DatagramSocket(SERVER_PORT);

        System.out.println("Servidor inicializado...\n");

        UserReceiverThread userReceiver = new UserReceiverThread(udpUserSocket, players, game);
        userReceiver.start();

        ControlReceiverThread controlReceiver = new ControlReceiverThread(udpControlSocket, players);
        controlReceiver.start();

        QuestionRepository qr = new QuestionRepository();
        qr.getQuestions(1);
    }
}
