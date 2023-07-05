//not complete.
//TODO: bind(), disconnect() not really needed
package com.example.networkdiscovery;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Arrays;

import static com.example.networkdiscovery.GlobalDefines.TAG;

public class UDPSocket {
	
	public class UDPExtracted{
		public int port;
		public String SenderIP;
		public byte[] data;
		public SocketAddress socketAddress;

		public void copyFrom(UDPExtracted original){
			port = original.port;
			SenderIP = original.SenderIP;
			data = original.data.clone();
			socketAddress = original.socketAddress;
		}
	}
	
	public DatagramSocket socket;
	public DatagramPacket packet;
	public InetAddress addr;
	public int port = 0;
	public boolean connected = false;
	
	public UDPSocket(){
		try {
			
			init(port, InetAddress.getLocalHost());
		
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public UDPSocket(int port){
		init(port, null);
	}
	
	public UDPSocket(int port, InetAddress ia){
		init(port, ia);
	}
	
	public UDPSocket(int port, String text_address){
		try {
			InetAddress ia = InetAddress.getByName(text_address);
			init(port, ia);
		} catch (UnknownHostException e) {
			Log.i(TAG, "UDPSocket: " + e.getMessage());
		}
	}
	
	private void init(int port, InetAddress ia){
		this.port = port;
		this.addr = ia;
		connected = false;

		packet = new DatagramPacket(new byte[1], 0, 1);

		try {
			socket = new DatagramSocket(null);
		} catch (SocketException e) {
			close();
			Log.i(TAG, "UDPSocket:init(): " + e.getMessage());
		}
	}

	public boolean connect() {
		Log.i(TAG, "connect(): ");

		socket.connect(addr, port);
		packet.setAddress(addr);
		packet.setPort(port);

		connected = socket.isConnected();
		return connected;
	}
	
	public boolean bind() {
		Log.i(TAG, "bind(): ");

		try {
			socket.bind(new InetSocketAddress(addr, port));
		} catch (SocketException e) {
			close();
			Log.i(TAG, "bind: " + e.getMessage());
		}

		return socket.isBound();
	}
	
	public void close(){
		Log.i(TAG, "close(): ");

		if(socket != null) {
			socket.close();
			socket = null;
		}

		connected = false;
	}

	public int getPort(){
		return port;
	}
	
	public void SendUDP(byte[] data) throws IOException {
		packet.setData(data);
		packet.setAddress(addr);
		packet.setPort(port);

		socket.send(packet);
	}

	public DatagramPacket ReceiveUDP() throws IOException{
		byte[] buffer = new byte[1024];
		DatagramPacket dp = new DatagramPacket(buffer, buffer.length);
		socket.receive(dp);
		return dp;
	}
	
	public UDPExtracted ReceiveUDPSimpleDetail() throws IOException{
		Log.i(TAG, "NDServer:ReceiveUDPSimpleDetail():");
		DatagramPacket dp = ReceiveUDP();
		UDPExtracted udp_extracted = new UDPExtracted();
		udp_extracted.data = Arrays.copyOf(dp.getData(), dp.getLength());
		udp_extracted.port = dp.getPort();
		udp_extracted.SenderIP = dp.getAddress().toString().substring(1);
		udp_extracted.socketAddress = dp.getSocketAddress();
		return udp_extracted;
	}
}

/*
changes:
 - UDPExtracted: removed '/' from beginning of IP before storing it in SenderIP

*/

