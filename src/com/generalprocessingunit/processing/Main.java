package com.generalprocessingunit.processing;
import processing.core.PApplet;
import processing.core.PShape;


public class Main extends PApplet{
    PShape gradient;
    PShape plane;

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
        plane.vertex(-1000, 0, 1000);
        plane.vertex(1000, 0, 1000);
        plane.vertex(1000, 0, -1000);
        plane.vertex(-1000, 0, -1000);
        plane.endShape();

    }
	
	@Override
	public void draw(){
        camera();
        perspective();
        hint(DISABLE_DEPTH_MASK);
        shape(gradient);
        hint(ENABLE_DEPTH_MASK);


        camera(0, 1000, 1000, 0, 0, 0, 0, -1f, 0);
        perspective(PI/2.0f, width/height, 0.001f, 10000f);

        shape(plane);

    }
}
