package com.generalprocessingunit.processing;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PShape;
import processing.core.PVector;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main extends PApplet{
    public static final int HALF_PLANE_WIDTH = 1500;
    public static final int NUM_DOGS = 75;
    public static final int NUM_HOLES = 10;
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

    PVector playerLocation = new PVector(0, 700, -1500);
    PVector lookAt = new PVector(0, -20, 100);

    public static void main(String[] args){
		PApplet.main(new String[] { /*"--present",*/ Main.class.getCanonicalName() });
	}
		
	@Override
	public void setup() {		
		size(1400, 900, PApplet.P3D);

        gradient = createShape();
        gradient.beginShape();
        gradient.fill(127);
        gradient.vertex(0,0);
        gradient.vertex(width, 0);
        gradient.fill(0);
        gradient.vertex(width, height);
        gradient.vertex(0, height);
        gradient.endShape();

        plane = createShape();
        plane.beginShape();
        plane.fill(20,255,0);
        plane.vertex(-HALF_PLANE_WIDTH, 0, HALF_PLANE_WIDTH);
        plane.vertex(HALF_PLANE_WIDTH, 0, HALF_PLANE_WIDTH);
        plane.vertex(HALF_PLANE_WIDTH, 0, -HALF_PLANE_WIDTH);
        plane.vertex(-HALF_PLANE_WIDTH, 0, -HALF_PLANE_WIDTH);
        plane.endShape();

        for(int i = 0; i < NUM_DOGS; i++){
            float h = random(150, 200);
            dogs.add(new Dog(   this,
                                random(-HALF_PLANE_WIDTH, HALF_PLANE_WIDTH), h/2 + 1, random(-HALF_PLANE_WIDTH, HALF_PLANE_WIDTH),
                                0, ((int)random(4))*HALF_PI, 0,
                                random(20, 50), h, random(50, 150)));
        }

        int holeBound = HALF_PLANE_WIDTH - Hole.r;
        for(int i = 0; i < NUM_HOLES; i++){
            PVector newHoleLoc = new PVector();

            while(isHoleClash(newHoleLoc)){
                newHoleLoc = new PVector(random(-holeBound, holeBound), 0,  random(-holeBound, holeBound));
            }

            holes.add(new Hole(this, newHoleLoc.x, newHoleLoc.z));
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
	
	@Override
	public void draw(){
        camera();
        perspective();
        hint(DISABLE_DEPTH_MASK);
        shape(gradient);
        hint(ENABLE_DEPTH_MASK);

        camera(playerLocation.x, playerLocation.y, playerLocation.z, playerLocation.x + lookAt.x, playerLocation.y + lookAt.y, playerLocation.z + lookAt.z, 0, -1f, 0);
        perspective(PI / 2.8f, width / height, 0.1f, 10000f);

        colorMode(RGB);
        pointLight(255, 0, 0, 0, 1000, 0);
        pointLight(0,255,0,100,1000,0);
        pointLight(0,0,255,0,1000,100);

        shape(plane);

        // draw grid
        for( int n = -HALF_PLANE_WIDTH; n <= HALF_PLANE_WIDTH; n += 100){
            stroke(0);
            strokeWeight(2);
            line(n, 1, -HALF_PLANE_WIDTH, n, 1, HALF_PLANE_WIDTH);
            line(-HALF_PLANE_WIDTH, 0.01f, n, HALF_PLANE_WIDTH, 0.01f, n );
        }

        for(Dog dog: dogs){
            dog.draw(this);
            dog.doAction(this);
        }

        for(Hole hole: holes){
            pushMatrix();
            translate(hole.location.x, 30, hole.location.z);
            rotateY(millis()/5000f);
            hole.draw(this);
            popMatrix();
        }

        List<Dog> deadDogs = new ArrayList<>();
        for(Dog dog: dogs){
            for(Hole hole: holes){
                noFill();
                PVector v = dog.getPawDriverSideFront();
                pushMatrix();
                translate(v.x,v.y,v.z);
                stroke(255,0,0);
                box(20);
                popMatrix();

                PVector w = dog.getPawPassengerSideFront();
                pushMatrix();
                translate(w.x,w.y,w.z);
                stroke(255,255,0);
                box(20);
                popMatrix();

                PVector x = dog.getPawDriverSideRear();
                pushMatrix();
                translate(x.x,x.y,x.z);
                stroke(0,255,0);
                box(20);
                popMatrix();

                PVector y = dog.getPawPassengerSideRear();
                pushMatrix();
                translate(y.x,y.y,y.z);
                stroke(0,0,255);
                box(20);
                popMatrix();

                if (
                        PVector.dist(v,hole.location) < Hole.r && PVector.dist(w,hole.location) < Hole.r ||
                        PVector.dist(x,hole.location) < Hole.r && PVector.dist(y,hole.location) < Hole.r ||
                        PVector.dist(v,hole.location) < Hole.r && PVector.dist(x,hole.location) < Hole.r ||
                        PVector.dist(w,hole.location) < Hole.r && PVector.dist(y,hole.location) < Hole.r
                ){
                    if(hole.active){
                        hole.millisAtBelch = millis();
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
            playerLocation.z += 5f;
        }
        if(keys.get(PConstants.DOWN)){
            playerLocation.z -= 5f;
        }
        if(keys.get(PConstants.LEFT)){
            playerLocation.x -= 5f;
        }
        if(keys.get(PConstants.RIGHT)){
            playerLocation.x += 5f;
        }
        if(mousePressed){
            lookAt = Rotation.rotatePVectorY((mouseX - mouseXAtClick) * 0.0001f, lookAt);
            lookAt = Rotation.rotatePVectorX((mouseY - mouseYAtClick) * 0.0001f, lookAt);
        }
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
        mouseXAtClick = e.getX();
        mouseYAtClick = e.getY();
        super.mousePressed(e);
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        keys.put(e.getKeyCode(), true);
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
        keys.put(e.getKeyCode(), false);
    }
}