public class Vec4f {
    public float x;
    public float y;
    public float z;
    public float w;
    
    public Vec4f() {
    	x = 0.0f;
    	y = 0.0f;
    	z = 0.0f;
    	w = 1.0f;
    }
    
    public Vec4f(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }
    
    public float dot(Vec4f other) {
    	return x*other.x + y*other.y + z*other.z + w*other.w;
    }
}