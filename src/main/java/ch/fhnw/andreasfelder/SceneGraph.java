package ch.fhnw.andreasfelder;

import ch.fhnw.andreasfelder.helpers.vertexColor;
import ch.fhnw.andreasfelder.vector.Matrix4x4;
import ch.fhnw.andreasfelder.vector.MeshGenerator;

public class SceneGraph {
    protected final SceneGraphNode graph = new SceneGraphNode();

    protected void createCubeScene(){
        MeshGenerator.addCube(
            graph.vertices,
            graph.tri,
            vertexColor.RED,
            vertexColor.GREEN,
            vertexColor.BLUE,
            vertexColor.YELLOW,
            vertexColor.CYAN,
            vertexColor.MAGENTA);

        graph.transformation = t -> Matrix4x4.createRotationY(t);
    }

    protected void create2CubeScene() {
        createCubeScene();
        SceneGraphNode child = new SceneGraphNode();
        MeshGenerator.addCube(
            child.vertices,
            child.tri,
            vertexColor.RED,
            vertexColor.GREEN,
            vertexColor.BLUE,
            vertexColor.YELLOW,
            vertexColor.CYAN,
            vertexColor.MAGENTA
        );
        child.transformation = t -> Matrix4x4.createRotationY(t);
        graph.children.add(child);
    }


}
