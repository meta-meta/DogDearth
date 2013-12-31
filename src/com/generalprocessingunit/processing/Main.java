package com.generalprocessingunit.processing;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PShape;
import processing.core.PVector;

import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class Main extends PApplet{
    public static final int PLANE_WIDTH = 1000;
    PShape gradient;
    PShape plane;

    List<Dog> dogs = new ArrayList<>();

    static Map<Integer, Boolean> keys = new HashMap<>();
    static {
        keys.put(PConstants.UP, false);
        keys.put(PConstants.DOWN, false);
        keys.put(PConstants.LEFT, false);
        keys.put(PConstants.RIGHT, false);
    }

    PVector playerLocation = new PVector(0, 400, 0);

    public static void main(String[] args){
		PApplet.main(new String[] { /*"--present",*/ Main.class.getCanonicalName() });
	}
		
	@Override
	public void setup() {		
		size(1280, 720, PApplet.P3D);

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
        plane.fill(0,255,0);
        plane.vertex(-PLANE_WIDTH, 0, PLANE_WIDTH);
        plane.vertex(PLANE_WIDTH, 0, PLANE_WIDTH);
        plane.vertex(PLANE_WIDTH, 0, -PLANE_WIDTH);
        plane.vertex(-PLANE_WIDTH, 0, -PLANE_WIDTH);
        plane.endShape();

        for(int i = 0; i < 10; i++){
            float h = random(50, 100);
            dogs.add(new Dog(   this,
                                random(-PLANE_WIDTH, PLANE_WIDTH), h/2 + 1, random(-PLANE_WIDTH, PLANE_WIDTH),
                                0, ((int)random(4))*HALF_PI, 0,
                                random(20, 50), h, random(50, 150)));
        }
    }
	
	@Override
	public void draw(){
        camera();
        perspective();
        hint(DISABLE_DEPTH_MASK);
        shape(gradient);
        hint(ENABLE_DEPTH_MASK);


        camera(playerLocation.x, playerLocation.y, playerLocation.z, playerLocation.x, playerLocation.y, playerLocation.z + 100f, 0, -1f, 0);
        perspective(PI/2.0f, width/height, 0.001f, 10000f);

        shape(plane);

        // draw grid
        for( int n = -PLANE_WIDTH; n <= PLANE_WIDTH; n += 100){
            stroke(0);
            strokeWeight(2);
            line(n, 1, -PLANE_WIDTH, n, 1, PLANE_WIDTH );
            line(-PLANE_WIDTH, 0.01f, n, PLANE_WIDTH, 0.01f, n );
        }

        for(Dog dog: dogs){
            dog.draw(this);
            dog.doAction(this);
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