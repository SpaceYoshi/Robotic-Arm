package utility;

import org.dyn4j.dynamics.Body;
import org.dyn4j.geometry.Vector2;
import org.jfree.fx.FXGraphics2D;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Objects;

/**
 * @author Johan Talboom
 * @since 2017-03-08
 */
public class GameObject {
    private final Body body;
    private BufferedImage image;
    private final Vector2 offset;
    private final double scale;

    public GameObject(String imageFile, Body body, Vector2 offset, double scale) {
        this.body = body;
        this.offset = offset;
        this.scale = scale;
        try {
            image = ImageIO.read(Objects.requireNonNull(getClass().getResource(imageFile)));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void draw(FXGraphics2D g2d) {
        if (image == null) {
            return;
        }

        AffineTransform tx = new AffineTransform();
        tx.translate(body.getTransform().getTranslationX() * 100, body.getTransform().getTranslationY() * 100);
        tx.rotate(body.getTransform().getRotation());
        tx.scale(scale, -scale);
        tx.translate(offset.x, offset.y);

        tx.translate(-image.getWidth() / 2, -image.getHeight() / 2);
        g2d.drawImage(image, tx, null);

    }
}