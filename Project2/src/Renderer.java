import javax.imageio.ImageIO;
import java.io.File;
import java.awt.Color;
import java.io.IOException;
import java.awt.image.BufferedImage;


public class Renderer {
	
	TriangleMesh triMesh;
	
    Matrix4f viewport;
    Matrix4f projection;
    Matrix4f modelView;
    Matrix4f totalTransform;
    Matrix4f normalMatrix;
    
    Vec4f lightDir;
    Vec4f invLightDir;

    float[] zBuffer;
    int[] colorBuffer;
    int renderWidth;
    int renderHeight;

    
    public Renderer(int width, int height, TriangleMesh t) {
        renderWidth = width;
        renderHeight = height;

        triMesh = t;
        
        colorBuffer = new int[renderWidth*renderHeight];
                
        viewport = Matrix4f.simpleViewport(renderWidth, renderHeight);

        float dist = 10.0f;
        projection = Matrix4f.simpleProjectionMatrix(dist);
        modelView = new Matrix4f();
        totalTransform = new Matrix4f();

        zBuffer = new float[renderWidth*renderHeight];
        for (int i=0; i<renderWidth*renderHeight; i++) {
        	zBuffer[i] = -999999.0f;
        }
        
        float invSqrt3 = 1.0f/(float)Math.sqrt(3.0);
        lightDir = new Vec4f(-1.0f*invSqrt3, -1.0f*invSqrt3, -1.0f*invSqrt3, 0.0f);
        invLightDir = new Vec4f(1.0f*invSqrt3, 1.0f*invSqrt3, 1.0f*invSqrt3, 0.0f);
    }
    
    public BufferedImage getImage() {
    	BufferedImage bi = new BufferedImage(renderWidth, renderHeight, BufferedImage.TYPE_INT_RGB);
    	for (int i=0; i<renderWidth; i++) {
    		for (int j=0; j<renderHeight; j++) {
    			bi.setRGB(i, (renderHeight-1)-j, colorBuffer[i+renderWidth*j]);
    		}
    	}
    	return bi;
    }
    
    public void clearBuffers() {
    	for (int i=0; i<renderWidth*renderHeight; i++) {
        	zBuffer[i] = -999999.0f;
        }
    	for (int i=0; i<renderWidth*renderHeight; i++) {
        	colorBuffer[i] = 0;
        }
    }

    // vs0, vs1, vs2 are in screen space after perspective divide
    // check if the point (i,j) is inside the triangle formed by (vs0, vs1, vs2)
    private Vec4f bc(Vec4f vs0, Vec4f vs1, Vec4f vs2, int i, int j) {
    	Vec4f w = new Vec4f();
    	
    	float fi = (float)i;
    	float fj = (float)j;
    	
    	// w0, w1, w2
    	w.x = (vs1.x - fi)*(vs2.y - fj) - (vs2.x - fi)*(vs1.y - fj);
    	w.y = (vs2.x - fi)*(vs0.y - fj) - (vs0.x - fi)*(vs2.y - fj);
    	w.z = (vs0.x - fi)*(vs1.y - fj) - (vs1.x - fi)*(vs0.y - fj);
    	w.w = 1.0f;
    	
    	// triangle area
    	float area = (vs1.x - vs0.x)*(vs2.y - vs0.y) - (vs2.x - vs0.x)*(vs1.y - vs0.y);
    	    	
    	w.x = w.x / area;
    	w.y = w.y / area;
    	w.z = w.z / area;
    	
    	return w;
    }
    
    private void rasterizeTriangle(Vec4f v0, Vec4f v1, Vec4f v2, Vec4f vn0, Vec4f vn1, Vec4f vn2) {
        // in screen space before perspective divide
        Vec4f vs0 = totalTransform.mul(v0);
        Vec4f vs1 = totalTransform.mul(v1);
        Vec4f vs2 = totalTransform.mul(v2);
        
        // in screen space after perspective divide
        Vec4f vsd0 = new Vec4f(vs0.x/vs0.w, vs0.y/vs0.w, vs0.z/vs0.w, 1.0f);
    	Vec4f vsd1 = new Vec4f(vs1.x/vs1.w, vs1.y/vs1.w, vs1.z/vs1.w, 1.0f);
    	Vec4f vsd2 = new Vec4f(vs2.x/vs2.w, vs2.y/vs2.w, vs2.z/vs2.w, 1.0f);

        // in camera space
        //Vec4f vc0 = modelView.mul(v0);
        //Vec4f vc1 = modelView.mul(v1);
        //Vec4f vc2 = modelView.mul(v2);
        
        // transformed normals in camera space
        Vec4f vnt0 = normalMatrix.mul(vn0);
        Vec4f vnt1 = normalMatrix.mul(vn1);
        Vec4f vnt2 = normalMatrix.mul(vn2);

        float minxf = Math.min(Math.min(vsd0.x, vsd1.x), vsd2.x);
        float minyf = Math.min(Math.min(vsd0.y, vsd1.y), vsd2.y);
        float maxxf = Math.max(Math.max(vsd0.x, vsd1.x), vsd2.x);
        float maxyf = Math.max(Math.max(vsd0.y, vsd1.y), vsd2.y);

        int minx = (int)minxf;
        int miny = (int)minyf;
        int maxx = (int)maxxf;
        int maxy = (int)maxyf;

        minx = Math.max(0, minx);
        miny = Math.max(0, miny);
        maxx = Math.min(renderWidth-1, maxx);
        maxy = Math.min(renderHeight-1, maxy);

        for (int i = minx; i <= maxx; i++) {
            for (int j = miny; j <= maxy; j++) {
                // compute barycentric weights of given point (i, j)
                Vec4f c = bc(vsd0, vsd1, vsd2, i, j);

                // point is inside the triangle
                if (c.x>=0.0f && c.y>=0.0f && c.z>=0.0f) {
                    // interpolate depth
                    float z = c.x*vs0.z + c.y*vs1.z + c.z*vs2.z;

                    if (z > zBuffer[i+j*renderWidth]) {
                        zBuffer[i+j*renderWidth] = z;
                        
                        // ambient term
                        float ar = 0.2f, ag = 0.2f, ab = 0.2f;
                        
                        // diffuse term
                        // interpolated normal
                        Vec4f in = new Vec4f(c.x*vnt0.x+c.y*vnt1.x+c.z*vnt2.x, 
                        		c.x*vnt0.y+c.y*vnt1.y+c.z*vnt2.y,
                        		c.x*vnt0.z+c.y*vnt1.z+c.z*vnt2.z,
                        		0.0f);
                        float nd = in.dot(invLightDir);
                        nd = Math.min(Math.max(0.f, nd), 1.0f);
                        float dr = 0.5f * nd, dg = 0.5f * nd, db = 0.5f * nd;

                        // total contribution
                        float r = ar + dr, g = ag + dg, b = ab + db;
                        r = Math.min(Math.max(0.f, r), 1.0f);
                        g = Math.min(Math.max(0.f, g), 1.0f);
                        b = Math.min(Math.max(0.f, b), 1.0f);
                        
                        Color col = new Color(r, g, b);
                        colorBuffer[i+renderWidth*j] = col.getRGB();
                    }
                }
            }
        }
    }

    public void draw(Matrix4f mv) {
        modelView = mv;

        // Form the matrix transforming vertices into pixels
        totalTransform = new Matrix4f(viewport);
        totalTransform.mul(projection);
        totalTransform.mul(modelView);

        // Matrix for transforming the normal
        normalMatrix = new Matrix4f(modelView);
        normalMatrix.inverse();
        normalMatrix.transpose();

        for (int i = 0; i < triMesh.getNumFaces(); i++) {
        	Vec4f v0 = triMesh.getVertex(i, 0);
        	Vec4f v1 = triMesh.getVertex(i, 1);
        	Vec4f v2 = triMesh.getVertex(i, 2);
        	
        	Vec4f vn0 = triMesh.getVertexNormal(i, 0);
        	Vec4f vn1 = triMesh.getVertexNormal(i, 1);
        	Vec4f vn2 = triMesh.getVertexNormal(i, 2);
        	
            rasterizeTriangle(v0, v1, v2, vn0, vn1, vn2);
        }
    }
    
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Renderer tri_mesh.obj");
            return;
        }

        int width = 800;
        int height = 800;
        
        TriangleMesh t = new TriangleMesh();
        t.readobj(args[0]);
        
        Renderer r = new Renderer(width, height, t);

        Matrix4f mv = new Matrix4f();
        r.draw(mv);

        // do something with image, e.g. write to file
        try {
        	File outFile = new File("./output.png");
        	BufferedImage img = r.getImage();
        	ImageIO.write(img, "png", outFile);
        } catch (IOException e) {
        	System.out.println("Error: writing result to file");
        }
        
    }
}
