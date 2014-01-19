package com.generalprocessingunit.processing;

import com.generalprocessingunit.vr.PAppletVR;
import processing.core.PGraphics;
import processing.core.PVector;

import java.awt.event.KeyEvent;

public class EntryPoint extends PAppletVR
{

    DogDearth dogDearth = new DogDearth();

    public static void main(String[] args){
        PAppletVR.main(new EntryPoint());
    }


    @Override
    public void setup() {
        super.setup();
        dogDearth.setup(this);
    }

    @Override
    public void beforeDraw() {
        super.beforeDraw();
        dogDearth.beforeDraw(this);
    }

    @Override
    public void draw() {
        super.draw();
    }

    @Override
    public void drawInitialScene(PGraphics scene)
    {
        super.drawInitialScene(scene);
    }

    @Override
    public void draw2dScene(PGraphics scene, int eye, PVector hydraBasePos)
    {
        dogDearth.draw2D(scene);
    }



    @Override
    public void drawScene(PGraphics scene, int eye, PVector hydraBasePos) {

//        pG.camera(dogDearth.playerLocation.x, dogDearth.playerLocation.y, dogDearth.playerLocation.z, dogDearth.playerLocation.x + dogDearth.lookAt.x, dogDearth.playerLocation.y + dogDearth.lookAt.y, dogDearth.playerLocation.z + dogDearth.lookAt.z, 0, -1f, 0);
//        pG.perspective(PI / 2.8f, width / height, 0.1f, 10000f);

        dogDearth.drawScene(scene, this);

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
    }
}