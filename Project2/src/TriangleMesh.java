
import org.jogamp.vecmath.*;

public class TriangleMesh {
	int number_vertices = 0;
	int number_faces = 0;

	Point3d[] point = null;
	Vector3f[] normal = null;
	int[][] face = null;

	public TriangleMesh(int nv, int nf) {
		number_vertices = nv;
		number_faces = nf;
		point = new Point3d[number_vertices];
		face = new int[number_faces][3];
	}

	public void setVertex(int i, double x, double y, double z) {
		point[i] = new Point3d(x, y, z);
	}

	public void setFace(int i, int fi, int fj, int fk) {
		face[i][0] = fi;
		face[i][1] = fj;
		face[i][2] = fk;
	}
	
	// Center the mesh such that its center of mass is the origin
	public void centerMesh() {
		Point3d c = new Point3d(0, 0, 0);
		for (int i=0; i<point.length; i++) {
			c.add(point[i]);
		}
		c.scale(1.0/point.length);
		
		for (int i=0; i<point.length; i++) {
			point[i].sub(c);
		}
	}

	private void computeBBox(Point3d lc, Point3d uc) {
		lc.x = Double.MAX_VALUE;
		lc.y = Double.MAX_VALUE;
		lc.z = Double.MAX_VALUE;

		uc.x = -Double.MAX_VALUE;
		uc.y = -Double.MAX_VALUE;
		uc.z = -Double.MAX_VALUE;

		for (int i = 0; i < number_vertices; ++i) {
			Point3d curr = point[i];
			double x = curr.x;
			double y = curr.y;
			double z = curr.z;

			lc.x = Math.min(lc.x, x);
			lc.y = Math.min(lc.y, y);
			lc.z = Math.min(lc.z, z);

			uc.x = Math.max(uc.x, x);
			uc.y = Math.max(uc.y, y);
			uc.z = Math.max(uc.z, z);
		}
	}

	private double computeBBoxDiagonalLength() {
		Point3d lower_corner = new Point3d();
		Point3d upper_corner = new Point3d();
		computeBBox(lower_corner, upper_corner);
		double len = lower_corner.distance(upper_corner);
		return len;
	}

	// Normalize the model such that it fits into a bounding box
	// with unit diagonal.
	public void normalizeMesh() {
		double len = computeBBoxDiagonalLength();
		if (len == 0.0) {
			System.out.println("BBox with zero length ...");
			System.exit(1);
		}

		for (int i = 0; i < number_vertices; ++i) {
			point[i].scale(1.0 / len);
		}
	}

	public void computeVertexNormal() {
		normal = new Vector3f[number_vertices];
		for (int i = 0; i < number_vertices; ++i) {
			normal[i] = new Vector3f();
		}

		for (int i = 0; i < number_faces; ++i) {
			int v0 = face[i][0];
			int v1 = face[i][1];
			int v2 = face[i][2];

			Point3d p0 = point[v0];
			Point3d p1 = point[v1];
			Point3d p2 = point[v2];
			
			Vector3f p0p1 = new Vector3f(Double.valueOf(p1.x - p0.x).floatValue(), Double.valueOf(p1.y - p0.y).floatValue(),
					Double.valueOf(p1.z - p0.z).floatValue());
			Vector3f p0p2 = new Vector3f(Double.valueOf(p2.x - p0.x).floatValue(), Double.valueOf(p2.y - p0.y).floatValue(),
					Double.valueOf(p2.z - p0.z).floatValue());

			Vector3f ni = new Vector3f();
			ni.cross(p0p1, p0p2);

			normal[v0].add(ni);
			normal[v1].add(ni);
			normal[v2].add(ni);
		}

		for (int i = 0; i < number_vertices; ++i) {
			normal[i].normalize();
		}
	}
}
