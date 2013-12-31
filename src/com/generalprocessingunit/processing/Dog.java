package com.generalprocessingunit.processing;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PShape;
import processing.core.PVector;

public class Dog
{
    PVector location;
    PVector orientation;
    float w, h, d;
    PShape model; // TODO: load blender mesh into this
    float hue, sat, bri;
    float walkingSpeed;
    float turningSpeed;

    Dog(PApplet p5, float x, float y, float z, float rx, float ry, float rz, float w, float h, float d){
//        model = p5.createShape();
//        model.beginShape();
//        model.endShape();

        location = new PVector(x, y, z);
        orientation = new PVector(rx, ry, rz);

        this.w = w;
        this.h = h;
        this.d = d;

        hue = p5.random(30, 70);
        sat = p5.random(10, 140);
        bri = p5.random(10, 200);

        walkingSpeed = p5.random(0.05f, 1.5f);
        turningSpeed = (walkingSpeed/100) * PConstants.HALF_PI;
    }

    void draw(PApplet p5){
        p5.pushMatrix();
        p5.translate(location.x, location.y, location.z);
        p5.rotateY(orientation.y);
        p5.colorMode(PConstants.HSB);
        p5.fill(hue, sat, bri);
        p5.noStroke();
        p5.colorMode(PConstants.RGB);
        p5.box(w, h, d);
        p5.popMatrix();
    }

    Action currentAction = Action.STAND;
    boolean actionInProgress = false;
    int millisAtActionStart;
    PVector locationAtActionStart;
    PVector orientationAtActionStart;
    boolean turnRight;

    enum State {
        WALKING     (0.6f),
        SITTING     (0.2f),
        STANDING    (0.3f),
        TURNING     (0.05f),
        LYING       (0.6f);

        float probabilityMaintainState;
        Action[] possibleActions;

        State(float probabilityMaintainState){
            this.probabilityMaintainState = probabilityMaintainState;
        }

        void setPossibleActions(Action ... possibleActions){
            this.possibleActions = possibleActions;
        }
    }

    enum Action {
        WALK        (1000, State.WALKING),
        SIT         (2000, State.SITTING),
        STAND       (2000, State.STANDING),
        TURN        (500, State.STANDING),
        LIE_DOWN    (3000, State.LYING);

        int duration;
        State endState;

        Action(int duration, State endState){
            this.duration = duration;
            this.endState = endState;
        }

        void doAction(int millis, Dog dog){
            switch (this) {
                case WALK:
                    float xCoef = PApplet.cos(dog.orientation.y),
                        zCoef = PApplet.sin(dog.orientation.y);
                    dog.location.x = dog.locationAtActionStart.x + xCoef * dog.walkingSpeed * millis;
                    dog.location.z = dog.locationAtActionStart.z + zCoef * dog.walkingSpeed * millis;
                    break;
                case TURN:
                    dog.orientation.y = dog.orientationAtActionStart.y + millis * dog.turningSpeed * (dog.turnRight ? 1 : -1);
                    break;
                default:
                    break;
            }
        }
    }

    static {
        State.WALKING.setPossibleActions     (Action.LIE_DOWN, Action.STAND);
        State.SITTING.setPossibleActions     (Action.TURN, Action.STAND);
        State.STANDING.setPossibleActions    (Action.TURN, Action.SIT, Action.LIE_DOWN, Action.WALK);
        State.TURNING.setPossibleActions     (Action.STAND, Action.SIT, Action.LIE_DOWN, Action.WALK);
        State.LYING.setPossibleActions       (Action.SIT, Action.STAND);
    }

    public void doAction(PApplet p5)
    {
        if(!actionInProgress){
            decideNextAction(p5);
        } else {
            continueAction(p5);
        }

    }

    private void continueAction(PApplet p5)
    {
        if(p5.millis() - millisAtActionStart > currentAction.duration){
            actionInProgress = false;
        } else {
            currentAction.doAction(p5.millis() - millisAtActionStart, this);
        }
    }

    void decideNextAction(PApplet p5){
        millisAtActionStart = p5.millis();
        locationAtActionStart = new PVector(location.x, location.y, location.z);
        orientationAtActionStart = new PVector(orientation.x, orientation.y, orientation.z);
        turnRight = (int)p5.random(2) == 1;

        Action[] possibleActions = currentAction.endState.possibleActions;
        if(p5.random(1) > currentAction.endState.probabilityMaintainState){
            currentAction = possibleActions[(int)p5.random(possibleActions.length)];
        }
        actionInProgress = true;
    }
}
