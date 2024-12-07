package ch.fhnw.andreasfelder;

import ch.fhnw.andreasfelder.helpers.vertexColor;
import ch.fhnw.andreasfelder.vector.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main extends JPanel {
    private final List<SceneGraphNode> sceneQueue = new ArrayList<>();

    private float angle = 0;

    public final Vector3 cameraPosition = new Vector3(0, 0, -6);
    public final Vector3 lookAt = new Vector3(0, 0, 0);
    public final Vector3 up = new Vector3(0, -1, 0);
    public Vector3 LightPos = new Vector3(0, 0, -6);

    private Vector3 PSpecular = vertexColor.WHITE;
    private Vector3 LightColour = vertexColor.WHITE;

    Matrix4x4 V = Matrix4x4.IDENTITY;
    Matrix4x4 P = Matrix4x4.IDENTITY;
    Matrix4x4 VP = Matrix4x4.IDENTITY;

    protected final float zNear = 0.1f;
    public final float zFar = 100f;
    public final float fov = (float) Math.PI / 2.0f;

    protected float[][] zBuffer;

    private boolean wireframeMode = false;

    public Main() {
        // Setup frame
        JFrame frame = new JFrame("Scene Graph Renderer");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(600, 600);
        frame.add(this);
        frame.setVisible(true);

        V = Matrix4x4.createLookAt(cameraPosition, lookAt, up);
        P = Matrix4x4.createPerspectiveFieldOfView(fov, 1, zNear, zFar);
        VP = V.multiply(P);

        BufferedImage bilinearTexture = getBilinearTexture();
        BufferedImage texture = null;
        try {
            texture = ImageIO.read(this.getClass().getResourceAsStream("/bricks.jpg"));
        } catch (IOException e){
            System.out.println("Could not load texture");
        }

        // Prepare cube vertices and triangles
        List<Vertex> cubeVertices = new ArrayList<>();
        List<Tri> cubeTris = new ArrayList<>();
        MeshGenerator.addCube(cubeVertices, cubeTris, vertexColor.RED, vertexColor.GREEN, vertexColor.BLUE, vertexColor.YELLOW, vertexColor.MAGENTA, vertexColor.CYAN);

        List<Vertex> cube2Vertices = new ArrayList<>();
        List<Tri> cube2Tris = new ArrayList<>();
        MeshGenerator.addCube(cube2Vertices,cube2Tris,vertexColor.BLUE, vertexColor.CYAN, vertexColor.RED, vertexColor.GREEN, vertexColor.MAGENTA, vertexColor.YELLOW);

        //Prepare the sphere1
        List<Vertex> sphereVertices = new ArrayList<>();
        List<Tri> sphereTris = new ArrayList<>();
        MeshGenerator.addSphere(sphereVertices, sphereTris, 15, vertexColor.BLUE);

        //Prepare the sphere2
        List<Vertex> sphereVertices2 = new ArrayList<>();
        List<Tri> sphereTris2 = new ArrayList<>();
        MeshGenerator.addSphere(sphereVertices2, sphereTris2, 15, vertexColor.RED);

        // Create root node
        SceneGraphNode rootNode = new SceneGraphNode(new ArrayList<>(), new ArrayList<>(), Matrix4x4.IDENTITY, null);

        // Create cube node 1
        SceneGraphNode cubeNode1 = new SceneGraphNode(cubeVertices, cubeTris, Matrix4x4.IDENTITY, texture);

        // Create cube node 2
        SceneGraphNode cubeNode2 = new SceneGraphNode(cube2Vertices, cube2Tris, Matrix4x4.IDENTITY, null);

        // Create sphere node as a child of cubeNode1
        SceneGraphNode sphereNode1 = new SceneGraphNode(sphereVertices, sphereTris, Matrix4x4.IDENTITY, null);

        //Create sphere node 2 as a independent node
        SceneGraphNode sphereNode2 = new SceneGraphNode(sphereVertices2, sphereTris2, Matrix4x4.IDENTITY, null);

        // Build hierarchy
        cubeNode1.addChild(sphereNode1);

        //Add to root node
        rootNode.addChild(cubeNode1);
        rootNode.addChild(sphereNode2);

        // Add root node to scene queue
        sceneQueue.add(rootNode);

        Timer timer = new Timer(15, actionEvent -> {
            angle += 0.01f;

            // Update transformations
            cubeNode1.transformation = Matrix4x4.createRotationY(angle);
            cubeNode2.transformation = Matrix4x4.createRotationY(-angle);
            //Due to sphereNode 2 being a child of cubeNode1, it 'inherits' the rotationY Matrix and we just multiply it with a translation.
            sphereNode1.transformation = Matrix4x4.createTranslation(4, 0, 0);
            //sphereNode2 is independent of cubeNode1 so we give it a translation and a rotation
            sphereNode2.transformation = Matrix4x4.createTranslation(0,2.5f,0).multiply(Matrix4x4.createRotationZ(-angle * 2));

            repaint();
        });
        timer.start();
    }

    private static BufferedImage getBilinearTexture() {
        BufferedImage bilinearTexture = new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                int r = (x * 255) / 7;
                int g = (y * 255) / 7;
                int b = 255 - r;
                int a = 255;
                int col = (a << 24) | (r << 16) | (g << 8) | b;
                bilinearTexture.setRGB(x, y, col);
            }
        }
        return bilinearTexture;
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
            renderNode(node, Matrix4x4.IDENTITY, screenImage, w_half, h_half);
        }

        g.drawImage(screenImage, 0, 0, null);
    }

    public void renderNode(SceneGraphNode node, Matrix4x4 parentTransform, BufferedImage screenImage, float w_half, float h_half) {
        Matrix4x4 currentTransform = node.transformation.multiply(parentTransform);
        Matrix4x4 MVP = currentTransform.multiply(VP);

        // Render this node's geometry
        for (Tri t : node.tri) {
            Vertex A = projectVertex(VertexShader(node.vertices.get(t.a()), MVP, currentTransform));
            Vertex B = projectVertex(VertexShader(node.vertices.get(t.b()), MVP, currentTransform));
            Vertex C = projectVertex(VertexShader(node.vertices.get(t.c()), MVP, currentTransform));

            renderTriangle(A, B, C, node.texture, screenImage, w_half, h_half);
        }

        // Recursively render child nodes
        for (SceneGraphNode child : node.children) {
            renderNode(child, currentTransform, screenImage, w_half, h_half);
        }
    }

    private void renderTriangle(Vertex A, Vertex B, Vertex C, BufferedImage texture, BufferedImage screenImage, float w_half, float h_half) {
        Vector2 p1 = transformToScreenPixel(A, w_half, h_half);
        Vector2 p2 = transformToScreenPixel(B, w_half, h_half);
        Vector2 p3 = transformToScreenPixel(C, w_half, h_half);

        Vertex AB = B.subtract(A);
        Vertex AC = C.subtract(A);

        Vector3 a = new Vector3(p1.x(), p1.y(), 0);
        Vector3 b = new Vector3(p2.x(), p2.y(), 0);
        Vector3 c = new Vector3(p3.x(), p3.y(), 0);

        // Backface culling
        if (Vector3.cross(b.subtract(a), c.subtract(a)).z() < 0) {
            return;
        }

        //Wireframe mode
        if (wireframeMode){
            Graphics g = screenImage.getGraphics();
            g.setColor(Color.BLACK);
            g.drawLine((int)p1.x(), (int)p1.y(), (int)p2.x(), (int)p2.y());
            g.drawLine((int)p2.x(), (int)p2.y(), (int)p3.x(), (int)p3.y());
            g.drawLine((int)p3.x(), (int)p3.y(), (int)p1.x(), (int)p1.y());
        } else {
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

                        Vector3 color = fragmentShader(Q, texture, true);
                        screenImage.setRGB(x, y, color.awtColorFromVector().getRGB());
                    }
                }
            }
        }
        }
    }

    protected Vector3 fragmentShader(Vertex Q, BufferedImage texture, boolean bilinearFiltering) {
        // Local illumination model
        Vector3 nHat = Vector3.normalize(Q.normal());

        Vector3 P = Q.worldCoordinates();
        Vector3 PL = LightPos.subtract(P);
        Vector3 PLHat = Vector3.normalize(PL);
        Vector3 color = vertexColor.BLACK;

        if (texture != null) {
            Vector2 uv = Q.st();
            float u = uv.x();
            float v = uv.y();

            if (bilinearFiltering) {
                float x = (float) ((u - Math.floor(u)) * (texture.getWidth() - 2)) + 1;
                float y = (float) ((v - Math.floor(v)) * (texture.getHeight() - 2)) + 1;

                int x_f = (int) x;
                int y_f = (int) y;

                Vector3 colourBL = vertexColor.fromPixel(texture, x_f, y_f);
                Vector3 colourTL = vertexColor.fromPixel(texture, x_f, y_f + 1);
                Vector3 colourBR = vertexColor.fromPixel(texture, x_f + 1, y_f);
                Vector3 colourTR = vertexColor.fromPixel(texture, x_f + 1, y_f + 1);

                color = Vector3.lerp(
                    Vector3.lerp(colourBL, colourTL, y - y_f),
                    Vector3.lerp(colourBR, colourTR, y - y_f),
                    x - x_f);
            } else {
                int x = (int) ((u - Math.floor(u)) * texture.getWidth());
                int y = (int) ((v - Math.floor(v)) * texture.getHeight());
                color = vertexColor.fromPixel(texture, x, y);
            }
        }

        float diffuseAngle = Vector3.dot(nHat, PLHat);
        if (diffuseAngle > 0) {
            // Lambert
            color = color.add(Vector3.multiply(LightColour, Q.color()).multiply(diffuseAngle));

            // Phong
            Vector3 sHat = Vector3.normalize(Vector3.reflect(PLHat, nHat));
            Vector3 EPHat = Vector3.normalize(P.subtract(cameraPosition));
            float specularAngle = Vector3.dot(sHat, EPHat);
            if (specularAngle > 0) {
                color = color.add(LightColour.multiply(PSpecular).multiply((float) Math.pow(specularAngle, 30)));
            }
        }

        return color;
    }

    Vertex VertexShader(Vertex v, Matrix4x4 MVP, Matrix4x4 M) {
        Matrix4x4 MNormal = Matrix4x4.transpose(Matrix4x4.invert(M));

        Vector4 transformedPosition = Vector4.transform(v.position(), MVP);
        Vector4 transformedWorldPosition = Vector4.transform(v.position(), M);
        Vector3 transformedNormal = Vector3.transformNormal(v.normal(), MNormal);

        return new Vertex(
            transformedPosition,
            new Vector3(transformedWorldPosition.x(), transformedWorldPosition.y(), transformedWorldPosition.z()),
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
            (-y * h_half + h_half) // Flip y-axis
        );
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