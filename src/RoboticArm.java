import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.jfree.fx.FXGraphics2D;
import utility.ResizableCanvas;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Rectangle2D;

public class RoboticArm extends Application {
    private ResizableCanvas canvas;
    private double scale = 1;
    private static final double CANVAS_START_WIDTH = 1280;
    private static final double CANVAS_START_HEIGHT = 720;

    public static void main(String[] args) {
        launch(RoboticArm.class);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane mainPane = new BorderPane();
        canvas = new ResizableCanvas(this::draw, mainPane, CANVAS_START_WIDTH, CANVAS_START_HEIGHT);
        mainPane.setCenter(canvas);

        // Set AnimationTimer
        new AnimationTimer() {
            long last = -1;
            @Override
            public void handle(long now) {
                if (last == -1) last = now;
                update((now - last) / 1000000000.0);
                last = now;
                draw(new FXGraphics2D(canvas.getGraphicsContext2D()));
            }
        }.start();

        // Set stage
        primaryStage.setScene(new Scene(mainPane));
        primaryStage.setTitle("Robotic Arm");
        primaryStage.getIcons().add(new Image("/textures/icon.png"));
        primaryStage.show();
    }

    private void draw(FXGraphics2D g2d) {
        AffineTransform at = new AffineTransform();
        at.scale(scale, scale);
        g2d.setTransform(at);

        // Create inverse transform and set canvas rectangle
        AffineTransform inverse;
        Shape canvasRectangle;
        try {
            inverse = at.createInverse();
            canvasRectangle = inverse.createTransformedShape(new Rectangle2D.Double(0, 0, canvas.getWidth(), canvas.getHeight()));
        } catch (NoninvertibleTransformException e) {
            throw new RuntimeException(e);
        }

        // Draw background gradient (also functions as clear)
        g2d.setPaint(new LinearGradientPaint(0, (float) canvas.getHeight(), 0, (float) g2d.getTransform().getScaleY() * -2000, new float[]{0, 0.6f, 0.75f, 0.95f, 1},
                new Color[]{new Color(143, 255, 251), new Color(255, 88, 0), new Color(78, 0, 183), new Color(0, 49, 255), Color.BLACK}));
        g2d.fill(canvasRectangle);
    }

    private void update(double deltaTime) {
        scale = canvas.getHeight() / CANVAS_START_HEIGHT;
    }

}
