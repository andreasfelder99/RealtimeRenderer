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

    public Matrix4x4 M = Matrix4x4.IDENTITY;
    public Matrix4x4 MVP;
    private float angle = 0;

    public final Vector3 cameraPosition = new Vector3(0, 2, -3);
    public final Vector3 lookAt = new Vector3(0, 0, 0);
    public final Vector3 up = new Vector3(0, -1, 0);
    public Vector3 LightPos = new Vector3(0, 5, -5);

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
            M = Matrix4x4.createRotationY(angle);
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
            Vertex A = projectVertex(VertexShader(vertices.get(t.a()), MVP, M));
            Vertex B = projectVertex(VertexShader(vertices.get(t.b()), MVP, M));
            Vertex C = projectVertex(VertexShader(vertices.get(t.c()), MVP, M));

            Vector2 p1 = transformToScreenPixel(A, w_half, h_half);
            Vector2 p2 = transformToScreenPixel(B, w_half, h_half);
            Vector2 p3 = transformToScreenPixel(C, w_half, h_half);

            final Vertex AB = B.subtract(A);
            final Vertex AC = C.subtract(A);

            final Vector3 a = new Vector3(p1.x(), p1.y(), A.position().z());
            final Vector3 b = new Vector3(p2.x(), p2.y(), B.position().z());
            final Vector3 c = new Vector3(p3.x(), p3.y(), C.position().z());

            if (Vector3.cross(b.subtract(a), c.subtract(a)).z() > 0){
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

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    final Vector2 AP = new Vector2(x + 0.25f, y + 0.25f).subtract(p1);
                    final float u = m11 * AP.x() + m21 * AP.y();
                    final float v = m12 * AP.x() + m22 * AP.y();

                    if (u >= 0 && v >= 0 && (u + v) < 1) {
                        Vertex Q = A.add(AB.multiply(u)).add(AC.multiply(v));


                        // Transform Q back to camera space
                        float z = (zFar * zNear) / (zFar + (zNear - zFar) * Q.position().z());
                        Q = Q.multiply(z);

                        screenImage.setRGB(x, y, FragmentShader(Q).awtColorFromVector().getRGB());
                    }
                }
            }
        }
        g.drawImage(screenImage, 0, 0, null);
    }

    private Vector3 FragmentShader(Vertex v){
        Vector3 nHat = Vector3.normalize(v.normal());

        Vector3 P = v.worldCoordinates();
        Vector3 PL = LightPos.subtract(P);
        Vector3 PLHat = Vector3.normalize(PL);
        Vector3 color = vertexColor.BLACK;

        float diffuseAngle = Vector3.dot(nHat, PLHat);
        if (diffuseAngle > 0) {
            color = color.add(Vector3.multiply(vertexColor.WHITE, v.color()).multiply(diffuseAngle));
        }

        Vector3 sHat = Vector3.normalize(Vector3.reflect(PL, nHat));
        Vector3 EPHat = Vector3.normalize(P.subtract(new Vector3(0,0,-1)));
        float specularAngle = Vector3.dot(sHat, EPHat);
        if (specularAngle > 0) {
            color = color.add(vertexColor.WHITE.multiply(vertexColor.WHITE).multiply((float) Math.pow(specularAngle, 10)));
        }

        return color;
    }

    Vertex VertexShader(Vertex v, Matrix4x4 MVP, Matrix4x4 M) {
        Matrix4x4 MNormal = Matrix4x4.transpose(Matrix4x4.invert(M)).multiply(M.getDeterminant());

        Vector4 transformedPosition = Vector4.transform(v.position(), MVP);
        Vector3 transformedWorldPosition = Vector3.transform(v.worldCoordinates(), M);
        Vector3 transformedNormal = Vector3.transformNormal(v.normal(), MNormal);

        return new Vertex(
            transformedPosition,
            transformedWorldPosition,
            v.color(),
            v.st(),
            transformedNormal
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