package com.generalprocessingunit.processing;

import com.generalprocessingunit.vr.RazerHydra;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PVector;

public class Player {
    PVector location;
    PVector lookAt = new PVector(0, .2f, 1);

    PVector sternumPosition = new PVector();
    PVector handPosition = new PVector();
    PVector quiverLocation = new PVector(52, -75, 96);

    RazerHydra razerHydra = new RazerHydra();

    Player(PVector location) {
        this.location = location;

    }

    void draw(PGraphics pG) {
        razerHydra.poll();

        sternumPosition = razerHydra.position[0];
        handPosition = getTranslatedHandPosition(razerHydra.position[1]);

        pG.pushMatrix();
        pG.translate(location.x, location.y -60, location.z); // -60 == approx. position of sternum relative to head
        pG.translate(handPosition.x, -handPosition.y, -handPosition.z);  // -y and -z cause uh????
        pG.fill(127 + 127f * razerHydra.trigger[1]);
        pG.box(20);



//        System.out.println(handPosition);

        pG.popMatrix();

        if (handInQuiver()) {
            pG.hint(PConstants.DISABLE_DEPTH_MASK);
            pG.fill(127, 0, 0, 50);
            pG.rect(-pG.width / 2, -pG.height / 2, pG.width, pG.height);
            pG.hint(PConstants.ENABLE_DEPTH_MASK);
        }

        if(gotHole()) {
            pG.hint(PConstants.DISABLE_DEPTH_MASK);
            pG.fill(0, 0, 0, 50);
            pG.rect(-pG.width / 2, -pG.height / 2, pG.width, pG.height);
            pG.hint(PConstants.ENABLE_DEPTH_MASK);
        }
    }

    private boolean gotHole() {
        return razerHydra.isGrabbing(1) && vectorInQuiver(getTranslatedHandPosition(razerHydra.positionAtGrab[1]));
    }

    private PVector getTranslatedHandPosition(PVector vec) {
        vec = PVector.sub(vec, sternumPosition);
        return PVector.mult(vec, 0.6f);
    }

    private boolean handInQuiver() {
        return vectorInQuiver(handPosition);
    }

    private boolean vectorInQuiver(PVector vec) {
        return PVector.dist(vec, quiverLocation) < 50;
    }

}
