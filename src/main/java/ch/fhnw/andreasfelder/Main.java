package ch.fhnw.andreasfelder;

import ch.fhnw.andreasfelder.helpers.vertexColor;
import ch.fhnw.andreasfelder.vector.Matrix3x2;
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
import java.awt.image.BufferedImage;
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

        if (MVP == null) {
            return;
        }

        int width = getWidth();
        int height = getHeight();
        BufferedImage screenImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        float w_half = width / 2f;
        float h_half = height / 2f;

        for (Tri t : tri) {
            Vertex v1 = projectVertex(VertexShader(vertices.get(t.a()), MVP));
            Vertex v2 = projectVertex(VertexShader(vertices.get(t.b()), MVP));
            Vertex v3 = projectVertex(VertexShader(vertices.get(t.c()), MVP));

            Vector2 p1 = transformToScreenPixel(v1, w_half, h_half);
            Vector2 p2 = transformToScreenPixel(v2, w_half, h_half);
            Vector2 p3 = transformToScreenPixel(v3, w_half, h_half);

            Vector3 AB = new Vector3(p2.x() - p1.x(), p2.y() - p1.y(), 0);
            Vector3 AC = new Vector3(p3.x() - p1.x(), p3.y() - p1.y(), 0);

            if (Vector3.cross(AB, AC).z() > 0) {
                continue;
            }

            final Vector2 ABScreenPixel = p2.subtract(p1);
            final Vector2 ACScreenPixel = p3.subtract(p1);
            final Matrix3x2 ABACInv = Matrix3x2.invert(new Matrix3x2(ABScreenPixel.x(), ABScreenPixel.y(), ACScreenPixel.x(), ACScreenPixel.y(), 0, 0));

            if (ABACInv == null) {
                continue;
            }

            float m11 = ABACInv.m11();
            float m21 = ABACInv.m21();
            float m12 = ABACInv.m12();
            float m22 = ABACInv.m22();
            int v1Color = v1.color().awtColorFromVector().getRGB();

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    final Vector2 AP = new Vector2(x + 0.25f, y + 0.25f).subtract(p1);
                    final float u = m11 * AP.x() + m21 * AP.y();
                    final float v = m12 * AP.x() + m22 * AP.y();

                    if (u >= 0 && v >= 0 && (u + v) < 1) {
                        screenImage.setRGB(x, y, v1Color);
                    }
                }
            }
        }
        g.drawImage(screenImage, 0, 0, null);
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