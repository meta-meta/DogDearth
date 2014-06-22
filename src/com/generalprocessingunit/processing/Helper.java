package com.generalprocessingunit.processing;

import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;

public class Helper {
    public static void drawBoxFeet(PGraphics pG, Iterable<Dog> dogs)
    {
        for(Dog dog: dogs){
            pG.noFill();

            pG.colorMode(PConstants.HSB);
            for (int i = 0; i < 4; i++) {
                PVector v = dog.legs[i].getGlobalLocation();
                v.y = 0;
                pG.pushMatrix();
                pG.translate(v.x, v.y, v.z);
                pG.stroke(i * (255 / 4) , 255, 255);
                pG.box(20);
                pG.popMatrix();
            }
            pG.colorMode(PConstants.RGB);
        }
    }
}
