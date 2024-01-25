import java.awt.Color;
import java.awt.image.BufferedImage;


public class MyImage {
    private int width;
    private int height;
    private Color[] data;

    public MyImage(int w, int h) {
        width = w;
        height = h;
        data = new Color[width*height];
        
        // initially: black background
        for (int i=0; i<(width*height); i++) {
        	data[i] = new Color(0, 0, 0);
        }
    }
    
    public void set(int i, int j, Color val) {
    	data[i + width*j] = val;
    }
    
    public Color get(int i, int j) {
    	return data[i + width*j];
    }
    
    public BufferedImage getImage() {
    	BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    	for (int i=0; i<width; i++) {
    		for (int j=0; j<height; j++) {
    			Color col = get(i, j);
    			bi.setRGB(i, (height-1)-j, col.getRGB());
    		}
    	}
    	return bi;
    }

}
