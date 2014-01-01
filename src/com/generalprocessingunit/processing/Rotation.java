package com.generalprocessingunit.processing;

import processing.core.PApplet;
import processing.core.PVector;

public class Rotation {
    static PVector getRotatedPVector(float x, float y, float z, float roll, float pitch, float yaw){
        PVector v = new PVector(x,y,z);
        v = rotatePVectorZ(roll, v);
        v = rotatePVectorX(pitch, v);
        v = rotatePVectorY(yaw, v);
        return v;
    }

    static PVector rotatePVectorZ(float angle, PVector vector) {

        PVector v = new PVector(0, 0, vector.z);

        v.x += (vector.x * PApplet.cos(angle) - vector.y * PApplet.sin(angle));
        v.y += (vector.x * PApplet.sin(angle) + vector.y * PApplet.cos(angle));

        return v;
    }

    static PVector rotatePVectorY(float angle, PVector vector) {

        PVector v = new PVector(0, vector.y, 0);

        v.z = (vector.z * PApplet.cos(angle) - vector.x * PApplet.sin(angle));
        v.x = (vector.z * PApplet.sin(angle) + vector.x * PApplet.cos(angle));

        return v;
    }

    static PVector rotatePVectorX(float angle, PVector vector) {

        PVector v = new PVector(vector.x, 0, 0);

        v.y = (vector.y * PApplet.cos(angle) - vector.z * PApplet.sin(angle));
        v.z = (vector.y * PApplet.sin(angle) + vector.z * PApplet.cos(angle));

        return v;
    }
}
