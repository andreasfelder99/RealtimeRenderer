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

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main extends JPanel {
    public final List<Vertex> vertices = new ArrayList<>();
    public final List<Tri> tri = new ArrayList<>();
    private final List<SceneGraphNode> sceneQueue = new ArrayList<>();

    private float angle = 0;

    public final Vector3 cameraPosition = new Vector3(0, 1, -3);  // Positioned slightly above and behind the scene
    public final Vector3 lookAt = new Vector3(0, 0, 0);           // Looking towards the origin
    public final Vector3 up = new Vector3(0, 1, 0);               // Y-axis as up direction
    public Vector3 LightPos = new Vector3(0, 1, -5);

    Matrix4x4 V = Matrix4x4.IDENTITY;
    Matrix4x4 P = Matrix4x4.IDENTITY;
    Matrix4x4 VP = Matrix4x4.IDENTITY;

    protected final float zNear = 0.1f;
    public final float zFar = 100f;
    public final float fov = (float) Math.toRadians(90);

    protected float[][] zBuffer;

    public Main() {
        // Setup frame
        JFrame frame = new JFrame("Multi-Object Renderer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);
        frame.add(this);
        frame.setVisible(true);

        V = Matrix4x4.createLookAt(cameraPosition, lookAt, up);
        P = Matrix4x4.createPerspectiveFieldOfView(fov, 1, zNear, zFar);
        VP = V.multiply(P);

        // Cube 1
        List<Vertex> cubeVertices = new ArrayList<>();
        List<Tri> cubeTris = new ArrayList<>();
        MeshGenerator.addCube(cubeVertices, cubeTris, vertexColor.RED, vertexColor.GREEN, vertexColor.BLUE, vertexColor.YELLOW, vertexColor.MAGENTA, vertexColor.CYAN);
        try {
            BufferedImage texture = ImageIO.read(this.getClass().getResourceAsStream("/bricks.jpg"));
            sceneQueue.add(new SceneGraphNode(cubeVertices, cubeTris, Matrix4x4.createRotationY(0.5f), texture));
        } catch (IOException e) {
            sceneQueue.add(new SceneGraphNode(cubeVertices, cubeTris, Matrix4x4.createRotationY(0.5f), null));
        }


//        // Cube 2
//        List<Vertex> cube2Vertices = new ArrayList<>();
//        List<Tri> cube2Tris = new ArrayList<>();
//        MeshGenerator.addCube(cube2Vertices, cube2Tris, vertexColor.RED, vertexColor.GREEN, vertexColor.BLUE, vertexColor.YELLOW, vertexColor.MAGENTA, vertexColor.CYAN);
//        sceneQueue.add(new SceneGraphNode(cube2Vertices, cube2Tris, Matrix4x4.createRotationX(0.8f), null));

        //Checkerboard texture to show off bilinear filtering
        BufferedImage checkerboard = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 128; y++) {
            for (int x = 0; x < 128; x++) {
                if ((x / 16 + y / 16) % 2 == 0) {
                    checkerboard.setRGB(x, y, 0xFFFFFFFF); // White
                } else {
                    checkerboard.setRGB(x, y, 0xFF000000); // Black
                }
            }
        }
        List<Vertex> cube2Vertices = new ArrayList<>();
        List<Tri> cube2Tris = new ArrayList<>();
        MeshGenerator.addCube(cube2Vertices, cube2Tris, vertexColor.RED, vertexColor.GREEN, vertexColor.BLUE, vertexColor.YELLOW, vertexColor.MAGENTA, vertexColor.CYAN);
        sceneQueue.add(new SceneGraphNode(cube2Vertices, cube2Tris, Matrix4x4.createRotationX(0.8f), checkerboard));

        Timer timer = new Timer(15, actionEvent -> {
            angle -= 0.01;
            repaint();
        });
        timer.start();
    }

    @Override
    public void paint(Graphics g) {
        super.paint(g);

        if (sceneQueue.isEmpty()) return;

        zBuffer = newZBuffer(getWidth(), getHeight());
        BufferedImage screenImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);

        float w_half = getWidth() / 2f;
        float h_half = getHeight() / 2f;

        for (SceneGraphNode node : sceneQueue) {
            Matrix4x4 rotationTransform = Matrix4x4.createRotationY(angle).multiply(node.transformation);
            Matrix4x4 MVP = rotationTransform.multiply(VP);

            for (Tri t : node.tri) {
                Vertex A = projectVertex(VertexShader(node.vertices.get(t.a()), MVP, rotationTransform));
                Vertex B = projectVertex(VertexShader(node.vertices.get(t.b()), MVP, rotationTransform));
                Vertex C = projectVertex(VertexShader(node.vertices.get(t.c()), MVP, rotationTransform));

                renderTriangle(A, B, C, node.texture, screenImage, w_half, h_half);
            }
        }

        g.drawImage(screenImage, 0, 0, null);
    }

    private void renderTriangle(Vertex A, Vertex B, Vertex C, BufferedImage texture, BufferedImage screenImage, float w_half, float h_half) {
        Vector2 p1 = transformToScreenPixel(A, w_half, h_half);
        Vector2 p2 = transformToScreenPixel(B, w_half, h_half);
        Vector2 p3 = transformToScreenPixel(C, w_half, h_half);

        Vertex AB = B.subtract(A);
        Vertex AC = C.subtract(A);

        Vector3 a = new Vector3(p1.x(), p1.y(), A.position().z());
        Vector3 b = new Vector3(p2.x(), p2.y(), B.position().z());
        Vector3 c = new Vector3(p3.x(), p3.y(), C.position().z());

        // Backface culling
        if (Vector3.cross(b.subtract(a), c.subtract(a)).z() > 0) {
            return;
        }

        Vector2 ABScreenPixel = p2.subtract(p1);
        Vector2 ACScreenPixel = p3.subtract(p1);
        Matrix3x2 ABACInv = Matrix3x2.invert(new Matrix3x2(
            ABScreenPixel.x(), ABScreenPixel.y(),
            ACScreenPixel.x(), ACScreenPixel.y(),
            0, 0));

        if (ABACInv == null) {
            return;
        }

        float m11 = ABACInv.m11();
        float m21 = ABACInv.m21();
        float m12 = ABACInv.m12();
        float m22 = ABACInv.m22();

        int minX = Math.max((int) Math.min(p1.x(), Math.min(p2.x(), p3.x())), 0);
        int maxX = Math.min((int) Math.max(p1.x(), Math.max(p2.x(), p3.x())), screenImage.getWidth() - 1);
        int minY = Math.max((int) Math.min(p1.y(), Math.min(p2.y(), p3.y())), 0);
        int maxY = Math.min((int) Math.max(p1.y(), Math.max(p2.y(), p3.y())), screenImage.getHeight() - 1);

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                Vector2 AP = new Vector2(x + 0.5f, y + 0.5f).subtract(p1);
                float u = m11 * AP.x() + m21 * AP.y();
                float v = m12 * AP.x() + m22 * AP.y();

                if (u >= 0 && v >= 0 && (u + v) < 1) {
                    Vertex Q = A.add(AB.multiply(u)).add(AC.multiply(v));

                    float z = (zFar * zNear) / (zFar + (zNear - zFar) * Q.position().z());
                    Q = Q.multiply(z);
                    if (z < zBuffer[y][x]) {
                        zBuffer[y][x] = z;

                        Vector3 color = FragmentShader(Q, texture, true);
                        screenImage.setRGB(x, y, color.awtColorFromVector().getRGB());
                    }
                }
            }
        }
    }

    private Vector3 FragmentShader(Vertex vertex, BufferedImage texture, boolean bilinearFiltering) {
        Vector3 nHat = Vector3.normalize(vertex.normal());
        Vector3 P = vertex.worldCoordinates();
        Vector3 PL = LightPos.subtract(P);
        Vector3 PLHat = Vector3.normalize(PL);
        Vector3 color = vertexColor.BLACK;

        if (texture != null) {
            Vector2 uv = vertex.st();
            float u = Math.max(0, Math.min(uv.x(), 1));
            float v = Math.max(0, Math.min(uv.y(), 1));

            if (bilinearFiltering) {
                float x = (float) ((u - Math.floor(u)) * (texture.getWidth() - 2)) + 1;
                float y = (float) ((v - Math.floor(v)) * (texture.getHeight() - 2)) + 1;

                int x_f = (int) x;
                int y_f = (int) y;

                Vector3 colorBL = vertexColor.fromPixel(texture, x_f, y_f);
                Vector3 colorTL = vertexColor.fromPixel(texture, x_f, y_f + 1);
                Vector3 colorBR = vertexColor.fromPixel(texture, x_f + 1, y_f);
                Vector3 colorTR = vertexColor.fromPixel(texture, x_f + 1, y_f + 1);

                color = Vector3.lerp(
                    Vector3.lerp(colorBL, colorTL, y - y_f),
                    Vector3.lerp(colorBR, colorTR, y - y_f),
                    x - x_f);
            } else {
                int x = (int) (u * texture.getWidth());
                int y = (int) (v * texture.getHeight());
                color = vertexColor.fromPixel(texture, x, y);
            }
        }

        float diffuseAngle = Vector3.dot(nHat, PLHat);
        if (diffuseAngle > 0) {
            color = color.add(Vector3.multiply(vertexColor.WHITE, vertex.color()).multiply(diffuseAngle));

            Vector3 sHat = Vector3.normalize(Vector3.reflect(PL, nHat));
            Vector3 EPHat = Vector3.normalize(P.subtract(new Vector3(0,0,-4)));
            float specularAngle = Vector3.dot(sHat, EPHat);
            if (specularAngle > 0) {
                color = color.add(vertexColor.WHITE.multiply(vertexColor.WHITE).multiply((float) Math.pow(specularAngle, 10)));
            }
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

    protected float[][] newZBuffer(final int width, final int height) {
        final float[][] zBuffer = new float[height][width];
        for (int y = 0; y < height; y++){
            for (int x = 0; x < width; x++){
                zBuffer[y][x] = zFar;
            }
        }
        return zBuffer;
    }

    public static void main(String[] args) {
        new Main();
    }
}