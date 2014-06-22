package com.generalprocessingunit.processing;

import processing.core.PGraphics;
import processing.core.PVector;

public class Helper {
    public static void drawBoxFeet(PGraphics pG, Iterable<Dog> dogs)
    {
        for(Dog dog: dogs){
            pG.noFill();
            PVector v = dog.getPawDriverSideFront();
            pG.pushMatrix();
            pG.translate(v.x, v.y, v.z);
            pG.stroke(255, 0, 0);
            pG.box(20);
            pG.popMatrix();

            PVector w = dog.getPawPassengerSideFront();
            pG.pushMatrix();
            pG.translate(w.x, w.y, w.z);
            pG.stroke(255, 255, 0);
            pG.box(20);
            pG.popMatrix();

            PVector x = dog.getPawDriverSideRear();
            pG.pushMatrix();
            pG.translate(x.x, x.y, x.z);
            pG.stroke(0, 255, 0);
            pG.box(20);
            pG.popMatrix();

            PVector y = dog.getPawPassengerSideRear();
            pG.pushMatrix();
            pG.translate(y.x, y.y, y.z);
            pG.stroke(0, 0, 255);
            pG.box(20);
            pG.popMatrix();
        }
    }
}
