package com.generalprocessingunit.processing;

import com.generalprocessingunit.vr.PAppletVR;
import processing.core.PGraphics;
import processing.core.PVector;

import java.awt.event.KeyEvent;

public class EntryPointVR extends PAppletVR
{

    DogDearth dogDearth = new DogDearth();

    public static void main(String[] args){
        PAppletVR.main(EntryPointVR.class);
    }


    @Override
    public void setup() {
        super.setup();
        dogDearth.setup(this);
    }

    @Override
    protected void updateState() {
        dogDearth.updateState(this);
        headContainer.setLocation(dogDearth.player.location);
    }


    @Override
    protected void drawViewPreCamera(int eye, PGraphics pG) {
        dogDearth.draw2D(pG);
    }

    @Override
    protected void drawView(int eye, PGraphics pG) {
        dogDearth.player.lookAt = lookat;
        dogDearth.drawScene(pG, this);
    }

    @Override
    public void keyPressed(KeyEvent e)
    {
        DogDearth.keys.put(e.getKeyCode(), true);
        super.keyPressed(e);
    }

    @Override
    public void keyReleased(KeyEvent e)
    {
        DogDearth.keys.put(e.getKeyCode(), false);

        if(e.getKeyCode() == ESC){
            exit();
        }

        if(e.getKeyCode() == KeyEvent.VK_SPACE) {
            dogDearth.tossHole(this);
        }

        if(e.getKeyCode() == KeyEvent.VK_L) {
            dogDearth.commandDogs(this, Dog.Action.LIE_DOWN);
            System.out.println("Lie Down");
        }

        if(e.getKeyCode() == KeyEvent.VK_S) {
            dogDearth.commandDogs(this, Dog.Action.SIT);
            System.out.println("Sit");

        }

        super.keyReleased(e);
    }
}