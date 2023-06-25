import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.dyn4j.dynamics.World;
import org.dyn4j.geometry.Vector2;
import org.jfree.fx.FXGraphics2D;
import org.jfree.fx.ResizableCanvas;
import utility.Camera;
import utility.GameObject;
import utility.MousePicker;
import utility.physics.DebugDraw;

import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

public class RoboticArm extends Application {
    private ResizableCanvas canvas;
    private final World world = new World();
    private Camera camera;
    private MousePicker mousePicker;
    private boolean debugSelected = false;
    private final List<GameObject> gameObjects = new ArrayList<>();
    private static final double CANVAS_START_WIDTH = 1280;
    private static final double CANVAS_START_HEIGHT = 720;
    private static final byte Y_AXIS_SCALE = -1;
    private double scale = 1;

    public static void main(String[] args) {
        launch(RoboticArm.class);
    }

    @Override
    public void start(Stage primaryStage) {
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
    }

    private void draw(FXGraphics2D g2d) {
        AffineTransform originalTransform = g2d.getTransform();

        g2d.setTransform(camera.getTransform((int) canvas.getWidth(), (int) canvas.getHeight()));
        g2d.scale(1, Y_AXIS_SCALE);

        // Draw
        for (GameObject gameObject : gameObjects) gameObject.draw(g2d);
        if (debugSelected) DebugDraw.draw(g2d, world, 100);

        g2d.setTransform(originalTransform);
    }

    private void update(double deltaTime) {
        mousePicker.update(world, camera.getTransform((int) canvas.getWidth(), (int) canvas.getHeight()), 100); // TODO add scale?
        scale = canvas.getHeight() / CANVAS_START_HEIGHT;
    }

}
