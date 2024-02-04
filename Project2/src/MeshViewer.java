
import java.awt.BorderLayout;
import java.awt.GraphicsConfiguration;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import org.jogamp.java3d.Appearance;
import org.jogamp.java3d.Background;
import org.jogamp.java3d.BoundingSphere;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.DirectionalLight;
import org.jogamp.java3d.GeometryArray;
import org.jogamp.java3d.LineArray;
import org.jogamp.java3d.Material;
import org.jogamp.java3d.PolygonAttributes;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.TriangleArray;
import org.jogamp.java3d.utils.behaviors.vp.OrbitBehavior;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Vector3f;


public class MeshViewer extends JFrame {
	private static final long serialVersionUID = 1L;

	TriangleMesh tmesh;
	OBJReader reader;
	DataPathMap dataPathMap;

	Canvas3D canvas;
	BranchGroup scene;
	BranchGroup objRoot;
	
	JComboBox<String> shapeSelector;
    JButton renderButton;
    
    JComboBox<String> objSelector;
    JButton readObjButton;

	// Set Rendering Color
    Color3f color = new Color3f(0.2f, 0.2f, 0.2f);

	public MeshViewer() {
		String filename = "./test_data/armadillo.obj";
		reader = new OBJReader();
		tmesh = reader.read_OBJ(filename);
		// Fit the triangle mesh in a bounding box with
		// unit diagonal length.
		tmesh.centerMesh();
		tmesh.normalizeMesh();

		setLayout(new BorderLayout());

		GraphicsConfiguration config = SimpleUniverse.getPreferredConfiguration();

		canvas = new Canvas3D(config);

		add("Center", canvas);

		SimpleUniverse u = new SimpleUniverse(canvas);

		objRoot = new BranchGroup();
		objRoot.setCapability(BranchGroup.ALLOW_CHILDREN_EXTEND);
		objRoot.setCapability(BranchGroup.ALLOW_CHILDREN_WRITE);
		objRoot.setCapability(BranchGroup.ALLOW_DETACH);

//		scene = createSceneGraph("Point-cloud");
//		scene = createSceneGraph("Wireframe");
		scene = createSceneGraph("Smooth Shading");
		
		Transform3D rotZPI = new Transform3D();
		rotZPI.rotZ(Math.PI);
		TransformGroup tg = new TransformGroup(rotZPI);
		tg.setCapability(TransformGroup.ALLOW_CHILDREN_EXTEND);
		tg.setCapability(TransformGroup.ALLOW_CHILDREN_WRITE);
		
		tg.addChild(scene);
		
		//select .obj
        objSelector = new JComboBox<>(DataPathMap.getOBJs().toArray(new String[0]));
        
        readObjButton = new JButton("Read");
        readObjButton.addActionListener(new ActionListener() {
        	@Override
            public void actionPerformed(ActionEvent e) {
                tg.removeAllChildren();
                String selectedObj = (String) objSelector.getSelectedItem();
                String filename = "./test_data/" + selectedObj + ".obj";
                tmesh = reader.read_OBJ(filename);
                tmesh.centerMesh();
                tmesh.normalizeMesh();
                scene = createSceneGraph("Smooth Shading");
                tg.addChild(scene);
            }
        });
		
        //select render type
        List<String> shapes = new ArrayList<>();
        shapes.add("Point-cloud");
        shapes.add("Wireframe");
        shapes.add("Filled with visible edges");
        shapes.add("Flat Shading");
        shapes.add("Smooth Shading");
        shapeSelector = new JComboBox<>(shapes.toArray(new String[0]));

        renderButton = new JButton("Render");
        renderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	tg.removeAllChildren();
                String selectedShape = (String) shapeSelector.getSelectedItem();
                scene = createSceneGraph(selectedShape);
                tg.addChild(scene);
            }
        });

        JPanel controlPanel = new JPanel();
        controlPanel.add(objSelector);
        controlPanel.add(readObjButton);
        controlPanel.add(new JSeparator());
        controlPanel.add(shapeSelector);
        controlPanel.add(renderButton);

        add("North", controlPanel);
	    
	    OrbitBehavior orbit = new OrbitBehavior(canvas, OrbitBehavior.REVERSE_ALL);
	    BoundingSphere bounds = new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);
	    orbit.setSchedulingBounds(bounds);

	    u.getViewingPlatform().setViewPlatformBehavior(orbit);
		
		objRoot.addChild(tg);

		u.getViewingPlatform().setNominalViewingTransform();

		// add a light to the root BranchGroup to illuminate the scene
		addLights(objRoot);

		// Set up the background
		Background bgNode = new Background(0.6f, 0.6f, 0.6f);
		bgNode.setApplicationBounds(getBoundingSphere());
		objRoot.addChild(bgNode);

		u.addBranchGraph(objRoot);
	}

	/*
	 * Create the geometry for the scene
	 */
	public BranchGroup createSceneGraph(String renderType) {

		// create a parent BranchGroup node for the geometry
		BranchGroup bg = new BranchGroup();
		bg.setCapability(BranchGroup.ALLOW_DETACH);
		
		// create shape - surface
		Shape3D shape = new Shape3D();
		
//		if ("Filled with visible edges".equals(renderType)) {
//			shape.setGeometry(getWireframeArray());
////			bg.addChild(shape);
//        }
		shape.setGeometry(getTriangleArray());
		shape.setAppearance(createAppearance(renderType));
		
		// add the geometry to the BranchGroup
		bg.addChild(shape);
		
		return bg;
	}
	
	public TriangleArray getTriangleArray() {
		// Approach 2: with TriangleArray
		TriangleArray triangleArray = new TriangleArray(3 * tmesh.number_faces,
				GeometryArray.COORDINATES | GeometryArray.NORMALS);

		// Make sure normals are computed before setting them
		tmesh.computeVertexNormal();

		int n = 3 * tmesh.number_faces;
		Point3d[] coords = new Point3d[n];
		Vector3f[] normals = new Vector3f[n];
		int index = 0;
		for (int i = 0; i < tmesh.number_faces; ++i) {
			for (int j = 0; j < 3; ++j) {
				int vj = tmesh.face[i][j];
				coords[index] = new Point3d(tmesh.point[vj]);
				normals[index] = new Vector3f(tmesh.normal[vj]);
				index++;
			}
		}

		triangleArray.setCoordinates(0, coords);
		triangleArray.setNormals(0, normals);
		
		return triangleArray;
	}
	
	public LineArray getWireframeArray() {
	    // Create LineArray with the same number of vertices
	    int n = 6 * tmesh.number_faces;
	    LineArray lineArray = new LineArray(n, GeometryArray.COORDINATES | GeometryArray.COLOR_3);

	    // Set up the vertices and color for the wireframe
	    Point3d[] coords = new Point3d[n];
	    Color3f[] colors = new Color3f[n / 2]; // Each line segment needs one color

	    int index = 0;
	    for (int i = 0; i < tmesh.number_faces; ++i) {
	        int v0 = tmesh.face[i][0];
	        int v1 = tmesh.face[i][1];
	        int v2 = tmesh.face[i][2];

	        coords[index] = new Point3d(tmesh.point[v0]);
	        colors[index / 2] = new Color3f(color);
	        index++;

	        coords[index] = new Point3d(tmesh.point[v1]);
	        index++;

	        coords[index] = new Point3d(tmesh.point[v1]);
	        colors[index / 2] = new Color3f(color);
	        index++;

	        coords[index] = new Point3d(tmesh.point[v2]);
	        index++;

	        coords[index] = new Point3d(tmesh.point[v2]);
	        colors[index / 2] = new Color3f(color);
	        index++;

	        coords[index] = new Point3d(tmesh.point[v0]);
	        index++;
	    }

	    lineArray.setCoordinates(0, coords);
	    lineArray.setColors(0, colors);

	    return lineArray;
	}

	/*
	 * create Appearance containing Material and PolygonAttributes
	 */
	Appearance createAppearance(String renderType) {

		Appearance app = new Appearance();

		// assign a Material to the Appearance.
		Color3f grayColor = new Color3f(0.5f, 0.5f, 0.5f);
		Color3f black = new Color3f(0.0f, 0.0f, 0.0f);
		app.setMaterial(new Material(grayColor, black, grayColor, black, 0.0f));

		// assign PolygonAttributes to the Appearance.
		PolygonAttributes polyAttrib = new PolygonAttributes();
		polyAttrib.setCullFace(PolygonAttributes.CULL_NONE);
		polyAttrib.setBackFaceNormalFlip(true);
//		polyAttrib.setPolygonOffset(0.1f); // You can adjust this value if needed
		
		if ("Point-cloud".equals(renderType)) {
		    polyAttrib.setPolygonMode(PolygonAttributes.POLYGON_POINT);
        } else if ("Wireframe".equals(renderType)) {
    	    polyAttrib.setPolygonMode(PolygonAttributes.POLYGON_LINE);
        } else if ("Filled with visible edges".equals(renderType)) {
    	    polyAttrib.setPolygonMode(PolygonAttributes.POLYGON_FILL);
        } else if ("Flat Shading".equals(renderType)) {
    	    polyAttrib.setPolygonMode(PolygonAttributes.POLYGON_FILL);
        } else if ("Smooth Shading".equals(renderType)) {
//    	    polyAttrib.setPolygonMode(PolygonAttributes.POLYGON_FILL);
        }
		
		app.setPolygonAttributes(polyAttrib);

		return app;
	}

	/*
	 * Add a directional light to the BranchGroup.
	 */
	public void addLights(BranchGroup bg) {
		// create the color for the light
		Color3f color = new Color3f(1.0f, 1.0f, 1.0f);

		// create a vector that describes the direction that
		// the light is shining.
		Vector3f direction = new Vector3f(1.0f, 1.0f, -1.0f);

		// create the directional light with the color and direction
		DirectionalLight light = new DirectionalLight(color, direction);

		// set the volume of influence of the light.
		// Only objects within the Influencing Bounds
		// will be illuminated.
		light.setInfluencingBounds(getBoundingSphere());

		// add the light to the BranchGroup
		bg.addChild(light);

	}

	/*
	 * Return a BoundingSphere that describes the volume of the scene.
	 */
	BoundingSphere getBoundingSphere() {
		return new BoundingSphere(new Point3d(0.0, 0.0, 0.0), 100.0);
	}
	
	public static void main(String args[]) {
		JFrame f = new MeshViewer();
//		f.setSize(2400, 1800);
		f.setSize(600, 600);
		f.setVisible(true);
		f.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
}
