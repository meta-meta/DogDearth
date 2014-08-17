package com.generalprocessingunit.processing;

import processing.core.*;

public class SpiralingShape {
    PShape spiral;

    PVector location = new PVector(0, 9000, 10000);
    PVector orientation = new PVector(-PConstants.QUARTER_PI, 0, 0);

    final static int segments = 200;
    final static float totalRotation = 5 * PConstants.TWO_PI;
    final static float maxWidth = 500;
    final static float maxR = 8000;

    final static float defaultRotateSpeed = .5f;
    float rotateSpeed = defaultRotateSpeed;
    final static float maxRotateSpeed = 5;

    public SpiralingShape(PApplet p5) {
        createShape(p5);
    }

    private void createShape(PApplet p5) {
        float widthCoef = PConstants.PI / segments;
        float deltaTheta = totalRotation / segments;

        spiral = p5.createShape();
        spiral.beginShape(PApplet.QUAD_STRIP);
        spiral.fill(255, 50, 170);
        spiral.noStroke();

        for (int i = 0; i < segments; i++) {
            float rInner = maxR * i / segments;
            float rOuter = rInner + maxWidth * PApplet.sin(widthCoef * i);

            float x = PApplet.cos(deltaTheta * i);
            float y = PApplet.sin(deltaTheta * i);

            spiral.vertex(x * rInner, y * rInner, -i *  maxR/segments);
            spiral.vertex(x * rOuter, y * rOuter, -i *  maxR/segments);
        }
        spiral.endShape();
    }

    int millisAtIncrease = 0;
    int millisAtDecrease = 0;
    boolean increasing = false;

    public void increaseSpeed(PApplet p5) {
        if(!increasing) {
            millisAtIncrease = p5.millis();
            increasing = true;
        }
        rotateSpeed = rotateSpeed < maxRotateSpeed ? rotateSpeed + (p5.millis() - millisAtIncrease) / 1000000f : maxRotateSpeed;
    }

    public void decreaseSpeed(PApplet p5) {
        if(increasing) {
            millisAtDecrease = p5.millis();
            increasing = false;
        }
        rotateSpeed = rotateSpeed > defaultRotateSpeed ? rotateSpeed - (p5.millis() - millisAtDecrease) / 10000f : defaultRotateSpeed;
        System.out.println(rotateSpeed);
    }

    int millisAtLastDraw = 0;
    public void draw(PApplet p5, PGraphics pG) {
        pG.pushMatrix();

        pG.translate(location.x, location.y, location.z);
        pG.rotateX(orientation.x);
        pG.rotateZ(orientation.z);

        pG.shape(spiral);

        pG.popMatrix();

        orientation.z -= rotateSpeed * (PConstants.TWO_PI / 1000) * (p5.millis() - millisAtLastDraw);
        millisAtLastDraw = p5.millis();
    }
}
