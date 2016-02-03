package pl.somapro.world3d;

import java.awt.HeadlessException;
import java.awt.Toolkit;

import javax.swing.JFrame;

public class DrawFrame extends JFrame {

	/**
	 * Serial UID
	 */
	private static final long serialVersionUID = 1L;
	private int dw = 1000;
	private int dh = 600;
	
	private int sw = Toolkit.getDefaultToolkit().getScreenSize().width;
	private int sh = Toolkit.getDefaultToolkit().getScreenSize().height;
	
	public DrawFrame(String arg0) throws HeadlessException {
		super(arg0);

		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);		
		this.setSize(dw, dh);
		int lx = (sw - dw)/2;
		int ly = (sh - dh)/2;
		
		this.setLocation(lx, ly);
	}
	
	public int getScreenWidth() {
		return sw;
	}
	
	public int getScreenHeight() {
		return sh;
	}
}
