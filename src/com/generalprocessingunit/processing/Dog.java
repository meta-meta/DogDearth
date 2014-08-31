package com.generalprocessingunit.processing;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PGraphics;
import processing.core.PShape;
import processing.core.PVector;

import java.util.Arrays;
import java.util.List;

public class Dog
{
    PVector location;
    PVector orientation;
    PVector dimensions;

    Action commandedAction;

    int color;
    float baseSpeed;
    float walkingSpeed;
    float runningSpeed;

    PShape body;

    Head head;

    Leg[] legs = new Leg[4];
    Leg[] frontLegs = new Leg[2];
    Leg[] backLegs = new Leg[2];

    public class Leg
    {
        float rotation;
        float rotationAtActionStart;
        PVector position;
        PShape model;

        Leg(PShape model, PVector position) {
            this.model = model;
            this.position = position;
        }

        public PVector getGlobalLocation() {
            PVector v = Rotation.rotatePVectorY(orientation.y, position);
            v.add(location.x, 0, location.z);
            return v;
        }

        public void draw(PGraphics pG) {
            pG.pushMatrix();
            pG.translate(position.x, position.y, position.z);
            pG.rotateZ(rotation);
            pG.shape(model);
            pG.popMatrix();
        }
    }

    public class Head {
        PVector position;
        PVector rotation;
        PVector rotationAtActionStart;
        PShape model;

        Head(PShape model, PVector position) {
            this.model = model;
            this.position = position;
            rotation = new PVector();
        }

        public void draw(PGraphics pG) {
            pG.pushMatrix();
            pG.translate(position.x, position.y, position.z);
            pG.rotateY(rotation.y);
            pG.rotateZ(rotation.z);
            pG.rotateX(rotation.x);
            pG.shape(model);
            pG.popMatrix();
        }
    }

    /**
     *
     * @param p5 reference to PApplet
     * @param x x-coord of dog location
     * @param z z-coord of dog location
     * @param rx rotation about x-axis
     * @param ry rotation about y-axis
     * @param rz rotation about z-axis
     * @param w width
     * @param h height
     * @param d depth
     * @param color color of dog
     */
    Dog(PApplet p5, float x, float z, float rx, float ry, float rz, float w, float h, float d, int color){
        float dogScale = p5.random(1,2) * 15f;
        this.color = color;

        /*
            Load and position dog body
         */
        body = p5.loadShape("body_smooth.obj");
        body.setFill(color);
        body.scale(dogScale);

        /*
        * Load dog head
        * */
        PShape headModel = p5.loadShape("head_smooth.obj");
        headModel.setFill(color);
        headModel.scale(dogScale);
        head = new Head(headModel, new PVector(1.5f * dogScale, .43f * dogScale, 0));


        /*
            Load and position dog legs
         */
        PShape[] legModels = new PShape[4];
        for (int i = 0; i < 4; i++) {
            legModels[i] = p5.loadShape(String.format("leg_%s_smooth.obj", i));
            legModels[i].setFill(color);
            legModels[i].scale(dogScale);
        }

        legs[0] = new Leg(legModels[0], new PVector( 0.8f * dogScale, -0.6f * dogScale, -0.37f * dogScale));
        legs[1] = new Leg(legModels[1], new PVector( 0.8f * dogScale, -0.6f * dogScale,  0.37f * dogScale));
        legs[2] = new Leg(legModels[2], new PVector(-1.6f * dogScale, -0.6f * dogScale, -0.37f * dogScale));
        legs[3] = new Leg(legModels[3], new PVector(-1.6f * dogScale, -0.6f * dogScale,  0.37f * dogScale));

        frontLegs[0] = legs[0];
        frontLegs[1] = legs[1];
        backLegs[0] = legs[2];
        backLegs[1] = legs[3];

        /*
            Set location, orientation and dimensions
         */
        w = body.getWidth();
        h = body.getHeight() + legs[0].model.getHeight() + headModel.getHeight();
        d = body.getDepth();
        float y = h/2;

        location = new PVector(x, y, z);
        orientation = new PVector(rx, ry, rz);
        dimensions = new PVector(w, h, d);

        baseSpeed = p5.random(0.05f, 1.5f);
        walkingSpeed = 300 * baseSpeed;
        runningSpeed = 1000 * baseSpeed;
    }


    void draw(PGraphics pG){
        pG.pushMatrix();

        // Body
        pG.translate(location.x, location.y, location.z);
        pG.rotateY(orientation.y);
        pG.rotateZ(orientation.z);

        pG.shape(body);

        head.draw(pG);

        for(Leg leg: legs) {
            leg.draw(pG);
        }

        pG.popMatrix();
    }

    boolean tryMove(float x, float z){
        // TODO: HALF_PLANE_WIDTH decides the coordinates of the turf, and the turf is really what should be referenced here
        if((x + dimensions.z / 2) > DogDearth.HALF_PLANE_WIDTH || (x - dimensions.z / 2) < -DogDearth.HALF_PLANE_WIDTH || (z + dimensions.z / 2) > DogDearth.HALF_PLANE_WIDTH || (z - dimensions.z / 2) < -DogDearth.HALF_PLANE_WIDTH){
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
        SITTING     (0.9f),
        STANDING    (0.5f),
        TURNING     (0.05f),
        LYING       (0.96f);

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
        LIE_DOWN    (800, State.LYING);

        int duration;
        State endState;

        Action(int duration){
            this.duration = duration;
        }

        Action(int duration, State endState){
            this(duration);
            this.endState = endState;
        }

        void execute(PApplet p5, int millis, Dog dog){
            float progress = (millis/(float)duration); // 0 - 1.0

            switch (this) {
                case WALK: {
                    move(p5, dog, dog.walkingSpeed, progress);
                    break;
                }
                case RUN: {
                    move(p5, dog, dog.runningSpeed, progress);
                    break;
                }
                case TURN: {
                    float target = dog.orientationAtActionStart.y + PConstants.HALF_PI * (dog.turnRight ? 1 : -1);
                    target = (target < PConstants.QUARTER_PI ? 0 : // Snap the target rotation to 12|3|6|9 o'clock
                            (Math.abs(target - PConstants.HALF_PI) < PConstants.QUARTER_PI ? PConstants.HALF_PI :
                                    (Math.abs(target - PConstants.PI) < PConstants.QUARTER_PI ? PConstants.PI :
                                            (Math.abs(target - (PConstants.PI + PConstants.HALF_PI)) < PConstants.QUARTER_PI ?
                                                    (PConstants.PI + PConstants.HALF_PI) : 0))));

                    dog.orientation.y = tween(dog.orientationAtActionStart.y, target, progress);
                    break;
                }
                case LIE_DOWN: {
                    dog.head.rotation.z = tween(dog.head.rotationAtActionStart.z, 0, progress);

                    if(State.STANDING == dog.currentState) {
                        dog.location.z = tween(dog.locationAtActionStart.z, dog.locationAtActionStart.z - dog.dimensions.z/4, progress);
                    }
                    dog.location.y = tween(dog.locationAtActionStart.y, dog.dimensions.y * .2f, progress);
                    dog.orientation.z = tween(dog.orientationAtActionStart.z, 0, progress);
                    for(Leg leg : dog.legs) {
                        leg.rotation = tween(leg.rotationAtActionStart, PConstants.HALF_PI, progress);
                    }
                    break;
                }
                case SIT: {
                    dog.location.y = tween(dog.locationAtActionStart.y, dog.dimensions.y * .4f, progress);
                    dog.orientation.z = tween(dog.orientationAtActionStart.z, PConstants.QUARTER_PI * 1.2f, progress);

                    dog.head.rotation.z = tween(dog.head.rotationAtActionStart.z, -PConstants.QUARTER_PI, progress);

                    for(Leg leg : dog.backLegs) {
                        leg.rotation = tween(leg.rotationAtActionStart, PConstants.QUARTER_PI, progress);
                    }

                    for(Leg leg : dog.frontLegs) {
                        leg.rotation = tween(leg.rotationAtActionStart, -PConstants.QUARTER_PI, progress);
                    }
                    break;
                }
                case STAND: {
                    dog.head.rotation.z = tween(dog.head.rotationAtActionStart.z, 0, progress);

                    if(State.LYING == dog.currentState) {
                        dog.location.z = tween(dog.locationAtActionStart.z, dog.locationAtActionStart.z + dog.dimensions.z/4, progress);
                    }
                    dog.location.y = tween(dog.locationAtActionStart.y, dog.dimensions.y/2, progress);
                    dog.orientation.z = tween(dog.orientationAtActionStart.z, 0, progress);
                    for (Leg leg : dog.legs) {
                        leg.rotation = tween(leg.rotationAtActionStart, 0, progress);
                    }
                    break;
                }
                default:
                    break;
            }
        }

        private void move(PApplet p5, Dog dog, float speed, float progress){
            int   xCoef = PApplet.round(PApplet.cos(-dog.orientation.y)),
                  zCoef = PApplet.round(PApplet.sin(-dog.orientation.y));

            if (dog.tryMove(dog.locationAtActionStart.x + xCoef * speed * progress,
                            dog.locationAtActionStart.z + zCoef * speed * progress )) {
                rotateLegs(dog, speed, progress);
            } else {
                dog.forceNextAction(p5, Action.STAND);
            }
        }

        private void rotateLegs(Dog dog, float speed, float progress) {
            float numSteps = duration / 1000; // 1 step per second??
            float s = speed / 1000f;

            dog.legs[0].rotation = s * PApplet.sin(progress * PConstants.TWO_PI * numSteps);
            dog.legs[1].rotation = s * PApplet.sin(PConstants.PI + progress * PConstants.TWO_PI * numSteps);
            dog.legs[2].rotation = s * PApplet.sin(1 + progress * PConstants.TWO_PI * numSteps);
            dog.legs[3].rotation = s * PApplet.sin(1 + PConstants.PI + progress * PConstants.TWO_PI * numSteps);
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
        State.SITTING.setPossibleActions     (Action.LIE_DOWN, Action.STAND);
        State.STANDING.setPossibleActions    (Action.TURN, Action.SIT, Action.LIE_DOWN, Action.WALK);
        State.TURNING.setPossibleActions     (Action.STAND, Action.SIT, Action.LIE_DOWN, Action.WALK, Action.RUN);
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
            currentAction.execute(p5, p5.millis() - millisAtActionStart, this);
        }
    }

    void decideNextAction(PApplet p5){
        initAction(p5);

        List<Action> possibleActions = Arrays.asList(currentState.possibleActions);

        if(null != commandedAction && possibleActions.contains(commandedAction)) {
            currentAction = commandedAction;
            commandedAction = null;
            return;
        }

        if(p5.random(1) > currentState.probabilityMaintainState){
            currentAction = possibleActions.get((int)p5.random(possibleActions.size()));
        }
    }

    public void commandNextAction(PApplet p5, Action action) {
        if(currentAction != Action.LIE_DOWN && currentAction != Action.SIT && currentAction != Action.STAND) {
            forceNextAction(p5, Action.STAND);
        }
        commandedAction = action;
    }

    private void initAction(PApplet p5) {
        currentState = currentAction.endState;

        millisAtActionStart = p5.millis();
        locationAtActionStart = new PVector(location.x, location.y, location.z);
        orientationAtActionStart = new PVector(orientation.x, orientation.y, orientation.z);
        head.rotationAtActionStart = new PVector(head.rotation.x, head.rotation.y, head.rotation.z);
        for (Leg leg : legs) {
            leg.rotationAtActionStart = leg.rotation;
        }
        turnRight = (int)p5.random(2) == 1;
        actionInProgress = true;
    }

    void forceNextAction(PApplet p5, Action action){
        initAction(p5);
        currentAction = action;
    }
}
