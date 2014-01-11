package com.generalprocessingunit.processing;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PShape;
import processing.core.PVector;

public class Hole
{
    PVector location;
    final float r = 100;
    boolean active; // once they are on the ground
    PShape shape;

    Hole(PApplet p5, float x, float y){
        location = new PVector(x, 30, y);
        createGeometry(p5);
    }

    private void createGeometry(PApplet p5)
    {
        shape = p5.createShape(PShape.GEOMETRY);
        shape.beginShape(PConstants.QUAD_STRIP);
        shape.noStroke();
        shape.fill(0);

        float steps = 15;
        float d = (r * 2) / steps;
        for(float x = -r; x < (r + d); x += d - ( (d/1.3f) * (PApplet.abs(x)/r) )){
            float y = PApplet.sqrt(r * r - x * x);
            shape.vertex(x, 0, -y);
            shape.vertex(x, 0, y);
        }

        shape.endShape();
    }

    private void wobble(int t){
        for(int i = 0; i < shape.getVertexCount(); i++){
            PVector v = shape.getVertex(i);
            v.y = 10f * ((r - PApplet.abs(v.x))/r) * PApplet.sin(t/100f + v.x );
            shape.setVertex(i, v);
        }
    }

    void draw(PApplet p5){
        wobble(p5.millis());
        p5.shape(shape);
    }
}
