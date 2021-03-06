package com.generalprocessingunit.processing;

import processing.core.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DogDearth extends MathsHelpers
{
    /*
        Constants
     */
    public static final int PLANE_WIDTH = 100;
    public static final int NUM_DOGS = 10;
//    public static final int NUM_HOLES = 0;


    /*
        Game Entities
     */

    // Environment
    PShape sky;
    PShape plane;
    PShape gazebo;

    SpiralingShape spiralingShape;

    // Characters
    List<Dog> dogs = new ArrayList<>();
    List<Hole> holes = new ArrayList<>();
    Player player = new Player(new PVector(0, 2, -10));

    static PFont arial;

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
        createCartesianPlane(p5);

        createGazebo(p5);

        createDogs(p5);

        spiralingShape = new SpiralingShape(p5);


        arial = p5.createFont("Arial", 5, true, WolframBar.charset);
        WolframBar.setBarLocation(new PVector(50, 30, 100));

//        createHoles(p5);
    }

//    private void createHoles(PApplet p5) {
//        int holeBound = PLANE_WIDTH - Hole.r;
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
                p5.random(-PLANE_WIDTH / 2, PLANE_WIDTH / 2), p5.random(-PLANE_WIDTH / 2, PLANE_WIDTH / 2),
                0, ((int) p5.random(4)) * PApplet.HALF_PI, 0,
                color
            );
            dogs.add(dog);
        }
    }

    private void createGazebo(PApplet p5) {
        gazebo=p5.loadShape("gazebo.obj");
        gazebo.rotateY(PConstants.PI);
        gazebo.scale(3);
        gazebo.translate(0, gazebo.getHeight() * 0.05f, 0);
        gazebo.setFill(p5.color(0,0,255));
    }

    private void createCartesianPlane(PApplet p5) {
        // this texture should be much smaller and use p5.textureWrap(PConstants.REPEAT);
        // currently can't do this on a PShape.
        PGraphics grid = p5.createGraphics(5000, 5000);
        grid.beginDraw();
        grid.background(20, 255, 0, 20);

        int boldLine = grid.width / 10;
        for( int n = 0; n <= grid.width; n += grid.width / 100){
            grid.stroke(n % boldLine == 0 ? 0 : 50);
            grid.strokeWeight(n % boldLine == 0 ? 2 : 1);
            grid.line(n, 0, n, grid.width);
            grid.line(0, n, grid.width, n);
        }
        grid.endDraw();

        int subPlaneWidth = 10;
        plane = p5.createShape(PConstants.GROUP);
        for(int x = -PLANE_WIDTH / 2; x < PLANE_WIDTH / 2; x+= subPlaneWidth) {
            for (int y = -PLANE_WIDTH / 2; y < PLANE_WIDTH / 2; y += subPlaneWidth) {
                PShape subPlane = p5.createShape();
                subPlane.beginShape();
                subPlane.fill(255);
                subPlane.noStroke();
                subPlane.texture(grid);
                subPlane.textureMode(PConstants.NORMAL);
                subPlane.translate(x + subPlaneWidth / 2, 0, y + subPlaneWidth / 2);
                subPlane.vertex(-subPlaneWidth / 2, 0,  subPlaneWidth / 2, 0, 1);
                subPlane.vertex( subPlaneWidth / 2, 0,  subPlaneWidth / 2, 1, 1);
                subPlane.vertex( subPlaneWidth / 2, 0, -subPlaneWidth / 2, 1, 0);
                subPlane.vertex(-subPlaneWidth / 2, 0, -subPlaneWidth / 2, 0, 0);
                subPlane.endShape();
                plane.addChild(subPlane);
            }
        }

    }

    void updateState(PApplet p5){
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

            PVector headLocation = PVector.add(
                    dog.location,
                    dog.head.position

//                    Rotation.rotatePVectorZ(
//                            dog.orientation.z,
////                            Rotation.rotatePVectorY(dog.orientation.y, dog.head.position)
//                    )
            );

            PVector playerToHead = PVector.sub(player.location, headLocation);
//            PApplet.println(playerToHead);

            float angle = atan2(playerToHead.z, playerToHead.x) - HALF_PI;

//            PApplet.println("a: " + angle);


            dog.head.rotation.y = (TWO_PI - angle/* - dog.orientation.y*/) % TWO_PI;
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
    boolean moving = false;
    int millisAtMove = 0;

    private void updatePlayerLocation(PApplet p5) {
        boost = keys.get(PConstants.SHIFT) ? 2 : 1;

        // move the player location according to keys pressed
        PVector moveBy = new PVector(player.lookAt.x, 0, player.lookAt.z).normalize(null);
        if(keys.get(PConstants.UP) || keys.get(PConstants.DOWN) || keys.get(PConstants.LEFT) || keys.get(PConstants.RIGHT)){
            if(!moving) {
                moving = true;
                millisAtMove = p5.millis();
            }
            int ellapsedMillis = p5.millis() - millisAtMove;

            // accelerate from stop
            moveBy.mult((keys.get(PConstants.UP) ? 1 : -1) * PApplet.min(0.1f * boost, ellapsedMillis / 5000f));

            if(keys.get(PConstants.LEFT)){
                moveBy = Rotation.rotatePVectorY(HALF_PI, moveBy);
            }
            if(keys.get(PConstants.RIGHT)){
                moveBy = Rotation.rotatePVectorY(-HALF_PI, moveBy);
            }

            player.location.add(moveBy);




        } else {
            moving = false;
        }


        if(p5.mousePressed){
            player.lookAt = Rotation.rotatePVectorY((p5.mouseX - mouseXAtClick) * 0.0003f, player.lookAt);
            player.lookAt = Rotation.rotatePVectorX((p5.mouseY - mouseYAtClick) * 0.0003f, player.lookAt);
        }
    }

    void draw2D(PGraphics pG){
//       pG.shape(sky);
        pG.background(127);
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

        pG.textFont(arial);
        WolframBar.draw(pG, p5);

        pG.pushMatrix();
        pG.rotateY(-PApplet.atan2(player.lookAt.z, player.lookAt.x) + PConstants.HALF_PI);
        float lookY = player.lookAt.normalize(null).y;
        if(0.5f < lookY && lookY < 0.8f) {
            spiralingShape.increaseSpeed(p5);
        } else {
            spiralingShape.decreaseSpeed(p5);
        }

        spiralingShape.draw(p5, pG);

        pG.popMatrix();

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
        Hole hole = new Hole(p5, player.location.x, player.location.y, player.location.z, player.lookAt, boost);
        hole.toss(p5);
        holes.add(hole);
    }

    public void commandDogs(PApplet p5, Dog.Action action) {
        for(Dog dog : dogs) {
            dog.commandNextAction(p5, action);
        }
    }
}
