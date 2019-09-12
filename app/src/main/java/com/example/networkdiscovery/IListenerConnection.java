package com.example.networkdiscovery;

import java.net.Socket;

public interface IListenerConnection {
    void onListenerServerStarted();
    void onListenerServerStopped();
    void onClientConnected(Socket client);
    void onDataReceived(String data);
    void onDataSent(String data);
    void onServerMessage(String msg);
    void onSenderIPDiscovered(String ip);
}
