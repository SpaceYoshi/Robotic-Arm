package utility;

import javafx.scene.Parent;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import org.jfree.fx.FXGraphics2D;

public class ResizableCanvas extends Canvas {
    private final Resizable observer;
    private final FXGraphics2D g2d;

    public ResizableCanvas(Resizable observer, Parent parent, double width, double height) throws IllegalArgumentException {
        super(width, height);
        this.observer = observer;
        g2d = new FXGraphics2D(getGraphicsContext2D());

        if (parent instanceof BorderPane borderPane) {
            borderPane.widthProperty().addListener((observable) -> resize(borderPane.getWidth(), borderPane.getHeight()));
            borderPane.heightProperty().addListener((observable) -> resize(borderPane.getWidth(), borderPane.getHeight()));
        } else if (parent instanceof StackPane stackPane) {
            stackPane.widthProperty().addListener((observable) -> resize(stackPane.getWidth(), stackPane.getHeight()));
            stackPane.heightProperty().addListener((observable) -> resize(stackPane.getWidth(), stackPane.getHeight()));
        } else {
            if (!(parent instanceof FlowPane flowPane)) throw new IllegalArgumentException("Parent type is not supported.");

            flowPane.widthProperty().addListener((observable) -> resize(flowPane.getWidth(), flowPane.getHeight()));
            flowPane.heightProperty().addListener((observable) -> resize(flowPane.getWidth(), flowPane.getHeight()));
        }

        heightProperty().addListener((observable) -> redraw());
    }

    public boolean isResizable() {
        return true;
    }

    public double minHeight(double width) {
        return 0.0;
    }

    public double maxHeight(double width) {
        return 10000.0;
    }

    public double prefWidth(double height) {
        return getWidth();
    }

    public double prefHeight(double width) {
        return getHeight();
    }

    public double minWidth(double height) {
        return 0.0;
    }

    public double maxWidth(double height) {
        return 10000.0;
    }

    public void resize(double width, double height) {
        super.setWidth(width);
        super.setHeight(height);
        if (width > 0.0 && height > 0.0) redraw();
    }

    private void redraw() {
        int width = (int) getWidth();
        int height = (int) getHeight();
        GraphicsContext gc = getGraphicsContext2D();
        gc.clearRect(0.0, 0.0, width, height);
        observer.draw(g2d);
    }

}
