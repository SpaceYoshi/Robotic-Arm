package utility.physics;

import org.dyn4j.collision.Fixture;
import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Circle;
import org.dyn4j.geometry.Convex;
import org.dyn4j.geometry.Polygon;
import org.dyn4j.geometry.Vector2;
import org.dyn4j.world.World;
import org.jfree.fx.FXGraphics2D;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;

/**
 * @author Johan Talboom & Max Hager
 * @since 2017-03-08
 * @version 1.3
 */
public class DebugDraw {

    public static void draw(FXGraphics2D g2d, World<Body> world, double scale, Color color) {
        for (Body b : world.getBodies()) {
            AffineTransform originalTransform = g2d.getTransform();

            AffineTransform bodyTransform = new AffineTransform();
            bodyTransform.translate(b.getTransform().getTranslationX() * scale, b.getTransform().getTranslationY() * scale);
            bodyTransform.rotate(b.getTransform().getRotationAngle());
            g2d.transform(bodyTransform);

            for (Fixture f : b.getFixtures()) {
                g2d.draw(AffineTransform.getScaleInstance(scale,scale).createTransformedShape(getShape(f.getShape())));
            }
            g2d.setTransform(originalTransform);
        }
    }

    private static Shape getShape(Convex shape) {
        if (shape instanceof Polygon) return getShape((Polygon) shape);
        if (shape instanceof Circle) return getShape((Circle) shape);
        else System.out.println("Unsupported shape: " + shape);
        return null;
    }

    private static Shape getShape(Polygon shape) {
        GeneralPath path = new GeneralPath();
        Vector2[] vertices = shape.getVertices();
        path.moveTo(vertices[0].x, vertices[0].y);
        for (int i = 1; i < vertices.length; i++) {
            path.lineTo(vertices[i].x, vertices[i].y);
        }
        path.closePath();
        return path;
    }

    private static Shape getShape(Circle shape) {
        return new Ellipse2D.Double(shape.getCenter().x - shape.getRadius(),
                shape.getCenter().y - shape.getRadius(),
                shape.getRadius()*2,
                shape.getRadius()*2);
    }

}