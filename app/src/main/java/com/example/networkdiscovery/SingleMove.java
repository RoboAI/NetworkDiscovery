package com.example.networkdiscovery;

import android.widget.Spinner;

public class SingleMove {
    public int x;
    public int y;
    public String src_ip;//stores where the packet came from
    public String dst_ip;//used to sedn the packets to..

    public SingleMove(int x, int y, String dst_ip){
        this.x = x;
        this.y = y;
        this.dst_ip = dst_ip;
    }
}
