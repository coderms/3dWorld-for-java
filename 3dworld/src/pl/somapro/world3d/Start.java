package pl.somapro.world3d;

import java.awt.BorderLayout;
import java.awt.CheckboxMenuItem;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.Timer;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Start {

	private static DrawFrame dialog;
	private static DrawPanel panel;
	private static MenuItem menuLoadObject;
	private static CheckboxMenuItem menuAntialiasing;
	
	public static String caption = "3D World";
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		dialog = new DrawFrame(caption);
//		dialog.setLayout(new BorderLayout());
//		dialog.setLayout(new FlowLayout());
//		dialog.setLayout(new GridLayout(2,1));
		
		MenuBar mb = new MenuBar();
		
		Menu menu = new Menu("Object");
		menuLoadObject = new MenuItem("Load object");
		menu.add(menuLoadObject);
		mb.add(menu);
		
		menu = new Menu("Settings");
		menuAntialiasing = new CheckboxMenuItem("Antialiasing");
		menu.add(menuAntialiasing);
		mb.add(menu);
		
		dialog.setMenuBar(mb);
		// ----------------
		
		panel = new DrawPanel(dialog);
		dialog.add(panel);
		dialog.setVisible(true);
		
		setListeners();
		
//		ObjectParser o = new ObjectParser("resources/cube.obj");
//		ObjectParser o = new ObjectParser("resources/cubeRotated.obj");
		ObjectParser o = new ObjectParser("resources/cubeTextured2.obj");
//		ObjectParser o = new ObjectParser("resources/torus2.obj");
		//ObjectParser o = new ObjectParser("resources/torus.obj");
//		ObjectParser o = new ObjectParser("resources/monkey.obj");
//		ObjectParser o = new ObjectParser("resources/demon_head.obj");
//		ObjectParser o = new ObjectParser("resources/spaceship.obj");
//		ObjectParser o = new ObjectParser("resources/genie_lamp.obj");
//		ObjectParser o = new ObjectParser("resources/elephantHigh.obj");
//		ObjectParser o = new ObjectParser("resources/elephantLow.obj");
//		ObjectParser o = new ObjectParser("resources/KingKongBust.obj");
//		ObjectParser o = new ObjectParser("resources/tarkus.obj");
//		ObjectParser o = new ObjectParser("resources/sphereHigh.obj");
//		ObjectParser o = new ObjectParser("resources/sphereLow.obj");
		
		// -------------- Texture objects
		
//		ObjectParser o = new ObjectParser("resources/face.obj");
		//ObjectParser o = new ObjectParser("resources/faceb.obj");
//		ObjectParser o = new ObjectParser("resources/2faces.obj");
//		ObjectParser o = new ObjectParser("resources/cubeTextured.obj");
//		ObjectParser o = new ObjectParser("resources/spaceFrigate.obj");
//		ObjectParser o = new ObjectParser("resources/maison.obj");
//		ObjectParser o = new ObjectParser("resources/chitinous_beast.obj");
//		ObjectParser o = new ObjectParser("resources/heli.obj");
//		ObjectParser o = new ObjectParser("resources/archvile.obj");
//		ObjectParser o = new ObjectParser("resources/demon.obj");
//		ObjectParser o = new ObjectParser("resources/ss-soldier.obj");
//		ObjectParser o = new ObjectParser("resources/house.obj");
//		ObjectParser o = new ObjectParser("resources/LPBuildX12r.obj");
//		ObjectParser o = new ObjectParser("resources/creature/creature3.obj");
//		ObjectParser o = new ObjectParser("resources/trex/Trex.obj");
//		ObjectParser o = new ObjectParser("resources/Snake.obj");
		
//		Manipulator m = new Manipulator(o.getObject());
//		Obj obj = m.Translate(0, 0, 5);
//		panel.setObject(obj);
		panel.setObject(o.getObject());
		
		//panel.updateUI();
		panel.setMode(DrawPanel.MODE_GOURAUD_TEXTURING);
		//panel.calculateFrame();
		//panel.repaint();
//		TestFps(panel);
		Run(panel);
	}
	
	private static void TestFps(DrawPanel comp) {
		MyTimerAction mta = new MyTimerAction(comp);
		
		Timer t = new Timer(-1, mta);
		t.start();
		
		long t1 = System.currentTimeMillis();
		while(t.isRunning()) {
			if (mta.counter == 100)
				t.stop();
		}
		long t2 = System.currentTimeMillis();
		long time = t2-t1;
		
		double seconds = (time / 1000.0) ;
		System.out.println("fps: " + mta.counter / seconds);
	}
	
	private static void Run(DrawPanel comp) {
		MyTimerAction mta = new MyTimerAction(comp);
		
		Timer t = new Timer(-1, mta);
		t.start();

		while(t.isRunning()) {
		}
	}
	
	static class MyTimerAction implements ActionListener {
		
		public int counter = 0;
		
		DrawPanel comp = null;
		
		public MyTimerAction(DrawPanel comp) {
			this.comp = comp;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			comp.calculateFrame();
			//comp.calculateFrame2();
			comp.repaint();
//			comp.invalidate();
			counter++;
		}
	}
	
	private static void setListeners() {
		
		// Dragging listeners ;)
		
		panel.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent mouseevent) {
				// TODO Auto-generated method stub
				panel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
				panel.setDraggingPoint(null);
			}
			
			@Override
			public void mousePressed(MouseEvent mouseevent) {

				panel.setCursor(new Cursor(Cursor.MOVE_CURSOR));
				panel.setDraggingPoint(mouseevent.getPoint());
			}
			
			@Override
			public void mouseExited(MouseEvent mouseevent) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(MouseEvent mouseevent) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseClicked(MouseEvent mouseevent) {
				// TODO Auto-generated method stub
				
			}
		});
		
		panel.addMouseMotionListener(new MouseMotionListener() {
			
			double kw = (dialog.getScreenWidth() / 6.0) / 360.0;
			double kh = (dialog.getScreenHeight() / 3.0) / 360.0;
			
			@Override
			public void mouseMoved(MouseEvent arg0) {
			}
			
			@Override
			public void mouseDragged(MouseEvent arg0) {
				
				Point act_point = arg0.getPoint();
				Point prev_point = panel.getDraggingPoint();
				
				int dx = prev_point.x - act_point.x;
				int dy = prev_point.y - act_point.y;
				int rx = (int) (kw * dx);
				int ry = (int) (kh * dy); 
				
				panel.setRx(panel.getRx()-ry);
				panel.setRy(panel.getRy()+rx);

				panel.setDraggingPoint(act_point);
				
			}
		});
		
		dialog.addComponentListener(new ComponentAdapter() {
            public void componentResized(ComponentEvent e) {
            	panel.updateSize();
            }
        });
		
		menuLoadObject.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent actionevent) {

				JFileChooser chooser = new JFileChooser("./resources");
			    FileNameExtensionFilter filter = new FileNameExtensionFilter("Wavefront file", "obj");
			    chooser.setFileFilter(filter);
			    int returnVal = chooser.showOpenDialog(dialog);
			    
			    if(returnVal == JFileChooser.APPROVE_OPTION) {
			       ObjectParser o = new ObjectParser(chooser.getSelectedFile().getAbsolutePath());
			       
			       if (o.getObject().getTextures().size() == 0)
			    	   panel.setMode(DrawPanel.MODE_PHONG);
			       else
			    	   panel.setMode(DrawPanel.MODE_CORRECT_TEXTURING);
			       
			       panel.setObject(o.getObject());
			    }
			}
		});
		
		menuAntialiasing.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent arg0) {
				if (menuAntialiasing.getState()) {
					panel.setAntialiasing(DrawPanel.AA_2);
				} else 
					panel.setAntialiasing(DrawPanel.AA_NO);
				
				panel.updateSize();
			}
		});
		
		dialog.addKeyListener(new KeyListener() {
			
			@Override
			public void keyTyped(KeyEvent arg0) {
			}
			
			@Override
			public void keyReleased(KeyEvent arg0) {
			}
			
			@Override
			public void keyPressed(KeyEvent arg0) {
				
				double kt = 0.05;
				double kr = 2;
				double ks = 0.01;
				
				int c = arg0.getKeyCode();

				if (c == KeyEvent.VK_UP)
					panel.setTy(panel.getTy()-kt);
				else if (c == KeyEvent.VK_DOWN)
					panel.setTy(panel.getTy()+kt);
				else if (c == KeyEvent.VK_LEFT)
					panel.setTx(panel.getTx()-kt);
				else if (c == KeyEvent.VK_RIGHT)
					panel.setTx(panel.getTx()+kt);
				
				if (c == KeyEvent.VK_W)
					panel.setRx(panel.getRx()-kr);
				else if (c == KeyEvent.VK_S)
					panel.setRx(panel.getRx()+kr);
				else if (c == KeyEvent.VK_A)
					panel.setRy(panel.getRy()-kr);
				else if (c == KeyEvent.VK_D)
					panel.setRy(panel.getRy()+kr);
				
				if (c == KeyEvent.VK_Z)
					panel.setS(panel.getS()-ks);
				else if (c == KeyEvent.VK_C)
					panel.setS(panel.getS()+ks);
				
				if (c == KeyEvent.VK_F1) {
					caption = "3D World - Phong shading";
					panel.setMode(DrawPanel.MODE_PHONG);
				} else if (c == KeyEvent.VK_F2) {
					caption = "3D World - Gouraud shading";
					panel.setMode(DrawPanel.MODE_GOURAUD);
				} else if (c == KeyEvent.VK_F3) {
					caption = "3D World - Flat shading";
					panel.setMode(DrawPanel.MODE_FLAT);
				} else if (c == KeyEvent.VK_F4) {
					caption = "3D World - Wireframe";
					panel.setMode(DrawPanel.MODE_WIREFRAME);
				} else if (c == KeyEvent.VK_F5) {
					caption = "3D World - Affine texturing";
					panel.setMode(DrawPanel.MODE_AFFINE_TEXTURING);
				} else if (c == KeyEvent.VK_F6) {
					caption = "3D World - Perspective texturing";
					panel.setMode(DrawPanel.MODE_CORRECT_TEXTURING);
				} else if (c == KeyEvent.VK_F7) {
					caption = "3D World - Texturing + Gouraud shading";
					panel.setMode(DrawPanel.MODE_GOURAUD_TEXTURING);
				} else if (c == KeyEvent.VK_F8) {
					caption = "3D World - Texturing + Phong shading";
					panel.setMode(DrawPanel.MODE_PHONG_TEXTURING);
				}
			}
		});
	}
}