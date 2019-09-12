package com.example.networkdiscovery;

import android.graphics.PointF;

public class Circle {
    public int id;
    public float x, y;
    public float radius;

    public Circle(){
        init();
    }

    public Circle(int id){
        init();
    }

    void init(){
        id = 0;
        x = 0;
        y = 150;
        radius = 150;
    }

    PointF getPointOnCircle(double angle, PointF in_p){
        in_p.y = (float)Math.sin(angle) * radius + y;
        in_p.x = (float)Math.cos(angle) * radius + x;
        return in_p;
    }

    double getAngleToOtherCircle(Circle c){
        return Math.atan2( c.y - y, c.x - x);
    }

    double getAngleToOtherPoint(float x, float y){
        return Math.atan2(y - this.y, x - this.x);
    }

    public float length(float width, float height){
        return (float)Math.sqrt((width * width) + (height * height));
    }
}
