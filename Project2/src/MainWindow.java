import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JFrame;

public class MainWindow extends JFrame {
	
	private static final long serialVersionUID = -2997147500624221468L;

	private int windowWidth;
	private int windowHeight;
	private Renderer renderer; 
	private float dThetaY = 3.0f;
	private float thetaY = 0.0f;
	
	public MainWindow(int width, int height, Renderer r) {
		windowWidth = width;
		windowHeight = height;
		renderer = r;
		setSize(width, height);
	}
	
	public void paint(Graphics g) {
		renderer.clearBuffers();
		
        Matrix4f mv = Matrix4f.rotY(thetaY);//new Matrix4f();
        renderer.drawSmoothShading(mv);
//        renderer.drawPoints(mv);
//        renderer.drawGrid(mv);
		Image img = renderer.getImage();
		g.drawImage(img, 0, 0, windowWidth, windowHeight, this);
		
		thetaY = thetaY + dThetaY;
		try {
			Thread.sleep(30);
		} catch (InterruptedException e) {
			repaint();
		}
		repaint();
	}
	
	public static void main(String[] args) {
		int width = 800;
		int height = 800;
		
		TriangleMesh t = new TriangleMesh();
		t.readobj("./test_data/diablo3_pose.obj");
//		t.readobj("./test_data/doraemon.obj");
		
		Renderer r = new Renderer(width, height, t);

		MainWindow mw = new MainWindow(width, height, r);
		mw.setVisible(true);
	}

}
