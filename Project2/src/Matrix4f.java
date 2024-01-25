public class Matrix4f {
	private float[][] data;
    private int COLS = 4;
    private int ROWS = 4;
	
    public Matrix4f() {
        data = new float[ROWS][COLS];
        for (int i=0; i<ROWS; i++) {
            for (int j=0; j<COLS; j++) {
                data[i][j]=0.0f;
            }
        }

        data[0][0] = 1.0f;
        data[1][1] = 1.0f;
        data[2][2] = 1.0f;
        data[3][3] = 1.0f;
    }
    
    // copy
    public Matrix4f(Matrix4f other) {
    	data = new float[ROWS][COLS];
    	for (int i=0; i<ROWS; i++) {
    		for (int j=0; j<COLS; j++) {
    			data[i][j] = other.data[i][j];
    		}
    	}
    }
    
    public void set(int i, int j, float val) {
        data[i][j] = val;
    }

    // Returns the transpose of this matrix
    public Matrix4f transpose() {
    	Matrix4f m = new Matrix4f();
    	for (int i=0; i<ROWS; i++) {
    		for (int j=0; j<COLS; j++) {
    			m.data[i][j] = data[j][i];
    		}
    	}
    	return m;
    }

    public Matrix4f inverse() {
    	Matrix4f m = new Matrix4f();
    	
    	float m00 = data[0][0];
    	float m01 = data[0][1];
    	float m02 = data[0][2];
    	float m03 = data[0][3];
    	float m10 = data[1][0];
    	float m11 = data[1][1];
    	float m12 = data[1][2];
    	float m13 = data[1][3];
    	float m20 = data[2][0];
    	float m21 = data[2][1];
    	float m22 = data[2][2];
    	float m23 = data[2][3];
    	float m30 = data[3][0];
    	float m31 = data[3][1];
    	float m32 = data[3][2];
    	float m33 = data[3][3];
    	
        float det = m03*m12*m21*m30-m02*m13*m21*m30-m03*m11*m22*m30+m01*m13*m22*m30+
        		m02*m11*m23*m30-m01*m12*m23*m30-m03*m12*m20*m31+m02*m13*m20*m31+
        		m03*m10*m22*m31-m00*m13*m22*m31-m02*m10*m23*m31+m00*m12*m23*m31+
        		m03*m11*m20*m32-m01*m13*m20*m32-m03*m10*m21*m32+m00*m13*m21*m32+
        		m01*m10*m23*m32-m00*m11*m23*m32-m02*m11*m20*m33+m01*m12*m20*m33+
        		m02*m10*m21*m33-m00*m12*m21*m33-m01*m10*m22*m33+m00*m11*m22*m33;
        det = 1.0f / det;
        
        float nm00, nm01, nm02, nm03, nm10, nm11, nm12, nm13, nm20, nm21, nm22, nm23, nm30, nm31, nm32, nm33;

        nm00 = (-(m13*m22*m31) + m12*m23*m31 + m13*m21*m32 - m11*m23*m32 - m12*m21*m33 + m11*m22*m33) * det;
        nm01 = (m03*m22*m31 - m02*m23*m31 - m03*m21*m32 + m01*m23*m32 + m02*m21*m33 - m01*m22*m33) * det;
        nm02 = (-(m03*m12*m31) + m02*m13*m31 + m03*m11*m32 - m01*m13*m32 - m02*m11*m33 + m01*m12*m33) * det;
        nm03 = (m03*m12*m21 - m02*m13*m21 - m03*m11*m22 + m01*m13*m22 + m02*m11*m23 - m01*m12*m23) * det;
        nm10 = (m13*m22*m30 - m12*m23*m30 - m13*m20*m32 + m10*m23*m32 + m12*m20*m33 - m10*m22*m33) * det;
        nm11 = (-(m03*m22*m30) + m02*m23*m30 + m03*m20*m32 - m00*m23*m32 - m02*m20*m33 + m00*m22*m33) * det;
        nm12 = (m03*m12*m30 - m02*m13*m30 - m03*m10*m32 + m00*m13*m32 + m02*m10*m33 - m00*m12*m33) * det;
        nm13 = (-(m03*m12*m20) + m02*m13*m20 + m03*m10*m22 - m00*m13*m22 - m02*m10*m23 + m00*m12*m23) * det;
        nm20 = (-(m13*m21*m30) + m11*m23*m30 + m13*m20*m31 - m10*m23*m31 - m11*m20*m33 + m10*m21*m33) * det;
        nm21 = (m03*m21*m30 - m01*m23*m30 - m03*m20*m31 + m00*m23*m31 + m01*m20*m33 - m00*m21*m33) * det;
        nm22 = (-(m03*m11*m30) + m01*m13*m30 + m03*m10*m31 - m00*m13*m31 - m01*m10*m33 + m00*m11*m33) * det;
        nm23 = (m03*m11*m20 - m01*m13*m20 - m03*m10*m21 + m00*m13*m21 + m01*m10*m23 - m00*m11*m23) * det;
        nm30 = (m12*m21*m30 - m11*m22*m30 - m12*m20*m31 + m10*m22*m31 + m11*m20*m32 - m10*m21*m32) * det;
        nm31 = (-(m02*m21*m30) + m01*m22*m30 + m02*m20*m31 - m00*m22*m31 - m01*m20*m32 + m00*m21*m32) * det;
        nm32 = (m02*m11*m30 - m01*m12*m30 - m02*m10*m31 + m00*m12*m31 + m01*m10*m32 - m00*m11*m32) * det;
        nm33 = (-(m02*m11*m20) + m01*m12*m20 + m02*m10*m21 - m00*m12*m21 - m01*m10*m22 + m00*m11*m22) * det;
        
        m.data[0][0] = nm00;
        m.data[0][1] = nm01;
        m.data[0][2] = nm02;
        m.data[0][3] = nm03;
        m.data[1][0] = nm10;
        m.data[1][1] = nm11;
        m.data[1][2] = nm12;
        m.data[1][3] = nm13;
        m.data[2][0] = nm20;
        m.data[2][1] = nm21;
        m.data[2][2] = nm22;
        m.data[2][3] = nm23;
        m.data[3][0] = nm30;
        m.data[3][1] = nm31;
        m.data[3][2] = nm32;
        m.data[3][3] = nm33;
        
        return m;
    }

    // this <- this * other
    public void mul(Matrix4f other) {
    	float m00 = data[0][0];
    	float m01 = data[0][1];
    	float m02 = data[0][2];
    	float m03 = data[0][3];
    	float m10 = data[1][0];
    	float m11 = data[1][1];
    	float m12 = data[1][2];
    	float m13 = data[1][3];
    	float m20 = data[2][0];
    	float m21 = data[2][1];
    	float m22 = data[2][2];
    	float m23 = data[2][3];
    	float m30 = data[3][0];
    	float m31 = data[3][1];
    	float m32 = data[3][2];
    	float m33 = data[3][3];
    	
    	float om00 = other.data[0][0];
    	float om01 = other.data[0][1];
    	float om02 = other.data[0][2];
    	float om03 = other.data[0][3];
    	float om10 = other.data[1][0];
    	float om11 = other.data[1][1];
    	float om12 = other.data[1][2];
    	float om13 = other.data[1][3];
    	float om20 = other.data[2][0];
    	float om21 = other.data[2][1];
    	float om22 = other.data[2][2];
    	float om23 = other.data[2][3];
    	float om30 = other.data[3][0];
    	float om31 = other.data[3][1];
    	float om32 = other.data[3][2];
    	float om33 = other.data[3][3];
    	
    	float nm00 = m00*om00+m01*om10+m02*om20+m03*om30;
        float nm01 = m00*om01+m01*om11+m02*om21+m03*om31;
        float nm02 = m00*om02+m01*om12+m02*om22+m03*om32;
        float nm03 = m00*om03+m01*om13+m02*om23+m03*om33;
        float nm10 = m10*om00+m11*om10+m12*om20+m13*om30;
        float nm11 = m10*om01+m11*om11+m12*om21+m13*om31;
        float nm12 = m10*om02+m11*om12+m12*om22+m13*om32;
        float nm13 = m10*om03+m11*om13+m12*om23+m13*om33;
        float nm20 = m20*om00+m21*om10+m22*om20+m23*om30;
        float nm21 = m20*om01+m21*om11+m22*om21+m23*om31;
        float nm22 = m20*om02+m21*om12+m22*om22+m23*om32;
        float nm23 = m20*om03+m21*om13+m22*om23+m23*om33;
        float nm30 = m30*om00+m31*om10+m32*om20+m33*om30;
        float nm31 = m30*om01+m31*om11+m32*om21+m33*om31;
        float nm32 = m30*om02+m31*om12+m32*om22+m33*om32;
        float nm33 = m30*om03+m31*om13+m32*om23+m33*om33;
        
        // replace the current matrix with the result of its right-multiplication with other
        data[0][0] = nm00;
        data[0][1] = nm01;
        data[0][2] = nm02;
        data[0][3] = nm03;
        data[1][0] = nm10;
        data[1][1] = nm11;
        data[1][2] = nm12;
        data[1][3] = nm13;
        data[2][0] = nm20;
        data[2][1] = nm21;
        data[2][2] = nm22;
        data[2][3] = nm23;
        data[3][0] = nm30;
        data[3][1] = nm31;
        data[3][2] = nm32;
        data[3][3] = nm33;      
    }
    
    // vout <- this * v
    public Vec4f mul(Vec4f v) {
    	Vec4f vout = new Vec4f();
    	
    	vout.x = data[0][0]*v.x + data[0][1]*v.y + data[0][2]*v.z + data[0][3]*v.w;
    	vout.y = data[1][0]*v.x + data[1][1]*v.y + data[1][2]*v.z + data[1][3]*v.w;
    	vout.z = data[2][0]*v.x + data[2][1]*v.y + data[2][2]*v.z + data[2][3]*v.w;
    	vout.w = data[3][0]*v.x + data[3][1]*v.y + data[3][2]*v.z + data[3][3]*v.w;
    	
    	return vout;
    }

    // Rotation matrix about y-axis
    // Assume that angle is in degrees
    public static Matrix4f rotY(float angle) {
    	Matrix4f m = new Matrix4f();
    	double arad = Math.toRadians(angle);
    	float cosa = (float)Math.cos(arad);
    	float sina = (float)Math.sin(arad);
    	m.data[0][0] = cosa;
    	m.data[0][2] = sina;
    	m.data[2][0] = -sina;
    	m.data[2][2] = cosa;
    	return m;
    }
    
    public static Matrix4f rotZ(float angle) {
    	Matrix4f m = new Matrix4f();
    	double arad = Math.toRadians(angle);
    	float cosa = (float)Math.cos(arad);
    	float sina = (float)Math.sin(arad);
    	m.data[0][0] = cosa;
    	m.data[1][0] = sina;
    	m.data[0][1] = -sina;
    	m.data[1][1] = cosa;
    	return m;
    }
    
    public static Matrix4f rotX(float angle) {
    	Matrix4f m = new Matrix4f();
    	double arad = Math.toRadians(angle);
    	float cosa = (float)Math.cos(arad);
    	float sina = (float)Math.sin(arad);
    	m.data[1][1] = cosa;
    	m.data[2][1] = sina;
    	m.data[1][2] = -sina;
    	m.data[2][2] = cosa;
    	return m;
    }

    public static Matrix4f simpleProjectionMatrix(float dist) {
    	Matrix4f m = new Matrix4f();   	
    	m.set(3, 2, -1.0f/dist);
    	return m;
    }
    
    public static Matrix4f simpleViewport(int w, int h) {
    	Matrix4f m = new Matrix4f();
        m.set(0,0,w/2.0f);
        m.set(1,1,h/2.0f);
        m.set(2,2,1.0f);
        m.set(3,3,1.0f);
        m.set(0,3,w/2.0f);
        m.set(1,3,h/2.0f);
        return m;
    }
}
