package ch.fhnw.andreasfelder.helpers;

import ch.fhnw.andreasfelder.vector.Vector3;

import java.awt.image.BufferedImage;

public class vertexColor {
    public static final Vector3 BLACK = new Vector3(0.0, 0.0, 0.0);
    public static final Vector3 WHITE = new Vector3(1.0, 1.0, 1.0);
    public static final Vector3 RED = new Vector3(0.8, 0.0, 0.0);
    public static final Vector3 BLUE = new Vector3(0.0, 0.0, 0.8);
    public static final Vector3 GREEN = new Vector3(0.0, 0.8, 0.0);
    public static final Vector3 YELLOW = new Vector3(0.8, 0.8, 0.0);
    public static final Vector3 CYAN = new Vector3(0.0, 0.8, 0.8);
    public static final Vector3 MAGENTA = new Vector3(0.9, 0, 0.9);

    public static Vector3 gammaCorrectFromInput(Vector3 value) {
        return value.pow(2.2f);
    }
    public static Vector3 fromPixel(BufferedImage img, int x, int y) {
        x = Math.max(0, Math.min(x, img.getWidth() - 1));
        y = Math.max(0, Math.min(y, img.getHeight() - 1));
        int rgb = img.getRGB(x, y);
        float r = ((rgb >>> 16) & 255) / 255.0f;
        float g = ((rgb >>> 8) & 255) / 255.0f;
        float b = (rgb & 255) / 255.0f;

        return vertexColor.gammaCorrectFromInput(new Vector3(r, g, b));
    }
}
