package ch.fhnw.andreasfelder;

import ch.fhnw.andreasfelder.vector.Matrix4x4;
import ch.fhnw.andreasfelder.vector.Tri;
import ch.fhnw.andreasfelder.vector.Vertex;

import java.util.ArrayList;
import java.util.List;

public class SceneGraphNode {
    public final List<Vertex> vertices = new ArrayList<>();
    public final List<Tri> tri = new ArrayList<>();
    public final List<SceneGraphNode> children = new ArrayList<>();
    public TransformationFunction transformation = t -> Matrix4x4.IDENTITY;

    @FunctionalInterface
    public interface TransformationFunction {
        Matrix4x4 transform(float t);
    }
}
