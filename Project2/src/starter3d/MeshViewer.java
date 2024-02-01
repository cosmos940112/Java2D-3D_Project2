package starter3d;
import java.awt.BorderLayout;
import java.awt.GraphicsConfiguration;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
// for picking
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

import org.jogamp.java3d.Appearance;
import org.jogamp.java3d.Background;
import org.jogamp.java3d.BoundingSphere;
import org.jogamp.java3d.BranchGroup;
import org.jogamp.java3d.Canvas3D;
import org.jogamp.java3d.DirectionalLight;
import org.jogamp.java3d.GeometryArray;
import org.jogamp.java3d.LineArray;
import org.jogamp.java3d.Material;
import org.jogamp.java3d.Node;
import org.jogamp.java3d.PickInfo;
import org.jogamp.java3d.PointArray;
import org.jogamp.java3d.PolygonAttributes;
import org.jogamp.java3d.Shape3D;
import org.jogamp.java3d.Transform3D;
import org.jogamp.java3d.TransformGroup;
import org.jogamp.java3d.TriangleArray;
import org.jogamp.java3d.utils.behaviors.vp.OrbitBehavior;
import org.jogamp.java3d.utils.geometry.Sphere;
import org.jogamp.java3d.utils.pickfast.PickCanvas;
import org.jogamp.java3d.utils.universe.SimpleUniverse;
import org.jogamp.vecmath.Color3f;
import org.jogamp.vecmath.Point3d;
import org.jogamp.vecmath.Vector3d;
import org.jogamp.vecmath.Vector3f;


public class MeshViewer extends JFrame {
	private static final long serialVersionUID = 1L;

	TriangleMesh tmesh;
	DataPathMap dataPathMap;

	Canvas3D canvas;
	BranchGroup scene;
	BranchGroup objRoot;
	
	JComboBox<String> shapeSelector;
    JButton renderButton;
    
    JComboBox<String> objSelector;
    JButton readObjButton;

	private Point3d picked = new Point3d();
	//private Vector3d view = new Vector3d();

	MeshViewer(String filename) {
		read_OFF(filename);
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

		scene = createSceneGraph("Point-cloud");
//		scene = createSceneGraph("Smooth Shading");
		
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
        		String selectedObj = (String) objSelector.getSelectedItem();
        		read_OFF(selectedObj);
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
//            	tg.removeAllChildren();
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

		canvas.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				Node node = null;
				if (SwingUtilities.isLeftMouseButton(e)) {
					node = pickSurface(e.getX(), e.getY());
				}
				if (node != null) {
					System.out.println("Intersection");
					System.out.println(picked);

					//after tg.addChild, red ball appear
					tg.addChild(createSphere(picked, 0.05f));
				}
			}
		});
		
		objRoot.addChild(tg);

		u.getViewingPlatform().setNominalViewingTransform();

		// add a light to the root BranchGroup to illuminate the scene
		addLights(objRoot);

		// Set up the background
		Background bgNode = new Background(0.2f, 0.2f, 0.2f);
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
		
		// create shape - surface
		Shape3D shape = new Shape3D();
		
		if ("Point-cloud".equals(renderType)) {
			shape.setGeometry(getPointArray());
        } else if ("Wireframe".equals(renderType)) {
			shape.setGeometry(getPointArray());
        } else if ("Filled with visible edges".equals(renderType)) {
			shape.setGeometry(getPointArray());
        } else if ("Flat Shading".equals(renderType)) {
			shape.setGeometry(getPointArray());
        } else if ("Smooth Shading".equals(renderType)) {
			shape.setGeometry(getTriangleArray());
        }

		shape.setAppearance(createAppearance());
		
//		System.out.println(shape.getPickable());
	
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
	    LineArray lineArray = new LineArray(2 * tmesh.number_faces, GeometryArray.COORDINATES);

	    // Set up the vertices for the wireframe
	    int n = 2 * tmesh.number_faces;
	    Point3d[] coords = new Point3d[n];
	    int index = 0;
	    for (int i = 0; i < tmesh.number_faces; ++i) {
	        int v0 = tmesh.face[i][0];
	        int v1 = tmesh.face[i][1];
	        int v2 = tmesh.face[i][2];

	        coords[index++] = new Point3d(tmesh.point[v0]);
	        coords[index++] = new Point3d(tmesh.point[v1]);

	        coords[index++] = new Point3d(tmesh.point[v1]);
	        coords[index++] = new Point3d(tmesh.point[v2]);

	        coords[index++] = new Point3d(tmesh.point[v2]);
	        coords[index++] = new Point3d(tmesh.point[v0]);
	    }

	    lineArray.setCoordinates(0, coords);

	    return lineArray;
	}

	
	public PointArray getPointArray() {
		// create shape - points
	    PointArray pointArray = new PointArray(tmesh.number_vertices, GeometryArray.COORDINATES);

	    Point3d[] coords = new Point3d[tmesh.number_vertices];
	    for (int i = 0; i < tmesh.number_vertices; ++i) {
	        coords[i] = new Point3d(tmesh.point[i]);
	    }

	    pointArray.setCoordinates(0, coords);
		
		return pointArray;
	}

	private Node pickSurface(int mx, int my) {
		PickCanvas pickCanvas = new PickCanvas(canvas, scene);
		pickCanvas.setMode(PickInfo.PICK_GEOMETRY);
		pickCanvas.setFlags(PickInfo.NODE | PickInfo.CLOSEST_INTERSECTION_POINT);
		pickCanvas.setTolerance(0.0f);

		pickCanvas.setShapeLocation(mx, my);
		
		PickInfo pinfo = pickCanvas.pickClosest();
		if (pinfo == null)
			return null;

		Node node = pinfo.getNode();
		Point3d closest = pinfo.getClosestIntersectionPoint();

		if (closest != null) {
			picked = closest;
		} else {
			System.out.println("PickRay pr is null");
		}

		return node;
	}

	private BranchGroup createSphere(Point3d center, float radius) {
		BranchGroup bg = new BranchGroup();

		Vector3d tv = new Vector3d(center.x, center.y, center.z);
		Transform3D t = new Transform3D();
		t.setTranslation(tv);
		TransformGroup tg = new TransformGroup(t);

		Appearance app = new Appearance();
		app.setCapability(Appearance.ALLOW_POINT_ATTRIBUTES_WRITE);
		app.setCapability(Appearance.ALLOW_POLYGON_ATTRIBUTES_WRITE);
		app.setCapability(Appearance.ALLOW_MATERIAL_WRITE);

		PolygonAttributes pa = new PolygonAttributes();
		pa.setCapability(PolygonAttributes.ALLOW_MODE_WRITE);
		pa.setCapability(PolygonAttributes.ALLOW_NORMAL_FLIP_WRITE);
		pa.setCapability(PolygonAttributes.ALLOW_CULL_FACE_WRITE);
		pa.setCullFace(PolygonAttributes.CULL_NONE);
		pa.setBackFaceNormalFlip(true);
		app.setPolygonAttributes(pa);

		Color3f eColor = new Color3f(0.0f, 0.0f, 0.0f);
		Color3f sColor = new Color3f(1.0f, 1.0f, 1.0f);
		Color3f normalc = new Color3f(1.0f, 0.0f, 0.0f);

		Material m = new Material(normalc, eColor, normalc, sColor, 100.0f);
		m.setCapability(Material.ALLOW_COMPONENT_WRITE);
		m.setLightingEnable(true);
		app.setMaterial(m);

		Sphere sp = new Sphere(radius, app);
		tg.addChild(sp);
		bg.addChild(tg);

		return bg;
	}

	/*
	 * create Appearance containing Material and PolygonAttributes
	 */
	Appearance createAppearance() {

		Appearance app = new Appearance();

		// assign a Material to the Appearance.
		Color3f diffColor = new Color3f(0.49f, 0.34f, 0.0f);
		Color3f specColor = new Color3f(0.89f, 0.79f, 0.0f);
		Color3f black = new Color3f(0.0f, 0.0f, 0.0f);
		app.setMaterial(new Material(diffColor, black, diffColor, specColor, 17.0f));

		// assign PolygonAttributes to the Appearance.
		PolygonAttributes polyAttrib = new PolygonAttributes();
		polyAttrib.setCullFace(PolygonAttributes.CULL_NONE);
		polyAttrib.setBackFaceNormalFlip(true);
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

	/*
	 * Read OFF files quick and dirty. For now, limited to triangle meshes.
	 */
	public boolean read_OFF(String filename) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));

			String line;
			line = br.readLine();
			Scanner sc = new Scanner(line);

			// For now ignore comments '#', TODO later

			String tmp = sc.next();
			if (!tmp.equalsIgnoreCase("OFF")) {
				sc.close();
				br.close();
				System.out.println("Incorrect OFF file format");
				return false;
			}
			sc.close();

			line = br.readLine();
			sc = new Scanner(line);
			int nv = sc.nextInt();
			int nf = sc.nextInt();
			sc.nextInt(); // can be zero so ignore it
			sc.close();

			tmesh = new TriangleMesh(nv, nf);

			double dx, dy, dz;

			for (int i = 0; i < tmesh.number_vertices; i++) {
				line = br.readLine();
				sc = new Scanner(line);

				dx = sc.nextDouble();
				dy = sc.nextDouble();
				dz = sc.nextDouble();

				if (sc.hasNext()) {
					// TODO later handle color or normal information?
				}

				tmesh.setVertex(i, dx, dy, dz);

				sc.close();
			}

			int fi, fj, fk;
			for (int i = 0; i < tmesh.number_faces; i++) {
				line = br.readLine();
				sc = new Scanner(line);

				sc.nextInt(); // dummy we assume triangle meshes
				fi = sc.nextInt();
				fj = sc.nextInt();
				fk = sc.nextInt();

				tmesh.setFace(i, fi, fj, fk);
				sc.close();
			}

			br.close();
			
//			System.out.println("Read mesh: " + filename);
//			System.out.println("made of " + tmesh.number_vertices + " vertices and " + tmesh.number_faces + " faces.");

			return true;
		} catch (IOException e) {
			System.out.println("An exception occurred while reading " + filename);
			return false;
		}
	}
	
	public boolean read_OBJ(String filename) {
	    try {
	        BufferedReader br = new BufferedReader(new FileReader(filename));

	        String line;
	        line = br.readLine();
	        Scanner sc = new Scanner(line);

	        // For now ignore comments '#', TODO later

	        String tmp = sc.next();
	        if (!tmp.equalsIgnoreCase("v")) {
	            sc.close();
	            br.close();
	            System.out.println("Incorrect OBJ file format");
	            return false;
	        }
	        sc.close();

	        int nv = 0; // Vertex count
	        int nf = 0; // Face count

	        // Count vertices and faces
	        while ((line = br.readLine()) != null) {
	            sc = new Scanner(line);
	            if (sc.hasNext()) {
	                tmp = sc.next();
	                if (tmp.equalsIgnoreCase("v")) {
	                    nv++;
	                } else if (tmp.equalsIgnoreCase("f")) {
	                    nf++;
	                }
	            }
	            sc.close();
	        }

	        br.close();

	        // Reopen the file for actual reading
	        br = new BufferedReader(new FileReader(filename));

	        // Skip comments and vertex data to reach face data
	        while ((line = br.readLine()) != null && !line.trim().startsWith("f")) {}

	        tmesh = new TriangleMesh(nv, nf);

	        for (int i = 0; i < tmesh.number_vertices; i++) {
	            line = br.readLine();
	            sc = new Scanner(line);

	            // Skip "v"
	            sc.next();

	            double dx = sc.nextDouble();
	            double dy = sc.nextDouble();
	            double dz = sc.nextDouble();

	            if (sc.hasNext()) {
	                // TODO later handle color or normal information?
	            }

	            tmesh.setVertex(i, dx, dy, dz);

	            sc.close();
	        }

	        for (int i = 0; i < tmesh.number_faces; i++) {
	            line = br.readLine();
	            while ((line = br.readLine()) != null && !line.trim().startsWith("f")) {
	                if (line.trim().isEmpty()) {
	                    continue;  // Skip empty lines
	                }
	                sc = new Scanner(line);
	                // Skip "f"
	                sc.next();
	                
	                int fi = sc.nextInt();
	                int fj = sc.nextInt();
	                int fk = sc.nextInt();
	                
	                tmesh.setFace(i, fi - 1, fj - 1, fk - 1); // Subtract 1 to convert to zero-based indexing
	                sc.close();
	            }
	        }

	        br.close();

//	        System.out.println("Read mesh: " + filename);
//	        System.out.println("made of " + tmesh.number_vertices + " vertices and " + tmesh.number_faces + " faces.");

	        return true;
	    } catch (IOException e) {
	        System.out.println("An exception occurred while reading " + filename);
	        return false;
	    }
	}
	
	public static void main(String args[]) {
		JFrame f = new MeshViewer("./test_data/doraemon.off");
//		f.setSize(2400, 1800);
		f.setSize(600, 600);
		f.setVisible(true);
		f.setDefaultCloseOperation(EXIT_ON_CLOSE);
	}
}
