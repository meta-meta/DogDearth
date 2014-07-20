package com.generalprocessingunit.processing;

import processing.core.PApplet;
import processing.core.PGraphics;

import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;

public class EntryPoint extends PApplet{

    DogDearth dogDearth = new DogDearth();
    PGraphics pG;


    public static void main(String[] args){
		PApplet.main(new String[] { /*"--present",*/ EntryPoint.class.getCanonicalName() });
	}


	@Override
	public void setup() {		
		size(1400, 900, P3D);
        pG = createGraphics(width,height, P3D);
        dogDearth.setup(this);
    }

	
	@Override
	public void draw(){
        dogDearth.beforeDraw(this);

        pG.beginDraw();
        pG.camera();
        pG.perspective();
        pG.hint(DISABLE_DEPTH_MASK);
        dogDearth.draw2D(pG);
        pG.hint(ENABLE_DEPTH_MASK);

        pG.camera(0,0,0, dogDearth.player.lookAt.x, dogDearth.player.lookAt.y, dogDearth.player.lookAt.z, 0, 1f, 0);
//        pG.camera(dogDearth.playerLocation.x, dogDearth.playerLocation.y, dogDearth.playerLocation.z, dogDearth.playerLocation.x + dogDearth.player.lookAt.x, dogDearth.playerLocation.y + dogDearth.player.lookAt.y, dogDearth.playerLocation.z + dogDearth.player.lookAt.z, 0, -1f, 0);
        pG.perspective(PI / 2.8f, width / height, 0.1f, 10000f);

        dogDearth.drawScene(pG, this);

        pG.endDraw();
        image(pG, 0, 0);
    }

    @Override
    public void mousePressed(MouseEvent e)
    {
        dogDearth.mouseXAtClick = e.getX();
        dogDearth.mouseYAtClick = e.getY();
        super.mousePressed(e);
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        DogDearth.keys.put(e.getKeyCode(), true);
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
        DogDearth.keys.put(e.getKeyCode(), false);
        if(e.getKeyCode() == KeyEvent.VK_SPACE) {
            dogDearth.tossHole(this);
        }
    }
}