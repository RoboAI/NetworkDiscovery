package com.example.networkdiscovery;

import android.content.Context;
import android.os.Looper;
import android.os.Process;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static com.example.networkdiscovery.GlobalDefines.CLIENT_PROTOCOL_STAGES;
import static com.example.networkdiscovery.GlobalDefines.DEFAULT_BC_PORT;
import static com.example.networkdiscovery.GlobalDefines.DEFAULT_MASTER_PORT;
import static com.example.networkdiscovery.GlobalDefines.SERVER_PROTOCOL_STAGES;
import static com.example.networkdiscovery.GlobalDefines.TAG;

//LAST EDIT:
/*
thread isnt terminating so next time the app starts, the previous thread resumes which we dont have a handle to so it crashes
because some variables are null.
 */

public class NDServer implements Runnable{
    ArrayList<IListenerConnection> callbacks;
    int masterServerPort;
    String masterServerIP;
    int portToListen;
    String myIP;

    public void addCallback(IListenerConnection cb){
        callbacks.add(cb);
    }

    public void onListenerServerStarted(){
        for(IListenerConnection cb : callbacks){
            cb.onListenerServerStarted();
        }
    }

    public void onListenerServerStopped(){
        for(IListenerConnection cb : callbacks){
            cb.onListenerServerStopped();
        }
    }

    public void onClientConnected(Socket client){
        for(IListenerConnection cb : callbacks){
            cb.onClientConnected(client);
        }
    }

    public void onDataReceived(String data){
        for(IListenerConnection cb : callbacks){
            cb.onDataReceived(data);
        }
    }

    public void onDataSent(String data){
        for(IListenerConnection cb : callbacks){
            cb.onDataSent(data);
        }
    }

    public void onServerMessage(String data){
        for(IListenerConnection cb : callbacks){
            cb.onServerMessage(data);
        }
    }

    public void onSenderIPDiscovered(String ip, int port){
        for(IListenerConnection cb : callbacks){
            cb.onSenderIPDiscovered(ip, port);
        }
    }

    UDPBroadcastSend udpBroadcastSend;
    UDPSocket serverListener;
    Thread thread;
    volatile Integer iExit = 0;
    int counter = 0;

    public NDServer(int portToListen, String masterServerIP ,int masterPort, String myip){
        init();

        this.portToListen = portToListen;
        this.masterServerPort = masterPort;
        this.masterServerIP = masterServerIP;
        this.myIP = myip;
    }

    private void init(){
        if(serverListener != null) {
            serverListener.close();
            serverListener = null;
        }

        callbacks = new ArrayList<>();
        //bExit = false;
        iExit = 0;
        masterServerPort = DEFAULT_MASTER_PORT;
        masterServerIP = "127.0.0.1";
    }

    public boolean startServer(){
        Log.i(TAG, "NDServer:startServer():");

        onServerMessage("UDP server is starting...");

        if(thread != null && thread.isAlive()) {
            Log.i(TAG, "startServer: " + (thread != null) + " : " + (thread.isAlive()));

            return false;
        }

        if(serverListener != null){
            serverListener.close();
            serverListener = null;
        }

        serverListener = new UDPSocket(portToListen, "0.0.0.0");
        serverListener.bind();

        //bExit = false;
        iExit = 0;

        thread = new Thread(this);
        thread.start();

        return true;
    }

    public void stopServer(){
        Log.i(TAG, "NDServer:stopServer()");

        onServerMessage("stopping UDP server...");

        //bExit = true;
        iExit = 1;

        thread.interrupt();
        serverListener.close();

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        synchronized (iExit){
            //bExit = true;
            iExit = 1;
        }

        thread = null;
        serverListener = null;
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_MORE_FAVORABLE);

        Log.i(TAG, "NDServer:run(): ");

        Looper.prepare();

        try {

            try {
                serverListener.socket.setBroadcast(true);
            } catch (Exception e) {
                Log.i(TAG, "NDServer:run: Exception:" + e.getMessage());
                serverListener = null;
                //bExit = true;
                iExit = 1;
                return;
            }

            Log.i(TAG, "server has started on port " + serverListener.port + " ...");

            onServerMessage("waiting for data on port " + serverListener.port + "...");

            //synchronized (iExit) {
            // try {
            while (iExit == 0) {
                counter++;

                Log.i(TAG, "NDServer:run: counter: " + counter);

                if (Thread.currentThread().isInterrupted()) {
                    Log.i(TAG, "NDServer:run: Thread.currentThread().isInterrupted()");
                    throw new InterruptedException();
                }

                UDPSocket.UDPExtracted ue = serverListener.new UDPExtracted();
                String data = null;

                //TODO: debug what happens when interrupted during getUDPData()
                //client stage 0
                try {
                    data = getUDPData(ue);
                    if (data != null && !ue.SenderIP.equals(myIP) ) {
                        if (data.equals(CLIENT_PROTOCOL_STAGES[0])) {
                            Log.i(TAG, "NDServer:run: CLIENT_PROTOCOL_STAGES[0]:");

                            onSenderIPDiscovered(ue.SenderIP, ue.port);

                            //server stage 0
                            sendResponseAvailable(ue);
                        }
                        else if(data.startsWith(SERVER_PROTOCOL_STAGES[0])){
                            Log.i(TAG, "NDServer:run: SERVER_PROTOCOL_STAGES[0]:");

                            String ip = data.substring(data.indexOf(':') + 1, data.lastIndexOf(':'));
                            String port = data.substring(data.lastIndexOf(':') + 1);
                            onSenderIPDiscovered(ip, Integer.parseInt(port));
                        }
                    }

                } catch (SocketException e) {
                    Log.i(TAG, "NDServer:run: SocketException: " + e.getMessage());

                } catch (SocketTimeoutException e) {
                    Log.i(TAG, "NDServer:run: SocketTimeoutException: " + e.getMessage());

                    try {
                        setSocketTimeout(0);
                    } catch (SocketException e1) {
                        e1.printStackTrace();
                    }

                } catch (IOException e) {
                    Log.i(TAG, "NDServer:run: IOException: " + e.getMessage());
                }
            }

        } catch (InterruptedException e) {
            Log.i(TAG, "NDServer:run: Disconnecting: " + e.getMessage());
            serverListener.close();
            return;
        }

        Looper.myLooper().quit();

        Log.i(TAG, "NDServer:run: return from run()");
    }

    private void sendResponseAvailable(UDPSocket.UDPExtracted ue) {
        DatagramSocket socket;
        DatagramPacket packet;

        StringBuilder sb = new StringBuilder();
        sb.append(SERVER_PROTOCOL_STAGES[0]);
        sb.append(masterServerIP);
        sb.append(":");
        sb.append(masterServerPort);

        //sendUDPData(ue.socketAddress, sb.toString().getBytes());

        try {
            socket = new DatagramSocket(null);
            socket.setReuseAddress(true);

            byte[] bytes = sb.toString().getBytes();
            packet = new DatagramPacket(bytes, 0, bytes.length);
            packet.setAddress(InetAddress.getByName(ue.SenderIP));
            packet.setPort(DEFAULT_BC_PORT);
            socket.send(packet);
            socket.close();
        } catch (Exception e) {

        }
    }

    private void setSocketTimeout(int millis) throws SocketException {
        serverListener.socket.setSoTimeout(millis);
    }

    private int getSocketTimeout(){
        try {
            return serverListener.socket.getSoTimeout();
        } catch (SocketException e) {
            Log.i(TAG, "NDServer:getSocketTimeout: " + e.getMessage());
        }

        return 0;
    }

    private void sendUDPData(SocketAddress sa, byte[] data) throws IOException {
        DatagramPacket outPacket = new DatagramPacket(data, data.length, sa);

        //serverListener.SendUDP(outPacket.getData());
        serverListener.socket.send(outPacket);

        String sentData = new String(data, StandardCharsets.UTF_8);
        Log.i(TAG, "NDServer:run(): sent -> " + sentData);

        onDataSent(sentData);
    }

    private String getUDPData(UDPSocket.UDPExtracted out) throws IOException{
        Log.i(TAG, "NDServer:getUDPData(): Receiving UDP Packet");

        UDPSocket.UDPExtracted udpe = receiveUDPPacket();

        if(udpe == null)
            return null;

        out.copyFrom(udpe);

        String data = new String(out.data, StandardCharsets.UTF_8);
        Log.i(TAG, "NDServer:getUDPData(): " + out.SenderIP + ":" + out.port + " -> " + data);

        onDataReceived(data);

        return data;
    }

    private UDPSocket.UDPExtracted receiveUDPPacket() throws IOException {
        Log.i(TAG, "NDServer:receiveUDPPacket(): port " + serverListener.port);

        return serverListener.ReceiveUDPSimpleDetail();
    }
}
