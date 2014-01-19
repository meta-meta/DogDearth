package com.generalprocessingunit.processing;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PShape;
import processing.core.PVector;

public class Hole
{
    PVector location;
    static final int r = 150;
    boolean active; // once they are on the ground
    PShape shape;

    int millisAtBelch = 0;

    Hole(PApplet p5, float x, float y){
        location = new PVector(x, 0, y);
        createGeometry(p5);
    }

    private void createGeometry(PApplet p5)
    {
        shape = p5.createShape(PShape.GEOMETRY);
        shape.beginShape(PConstants.QUAD_STRIP);
//        shape.noStroke();
        shape.stroke(40, 20, 30);
        shape.fill(0);

        float steps = 25;
        float d = (r * 2) / steps;
        for(float x = -r; x < (r + d); x += d - ( (d/1.3f) * (PApplet.abs(x)/r) )){
            float y = PApplet.sqrt(r * r - x * x);
            shape.vertex(x, 0, -y);
            shape.vertex(x, 0, y);
        }

        shape.endShape();
    }

    private void wobble(int t, float a){
        for(int i = 0; i < shape.getVertexCount(); i++){
            PVector v = shape.getVertex(i);
            v.y = (2 * a * PApplet.sin(t/1000f)) * ( (r - PApplet.abs(v.x))/r) * PApplet.sin(t/100f + (v.x / 10f ));
            shape.setVertex(i, v);
        }
    }

    void draw(PGraphics pG, PApplet p5){

        int m = p5.millis()-millisAtBelch;
        float belchAmplitude;
        if(m < 500){
            active = false;
            belchAmplitude = m/20f;
        } else if(m < 1500) {
            belchAmplitude = 25 - (m - 500)/40f;
        } else {
            active = true;
            belchAmplitude = 0;
        }

        wobble(p5.millis(), belchAmplitude );
        pG.shape(shape);
    }
}
