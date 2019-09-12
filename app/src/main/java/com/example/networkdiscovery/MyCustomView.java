package com.example.networkdiscovery;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;

import java.net.Socket;

import static com.example.networkdiscovery.GlobalDefines.DEFAULT_MASTER_PORT;
import static com.example.networkdiscovery.GlobalDefines.DEFAULT_UDP_PORT;
import static com.example.networkdiscovery.GlobalDefines.globalMyIP;

public class MyCustomView extends View implements MultiTouchController.TouchUpdateListener, IListenerConnection, MyTimer.ITimerElapsed{
    MyCircle circle_me;
    MyCircle circle_them;
    MyTimer timer;
    PointF[] points;
    Paint pen;

    MultiTouchController multitouch;

    NDClient client;
    NDServer server;

    SingleMove singleMove;

    String myIP = globalMyIP.substring(globalMyIP.length() - 3);

    public MyCustomView(Context context) {
        super(context);

        init();
    }

    public MyCustomView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    private void init() {
        circle_me = new MyCircle(0xFFFFA500);
        circle_them = new MyCircle(Color.LTGRAY);

        singleMove = new SingleMove(0,0, "");

        client = new NDClient(singleMove);

        server = new NDServer(DEFAULT_UDP_PORT, myIP, DEFAULT_MASTER_PORT);
        server.addCallback(this);

        multitouch = new MultiTouchController();
        multitouch.setOnTouchUpdate(this);
        this.setOnTouchListener(multitouch);

        timer = new MyTimer(this, 1, true);

        pen = new Paint();
        pen.setStrokeWidth(2);
        pen.setStyle(Paint.Style.FILL_AND_STROKE);
        pen.setColor(Color.BLACK);
    }

    public void startClient(){
        if(client != null){
            client.startClient();
        }
    }

    public void stopClient(){
        if(client != null){
            client.stopClient();
        }
    }

    public void startServer(){
        if(server != null){
            server.startServer();
        }
    }

    public void stopServer(){
        if(server != null){
            server.stopServer();
        }
    }

    public void updateMe(float x, float y) {
        circle_me.x = x;
        circle_me.y = y;
        invalidate();
        synchronized (singleMove) {
            singleMove.x = (int)x;
            singleMove.y = (int)y;
            singleMove.notifyAll();
        }
    }

    public void updateThem(float x, float y) {
        //points = circle_them.calculateAndGetBuffer(x, y);
        //timer.start();

        circle_them.x = x;
        circle_them.y = y;

        invalidate();
    }

    public void updateCircleCoords(float x, float y){

    }

    public String getIPFromMessage(String data){
        int length = GlobalDefines.START_TAG.length();
        return data.substring(length, length + 3);
    }

    public String getFullIPFromMessage(String data){
        int length = GlobalDefines.START_TAG.length();
        return data.substring(length, data.indexOf('x'));
    }

    public int getXCoord(String data){
        return Integer.parseInt (data.substring(data.indexOf('x') + 1, data.indexOf('y')));
    }

    public int getYCoord(String data){
        return Integer.parseInt (data.substring(data.indexOf('y') + 1));
    }

    @Override
    public void onDataReceived(String data){

        if(data.startsWith(GlobalDefines.START_TAG)){

            String ip = getIPFromMessage(data);
            if(myIP.equals(ip))
                return;

            singleMove.ip = getFullIPFromMessage(data);

            updateThem(getXCoord(data), getYCoord(data));
        }
    }

    @Override
    public void onDataSent(String data) {

    }

    @Override
    public void onListenerServerStarted() {

    }

    @Override
    public void onListenerServerStopped() {

    }

    @Override
    public void onClientConnected(Socket client) {

    }

    @Override
    public void onServerMessage(String msg) {

    }

    @Override
    public void onSenderIPDiscovered(String ip) {

    }

    private void drawLineDots(Canvas canvas){
        if(points != null) {
            for (int i = 0; i < points.length; i++) {
                canvas.drawCircle(points[i].x, points[i].y, 10, pen);
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        circle_me.draw(canvas);
        circle_them.draw(canvas);

        //drawLineDots(canvas);
    }

    @Override
    public void onTouchUpdate(MultiTouchController.SingleTouch touch) {
        updateMe(touch.x, touch.y);
    }

    @Override
    public void onTouchDown(MultiTouchController.SingleTouch touch) {

    }

    @Override
    public void onTouchUp(MultiTouchController.SingleTouch touch) {

    }

    @Override
    public void onTimerElapsed(long timeNow) {
        PointF p = circle_them.getNextPoint();

        if(p.x == circle_them.x && p.y == circle_them.y)
            timer.stop();

        circle_them.x = p.x;
        circle_them.y = p.y;

        invalidate();
    }
}
