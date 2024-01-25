import java.util.ArrayList;


public class Face {
    // number of vertices for this face
    private int numVertices;
    private ArrayList<Integer> vertexIndices;
    private ArrayList<Integer> textureIndices;
    private ArrayList<Integer> vertexNormalIndices;

    public Face(int nV) {
        numVertices = nV;
        vertexIndices = new ArrayList<Integer>();
        textureIndices = new ArrayList<Integer>();
        vertexNormalIndices = new ArrayList<Integer>();
    }

    public int getNumVertices() {
    	return numVertices;
    }
    
    public void addVertexIndex(int v) {
        vertexIndices.add(v);
    }

    public void addTextureIndex(int t) {
        textureIndices.add(t);
    }

    public void addVertexNormalIndex(int n) {
        vertexNormalIndices.add(n);
    }

    public int getVIndex(int idx) {
        return vertexIndices.get(idx);
    }

    public int getVNIndex(int idx) {
        return vertexNormalIndices.get(idx);
    }
}
