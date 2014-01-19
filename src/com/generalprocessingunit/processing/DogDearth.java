package com.generalprocessingunit.processing;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PShape;
import processing.core.PVector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DogDearth
{
    public static final int HALF_PLANE_WIDTH = 2000;
    public static final int NUM_DOGS = 28;
    public static final int NUM_HOLES = 17;
    PShape gradient;
    PShape plane;

    List<Dog> dogs = new ArrayList<>();
    List<Hole> holes = new ArrayList<>();

    int mouseXAtClick = 0;
    int mouseYAtClick = 0;

    static Map<Integer, Boolean> keys = new HashMap<>();
    static {
        keys.put(PConstants.UP, false);
        keys.put(PConstants.DOWN, false);
        keys.put(PConstants.LEFT, false);
        keys.put(PConstants.RIGHT, false);
    }

    PVector playerLocation = new PVector(0, 400, -1500);
    PVector lookAt = new PVector(0, -20, 100);

    void setup(PApplet p5){
        gradient = p5.createShape();
        gradient.beginShape();
        gradient.fill(127);
        gradient.vertex(-p5.width, -p5.height);
        gradient.vertex(p5.width*2, -p5.height);
        gradient.fill(0);
        gradient.vertex(p5.width*2, p5.height*2);
        gradient.vertex(-p5.width, p5.height*2);
        gradient.endShape();

        plane = p5.createShape();
        plane.beginShape();
        plane.fill(20,255,0);
        plane.vertex(-HALF_PLANE_WIDTH, 0, HALF_PLANE_WIDTH);
        plane.vertex(HALF_PLANE_WIDTH, 0, HALF_PLANE_WIDTH);
        plane.vertex(HALF_PLANE_WIDTH, 0, -HALF_PLANE_WIDTH);
        plane.vertex(-HALF_PLANE_WIDTH, 0, -HALF_PLANE_WIDTH);
        plane.endShape();

        for(int i = 0; i < NUM_DOGS; i++){
            float h = p5.random(150, 200);
            dogs.add(new Dog( p5,
                    p5.random(-HALF_PLANE_WIDTH, HALF_PLANE_WIDTH), h/2 + 1, p5.random(-HALF_PLANE_WIDTH, HALF_PLANE_WIDTH),
                    0, ((int)p5.random(4))*PApplet.HALF_PI, 0,
                    p5.random(20, 50), h, p5.random(50, 150)));
        }

        int holeBound = HALF_PLANE_WIDTH - Hole.r;
        for(int i = 0; i < NUM_HOLES; i++){
            PVector newHoleLoc = new PVector();

            while(isHoleClash(newHoleLoc)){
                newHoleLoc = new PVector(p5.random(-holeBound, holeBound), 0,  p5.random(-holeBound, holeBound));
            }

            holes.add(new Hole(p5, newHoleLoc.x, newHoleLoc.z));
        }
    }

    boolean isHoleClash(PVector newLocation){
        for(Hole hole: holes){
            if(PVector.dist(hole.location, newLocation) < 2*Hole.r){
                return true;
            }
        }
        return false;
    }

    void beforeDraw(PApplet p5){
        for(Dog dog: dogs){
            dog.doAction(p5);
        }

        List<Dog> deadDogs = new ArrayList<>();
        for(Dog dog: dogs){
            for(Hole hole: holes){
                PVector v = dog.getPawDriverSideFront();
                PVector w = dog.getPawPassengerSideFront();
                PVector x = dog.getPawDriverSideRear();
                PVector y = dog.getPawPassengerSideRear();

                if (
                        PVector.dist(v,hole.location) < Hole.r && PVector.dist(w,hole.location) < Hole.r ||
                                PVector.dist(x,hole.location) < Hole.r && PVector.dist(y,hole.location) < Hole.r ||
                                PVector.dist(v,hole.location) < Hole.r && PVector.dist(x,hole.location) < Hole.r ||
                                PVector.dist(w,hole.location) < Hole.r && PVector.dist(y,hole.location) < Hole.r
                        ){
                    if(hole.active){
                        hole.millisAtBelch = p5.millis();
                    }
                    deadDogs.add(dog);
                }
            }
        }

        for(Dog dog: deadDogs){
            dogs.remove(dog);
        }

        // move the player location according to keys pressed
        if(keys.get(PConstants.UP)){
            playerLocation.z += 15f;
        }
        if(keys.get(PConstants.DOWN)){
            playerLocation.z -= 15f;
        }
        if(keys.get(PConstants.LEFT)){
            playerLocation.x -= 15f;
        }
        if(keys.get(PConstants.RIGHT)){
            playerLocation.x += 15f;
        }
        if(p5.mousePressed){
            lookAt = Rotation.rotatePVectorY((p5.mouseX - mouseXAtClick) * 0.0001f, lookAt);
            lookAt = Rotation.rotatePVectorX((p5.mouseY - mouseYAtClick) * 0.0001f, lookAt);
        }
    }

    void draw2D(PGraphics pG){
       pG.shape(gradient);
    }

    void drawScene(PGraphics pG, PApplet p5){
        pG.rotateZ(PConstants.PI);
        pG.translate(-playerLocation.x, -playerLocation.y, -playerLocation.z);

        pG.colorMode(PConstants.RGB);
        pG.pointLight(255, 0, 0, 0, 1000, 0);
        pG.pointLight(0, 255, 0, 100, 1000, 0);
        pG.pointLight(0, 0, 255, 0, 1000, 100);

        pG.shape(plane);

        // draw grid
        for( int n = -HALF_PLANE_WIDTH; n <= HALF_PLANE_WIDTH; n += 100){
            pG.stroke(0);
            pG.strokeWeight(2);
            pG.line(n, 1, -HALF_PLANE_WIDTH, n, 1, HALF_PLANE_WIDTH);
            pG.line(-HALF_PLANE_WIDTH, 0.01f, n, HALF_PLANE_WIDTH, 0.01f, n);
        }

        for(Dog dog: dogs){
            dog.draw(pG);
        }

        for(Hole hole: holes){
            pG.pushMatrix();
            pG.translate(hole.location.x, 30, hole.location.z);
            pG.rotateY(p5.millis() / 5000f);
            hole.draw(pG, p5);
            pG.popMatrix();
        }

        for(Dog dog: dogs){
            for(Hole hole: holes){
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
}
