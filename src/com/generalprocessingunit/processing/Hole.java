package com.generalprocessingunit.processing;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PShape;
import processing.core.PVector;

public class Hole
{
    PVector location;
    PVector velocity;

    int millisAtPrevCalc;
    boolean tossed;

    static final int r = 150;
    boolean active; // once they are on the ground
    PShape shape;

    int millisAtBelch = 0;

    Hole(PApplet p5, float x, float y, float z, PVector lookat, float boost){
        location = new PVector(x, y, z);
        velocity = new PVector(lookat.x, .3f, lookat.z).normalize(null);
        velocity.mult(boost);
        createGeometry(p5);
    }

    private void createGeometry(PApplet p5)
    {
        shape = p5.createShape(PShape.GEOMETRY);
        shape.beginShape(PConstants.QUAD_STRIP);
//        shape.noStroke();
        shape.stroke(80, 40, 60);
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

    void toss(PApplet p5) {
        tossed = true;
        millisAtPrevCalc = p5.millis();
    }

    void beforeDraw(PApplet p5) {
        calculateLocation(p5);
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

        if(tossed) {
            active = false;
            belchAmplitude = 3f;
        }
        wobble(p5.millis(), belchAmplitude );

        pG.pushMatrix();
        pG.translate(location.x, location.y, location.z);
        pG.rotateY(p5.millis() / (tossed ? -50f : 5000f));
        pG.shape(shape);
        pG.popMatrix();
    }

    private void calculateLocation(PApplet p5) {
        if(tossed) {
            int millisNow = p5.millis();
            int dT = millisNow - millisAtPrevCalc;
            millisAtPrevCalc = millisNow;
            location = new PVector(
                location.x + dT * velocity.x,
                location.y + dT * velocity.y,
                location.z + dT * velocity.z
            );

            if(location.y < 10) {
                location.y = 10;
                tossed = false;
            } else {
                float g = .0007f;
                velocity.y -= dT * g;
            }

        }
    }
}
