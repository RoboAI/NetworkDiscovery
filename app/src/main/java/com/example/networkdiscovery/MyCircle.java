package com.example.networkdiscovery;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.graphics.Paint;
import android.graphics.PointF;

import java.util.Random;

public class MyCircle extends Circle{
    Paint pen;

    PointF[] buffer;
    int bufferIndex = 0;

    public MyCircle(int colour){
        Random random = new Random();

        x = y = 300;

        pen = new Paint();
        pen.setStrokeWidth(10);
        //pen.setColor(random.nextInt(0xFFFFFF) | 0xFF000000);
        pen.setColor(colour);
        pen.setStyle(Paint.Style.FILL_AND_STROKE);

        buffer = new PointF[10];
        for(int i = 0; i < buffer.length; i++){
            buffer[i] = new PointF();
        }
    }

    PointF[] calculateAndGetBuffer(float x, float y){
        bufferIndex = 0;

        double angle = getAngleToOtherPoint(x, y);
        double length = length(this.x - x, this.y - y);
        double eachPointLength = length / (float)buffer.length;
        float nextPointDistance;

        for(int i=0 ; i < buffer.length; i++){
            nextPointDistance = (float)(i * eachPointLength);
            buffer[i].y = (float)Math.sin(angle) * (float)(i * eachPointLength) + y;
            buffer[i].x = (float)Math.cos(angle) * (float)(i * eachPointLength) + x;
        }
        return buffer;
    }

    PointF getNextPoint(){
        if(bufferIndex + 1 < buffer.length)
            return buffer[++bufferIndex];
        else
            return buffer[buffer.length - 1];
    }

    public void draw(Canvas canvas){
        canvas.drawCircle(x, y, radius, pen);
    }
}
