package com.example.networkdiscovery;

/**
 * Created by Fuat on 21/09/15.
 */

import android.os.Handler;
import android.os.SystemClock;

public class MyTimer extends Handler implements Runnable {
    public interface ITimerElapsed{
        void onTimerElapsed(long timeNow);
    }

    private ITimerElapsed timerElapsed;
    private long millis;
    private long startTime;
    private long lastEndTime;
    private long newTime;
    private boolean bLoop;
    private boolean bStarted;
    volatile private boolean bStop;

    public MyTimer(ITimerElapsed callback, long milli, boolean loop){
        init();
        setCallback(callback);
        setCallbackTime(milli);
        setLoop(loop);
    }

    private void init(){
        timerElapsed = null;
        millis = 0;
        startTime = 0;
        lastEndTime = 0;
        newTime = 0;
        bLoop = false;
        bStop = false;
    }

    public void setCallback(ITimerElapsed callback){
        timerElapsed = callback;
    }

    public void setLoop(boolean loop){
        bLoop = loop;
    }

    public void setCallbackTime(long milli){
        millis = milli;
    }

    public boolean isStarted(){
        return bStarted;
    }

    public void start(){
        if(!bStarted) {
            if (timerElapsed != null) {
                bStop = false;
                bStarted = this.postDelayed(this, millis);
            }
        }
    }

    public void start(long milli){
        setCallbackTime(milli);
        start();
    }

    public void stop(){
        bStop = true;//we use this just in case its too late to remove last postDelayed..
        this.removeCallbacks(this);
        bStarted = false;
    }

    private void callback(long timeNow){
        timerElapsed.onTimerElapsed(timeNow);
    }

    @Override
    public void run() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        if(!bStop) {
            if (bLoop == false) {
                stop();
                callback(SystemClock.elapsedRealtime());
            } else {
                startTime = SystemClock.elapsedRealtime();
                callback(startTime);
                lastEndTime = SystemClock.elapsedRealtime();
                newTime = millis - (lastEndTime - startTime);
                this.postDelayed(this, (newTime > 0 ? newTime : 0));
            }
        }
    }
}
