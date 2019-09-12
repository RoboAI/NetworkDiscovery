package com.example.networkdiscovery;

import android.os.Looper;
import android.os.Process;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static com.example.networkdiscovery.GlobalDefines.CLIENT_PROTOCOL_STAGES;
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

    public void onServerIPDiscovered(String data){
        for(IListenerConnection cb : callbacks){
            cb.onSenderIPDiscovered(data);
        }
    }

    UDPSocket serverListener;
    Thread thread;
    //boolean bExit;
    volatile Integer iExit = 0;
    int counter = 0;

    public NDServer(int portToListen, String masterServerIP ,int masterPort){
        init();

        this.portToListen = portToListen;
        this.masterServerPort = masterPort;
        this.masterServerIP = masterServerIP;
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
                    if ((data = getUDPData(ue)) != null) {
                        if (data.equals(CLIENT_PROTOCOL_STAGES[0])) {
                            Log.i(TAG, "NDServer:run: Client protocol stage 0 passed");

                            //server stage 0
                            StringBuilder sb = new StringBuilder();
                            sb.append(SERVER_PROTOCOL_STAGES[0]);
                            sb.append(masterServerIP);
                            sb.append(":");
                            sb.append(masterServerPort);
                            sendUDPData(ue.socketAddress, sb.toString().getBytes());

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

    private void setSocketTimeout(int millis) throws SocketException {
        serverListener.socket.setSoTimeout(millis);
    }

    private int getSocketTimeout(){
        try {
            return serverListener.socket.getSoTimeout();
        } catch (SocketException e) {
            Log.i(TAG, "getTimeout: " + e.getMessage());
        }

        return 0;
    }

    private void sendUDPData(SocketAddress sa, byte[] data) throws IOException {
        DatagramPacket outPacket = new DatagramPacket(data, data.length, sa);
        //serverListener.SendUDP(outPacket.getData());
        DatagramPacket out = null;

        out = new DatagramPacket(data, data.length, sa);
        serverListener.socket.send(out);

        String sentData = new String(data, StandardCharsets.UTF_8);
        Log.i(TAG, "run(): sent -> " + sentData);

        onDataSent(sentData);
    }

    private String getUDPData(UDPSocket.UDPExtracted out) throws IOException{
        Log.i(TAG, "getUDPData: Receiving UDP Packet");

        UDPSocket.UDPExtracted udpe = receiveUDPPacket();

        if(udpe == null)
            return null;

        out.copyFrom(udpe);

        String data = new String(out.data, StandardCharsets.UTF_8);
        Log.i(TAG, out.SenderIP + ":" + out.port + " -> " + data);

        onDataReceived(data);

        return data;
    }

    private UDPSocket.UDPExtracted receiveUDPPacket() throws IOException {
        Log.i(TAG, "NDServer:receiveUDPPacket(): port " + serverListener.port + "...");

        return serverListener.ReceiveUDPSimpleDetail();
    }
}

/*
while (iExit == 0) {
                counter++;

                Log.i(TAG, "run: counter: " + counter);

                if (Thread.currentThread().isInterrupted()) {
                    Log.i(TAG, "run: Thread.currentThread().isInterrupted()");
                    throw new InterruptedException();
                }

                UDPSocket.UDPExtracted ue = serverListener.new UDPExtracted();
                String data = null;

                //TODO: debug what happens when interrupted during getUDPData()
                //client stage 0
                try {
                    if ((data = getUDPData(ue)) != null) {
                        if (data.equals(CLIENT_PROTOCOL_STAGES[0])) {
                            Log.i(TAG, "run: Client protocol stage 0 passed");

                            //server stage 0
                            sendUDPData(ue.socketAddress, SERVER_PROTOCOL_STAGES[0].getBytes());
                            Thread.sleep(150);
                            sendUDPData(ue.socketAddress, SERVER_PROTOCOL_STAGES[0].getBytes());
                            Thread.sleep(150);

                            //set timeout so this wont hang if next packet isnt received
                            setSocketTimeout(1000);

                            //client stage 1
                            if ((data = getUDPData(ue)) != null) {
                                if (data.equals(CLIENT_PROTOCOL_STAGES[1])) {
                                    Log.i(TAG, "run: Client protocol stage 1 passed");

                                    //server stage 1
                                    StringBuilder sb = new StringBuilder();
                                    sb.append(SERVER_PROTOCOL_STAGES[1]);
                                    sb.append(masterServerIP);
                                    sb.append(":");
                                    sb.append(masterServerPort);
                                    sendUDPData(ue.socketAddress, sb.toString().getBytes());
                                }
                            }

                            setSocketTimeout(timeOut);

                        } else if (data.equals("end thread")) {
                            iExit = 1;
                        }
                    }

                } catch (InterruptedException e) {
                    Log.i(TAG, "run: InterruptedException: " + e.getMessage());
                    return;

                } catch (SocketException e) {
                    Log.i(TAG, "run: SocketException: " + e.getMessage());

                } catch (SocketTimeoutException e) {
                    Log.i(TAG, "run: SocketTimeoutException: " + e.getMessage());

                    try {
                        setSocketTimeout(timeOut);
                    } catch (SocketException e1) {
                        e1.printStackTrace();
                    }

                } catch (IOException e) {
                    Log.i(TAG, "run: IOException: " + e.getMessage());
                }
            }
 */
