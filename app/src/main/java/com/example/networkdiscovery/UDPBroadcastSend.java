package com.example.networkdiscovery;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

import static com.example.networkdiscovery.GlobalDefines.TAG;

//TODO: what if closeSocket() and then sendPacket()??

public class UDPBroadcastSend {

    DatagramSocket socket;
    DatagramPacket packet;

    public UDPBroadcastSend(Context context){
        init();

        packet = new DatagramPacket(new byte[1], 0, 1);
        packet.setAddress(getBroadcastAddress(context));
    }

    private void init(){
       /* try {
            //socket = new DatagramSocket(null);
            //socket.setBroadcast(true);
        } catch (SocketException e) {
            Log.i(TAG, "init: SocketException: " + e.getMessage());
        } /*catch (UnknownHostException e) {
            Log.i(TAG, "init: UnknownHostException: " + e.getMessage());
        }*/
    }

    public void openSocket(int port){
        try {
            socket = new DatagramSocket(null);
            socket.setReuseAddress(true);
            socket.setBroadcast(true);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public void closeSocket(){
        socket.close();
        socket = null;
    }

    public void sendPacket(byte[] bytes, int port) throws IOException {
        //DatagramPacket packet = new DatagramPacket(bytes, bytes.length, ip, port);
        packet.setPort(port);
        packet.setData(bytes);
        socket.send(packet);
    }

    //added try-catch to code below
    //https://stackoverflow.com/questions/2993874/android-broadcast-address
    public static InetAddress getBroadcastAddress(Context context) {
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();

        if(dhcp == null)
            return null;

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) (broadcast >> (k * 8));

        try {
            return InetAddress.getByAddress(quads);
        }catch (IOException e){
            Log.i(TAG, "getBroadcastAddress: IOException: " + e.getMessage());
            return null;
        }
    }
}
