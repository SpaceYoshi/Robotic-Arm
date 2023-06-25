package roboticarm;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.dyn4j.dynamics.Body;
import org.dyn4j.dynamics.World;
import org.dyn4j.dynamics.joint.PrismaticJoint;
import org.dyn4j.dynamics.joint.RevoluteJoint;
import org.dyn4j.geometry.Geometry;
import org.dyn4j.geometry.MassType;
import org.dyn4j.geometry.Vector2;
import org.jfree.fx.FXGraphics2D;
import org.jfree.fx.ResizableCanvas;
import roboticarm.utility.Camera;
import roboticarm.utility.GameObject;
import roboticarm.utility.MousePicker;
import roboticarm.utility.physics.DebugDraw;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

public class RoboticArm extends Application {
    private ResizableCanvas canvas;
    private Stage primaryStage;
    private final World world = new World();
    private Camera camera;
    private MousePicker mousePicker;
    private boolean debugSelected = false;
    private final List<GameObject> gameObjects = new ArrayList<>();
    private PrismaticJoint baseJoint;
    private RevoluteJoint baseLargeJoint;
    private RevoluteJoint largeMediumJoint;
    private RevoluteJoint mediumSmallJoint;
    private RevoluteJoint smallHeadJoint;
    private static final double BASE_MOVEMENT_SPEED = 5;
    private static final double ROTATION_SPEED = 2;
    private static final double CANVAS_START_WIDTH = 1920;
    private static final double CANVAS_START_HEIGHT = 1000;
    public static final double Y_AXIS_SCALE = -1;
//    private double scale = 1; // TODO add scaling?

    public static void main(String[] args) {
        launch(RoboticArm.class);
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        BorderPane mainPane = new BorderPane();
        canvas = new ResizableCanvas(this::draw, mainPane);

        // Initialize variables
        FXGraphics2D g2d = new FXGraphics2D(canvas.getGraphicsContext2D());
        camera = new Camera(canvas, this::draw, g2d);
        mousePicker = new MousePicker(canvas);

        // Create debug button
        CheckBox showDebug = new CheckBox("Debug Mode");
        showDebug.setOnAction(e -> debugSelected = showDebug.isSelected());

        // Set mainPane
        mainPane.setCenter(canvas);
        mainPane.setTop(showDebug);

        // Set events
        canvas.setFocusTraversable(true);
        mainPane.setOnKeyPressed(this::onKeyPressed);
        mainPane.setOnKeyReleased(this::onKeyReleased);

        // Set AnimationTimer
        new AnimationTimer() {
            long last = -1;
            @Override
            public void handle(long now) {
                if (last == -1) last = now;
                update((now - last) / 1000000000.0);
                last = now;
                draw(g2d);
            }
        }.start();

        // Set stage
        primaryStage.setScene(new Scene(mainPane, CANVAS_START_WIDTH, CANVAS_START_HEIGHT));
        primaryStage.setTitle("Robotic Arm");
        primaryStage.getIcons().add(new Image("/textures/icon.png"));
        primaryStage.show();
    }

    @Override
    public void init() {
        // Set world attributes
        world.setGravity(new Vector2(0, 9.81 * Y_AXIS_SCALE));

        // ### BODIES ###

        // Create lower base
        Body lowerBase = new Body();
        lowerBase.addFixture(Geometry.createRectangle(10, 0.6));
        lowerBase.getTransform().setTranslationY(-5.1);
        lowerBase.setMass(MassType.INFINITE);
        world.addBody(lowerBase);
        gameObjects.add(new GameObject("base-lower-extended.png", lowerBase, new Vector2(0, 0), 1));

        // Create upper base
        Body upperBase = new Body();
        upperBase.addFixture(Geometry.createRectangle(1.35, 0.55));
        upperBase.getTransform().setTranslationY(-4.5);
        upperBase.setMass(MassType.NORMAL);
        world.addBody(upperBase);
        gameObjects.add(new GameObject("base-upper.png", upperBase, new Vector2(0, 26), 1));

        // Create large segment
        Body largeSegment = new Body();
        largeSegment.addFixture(Geometry.createCircle(0.72));
        largeSegment.getTransform().setTranslationY(-3.42);
        largeSegment.setMass(MassType.NORMAL);
        world.addBody(largeSegment);
        gameObjects.add(new GameObject(("segment-large.png"), largeSegment, new Vector2(0, 105), 1));

        // Create medium segment
        Body mediumSegment = new Body();
        mediumSegment.addFixture(Geometry.createCircle(0.65));
        mediumSegment.getTransform().setTranslationY(-0.15);
        mediumSegment.setMass(MassType.NORMAL);
        world.addBody(mediumSegment);
        gameObjects.add(new GameObject(("segment-small.png"), mediumSegment, new Vector2(0, 76), 1.3));

        // Create small segment
        Body smallSegment = new Body();
        smallSegment.addFixture(Geometry.createCircle(0.5));
        smallSegment.getTransform().setTranslationY(2.78);
        smallSegment.setMass(MassType.NORMAL);
        world.addBody(smallSegment);
        gameObjects.add(new GameObject(("segment-small.png"), smallSegment, new Vector2(0, 76), 1));

        // Create head segment
        Body headSegment = new Body();
        headSegment.addFixture(Geometry.createCircle(0.4));
        headSegment.getTransform().setTranslationY(5.08);
        headSegment.setMass(MassType.NORMAL);
        world.addBody(headSegment);
        gameObjects.add(new GameObject(("segment-head.png"), headSegment, new Vector2(0, 53), 1));

        // ### JOINTS ###

        final double maxForceTorque = 100;

        // Join lower and upper base
        baseJoint = new PrismaticJoint(lowerBase, upperBase, upperBase.getWorldCenter(), new Vector2(1, 0));
        final double movementLimit = 4.1;
        baseJoint.setLimitEnabled(true);
        baseJoint.setUpperLimit(movementLimit);
        baseJoint.setLowerLimit(-movementLimit);
        baseJoint.setMotorEnabled(true);
        baseJoint.setMotorSpeed(0);
        baseJoint.setMaximumMotorForce(maxForceTorque);
        world.addJoint(baseJoint);

        // Join upper base and large segment
        baseLargeJoint = new RevoluteJoint(upperBase, largeSegment, largeSegment.getWorldCenter());
        baseLargeJoint.setLimitEnabled(true);
        final double rotationLimitLargeSegment = 0.75;
        baseLargeJoint.setUpperLimit(rotationLimitLargeSegment);
        baseLargeJoint.setLowerLimit(-rotationLimitLargeSegment);
        baseLargeJoint.setMotorEnabled(true);
        baseLargeJoint.setMotorSpeed(0);
        baseLargeJoint.setMaximumMotorTorque(maxForceTorque);
        world.addJoint(baseLargeJoint);

        // Join large and medium segments
        largeMediumJoint = new RevoluteJoint(largeSegment, mediumSegment, mediumSegment.getWorldCenter());
        largeMediumJoint.setLimitEnabled(true);
        final double rotationLimitMediumSegment = 1.1;
        largeMediumJoint.setUpperLimit(rotationLimitMediumSegment);
        largeMediumJoint.setLowerLimit(-rotationLimitMediumSegment);
        largeMediumJoint.setMotorEnabled(true);
        largeMediumJoint.setMotorSpeed(0);
        largeMediumJoint.setMaximumMotorTorque(maxForceTorque);
        world.addJoint(largeMediumJoint);

        // Join medium and small segments
        mediumSmallJoint = new RevoluteJoint(mediumSegment, smallSegment, smallSegment.getWorldCenter());
        mediumSmallJoint.setLimitEnabled(true);
        final double rotationLimitSmallSegment = 0.95;
        mediumSmallJoint.setUpperLimit(rotationLimitSmallSegment);
        mediumSmallJoint.setLowerLimit(-rotationLimitSmallSegment);
        mediumSmallJoint.setMotorEnabled(true);
        mediumSmallJoint.setMotorSpeed(0);
        mediumSmallJoint.setMaximumMotorTorque(maxForceTorque);
        world.addJoint(mediumSmallJoint);

        // Join small and head segments
        smallHeadJoint = new RevoluteJoint(smallSegment, headSegment, headSegment.getWorldCenter());
        smallHeadJoint.setLimitEnabled(true);
        final double rotationLimitHeadSegment = 1.1;
        smallHeadJoint.setUpperLimit(rotationLimitHeadSegment);
        smallHeadJoint.setLowerLimit(-rotationLimitHeadSegment);
        smallHeadJoint.setMotorEnabled(true);
        smallHeadJoint.setMotorSpeed(0);
        smallHeadJoint.setMaximumMotorTorque(maxForceTorque);
        world.addJoint(smallHeadJoint);
    }

    private void draw(FXGraphics2D g2d) {
        g2d.setBackground(Color.WHITE);
        g2d.clearRect(0, 0, (int) canvas.getWidth(), (int) canvas.getHeight());

        AffineTransform originalTransform = g2d.getTransform();

        g2d.setTransform(camera.getTransform((int) canvas.getWidth(), (int) canvas.getHeight()));
        g2d.scale(1, Y_AXIS_SCALE);

        // Draw
        for (GameObject gameObject : gameObjects) gameObject.draw(g2d);
        if (true) { // TODO
            g2d.setColor(Color.BLUE);
            DebugDraw.draw(g2d, world, 100);
        }

        g2d.setTransform(originalTransform);
    }

    private void update(double deltaTime) {
        mousePicker.update(world, camera.getTransform((int) canvas.getWidth(), (int) canvas.getHeight()), 100); // TODO current scale does not change anything?
        world.update(deltaTime); // TODO WARNING
//        scale = canvas.getHeight() / CANVAS_START_HEIGHT; // TODO add scaling?

    }

    private void onKeyPressed(KeyEvent keyEvent) {
        switch (keyEvent.getCode()) {
            // Toggle fullscreen mode
            case F11:
                primaryStage.setFullScreen(!primaryStage.isFullScreen());
                break;
            // Move base
            case A:
            case LEFT:
                System.out.println("Move base left");
                baseJoint.setMotorSpeed(BASE_MOVEMENT_SPEED);
                break;
            case D:
            case RIGHT:
                System.out.println("Move base right");
                baseJoint.setMotorSpeed(-BASE_MOVEMENT_SPEED);
                break;
            // Move large segment
            case N:
                System.out.println("Rotate large left");
                baseLargeJoint.setMotorSpeed(-ROTATION_SPEED);
                break;
            case M:
                System.out.println("Rotate large right");
                baseLargeJoint.setMotorSpeed(ROTATION_SPEED);
                break;
            // Move medium segment
            case H:
                System.out.println("Rotate medium left");
                largeMediumJoint.setMotorSpeed(-ROTATION_SPEED);
                break;
            case J:
                System.out.println("Rotate medium right");
                largeMediumJoint.setMotorSpeed(ROTATION_SPEED);
                break;
            // Move small segment
            case Y:
                System.out.println("Rotate small left");
                mediumSmallJoint.setMotorSpeed(-ROTATION_SPEED);
                break;
            case U:
                System.out.println("Rotate small right");
                mediumSmallJoint.setMotorSpeed(ROTATION_SPEED);
                break;
            // Move head segment
            case DIGIT6:
                System.out.println("Rotate head left");
                smallHeadJoint.setMotorSpeed(-ROTATION_SPEED);
                break;
            case DIGIT7:
                System.out.println("Rotate head right");
                smallHeadJoint.setMotorSpeed(ROTATION_SPEED);
                break;
        }
    }

    private void onKeyReleased(KeyEvent keyEvent) {
        switch (keyEvent.getCode()) {
            // Stop base movement
            case A:
            case LEFT:
            case D:
            case RIGHT:
                baseJoint.setMotorSpeed(0);
                break;
            // Stop large segment movement
            case N:
            case M:
                baseLargeJoint.setMotorSpeed(0);
                break;
            // Stop medium segment movement
            case H:
            case J:
                largeMediumJoint.setMotorSpeed(0);
                break;
            // Stop small segment movement
            case Y:
            case U:
                mediumSmallJoint.setMotorSpeed(0);
                break;
            // Stop head segment movement
            case DIGIT6:
            case DIGIT7:
                smallHeadJoint.setMotorSpeed(0);
                break;
        }
    }

}
