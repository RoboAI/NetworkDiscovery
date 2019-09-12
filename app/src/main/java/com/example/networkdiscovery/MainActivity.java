package com.example.networkdiscovery;

import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.net.Socket;

import static com.example.networkdiscovery.GlobalDefines.DEFAULT_MASTER_PORT;
import static com.example.networkdiscovery.GlobalDefines.DEFAULT_UDP_PORT;
import static com.example.networkdiscovery.GlobalDefines.globalMyIP;

public class MainActivity extends AppCompatActivity{
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
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    public void onClick_Button(View view){
        toast("working..");

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
}
