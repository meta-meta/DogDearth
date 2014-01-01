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
    float lyingW, lyingH, lyingD, sittingW, sittingH, sittingD, standingW, standingH, standingD;
    float hue, sat, bri;
    float speed;
    float walkingSpeed;
    float runningSpeed;

    Dog(PApplet p5, float x, float y, float z, float rx, float ry, float rz, float w, float h, float d){
//        model = p5.createShape();
//        model.beginShape();
//        model.endShape();

        location = new PVector(x, y, z);
        orientation = new PVector(rx, ry, rz);

        standingW = w;
        standingH = h;
        standingD = d;

        sittingW = w;
        sittingH = h*1.5f;
        sittingD = d*0.7f;

        lyingW = w;
        lyingH = h/2;
        lyingD = d;

        this.w = w;
        this.h = h;
        this.d = d;

        hue = p5.random(30, 70);
        sat = p5.random(10, 140);
        bri = p5.random(10, 200);

        speed = p5.random(0.05f, 1.5f);
        walkingSpeed = 500 * speed;
        runningSpeed = 1000 * speed;
    }

    void draw(PApplet p5){
        p5.pushMatrix();
        p5.colorMode(PConstants.HSB);
//        p5.stroke(hue, sat, bri);
//        p5.noFill();
        p5.fill(hue, sat, bri);
        p5.stroke(200);
        p5.colorMode(PConstants.RGB);


        p5.translate(location.x, location.y, location.z);
        p5.rotateY(orientation.y);
        p5.box(d, h, w);

        p5.translate(d/2, h/2, 0);
        p5.box(w);
        p5.popMatrix();
    }

    boolean tryMove(float x, float z){
        // TODO: PLANE_WIDTH decides the coordinates of the turf, and the turf is really what should be referenced here
        if(x > Main.PLANE_WIDTH || x < -Main.PLANE_WIDTH || z > Main.PLANE_WIDTH || z < -Main.PLANE_WIDTH){
            return false;
        } else {
            location.x = x;
            location.z = z;
            return true;
        }
    }

    Action currentAction = Action.STAND;
    State currentState = State.STANDING;
    boolean actionInProgress = false;
    int millisAtActionStart;
    PVector locationAtActionStart;
    PVector orientationAtActionStart;
    boolean turnRight;

    enum State {
        WALKING     (0.6f),
        RUNNING     (0.8f),
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
        RUN         (1000, State.RUNNING),
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

        private void move(Dog dog, float speed, float progress){
            float xCoef = PApplet.cos(-dog.orientation.y),
                    zCoef = PApplet.sin(-dog.orientation.y);
            boolean res = dog.tryMove(
                    dog.locationAtActionStart.x + xCoef * speed * progress,
                    dog.locationAtActionStart.z + zCoef * speed * progress );
            if(!res){
                dog.actionInProgress = false;
            }
        }

        void doAction(int millis, Dog dog){
            float progress = (millis/(float)duration);

            switch (this) {
                case WALK: {
                    move(dog, dog.walkingSpeed, progress);
                    break;
                }
                case RUN: {
                    move(dog, dog.runningSpeed, progress);
                    break;
                }
                case TURN: {
                    dog.orientation.y = dog.orientationAtActionStart.y + progress * PConstants.HALF_PI * (dog.turnRight ? 1 : -1);
                    break;
                }
                case LIE_DOWN: {
                    if(State.SITTING == dog.currentState){
                        lieDown(dog, progress, dog.sittingH, dog.sittingW, dog.sittingD, dog.lyingH, dog.lyingW, dog.lyingD);
                    } else if(State.STANDING == dog.currentState){
                        lieDown(dog, progress, dog.standingH, dog.standingW, dog.standingD, dog.lyingH, dog.lyingW, dog.lyingD);
                    }
                    break;
                }
                case SIT: {
                    if(State.LYING == dog.currentState){
                        lieDown(dog, progress, dog.lyingH, dog.lyingW, dog.lyingD, dog.sittingH, dog.sittingW, dog.sittingD);
                    } else if(State.STANDING == dog.currentState){
                        lieDown(dog, progress, dog.standingH, dog.standingW, dog.standingD, dog.sittingH, dog.sittingW, dog.sittingD);
                    }
                    break;                    
                }
                case STAND: {
                    if(State.LYING == dog.currentState){
                        lieDown(dog, progress, dog.lyingH, dog.lyingW, dog.lyingD, dog.standingH, dog.standingW, dog.standingD);
                    } else if(State.SITTING == dog.currentState){
                        lieDown(dog, progress, dog.sittingH, dog.sittingW, dog.sittingD, dog.standingH, dog.standingW, dog.standingD);
                    }
                    break;
                }
                default:
                    break;
            }
        }
        
        void lieDown(Dog dog, float progress, float initialH, float initialW, float initialD, float goalH, float goalW, float goalD){
            dog.h = initialH + ((goalH - initialH)) * progress;
            dog.w = initialW + ((goalW - initialW)) * progress;
            dog.d = initialD + ((goalD - initialD)) * progress;
        }
    }

    static {
        State.WALKING.setPossibleActions     (Action.TURN, Action.STAND, Action.RUN);
        State.RUNNING.setPossibleActions     (Action.TURN /*TODO: slide when turning and running*/, Action.STAND, Action.WALK);
        State.SITTING.setPossibleActions     (Action.LIE_DOWN, Action.STAND);
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
        currentState = currentAction.endState;
        millisAtActionStart = p5.millis();
        locationAtActionStart = new PVector(location.x, location.y, location.z);
        orientationAtActionStart = new PVector(orientation.x, orientation.y, orientation.z);
        turnRight = (int)p5.random(2) == 1;

        Action[] possibleActions = currentState.possibleActions;
        if(p5.random(1) > currentState.probabilityMaintainState){
            currentAction = possibleActions[(int)p5.random(possibleActions.length)];
        }
        actionInProgress = true;
    }
}
