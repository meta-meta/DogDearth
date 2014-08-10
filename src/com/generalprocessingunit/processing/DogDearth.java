package com.generalprocessingunit.processing;

import processing.core.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DogDearth
{
    /*
        Constants
     */
    public static final int HALF_PLANE_WIDTH = 4000;
    public static final int NUM_DOGS = 10;
//    public static final int NUM_HOLES = 0;


    /*
        Game Entities
     */

    // Environment
    PShape sky;
    PShape plane;
    PShape gazebo;

    // Characters
    List<Dog> dogs = new ArrayList<>();
    List<Hole> holes = new ArrayList<>();
    Player player = new Player(new PVector(0, 250, -1000));


    /*
        Keyboard state
     */
    static Map<Integer, Boolean> keys = new HashMap<>();
    static {
        keys.put(PConstants.UP, false);
        keys.put(PConstants.DOWN, false);
        keys.put(PConstants.LEFT, false);
        keys.put(PConstants.RIGHT, false);
        keys.put(PConstants.SHIFT, false);
    }

    // Mouse State TODO: create robust 3d control class for mouse
    int mouseXAtClick = 0;
    int mouseYAtClick = 0;


    void setup(PApplet p5){
        createSky(p5);

        createCartesianPlane(p5);

        createGazebo(p5);

        createDogs(p5);

//        createHoles(p5);
    }

//    private void createHoles(PApplet p5) {
//        int holeBound = HALF_PLANE_WIDTH - Hole.r;
//        for(int i = 0; i < NUM_HOLES; i++){
//            PVector newHoleLoc = new PVector();
//
//            while(isHoleClash(newHoleLoc)){
//                newHoleLoc = new PVector(p5.random(-holeBound, holeBound), 0,  p5.random(-holeBound, holeBound));
//            }
//
//            holes.add(new Hole(p5, newHoleLoc.x, newHoleLoc.z));
//        }
//    }

    private boolean isHoleClash(PVector newLocation){
        if (PVector.dist(new PVector(), newLocation) < 2 * Hole.r) {
            return true;
        }

        for (Hole hole : holes) {
            if (PVector.dist(hole.location, newLocation) < 2 * Hole.r) {
                return true;
            }
        }

        return false;
    }

    private void createDogs(PApplet p5) {
        for (int i = 0; i < NUM_DOGS; i++) {
            float h = p5.random(150, 200);

            p5.colorMode(PConstants.HSB);
            int color = p5.color(p5.random(30, 55), p5.random(10, 140), p5.random(10, 200));
            p5.colorMode(PConstants.RGB);

            Dog dog = new Dog(p5,
//                p5.random(-HALF_PLANE_WIDTH, HALF_PLANE_WIDTH), p5.random(-HALF_PLANE_WIDTH, HALF_PLANE_WIDTH),
                0, 0,
                0, ((int) p5.random(4)) * PApplet.HALF_PI, 0,
                p5.random(20, 50), h, p5.random(50, 150),
                color
            );
            dogs.add(dog);
        }
    }

    private void createGazebo(PApplet p5) {
        gazebo=p5.loadShape("gazebo.obj");
        gazebo.translate(0, 80, 0);
        gazebo.rotateY(PConstants.PI);
//        gazebo.translate(gazebo.getWidth()/2, 0, 0);
        gazebo.scale(300);
        gazebo.setFill(p5.color(0,0,255));
    }

    private void createSky(PApplet p5) {
        //TODO: replace with a skybox or textured sphere
        sky = p5.createShape();
        sky.beginShape();
        sky.fill(127);
        sky.vertex(-p5.width * 2, -p5.height * 2);
        sky.vertex(p5.width * 4, -p5.height * 2);
        sky.fill(0);
        sky.vertex(p5.width * 4, p5.height * 4);
        sky.vertex(-p5.width * 2, p5.height * 4);
        sky.endShape();
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

    void beforeDraw(PApplet p5){
        updateDogs(p5);

        updatePlayerLocation(p5);

        updateHoles(p5);
    }

    private void updateHoles(PApplet p5) {
        for(Hole hole: holes) {
            hole.beforeDraw(p5);
        }
    }

    private void updateDogs(PApplet p5) {
        for(Dog dog: dogs){
            dog.doAction(p5);
        }

        List<Dog> deadDogs = new ArrayList<>();
        for(Dog dog: dogs){
            PVector a = dog.legs[0].getGlobalLocation();
            PVector b = dog.legs[1].getGlobalLocation();
            PVector c = dog.legs[2].getGlobalLocation();
            PVector d = dog.legs[3].getGlobalLocation();

            for(Hole hole: holes){

                if (
                        PVector.dist(a, hole.location) < Hole.r && PVector.dist(b, hole.location) < Hole.r ||
                        PVector.dist(c, hole.location) < Hole.r && PVector.dist(d, hole.location) < Hole.r ||
                        PVector.dist(a, hole.location) < Hole.r && PVector.dist(c, hole.location) < Hole.r ||
                        PVector.dist(b, hole.location) < Hole.r && PVector.dist(d, hole.location) < Hole.r
                ) {
                    if (hole.active) {
                        hole.millisAtBelch = p5.millis();
                        deadDogs.add(dog);
                    }
                }
            }
        }

        for(Dog dog: deadDogs){
            dogs.remove(dog);
        }
    }


    float boost = 1;

    private void updatePlayerLocation(PApplet p5) {
        if(keys.get(PConstants.SHIFT)){
            boost = 2;
        } else {
            boost = 1;
        }

        // move the player location according to keys pressed
        PVector moveBy = new PVector(player.lookAt.x, 0, player.lookAt.z).normalize(null);
        moveBy.mult(8 * boost);

        if(keys.get(PConstants.UP)){
            player.location.add(moveBy);
        }
        if(keys.get(PConstants.DOWN)){
            player.location.sub(moveBy);
        }
        if(keys.get(PConstants.LEFT)){
            player.lookAt = Rotation.rotatePVectorY(-0.015f * boost, player.lookAt);
        }
        if(keys.get(PConstants.RIGHT)){
            player.lookAt = Rotation.rotatePVectorY(0.015f * boost, player.lookAt);
        }
        if(p5.mousePressed){
            player.lookAt = Rotation.rotatePVectorY((p5.mouseX - mouseXAtClick) * 0.0003f, player.lookAt);
            player.lookAt = Rotation.rotatePVectorX((p5.mouseY - mouseYAtClick) * 0.0003f, player.lookAt);
        }
    }

    void draw2D(PGraphics pG){
       pG.shape(sky);
    }

    void drawScene(PGraphics pG, PApplet p5){

        /*
            Lights
          */
        pG.colorMode(PConstants.HSB);

        // RGB sky lights
        pG.directionalLight(0, 100,   120, 1f, -1, 1f);
        pG.directionalLight(100, 100, 120, 0, -1, 0);
        pG.directionalLight(180, 100, 120, -1f, -1, 1f);

        // Green floor light
        pG.directionalLight(90, 200, 255, 0, 1, 0);

        /*
            Entities
         */
        pG.shape(plane);
        pG.shape(gazebo);

        for(Dog dog: dogs){
            dog.draw(pG);
        }

        for(Hole hole: holes){
            hole.draw(pG, p5);
        }

        player.draw(pG);

        Helper.drawBoxFeet(pG, dogs);
    }


    public void tossHole(PApplet p5) {
        // TODO: hole should get created when player grabs it. hole.toss() gets called when player tosses it
        Hole hole = new Hole(p5, player.location.x, 200f, player.location.z, player.lookAt, boost);
        hole.toss(p5);
        holes.add(hole);
    }

    public void commandDogs(PApplet p5, Dog.Action action) {
        for(Dog dog : dogs) {
            dog.commandNextAction(p5, action);
        }
    }
}
