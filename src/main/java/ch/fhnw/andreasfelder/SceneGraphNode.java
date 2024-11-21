package ch.fhnw.andreasfelder;

import ch.fhnw.andreasfelder.vector.Matrix4x4;
import ch.fhnw.andreasfelder.vector.Tri;
import ch.fhnw.andreasfelder.vector.Vertex;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class SceneGraphNode {
    public final List<Vertex> vertices = new ArrayList<>();
    public final List<Tri> tri = new ArrayList<>();
    public Matrix4x4 transformation = Matrix4x4.IDENTITY;
    public BufferedImage texture = null;

    public SceneGraphNode(List<Vertex> vertices, List<Tri> tri, Matrix4x4 transformation, BufferedImage texture) {
        this.vertices.addAll(vertices);
        this.tri.addAll(tri);
        this.transformation = transformation;
        this.texture = texture;
    }
}
