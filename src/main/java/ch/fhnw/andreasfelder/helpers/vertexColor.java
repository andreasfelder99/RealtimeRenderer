package ch.fhnw.andreasfelder.helpers;

import ch.fhnw.andreasfelder.vector.Vector3;

public class vertexColor {
    public static final Vector3 BLACK = new Vector3(0.0, 0.0, 0.0);
    public static final Vector3 WHITE = new Vector3(1.0, 1.0, 1.0);
    public static final Vector3 RED = new Vector3(0.8, 0.0, 0.0);
    public static final Vector3 BLUE = new Vector3(0.0, 0.0, 0.8);
    public static final Vector3 GREEN = new Vector3(0.0, 0.8, 0.0);
    public static final Vector3 GRAY = new Vector3(0.35, 0.35, 0.35);
    public static final Vector3 YELLOW = new Vector3(0.8, 0.8, 0.0);
    public static final Vector3 CYAN = new Vector3(0.0, 0.8, 0.8);
    public static final Vector3 MAGENTA = new Vector3(0.9, 0, 0.9);

    public static Vector3 gammaCorrectOutput(Vector3 value) {
        return value.pow(1 / 2.2F);
    }
}
