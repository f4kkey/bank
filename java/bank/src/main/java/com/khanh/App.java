package com.khanh;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import com.khanh.worker.CallbackWorker;
import com.khanh.worker.TransactionExportWorker;

public class App {
    private static void handle(Socket socket) {
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);) {

            String request = in.readLine();
            String response = new Router().route(request);

            out.println(response);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws Exception {
        Thread callbackWorker = new Thread(new CallbackWorker());
        callbackWorker.setDaemon(true);
        callbackWorker.start();

        Thread transactionExportWorker = new Thread(new TransactionExportWorker());
        transactionExportWorker.setDaemon(true);
        transactionExportWorker.start();

        ServerSocket server = new ServerSocket(12345);
        System.out.println("java bank server running at 12345");

        while (true) {
            Socket socket = server.accept();
            new Thread(() -> handle(socket)).start();
        }
    }
}
