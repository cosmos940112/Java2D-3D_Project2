import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class TriangleMesh {
    private ArrayList<Vec4f> vertices;
    private ArrayList<Vec4f> normals;
    private ArrayList<Face> faces;

    public TriangleMesh() {
        vertices = new ArrayList<Vec4f>();
        normals = new ArrayList<Vec4f>();
        faces = new ArrayList<Face>();
    }
    
    public int getNumFaces() {
    	return faces.size();
    }
    
    public Face getFace(int i) {
    	return faces.get(i);
    }
    
    public Vec4f getVertex(int faceIdx, int vertIdx) {
    	Face f = faces.get(faceIdx);
    	return vertices.get(f.getVIndex(vertIdx));
    }
    
    public Vec4f getVertexNormal(int faceIdx, int vertIdx) {
    	Face f = faces.get(faceIdx);
    	return normals.get(f.getVNIndex(vertIdx));
    }
    
    public void readobj(String fileName) {
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split("\\s+");
                // skip empty lines
                if (tokens.length<=0) continue;
                // skip the object name
                if (tokens[0].equals("o")) continue;
                if (tokens[0].equals("v")) {
                    Vec4f v = new Vec4f(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3]), 1.0f);
                    vertices.add(v);
                }
                // skip texture coordinates
                if (tokens[0].equals("vt")) continue;
                if (tokens[0].equals("vn")) {
                    Vec4f n = new Vec4f(Float.parseFloat(tokens[1]), Float.parseFloat(tokens[2]), Float.parseFloat(tokens[3]), 0.0f);
                    normals.add(n);
                }
                if (tokens[0].equals("f")) {
                    int numV = tokens.length;
                    Face f = new Face(numV-1);

                    for (int i = 1; i < numV; i++) {
                        String[] faceTokens = tokens[i].split("/");
                        int vi = Integer.parseInt(faceTokens[0]);
                        int ti = Integer.parseInt(faceTokens[1]);
                        int ni = Integer.parseInt(faceTokens[2]);
                        
                        // index starts at 1 in .obj
                        f.addVertexIndex(vi-1);
                        f.addTextureIndex(ti-1);
                        f.addVertexNormalIndex(ni-1);
                    }
                    faces.add(f);
                }
            }
        } catch(Exception e) {
            System.out.println("Error in readobj: " + e);
        }
    }
}
