package com.example.networkdiscovery;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import static com.example.networkdiscovery.GlobalDefines.DEFAULT_MASTER_PORT;
import static com.example.networkdiscovery.GlobalDefines.DEFAULT_UDP_PORT;
import static com.example.networkdiscovery.GlobalDefines.globalMyIP;

public class MainActivity extends AppCompatActivity implements MyCustomView.OnUdpMsgReceived{
    TextView tvMain;
    TextView tvStatus;
    MyCustomView cv;

    StringBuilder stringReceived = new StringBuilder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(GlobalDefines.TAG, "MainActivity:onCreate(): ");

        super.onCreate(savedInstanceState);

        String myIP = globalMyIP = getIpAddr();

        setContentView(R.layout.activity_main);

        if(savedInstanceState != null){
            toast("has saved data");
        }

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        tvMain = findViewById(R.id.textViewMain);
        tvStatus = findViewById(R.id.textViewMsgs);
        cv = findViewById(R.id.viewDrawing);

        stringReceived.append(myIP + "\n");

        tvMain.setMovementMethod(new ScrollingMovementMethod());
        tvMain.setText("  ---->");

        setTitle(getTitle() + " - " + myIP);

        ((EditText)findViewById(R.id.editTextIP)).setText(myIP.substring(0, 10));

        cv.addMeForCallback(this);
    }

    public String getIpAddr() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        String ip = intToIp(ipAddress);
        return ip;
    }

    public String intToIp(int i) {
        return  ((i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                ((i >> 24) & 0xFF));
    }

    public void toast(String s){
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
    }

    public void onClick_Button(View view){
        cv.checkForClients();
        toast("sent signal");
    }

    public void onClick_SetHostIP(View view){
        String ip = ((EditText)findViewById(R.id.editTextIP)).getText().toString();
        if(ip.isEmpty())
            toast("ip is blank");
        else {
            cv.SetDestinationIP(ip);
            toast("IP Set");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.i(GlobalDefines.TAG, "onPause():");
        cv.stopServer();
        cv.stopClient();
        //server.serverListener.close();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        Log.i(GlobalDefines.TAG, "onPostResume():");

        cv.startServer();
        cv.startClient();
    }

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
            Log.i("ABCD", "getBroadcastAddress: IOException: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void OnClientFound(final String ip, final int port) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((EditText)findViewById(R.id.editTextIP)).setText(ip);
                Toast.makeText(getApplicationContext(), "client found: " + ip + ":" + String.valueOf(port), Toast.LENGTH_LONG).show();
            }
        });

        //toast(ip);
    }
}
