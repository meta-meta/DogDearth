package com.generalprocessingunit.processing;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PShape;
import processing.core.PVector;

public class Dog
{
    PVector location;
    PVector orientation;
    PVector dimensions, lyingDimensions, sittingDimensions, standingDimensions;
    float hue, sat, bri;
    float speed;
    float walkingSpeed;
    float runningSpeed;

    static PShape defaultBody;
    PShape body;
    PShape[] legs = new PShape[4];  // [FL, FR, RL, RR]
    float legRotation[] = new float[4];
    PVector legPosition[] = new PVector[4];

    Dog(PApplet p5, float x, float y, float z, float rx, float ry, float rz, float w, float h, float d){
        hue = p5.random(30, 55);
        sat = p5.random(10, 140);
        bri = p5.random(10, 200);
        p5.colorMode(PConstants.HSB);

        if(null == defaultBody) {
            defaultBody = p5.loadShape("dog_body.obj");
        }

        body = defaultBody;
        float scale = p5.random(1,2);

        float yAdj = /*(scale*15f) **/ 1.2f * body.getHeight() / 2;
        float xAdj = (scale*15f) * 0.75f * body.getWidth() / 2;
        float zAdj = (scale*15f) * 0.6f * body.getDepth() / 2;


        body.translate(0, yAdj, 0);
        body.scale(scale*15f);

        body.setFill(p5.color(hue, sat, bri));

        for(int i = 0; i < 4; i++){
            legs[i] = p5.loadShape(String.format("dog_leg_%s.obj", i));
            legs[i].scale(scale*15f);
            legs[i].setFill(p5.color(hue, sat, bri));
        }

        legPosition[0] = new PVector(xAdj * 0.15f, (scale*15f) * yAdj * 0.6f, zAdj );
        legPosition[1] = new PVector(xAdj * 0.15f, (scale*15f) * yAdj * 0.6f, -zAdj);
        legPosition[2] = new PVector(-xAdj,        (scale*15f) *yAdj * 0.6f, zAdj * 0.8f );
        legPosition[3] = new PVector(-xAdj,        (scale*15f) *yAdj * 0.6f, -zAdj * 0.8f);


        p5.colorMode(PConstants.RGB);

        w = body.getWidth();
        h = body.getHeight();
        d = body.getDepth();
        y = h/2;

        location = new PVector(x, y, z);
        orientation = new PVector(rx, ry, rz);

        dimensions = new PVector(w, h, d);
        standingDimensions = new PVector(w, h, d);
        sittingDimensions = new PVector(w, h * 1.5f, d * 0.7f);
        lyingDimensions = new PVector(w, h / 2, d);

        speed = p5.random(0.05f, 1.5f);
        walkingSpeed = 300 * speed;
        runningSpeed = 1000 * speed;
    }

    PVector getPawDriverSideFront(){
        PVector v = Rotation.rotatePVectorY(orientation.y, new PVector(dimensions.x/2 - 30, 0, dimensions.z/2 - 10));
        v.add(location.x, 0, location.z);
        return v;
    }
    PVector getPawPassengerSideFront(){
        PVector v = Rotation.rotatePVectorY(orientation.y, new PVector(dimensions.x/2 - 30, 0, -dimensions.z/2 + 10));
        v.add(location.x, 0, location.z);
        return v;
    }

    PVector getPawDriverSideRear(){
        PVector v = Rotation.rotatePVectorY(orientation.y, new PVector(-dimensions.x/2 + 45, 0, dimensions.z/2 - 10));
        v.add(location.x, 0, location.z);
        return v;
    }
    PVector getPawPassengerSideRear(){
        PVector v = Rotation.rotatePVectorY(orientation.y, new PVector(-dimensions.x/2 + 45, 0, -dimensions.z/2 + 10));
        v.add(location.x, 0, location.z);
        return v;
    }

    void draw(PGraphics pG){
        pG.colorMode(PConstants.HSB);
        pG.fill(hue, sat, bri);
        pG.stroke(200);
        pG.colorMode(PConstants.RGB);

        pG.pushMatrix();

        // Body
        pG.translate(location.x, location.y, location.z);
        pG.rotateY(orientation.y);

        pG.shape(body);


        for(int i = 0; i < 4; i++){
            pG.pushMatrix();
            pG.translate(legPosition[i].x, legPosition[i].y, legPosition[i].z);
            pG.rotateZ(legRotation[i]);
            pG.shape(legs[i]);
            pG.popMatrix();
        }
//        p5.box(dimensions.z, dimensions.y, dimensions.x);

        // Head
//        p5.translate(dimensions.z/2, dimensions.y/2, 0);
//        p5.box(standingDimensions.x * 1.3f);

        pG.popMatrix();
    }

    boolean tryMove(float x, float z){
        // TODO: HALF_PLANE_WIDTH decides the coordinates of the turf, and the turf is really what should be referenced here
        if(x > DogDearth.HALF_PLANE_WIDTH || x < -DogDearth.HALF_PLANE_WIDTH || z > DogDearth.HALF_PLANE_WIDTH || z < -DogDearth.HALF_PLANE_WIDTH){
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
        WALK        (2000, State.WALKING),
        RUN         (1000, State.RUNNING),
        SIT         (500, State.SITTING),
        STAND       (600, State.STANDING),
        TURN        (500, State.STANDING),
        LIE_DOWN    (800, State.LYING),
        STAY        (1000);

        int duration;
        State endState;

        Action(int duration){
            this.duration = duration;
        }

        Action(int duration, State endState){
            this(duration);
            this.endState = endState;
        }

        void doAction(int millis, Dog dog){
            float progress = (millis/(float)duration); // 0 - 1.0

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
                    float target = dog.orientationAtActionStart.y + PConstants.HALF_PI * (dog.turnRight ? 1 : -1);
                    target = (target < PConstants.QUARTER_PI ? 0 : // Snap the target rotation to 12|3|6|9 o'clock
                            (Math.abs(target - PConstants.HALF_PI) < PConstants.QUARTER_PI ? PConstants.HALF_PI :
                                    (Math.abs(target - PConstants.PI) < PConstants.QUARTER_PI ? PConstants.PI :
                                            (Math.abs(target - (PConstants.PI + PConstants.HALF_PI)) < PConstants.QUARTER_PI ? (PConstants.PI + PConstants.HALF_PI) : 0))));

                    dog.orientation.y = tween(dog.orientationAtActionStart.y, target, progress);
                    break;
                }
                case LIE_DOWN: {
                    if(State.SITTING == dog.currentState){
                        changePose(dog, progress, dog.sittingDimensions, dog.lyingDimensions);
                    } else if(State.STANDING == dog.currentState){
                        changePose(dog, progress, dog.standingDimensions, dog.lyingDimensions);
                    }
                    break;
                }
                case SIT: {
                    if(State.LYING == dog.currentState){
                        changePose(dog, progress, dog.lyingDimensions, dog.sittingDimensions);
                    } else if(State.STANDING == dog.currentState){
                        changePose(dog, progress, dog.standingDimensions, dog.sittingDimensions);
                    }
                    break;
                }
                case STAND: {
                    if(State.LYING == dog.currentState){
                        changePose(dog, progress, dog.lyingDimensions, dog.standingDimensions);
                    } else if(State.SITTING == dog.currentState){
                        changePose(dog, progress, dog.sittingDimensions, dog.standingDimensions);
                    }
                    break;
                }
                case STAY: {
                    break;
                }
                default:
                    break;
            }
        }

        private void move(Dog dog, float speed, float progress){
            float   xCoef = PApplet.cos(-dog.orientation.y),
                    zCoef = PApplet.sin(-dog.orientation.y);

            boolean res = dog.tryMove(
                    dog.locationAtActionStart.x + xCoef * speed * progress,
                    dog.locationAtActionStart.z + zCoef * speed * progress );

            float numSteps = duration / 1000; // 1 step per second??
            float s = speed / 1000f;
            dog.legRotation[0] = s * PApplet.sin(progress * PConstants.TWO_PI * numSteps);
            dog.legRotation[1] = s * PApplet.sin(PConstants.PI + progress * PConstants.TWO_PI * numSteps);
            dog.legRotation[2] = s * PApplet.sin(1 + progress * PConstants.TWO_PI * numSteps);
            dog.legRotation[3] = s * PApplet.sin(1 + PConstants.PI + progress * PConstants.TWO_PI * numSteps);

            if(!res){
                dog.actionInProgress = false;
            }
        }

        private void changePose(Dog dog, float progress, PVector initial, PVector goal){
            dog.dimensions = tween(initial, goal, progress);
            dog.location.y = dog.dimensions.y / 2;
        }

        private PVector tween(PVector initial, PVector goal, float progress){
            return new PVector( tween(initial.x, goal.x, progress),
                                tween(initial.y, goal.y, progress),
                                tween(initial.z, goal.z, progress));
        }

        private float tween(float initial, float goal, float progress){
            return initial + (goal - initial) * progress;
        }
    }

    static {
        State.WALKING.setPossibleActions     (Action.TURN, Action.STAND, Action.RUN);
        State.RUNNING.setPossibleActions     (Action.TURN /*TODO: slide when turning and running*/, Action.STAND, Action.WALK);
        State.SITTING.setPossibleActions     (/*Action.LIE_DOWN,*/ Action.STAND);
        State.STANDING.setPossibleActions    (Action.TURN, /*Action.SIT, Action.LIE_DOWN,*/ Action.WALK);
        State.TURNING.setPossibleActions     (Action.STAND, /*Action.SIT, Action.LIE_DOWN,*/ Action.WALK, Action.RUN);
        State.LYING.setPossibleActions       (/*Action.SIT, */Action.STAND);
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
        if(Action.STAY != currentAction){
            currentState = currentAction.endState;
        }
        millisAtActionStart = p5.millis();
        locationAtActionStart = new PVector(location.x, location.y, location.z);
        orientationAtActionStart = new PVector(orientation.x, orientation.y, orientation.z);
        turnRight = (int)p5.random(2) == 1;

        Action[] possibleActions = currentState.possibleActions;
        if(p5.random(1) > currentState.probabilityMaintainState){
            currentAction = possibleActions[(int)p5.random(possibleActions.length)];
        } else {
            currentAction = Action.STAY;
        }
        actionInProgress = true;
    }
}
