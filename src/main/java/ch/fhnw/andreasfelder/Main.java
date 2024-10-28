package ch.fhnw.andreasfelder;

import ch.fhnw.andreasfelder.helpers.vertexColor;
import ch.fhnw.andreasfelder.vector.Matrix4x4;
import ch.fhnw.andreasfelder.vector.MeshGenerator;
import ch.fhnw.andreasfelder.vector.Tri;
import ch.fhnw.andreasfelder.vector.Vector2;
import ch.fhnw.andreasfelder.vector.Vector3;
import ch.fhnw.andreasfelder.vector.Vector4;
import ch.fhnw.andreasfelder.vector.Vertex;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

public class Main extends JPanel {
    public final List<Vertex> vertices = new ArrayList<>();
    public final List<Tri> tri = new ArrayList<>();

    public Matrix4x4 MVP;
    private float angle = 0;

    public final Vector3 cameraPosition = new Vector3(0, 2, -3);
    public final Vector3 lookAt = new Vector3(0, 0, 0);
    public final Vector3 up = new Vector3(0, -1, 0);

    public final float zNear = 0.1f;
    public final float zFar = 100f;
    public final float fov = (float) Math.toRadians(90);

    public Main() {
        JFrame frame = new JFrame("comgr Part B");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);
        frame.add(this);
        frame.setVisible(true);

        MeshGenerator.addCube(
            vertices,
            tri,
            vertexColor.RED,
            vertexColor.GREEN,
            vertexColor.BLUE,
            vertexColor.YELLOW,
            vertexColor.CYAN,
            vertexColor.MAGENTA);

        Matrix4x4 V = Matrix4x4.createLookAt(cameraPosition, lookAt, up);
        Matrix4x4 P = Matrix4x4.createPerspectiveFieldOfView(fov, 1, zNear, zFar);
        Matrix4x4 VP = V.multiply(P);

        Timer timer = new Timer(10, actionEvent -> {
            angle -= 0.01;
            Matrix4x4 M = Matrix4x4.createRotationY(angle);
            MVP = M.multiply(VP);
            repaint();
        });
        timer.start();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);
        //Skip if MVP is null -> not yet calculated / initialized
        if (MVP == null) {
            return;
        }
        for (Tri t : tri) {
            Vertex v1 = projectVertex(VertexShader(vertices.get(t.a()), MVP));
            Vertex v2 = projectVertex(VertexShader(vertices.get(t.b()), MVP));
            Vertex v3 = projectVertex(VertexShader(vertices.get(t.c()), MVP));

            Vector2 p1 = transformToScreenPixel(v1, getWidth() / 2f, getHeight() / 2f);
            Vector2 p2 = transformToScreenPixel(v2, getWidth() / 2f, getHeight() / 2f);
            Vector2 p3 = transformToScreenPixel(v3, getWidth() / 2f, getHeight() / 2f);

            Vector3 AB = new Vector3(p2.x() - p1.x(), p2.y() - p1.y(), 0);
            Vector3 AC = new Vector3(p3.x() - p1.x(), p3.y() - p1.y(), 0);

            if (Vector3.cross(AB, AC).z() > 0) {
                continue;
            }

            g.setColor(v1.color().awtColorFromVector());
            g.fillPolygon(
                new int[] {(int) p1.x(), (int) p2.x(), (int) p3.x()},
                new int[] {(int) p1.y(), (int) p2.y(), (int) p3.y()},
                3);
        }
    }

    Vertex VertexShader(Vertex v, Matrix4x4 MVP) {
        Vector4 transformedPosition = Vector4.transform(v.position(), MVP);
        return new Vertex(
            transformedPosition,
            v.worldCoordinates(),
            v.color(),
            v.st(),
            v.normal()
        );
    }

    private Vector2 transformToScreenPixel(Vertex v, float w_half, float h_half) {
        float x = v.position().x();
        float y = v.position().y();

        return new Vector2(
            (x * w_half + w_half),
            (y * w_half + h_half));
    }

    private Vertex projectVertex(Vertex v) {
        return v.multiply(1 / v.position().w());
    }

    public static void main(String[] args) {
        new Main();
    }
}