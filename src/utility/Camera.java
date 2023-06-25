package utility;

import javafx.scene.canvas.Canvas;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import org.jfree.fx.FXGraphics2D;
import org.jfree.fx.Resizable;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

/**
 * Created by johan on 15-2-2017.
 */
public class Camera {
	private Point2D centerPoint = new Point2D.Double(0,0);
	private double zoom = 1;
	private double rotation = 0;
	private Point2D lastMousePos;
	private final Canvas canvas;
	private final Resizable resizable;
	private final FXGraphics2D g2d;

	public Camera(Canvas canvas, Resizable resizable, FXGraphics2D g2d) {
		this.canvas = canvas;
		this.resizable = resizable;
		this.g2d = g2d;

		canvas.setOnMousePressed(e -> lastMousePos = new Point2D.Double(e.getX(), e.getY()));
		canvas.setOnMouseDragged(this::mouseDragged);
		canvas.setOnScroll(this::mouseScroll);
	}
	
	public AffineTransform getTransform(int windowWidth, int windowHeight)  {
		AffineTransform tx = new AffineTransform();
		tx.translate((double) windowWidth /2, (double) windowHeight /2);
		tx.scale(zoom, zoom);
		tx.translate(centerPoint.getX(), centerPoint.getY());
		tx.rotate(rotation);
		return tx;
	}

	public void mouseDragged(MouseEvent e) {
		if(e.getButton() == MouseButton.MIDDLE) {
			centerPoint = new Point2D.Double(
					centerPoint.getX() - (lastMousePos.getX() - e.getX()) / zoom,
					centerPoint.getY() - (lastMousePos.getY() - e.getY()) / zoom
			);
			lastMousePos = new Point2D.Double(e.getX(), e.getY());
			resizable.draw(g2d);
		}
	}

	public void mouseScroll(ScrollEvent e) {
		zoom *= (1 + e.getDeltaY()/250.0f);
		resizable.draw(g2d);
	}

}
