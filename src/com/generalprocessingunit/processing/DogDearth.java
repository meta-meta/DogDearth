package com.generalprocessingunit.processing;

import processing.core.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DogDearth
{
    public static final int HALF_PLANE_WIDTH = 1000;
    public static final int NUM_DOGS = 30;
    public static final int NUM_HOLES = 1;
    PShape gradient;
    PShape plane;
    PShape gazebo;

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

    Player player = new Player(new PVector(0, 250, -1000));


    void setup(PApplet p5){
        gradient = p5.createShape();
        gradient.beginShape();
        gradient.fill(127);
        gradient.vertex(-p5.width*2, -p5.height*2);
        gradient.vertex(p5.width*4, -p5.height*2);
        gradient.fill(0);
        gradient.vertex(p5.width*4, p5.height*4);
        gradient.vertex(-p5.width*2, p5.height*4);
        gradient.endShape();

        createCartesianPlane(p5);

        gazebo=p5.loadShape("gazebo.obj");
        gazebo.translate(0, 80, 0);
        gazebo.rotateY(PConstants.PI);
//        gazebo.translate(gazebo.getWidth()/2, 0, 0);
        gazebo.scale(300);
        gazebo.setFill(p5.color(0,0,255));

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

    private void createCartesianPlane(PApplet p5) {
        // this texture should be much smaller and use p5.textureWrap(PConstants.REPEAT);
        // currently can't do this on a PShape. Do we want to create this plane every render??
        PGraphics grid = p5.createGraphics(HALF_PLANE_WIDTH, HALF_PLANE_WIDTH);
        grid.beginDraw();
        grid.background(20, 255, 0);

        for( int n = 0; n <= HALF_PLANE_WIDTH; n += 10){
            grid.stroke(n % 100 == 0 ? 0 : 50);
            grid.strokeWeight(n % 100 == 0 ? 2 : 1);
            grid.line(n, 0, n, HALF_PLANE_WIDTH);
            grid.line(0, n, HALF_PLANE_WIDTH, n);
        }
        grid.endDraw();

        plane = p5.createShape();
        plane.beginShape();
        plane.fill(255);
        plane.texture(grid);
        plane.textureMode(PConstants.NORMAL);
        plane.vertex(-HALF_PLANE_WIDTH, 0, HALF_PLANE_WIDTH, 0, 1);
        plane.vertex(HALF_PLANE_WIDTH, 0, HALF_PLANE_WIDTH, 1, 1);
        plane.vertex(HALF_PLANE_WIDTH, 0, -HALF_PLANE_WIDTH, 1, 0);
        plane.vertex(-HALF_PLANE_WIDTH, 0, -HALF_PLANE_WIDTH, 0, 0);
        plane.endShape();
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

//        System.out.println(p5.frameRate);
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
            player.location.z += 15f;
        }
        if(keys.get(PConstants.DOWN)){
            player.location.z -= 15f;
        }
        if(keys.get(PConstants.LEFT)){
            player.location.x -= 15f;
        }
        if(keys.get(PConstants.RIGHT)){
            player.location.x += 15f;
        }
        if(p5.mousePressed){
            player.lookAt = Rotation.rotatePVectorY((p5.mouseX - mouseXAtClick) * 0.0001f, player.lookAt);
            player.lookAt = Rotation.rotatePVectorX((p5.mouseY - mouseYAtClick) * 0.0001f, player.lookAt);
        }
    }

    void draw2D(PGraphics pG){
       pG.shape(gradient);
    }

    void drawScene(PGraphics pG, PApplet p5){
        pG.rotateZ(PConstants.PI);
        pG.translate(-playerLocation.x, -playerLocation.y, -playerLocation.z);

        pG.colorMode(PConstants.HSB);
        pG.directionalLight(0, 100,   120, 1f, -1, 1f);
        pG.directionalLight(100, 100, 120, 0, -1, 0);
        pG.directionalLight(180, 100, 120, -1f, -1, 1f);

        pG.directionalLight(90, 200, 255, 0, 1, 0);

        pG.shape(plane);
        pG.shape(gazebo);

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

        player.draw(pG);

//        drawHelperBoxFeet(pG);
    }

    private void drawHelperBoxFeet(PGraphics pG)
    {
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
