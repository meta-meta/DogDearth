package com.generalprocessingunit.processing;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PShape;
import processing.core.PVector;

public class Dog
{
    PVector location;
    PVector orientation;
    float w, h, d;
    PShape model; // TODO: load blender mesh into this
    float hue, sat, bri;


    Dog(PApplet p5, float x, float y, float z, float rx, float ry, float rz, float w, float h, float d){
//        model = p5.createShape();
//        model.beginShape();
//        model.endShape();

        location = new PVector(x, y, z);
        orientation = new PVector(rx, ry, rz);

        this.w = w;
        this.h = h;
        this.d = d;

        hue = p5.random(30, 70);
        sat = p5.random(10, 140);
        bri = p5.random(10, 200);
    }

    void draw(PApplet p5){
        p5.pushMatrix();
        p5.translate(location.x, location.y, location.z);
        p5.rotateY(orientation.y);
        p5.colorMode(PConstants.HSB);
        p5.fill(hue, sat, bri);
        p5.noStroke();
        p5.colorMode(PConstants.RGB);
        p5.box(w, h, d);
        p5.popMatrix();
    }
}
