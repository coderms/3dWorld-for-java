package pl.somapro.world3d;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.MemoryImageSource;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;

import pl.somapro.world3d.common.Face;
import pl.somapro.world3d.common.FaceMap;
import pl.somapro.world3d.common.Obj;
import pl.somapro.world3d.common.PointRGB;
import pl.somapro.world3d.common.Texture;
import pl.somapro.world3d.common.TextureCoord;
import pl.somapro.world3d.common.Vertex;

public class DrawPanel extends JPanel {

	// Screen;
	private int w;
	private int h;
	private int w2;
	private int h2;
	
	private Point drag_point;

	// Image buffer
	Image img;
	private int m;
	int[] mem;
	MemoryImageSource mis;

	// Camera
	private double focalLength = 1000;
	private double zoom = 150;

	// Move
	private double tx = 0;
	private double ty = 0;

	// Rotate
	private double rx = 0;
	private double ry = 0;

	// Scale
	private double s = 1;

	double pi180 = Math.PI / 180.0;

	// Object
	private Obj object;
	private Vector<Texture> textures;
	private Vector<TextureCoord> textureCoords;
	private Vector<Vertex> vertices;
	private Vector<Face> faces;

	// Temporary
	// Vector<Face> facesVisible;

	// fps counter
	private long tmp = System.currentTimeMillis();
	private int fps_counter = 0;
	private int update_frequency = 10;
	private double fps = 0;
	private JFrame frame;

	public static final int MODE_PHONG = 0;
	public static final int MODE_GOURAUD = 1;
	public static final int MODE_FLAT = 2;
	public static final int MODE_AFFINE_TEXTURING = 3;
	public static final int MODE_CORRECT_TEXTURING = 4;
	public static final int MODE_GOURAUD_TEXTURING = 5;
	public static final int MODE_PHONG_TEXTURING = 6;
	public static final int MODE_WIREFRAME = 7;

	public static final int AA_NO = 1;
	public static final int AA_2 = 2;
	public static final int AA_4 = 4;

	private int mode;
	private int aa;

	AffineTransform transform = null;

	public DrawPanel(JFrame frame) {
		super();
		updateSize();

		this.frame = frame;
		this.mode = MODE_PHONG;
		this.aa = AA_NO;
	}

	public void updateSize() {
		Dimension size = getSize();
		Insets insets = getInsets();

		w = size.width - insets.left - insets.right;
		h = size.height - insets.top - insets.bottom;

		if (aa == AA_NO) {
			transform = null;
		} else if (aa == AA_2) {
			w = w * 2;
			h = h * 2;

			transform = new AffineTransform();
			transform.scale(0.5, 0.5);
		} else if (aa == AA_4) {
			w = w * 4;
			h = h * 4;

			transform = new AffineTransform();
			transform.scale(0.25, 0.25);
		}

		w2 = w / 2;
		h2 = h / 2;

		mem = new int[w * h];
		m = w * h;
		mis = new MemoryImageSource(w, h, mem, 0, w);
		mis.setAnimated(true);
		// mis.setFullBufferUpdates(true);

		img = createImage(mis);
		// System.out.println("width: " + w);
		// System.out.println("height: " + h);
	}

	public int[] getVideoMemory() {
		return mem;
	}

	private void loadObject() {
		textures = object.getTextures();
		textureCoords = object.getTextureCoords();
		vertices = object.getVertices();
		faces = object.getFaces();

		// facesVisible = new Vector();
	}

	public int getVideoSize() {
		return m;
	}

	public void newData() {
		mis.newPixels();
	}

	@Override
	public void repaint() {
		// TODO Auto-generated method stub

		super.repaint();
	}

	@Override
	public void paintComponent(Graphics g) {

		super.paintComponent(g);

	}
	
	public void setDraggingPoint(Point p) {
		drag_point = p;
	}

	public Point getDraggingPoint() {
		return drag_point;
	}

	public void calculateFrame() {
		if (object == null)
			return;

		Vector verticesTranslated = new Vector();
		Vector points = new Vector();

		double cosrx = Math.cos(rx * pi180);
		double sinrx = Math.sin(rx * pi180);

		double cosry = Math.cos(ry * pi180);
		double sinry = Math.sin(ry * pi180);

		// Clear frame
		int bg = (255 << 24) | (200 << 16) | (200 << 8) | 200;
		Arrays.fill(mem, bg);
		// -----------

		// Vertex to screen projection
		for (int i = 0; i < vertices.size(); i++) {
			Vertex v = (Vertex) vertices.get(i);

			// X rotation
			double xp = v.getX();
			double yp = v.getY() * cosrx - v.getZ() * sinrx;
			double zp = v.getY() * sinrx + v.getZ() * cosrx;

			// Y rotation
			double xp2 = zp * sinry + xp * cosry;
			double yp2 = yp;
			double zp2 = zp * cosry - xp * sinry;

			// Translation
			xp2 = xp2 + tx;
			yp2 = yp2 + ty;
			zp2 = zp2;

			// Scale
			xp2 = xp2 * s;
			yp2 = yp2 * s;
			zp2 = zp2 * s;

			Vertex vt = new Vertex(xp2, yp2, zp2);
			vt.setNormal(new Vertex(0, 0, 0));
			verticesTranslated.add(vt);

			double t = focalLength / (focalLength + zp2 * zoom);
			//
			int x = (int) (xp2 * zoom * t) * aa + w2;
			int y = (int) (yp2 * zoom * t) * aa + h2;

			// int x = (int) (xp2 / zp2) + w2;
			// int y = (int) (yp2 / zp2) + h2;
			//
			// int c = (int) (-(zp2 + 0.5) * 255.0);
			// if (c < 0)
			// c = 0;
			// if (c > 255)
			// c = 255;
			points.add(new PointRGB(x, y));
		}

		// Faces calculations
		// facesVisible.clear();
		for (int i = 0; i < faces.size(); i++) {
			Face f = (Face) faces.get(i);

			Vertex v1 = (Vertex) verticesTranslated.get(f.getV1() - 1);
			Vertex v2 = (Vertex) verticesTranslated.get(f.getV2() - 1);
			Vertex v3 = (Vertex) verticesTranslated.get(f.getV3() - 1);

			PointRGB p1 = (PointRGB) points.get(f.getV1() - 1);
			PointRGB p2 = (PointRGB) points.get(f.getV2() - 1);
			PointRGB p3 = (PointRGB) points.get(f.getV3() - 1);
			// double cross = calcCross(v1, v2, v3);
			// f.setVisible(cross >= 0);

			// double backFace = determineBackFace(v1, v2, v3);
			double backFace = determineBackFace(p1, p2, p3);
			f.setVisible(backFace < 0);
			f.calculateZvars(v1.getZ(), v2.getZ(), v3.getZ());

			// Phong
			if (mode == MODE_PHONG || mode == MODE_GOURAUD || mode == MODE_FLAT) {
				Vertex fn = calcFaceNormal(v1, v2, v3);
				// Vertex fn = calcFaceNormal2(v1, v2, v3);
				f.setNormal(fn);
				// Vertex normals
				if (mode == MODE_PHONG || mode == MODE_GOURAUD) {
					v1.addToNormal(fn);
					v2.addToNormal(fn);
					v3.addToNormal(fn);
				}
			}
			// -----------------------
		}

		// Sort
		Collections.sort(faces);

		int tmpMode = mode;

		// Draw faces
		for (int i = 0; i < faces.size(); i++) {
			Face f = (Face) faces.get(i);

			//System.out.println(i + " " + f.isVisible());
			
			if (f.isVisible()) {
				PointRGB p1 = (PointRGB) points.get(f.getV1() - 1);
				PointRGB p2 = (PointRGB) points.get(f.getV2() - 1);
				PointRGB p3 = (PointRGB) points.get(f.getV3() - 1);

				FaceMap fm = f.getFaceMap();

				if (fm == null
						&& (mode == MODE_AFFINE_TEXTURING
								|| mode == MODE_CORRECT_TEXTURING
								|| mode == MODE_GOURAUD_TEXTURING || mode == MODE_PHONG_TEXTURING))
					tmpMode = MODE_FLAT;
				else
					tmpMode = mode;

				switch (tmpMode) {
				case MODE_PHONG:
					Vertex v1 = (Vertex) verticesTranslated.get(f.getV1() - 1);
					Vertex v2 = (Vertex) verticesTranslated.get(f.getV2() - 1);
					Vertex v3 = (Vertex) verticesTranslated.get(f.getV3() - 1);
					Vertex n1 = v1.getNormal();
					Vertex n2 = v2.getNormal();
					Vertex n3 = v3.getNormal();
					drawFacePhong(p1, p2, p3, n1, n2, n3);
					break;
				case MODE_GOURAUD:
					Vertex gv1 = (Vertex) verticesTranslated.get(f.getV1() - 1);
					Vertex gv2 = (Vertex) verticesTranslated.get(f.getV2() - 1);
					Vertex gv3 = (Vertex) verticesTranslated.get(f.getV3() - 1);
					int gn1 = (int) (-255.0 * gv1.getNormal().getZ());
					int gn2 = (int) (-255.0 * gv2.getNormal().getZ());
					int gn3 = (int) (-255.0 * gv3.getNormal().getZ());
					if (gn1 < 0)
						gn1 = 0;
					if (gn2 < 0)
						gn2 = 0;
					if (gn3 < 0)
						gn3 = 0;
					drawFaceGouraud(p1, p2, p3, gn1, gn2, gn3);
					break;
				case MODE_FLAT:
					int c = (int) (-255.0 * f.getNormal().getZ());
					if (c < 0)
						c = 0;
					drawFaceFlat(p1, p2, p3, c);
					break;
				case MODE_WIREFRAME:
					drawFaceWireframe(p1, p2, p3, 0);
					break;
				case MODE_AFFINE_TEXTURING:
					if (fm != null)
						drawTexturedFace(p1, p2, p3, fm);
					break;
				case MODE_CORRECT_TEXTURING:
					if (fm != null)
						drawTexturedFaceCorrect(p1, p2, p3, f.getZ1(),
								f.getZ2(), f.getZ3(), fm);
					break;
				case MODE_GOURAUD_TEXTURING:
					if (fm != null)
						drawTexturedFaceLight(p1, p2, p3, f.getZ1(), f.getZ2(),
								f.getZ3(), fm);
					break;
				case MODE_PHONG_TEXTURING:
					if (fm != null)
						drawTexturedFaceLight(p1, p2, p3, f.getZ1(), f.getZ2(),
								f.getZ3(), fm);
				}
			}
		}
		mis.newPixels();

		fps_counter++;
		if (fps_counter == update_frequency) {
			fps = ((double) update_frequency)
					/ (((double) (System.currentTimeMillis() - tmp)) / 1000.0);
			tmp = System.currentTimeMillis();
			fps_counter = 0;

			frame.setTitle(Start.caption + " - Fps: " + fps);
		}
	}
	
	public void calculateFrame2() {

		// Clear frame
		int bg = (255 << 24) | (200 << 16) | (200 << 8) | 200;
		Arrays.fill(mem, bg);

		// Faces calculations
		for (int x = 0; x < 256; x++) {
			for (int y = 0; y < 256; y++) {
				double k = 180.0/127.0;
				double vy = y * k;
				double vx = x * k;
				int c = (int) ((Math.sin(vy*pi180) - Math.cos(vx*pi180))*256);
				mem[w*x + y] = (255 << 24) | (c << 16) | (c << 8) | c;
			}
		}
		mis.newPixels();

	}

	@Override
	public void paint(Graphics g) {

		Graphics2D g2d = (Graphics2D) g;
		if (transform != null) {
			// g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
			// RenderingHints.VALUE_ANTIALIAS_ON);
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					RenderingHints.VALUE_INTERPOLATION_BILINEAR);
			// g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
			// RenderingHints.VALUE_INTERPOLATION_BICUBIC);

			g2d.setTransform(transform);
		}

		g2d.drawImage(img, 0, 0, null);
		// g2d.drawImage(img, xform, null);
	}

	public void setObject(Obj object) {
		this.object = object;

		loadObject();
	}

	private double determineBackFace(Vertex v1, Vertex v2, Vertex v3) {
		double d1x = v3.getX() - v1.getX();
		double d1y = v3.getY() - v1.getY();
		double d2x = v3.getX() - v2.getX();
		double d2y = v3.getY() - v2.getY();

		double z = (d1x * d2y) - (d1y * d2x);

		return z;
	}

	private double determineBackFace(PointRGB p1, PointRGB p2, PointRGB p3) {
		double d1x = p3.getX() - p1.getX();
		double d1y = p3.getY() - p1.getY();
		double d2x = p3.getX() - p2.getX();
		double d2y = p3.getY() - p2.getY();

		double z = (d1x * d2y) - (d1y * d2x);

		return z;
	}

	private double calcCross(Vertex v1, Vertex v2, Vertex v3) {
		Vertex cameraToFace = new Vertex(-v1.getX(), -v1.getY(), -150
				+ v1.getZ());

		Vertex ab = new Vertex(v2.getX() - v1.getX(), v2.getY() - v1.getY(),
				v2.getZ() - v1.getZ());
		Vertex cb = new Vertex(v3.getX() - v2.getX(), v3.getY() - v2.getY(),
				v3.getZ() - v2.getZ());

		Vertex faceNormal = new Vertex(cb.getY() * ab.getZ() - cb.getZ()
				* ab.getY(), cb.getZ() * ab.getX() - cb.getX() * ab.getZ(),
				cb.getX() * ab.getY() - cb.getY() * ab.getX());

		return (cameraToFace.getX() * faceNormal.getX() + cameraToFace.getY()
				* faceNormal.getY() - cameraToFace.getZ() * faceNormal.getZ());
	}

	private double calcDot(Vertex v1, Vertex v2) {
		return (v1.getX() * v2.getX() + v1.getY() * v2.getY() - v1.getZ()
				* v2.getZ());
	}

	private Vertex calcNormal(Vertex v1, Vertex v2) {
		double x = v1.getX() - v2.getX();
		double y = v1.getY() - v2.getY();
		double z = v1.getZ() - v2.getZ();

		// System.out.println("X = " + x + ", Y = " + y + ", Z = " + z);

		double sx = Math.abs(1.0 / x);
		double sy = Math.abs(1.0 / y);
		double sz = Math.abs(1.0 / z);

		// double sx = Math.abs(x);
		// double sy = Math.abs(y);
		// double sz = Math.abs(z);
		//
		//
		double s = Math.max(Math.max(sx, sy), sz);
		//
		Vertex v = new Vertex(x / s, y / s, z / s);

		// Vertex v = new Vertex(x, y, z);

		// System.out.println(v);

		return v;
	}

	private Vertex calcFaceNormal(Vertex v1, Vertex v2, Vertex v3) {
		double normx = (v1.getZ() - v2.getZ()) * (v3.getY() - v2.getY())
				- (v1.getY() - v2.getY()) * (v3.getZ() - v2.getZ());
		double normy = (v1.getX() - v2.getX()) * (v3.getZ() - v2.getZ())
				- (v1.getZ() - v2.getZ()) * (v3.getX() - v2.getX());
		double normz = (v1.getY() - v2.getY()) * (v3.getX() - v2.getX())
				- (v1.getX() - v2.getX()) * (v3.getY() - v2.getY());

		double normlength = Math.sqrt(Math.pow(normx, 2) + Math.pow(normy, 2)
				+ Math.pow(normz, 2));

		normx /= normlength;
		normy /= normlength;
		normz /= normlength;

		return new Vertex(normx, normy, normz);
	}

	private Vertex calcFaceNormal2(Vertex v1, Vertex v2, Vertex v3) {
		double normx = (v1.getZ() - v2.getZ()) * (v3.getY() - v2.getY())
				- (v1.getY() - v2.getY()) * (v3.getZ() - v2.getZ());
		double normy = (v1.getX() - v2.getX()) * (v3.getZ() - v2.getZ())
				- (v1.getZ() - v2.getZ()) * (v3.getX() - v2.getX());
		double normz = (v1.getY() - v2.getY()) * (v3.getX() - v2.getX())
				- (v1.getX() - v2.getX()) * (v3.getY() - v2.getY());

		double sx = Math.abs(1.0 / normx);
		double sy = Math.abs(1.0 / normy);
		double sz = Math.abs(1.0 / normz);

		double s = Math.min(Math.min(sx, sy), sz);

		return new Vertex(normx * s, normy * s, normz * s);
	}

	private Vertex normalize(Vertex v) {
		double normx = v.getX();
		double normy = v.getY();
		double normz = v.getZ();

		double normlength = Math.sqrt(Math.pow(normx, 2) + Math.pow(normy, 2)
				+ Math.pow(normz, 2));

		normx /= normlength;
		normy /= normlength;
		normz /= normlength;

		return new Vertex(normx, normy, normz);
	}

	public void drawPoint(int x, int y) {
		int id = w * y + x;

		int cr = (int) 255;
		int cg = (int) 0;
		int cb = (int) 0;

		mem[id] = (255 << 24) | (cr << 16) | (cg << 8) | cb;
		mis.newPixels();
	}

	public void drawFace(Graphics2D g2d, Color c, PointRGB p1, PointRGB p2,
			PointRGB p3) {
		g2d.setColor(c);
		g2d.drawLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
		g2d.drawLine(p2.getX(), p2.getY(), p3.getX(), p3.getY());
		g2d.drawLine(p3.getX(), p3.getY(), p1.getX(), p1.getY());
	}

	public void drawFace(Graphics2D g2d, PointRGB p1, PointRGB p2, PointRGB p3,
			int c) {
		int c2 = 255 << 24 | c << 16 | c << 8 | c;

		g2d.setColor(new Color(c2));

		int x[] = { p1.getX(), p2.getX(), p3.getX() };
		int y[] = { p1.getY(), p2.getY(), p3.getY() };
		g2d.fillPolygon(x, y, 3);
	}

	public void drawFaceGouraud(PointRGB p1, PointRGB p2, PointRGB p3, int c1,
			int c2, int c3) {
		if (p1.getY() > p2.getY()) {
			PointRGB tp = p2;
			p2 = p1;
			p1 = tp;

			int tc = c2;
			c2 = c1;
			c1 = tc;
		}

		if (p2.getY() > p3.getY()) {
			PointRGB tp = p3;
			p3 = p2;
			p2 = tp;

			int tc = c3;
			c3 = c2;
			c2 = tc;
		}

		if (p1.getY() > p2.getY()) {
			PointRGB tp = p2;
			p2 = p1;
			p1 = tp;

			int tc = c2;
			c2 = c1;
			c1 = tc;
		}

		int x1 = p1.getX();
		int y1 = p1.getY();

		int x2 = p2.getX();
		int y2 = p2.getY();

		int x3 = p3.getX();
		int y3 = p3.getY();

		if (y1 > h)
			return;
		if (y3 < 0)
			return;

		int cshifted = 255 << 24;

		// Wspó³czynniki dla X
		double xkp = 0;
		double xkl = 0;

		// Wspó³czynniki dla koloru
		double ckp = 0;
		double ckl = 0;

		double xl = x1;
		double xp = x1;

		double cl = c1;
		double cp = c1;

		double y21 = y2 - y1;
		double y31 = y3 - y1;
		double y32 = y3 - y2;

		double xk21 = (x2 - x1) / y21;
		double xk31 = (x3 - x1) / y31;
		double xk32 = (x3 - x2) / y32;
		double ck21 = (c2 - c1) / y21;
		double ck31 = (c3 - c1) / y31;
		double ck32 = (c3 - c2) / y32;

		if (y21 != 0) {
			if (xk21 > xk31) {
				xkl = xk31;
				xkp = xk21;

				ckl = ck31;
				ckp = ck21;
			} else {
				xkl = xk21;
				xkp = xk31;

				ckl = ck21;
				ckp = ck31;
			}

			// Clipping
			int id = 0;

			if (y1 >= 0)
				id = w * y1;
			else {
				xl = xl - xkl * y1;
				xp = xp - xkp * y1;

				cl = cl - ckl * y1;
				cp = cp - ckp * y1;

				y21 = y21 + y1;
				y31 = y31 + y1;
			}

			if (y2 > h)
				y21 = h - y1;
			// --- Clipping
			int cr = 0;
			for (int i = 0; i < y21; i++) {
				if (xp >= 0 && xl < w) {
					double ck = (cp - cl) / (xp - xl);
					double c = cl;

					double xp_loop = xp;
					double xl_loop = xl;

					if (xp > w)
						xp_loop = w;
					if (xl < 0) {
						xl_loop = 0;
						c = c - ck * xl;
					}

					for (int j = (int) xl_loop; j < xp_loop; j++) {
						cr = (int) c;

						mem[id + j] = cshifted | (cr << 16) | (cr << 8) | cr;

						c = c + ck;

						if (c < 0)
							c = 0;
						if (c > 255)
							c = 255;
					}
				}

				id = id + w;

				xl = xl + xkl;
				xp = xp + xkp;

				cl = cl + ckl;
				cp = cp + ckp;
			}

			if (xk21 > xk31) {
				xkp = xk32;
				ckp = ck32;
			} else {
				xkl = xk32;
				ckl = ck32;
			}

		} else {
			if (x1 < x2) {
				xp = x2;

				cp = c2;

				xkl = xk31;
				ckl = ck31;

				xkp = xk32;
				ckp = ck32;

			} else {
				xl = x2;

				cl = c2;

				xkl = xk32;
				ckl = ck32;

				xkp = xk31;
				ckp = ck31;
			}
		}

		int id = 0;

		if (y1 >= 0)
			id = (int) (w * (y1 + y21));
		else {
			id = (int) (w * y21);
		}

		if (y2 < 0) {
			id = 0;

			if (xk31 < xk32) {
				xkl = xk32;
				xkp = xk31;

				ckl = ck32;
				ckp = ck31;

				xl = x2 - xkl * y2;
				xp = x1 - xkp * y1;

				cl = c2 - ckl * y2;
				cp = c1 - ckp * y1;
			} else {
				xkl = xk31;
				xkp = xk32;

				ckl = ck31;
				ckp = ck32;

				xl = x1 - xkl * y1;
				xp = x2 - xkp * y2;

				cl = c1 - ckl * y1;
				cp = c2 - ckp * y2;
			}

			y31 = y31 + y2;
		}

		if (y3 > h)
			y31 = y31 - (y3 - h);
		// --- Clipping
		int cr = 0;
		for (int i = (int) y21; i < y31; i++) {
			if (xp >= 0 && xl < w) {
				double ck = (cp - cl) / (xp - xl);
				double c = cl;

				double xp_loop = xp;
				double xl_loop = xl;

				if (xp > w)
					xp_loop = w;
				if (xl < 0) {
					xl_loop = 0;
					c = c - ck * xl;
				}
				for (int j = (int) xl_loop; j < xp_loop; j++) {
					cr = (int) c;

					mem[id + j] = cshifted | (cr << 16) | (cr << 8) | cr;

					c = c + ck;

					if (c < 0)
						c = 0;
					if (c > 255)
						c = 255;
				}
			}
			id = id + w;

			xl = xl + xkl;
			xp = xp + xkp;

			cl = cl + ckl;
			cp = cp + ckp;
		}
	}

	public void drawLine(PointRGB p1, PointRGB p2, int c) {

		int x1 = p1.getX();
		int y1 = p1.getY();

		int x2 = p2.getX();
		int y2 = p2.getY();

		if (y1 > h)
			return;
		if (y2 < 0)
			return;

		int cshifted = 255 << 24 | (c << 16) | (c << 8) | c;

		// Wspó³czynniki dla X
		double xkp = 0;
		double xkl = 0;

		double xl = x1;
		double xp = x2;

		double y21 = y2 - y1;

		double xk21 = (x2 - x1) / y21;

		int id = w * y1;

		for (int i = 0; i < y21; i++) {
			mem[id + (int) xl] = cshifted;
			id = id + w;

			xl = xl + xk21;
		}
	}

	public void drawFaceFlat(PointRGB p1, PointRGB p2, PointRGB p3, int c) {

		if (p1.getY() > p2.getY()) {
			PointRGB tp = p2;
			p2 = p1;
			p1 = tp;
		}

		if (p2.getY() > p3.getY()) {
			PointRGB tp = p3;
			p3 = p2;
			p2 = tp;
		}

		if (p1.getY() > p2.getY()) {
			PointRGB tp = p2;
			p2 = p1;
			p1 = tp;
		}

		int x1 = p1.getX();
		int y1 = p1.getY();

		int x2 = p2.getX();
		int y2 = p2.getY();

		int x3 = p3.getX();
		int y3 = p3.getY();

		if (y1 > h)
			return;
		if (y3 < 0)
			return;
		if (x1 > w && x2 > w && x3 > w)
			return;
		if (x1 < 0 && x2 < 0 && x3 < 0)
			return;

		int cshifted = 255 << 24 | (c << 16) | (c << 8) | c;

		// Wspó³czynniki dla X
		double xkp = 0;
		double xkl = 0;

		double xl = x1;
		double xp = x1;

		double y21 = y2 - y1;
		double y31 = y3 - y1;
		double y32 = y3 - y2;

		double xk21 = (x2 - x1) / y21;
		double xk31 = (x3 - x1) / y31;
		double xk32 = (x3 - x2) / y32;

		if (y21 != 0) {
			if (xk21 > xk31) {
				xkl = xk31;
				xkp = xk21;
			} else {
				xkl = xk21;
				xkp = xk31;
			}

			// Clipping
			int id = 0;

			if (y1 >= 0)
				id = w * y1;
			else {
				xl = xl - xkl * y1;
				xp = xp - xkp * y1;

				y21 = y21 + y1;
				y31 = y31 + y1;
			}

			if (y2 > h)
				y21 = h - y1;
			// --- Clipping

			for (int i = 0; i < y21; i++) {
				if (xp >= 0 && xl < w) {
					double xp_loop = xp;
					double xl_loop = xl;

					if (xp > w)
						xp_loop = w;
					if (xl < 0)
						xl_loop = 0;

					for (int j = (int) xl_loop; j < xp_loop; j++)
						mem[id + j] = cshifted;
				}

				id = id + w;

				xl = xl + xkl;
				xp = xp + xkp;
			}

			if (xk21 > xk31) {
				xkp = xk32;
			} else {
				xkl = xk32;
			}

		} else {
			if (x1 < x2) {
				xp = x2;
				xkl = xk31;
				xkp = xk32;
			} else {
				xl = x2;
				xkl = xk32;
				xkp = xk31;
			}
		}

		int id = 0;

		if (y1 >= 0)
			id = (int) (w * (y1 + y21));
		else {
			id = (int) (w * y21);
		}

		if (y2 < 0) {
			id = 0;

			if (xk31 < xk32) {
				xkl = xk32;
				xkp = xk31;
				xl = x2 - xkl * y2;
				xp = x1 - xkp * y1;
			} else {
				xkl = xk31;
				xkp = xk32;
				xl = x1 - xkl * y1;
				xp = x2 - xkp * y2;
			}

			y31 = y31 + y2;
		}

		if (y3 > h)
			y31 = y31 - (y3 - h);
		// --- Clipping

		for (int i = (int) y21; i < y31; i++) {
			if (xp >= 0 && xl < w) {
				double xp_loop = xp;
				double xl_loop = xl;

				if (xp > w)
					xp_loop = w;
				if (xl < 0)
					xl_loop = 0;

				for (int j = (int) xl_loop; j < xp_loop; j++)
					mem[id + j] = cshifted;
			}
			id = id + w;

			xl = xl + xkl;
			xp = xp + xkp;
		}
	}

	public void drawFaceWireframe(PointRGB p1, PointRGB p2, PointRGB p3, int c) {

		if (p1.getY() > p2.getY()) {
			PointRGB tp = p2;
			p2 = p1;
			p1 = tp;
		}

		if (p2.getY() > p3.getY()) {
			PointRGB tp = p3;
			p3 = p2;
			p2 = tp;
		}

		if (p1.getY() > p2.getY()) {
			PointRGB tp = p2;
			p2 = p1;
			p1 = tp;
		}

		int x1 = p1.getX();
		int y1 = p1.getY();

		int x2 = p2.getX();
		int y2 = p2.getY();

		int x3 = p3.getX();
		int y3 = p3.getY();

		if (y1 > h)
			return;
		if (y3 < 0)
			return;

		int cshifted = 255 << 24 | (c << 16) | (c << 8) | c;

		// Wspó³czynniki dla X
		double xkp = 0;
		double xkl = 0;

		double xl = x1;
		double xp = x1;

		double y21 = y2 - y1;
		double y31 = y3 - y1;
		double y32 = y3 - y2;

		double xk21 = (x2 - x1) / y21;
		double xk31 = (x3 - x1) / y31;
		double xk32 = (x3 - x2) / y32;

		if (y21 != 0) {
			if (xk21 > xk31) {
				xkl = xk31;
				xkp = xk21;
			} else {
				xkl = xk21;
				xkp = xk31;
			}

			// Clipping
			int id = 0;

			if (y1 >= 0)
				id = w * y1;
			else {
				xl = xl - xkl * y1;
				xp = xp - xkp * y1;

				y21 = y21 + y1;
				y31 = y31 + y1;
			}

			if (y2 > h)
				y21 = h - y1;
			// --- Clipping

			for (int i = 0; i < y21; i++) {
				mem[id + (int) xl] = cshifted;
				mem[id + (int) xp] = cshifted;

				id = id + w;

				xl = xl + xkl;
				xp = xp + xkp;
			}

			if (xk21 > xk31) {
				xkp = xk32;
			} else {
				xkl = xk32;
			}

		} else {
			if (x1 < x2) {
				xp = x2;
				xkl = xk31;
				xkp = xk32;
			} else {
				xl = x2;
				xkl = xk32;
				xkp = xk31;
			}
		}

		int id = 0;

		if (y1 >= 0)
			id = (int) (w * (y1 + y21));
		else {
			id = (int) (w * y21);
		}

		if (y2 < 0) {
			id = 0;

			if (xk31 < xk32) {
				xkl = xk32;
				xkp = xk31;
				xl = x2 - xkl * y2;
				xp = x1 - xkp * y1;
			} else {
				xkl = xk31;
				xkp = xk32;
				xl = x1 - xkl * y1;
				xp = x2 - xkp * y2;
			}

			y31 = y31 + y2;
		}

		if (y3 > h)
			y31 = y31 - (y3 - h);
		// --- Clipping

		for (int i = (int) y21; i < y31; i++) {
			mem[id + (int) xl] = cshifted;
			mem[id + (int) xp] = cshifted;

			id = id + w;

			xl = xl + xkl;
			xp = xp + xkp;
		}
	}

	public void drawTexturedFace(PointRGB p1, PointRGB p2, PointRGB p3,
			FaceMap tm) {

		int tp1 = tm.getTc1();
		int tp2 = tm.getTc2();
		int tp3 = tm.getTc3();

		if (p1.getY() > p2.getY()) {
			PointRGB tp = p2;
			p2 = p1;
			p1 = tp;

			int tmp = tp2;
			tp2 = tp1;
			tp1 = tmp;
		}

		if (p2.getY() > p3.getY()) {
			PointRGB tp = p3;
			p3 = p2;
			p2 = tp;

			int tmp = tp3;
			tp3 = tp2;
			tp2 = tmp;
		}

		if (p1.getY() > p2.getY()) {
			PointRGB tp = p2;
			p2 = p1;
			p1 = tp;

			int tmp = tp2;
			tp2 = tp1;
			tp1 = tmp;
		}

		Texture t = textures.get(tm.getTextureId() - 1);

		int tmem[][] = t.getMem();

		int tw = t.getW() - 1;
		int th = t.getH() - 1;

		int x1 = p1.getX();
		int y1 = p1.getY();
		double tx1 = textureCoords.get(tp1 - 1).getX1() * tw;
		double ty1 = th - textureCoords.get(tp1 - 1).getY1() * th;

		int x2 = p2.getX();
		int y2 = p2.getY();
		double tx2 = textureCoords.get(tp2 - 1).getX1() * tw;
		double ty2 = th - textureCoords.get(tp2 - 1).getY1() * th;

		int x3 = p3.getX();
		int y3 = p3.getY();
		double tx3 = textureCoords.get(tp3 - 1).getX1() * tw;
		double ty3 = th - textureCoords.get(tp3 - 1).getY1() * th;

		if (y1 > h)
			return;
		if (y3 < 0)
			return;
		if (x1 > w && x2 > w && x3 > w)
			return;
		if (x1 < 0 && x2 < 0 && x3 < 0)
			return;

		// Wspó³czynniki dla X
		double xkp = 0;
		double xkl = 0;

		// Wspó³czynniki dla tekstury
		double tkxp = 0;
		double tkyp = 0;

		double tkxl = 0;
		double tkyl = 0;

		double xl = x1;
		double xp = x1;

		double txl = tx1;
		double tyl = ty1;
		double txp = tx1;
		double typ = ty1;

		double y32 = y3 - y2;
		double y31 = y3 - y1;
		double y21 = y2 - y1;

		double xk21 = (x2 - x1) / y21;
		double xk31 = (x3 - x1) / y31;
		double xk32 = (x3 - x2) / y32;

		double tkx21 = (tx2 - tx1) / y21;
		double tky21 = (ty2 - ty1) / y21;
		double tkx31 = (tx3 - tx1) / y31;
		double tky31 = (ty3 - ty1) / y31;
		double tkx32 = (tx3 - tx2) / y32;
		double tky32 = (ty3 - ty2) / y32;

		if (y21 != 0) {
			if (xk21 > xk31) {
				xkl = xk31;
				xkp = xk21;

				tkxl = tkx31;
				tkyl = tky31;

				tkxp = tkx21;
				tkyp = tky21;
			} else {
				xkl = xk21;
				xkp = xk31;

				tkxl = tkx21;
				tkyl = tky21;

				tkxp = tkx31;
				tkyp = tky31;
			}

			// Clipping
			int id = 0;

			if (y1 >= 0)
				id = w * y1;
			else {
				xl = xl - xkl * y1;
				xp = xp - xkp * y1;

				txl = txl - tkxl * y1;
				tyl = tyl - tkyl * y1;

				txp = txp - tkxp * y1;
				typ = typ - tkyp * y1;

				y21 = y21 + y1;
				y31 = y31 + y1;
			}

			if (y2 > h)
				y21 = h - y1;
			// --- Clipping

			for (int i = 0; i < y21; i++) {
				if (xp >= 0 && xl < w) {
					double tkx = (txp - txl) / (double) (xp - xl);
					double tky = (typ - tyl) / (double) (xp - xl);
					double tx = txl;
					double ty = tyl;

					double xp_loop = xp;
					double xl_loop = xl;

					if (xp > w)
						xp_loop = w;
					if (xl < 0) {
						xl_loop = 0;
						tx = tx - tkx * xl;
						ty = ty - tky * xl;
					}

					for (int j = (int) xl_loop; j < xp_loop; j++) {
						mem[id + j] = tmem[(int) tx][(int) ty];

						tx = tx + tkx;
						ty = ty + tky;

						if (tx < 0 || ty < 0 || tx > tw || ty > th)
							break;
					}
				}
				id = id + w;

				xl = xl + xkl;
				xp = xp + xkp;

				txl = txl + tkxl;
				tyl = tyl + tkyl;
				txp = txp + tkxp;
				typ = typ + tkyp;
			}

			if (xk21 > xk31) {
				xkp = xk32;
				tkxp = tkx32;
				tkyp = tky32;
			} else {
				xkl = xk32;
				tkxl = tkx32;
				tkyl = tky32;
			}

		} else {
			if (x1 < x2) {
				xp = x2;

				txp = tx2;
				typ = ty2;

				xkl = xk31;
				tkxl = tkx31;
				tkyl = tky31;

				xkp = xk32;
				tkxp = tkx32;
				tkyp = tky32;

			} else {
				xl = x2;

				txl = tx2;
				tyl = ty2;

				xkl = xk32;
				tkxl = tkx32;
				tkyl = tky32;

				xkp = xk31;
				tkxp = tkx31;
				tkyp = tky31;
			}
		}

		// Clipping
		int id = 0;

		if (y1 >= 0)
			id = (int) (w * (y1 + y21));
		else {
			id = (int) (w * y21);
		}

		if (y2 < 0) {
			id = 0;

			if (xk31 < xk32) {
				xkl = xk32;
				xkp = xk31;

				tkxl = tkx32;
				tkxp = tkx31;

				xl = x2 - xkl * y2;
				xp = x1 - xkp * y1;

				txl = tx2 - tkxl * y2;
				tyl = ty2 - tkyl * y2;

				txp = tx1 - tkxp * y1;
				typ = ty1 - tkyp * y1;
			} else {
				xkl = xk31;
				xkp = xk32;

				tkxl = tkx31;
				tkxp = tkx32;

				xl = x1 - xkl * y1;
				xp = x2 - xkp * y2;

				txl = tx1 - tkxl * y1;
				tyl = ty1 - tkyl * y1;

				txp = tx2 - tkxp * y2;
				typ = ty2 - tkyp * y2;
			}

			y31 = y31 + y2;
		}

		if (y3 > h)
			y31 = y31 - (y3 - h);
		// --- Clipping

		for (int i = (int) y21; i < y31; i++) {
			if (xp >= 0 && xl < w) {
				double tkx = (txp - txl) / (double) (xp - xl);
				double tky = (typ - tyl) / (double) (xp - xl);
				double tx = txl;
				double ty = tyl;

				double xp_loop = xp;
				double xl_loop = xl;

				if (xp > w)
					xp_loop = w;
				if (xl < 0) {
					xl_loop = 0;
					tx = tx - tkx * xl;
					ty = ty - tky * xl;
				}
				for (int j = (int) xl_loop; j < xp_loop; j++) {
					mem[id + j] = tmem[(int) tx][(int) ty];

					tx = tx + tkx;
					ty = ty + tky;

					if (tx < 0 || ty < 0 || tx > tw || ty > th)
						break;
				}
			}
			id = id + w;

			xl = xl + xkl;
			xp = xp + xkp;

			txl = txl + tkxl;
			tyl = tyl + tkyl;
			txp = txp + tkxp;
			typ = typ + tkyp;
		}
	}

	public void drawTexturedFaceCorrect(PointRGB p1, PointRGB p2, PointRGB p3,
			double z1, double z2, double z3, FaceMap tm) {

		int tp1 = tm.getTc1();
		int tp2 = tm.getTc2();
		int tp3 = tm.getTc3();

		if (p1.getY() > p2.getY()) {
			PointRGB tp = p2;
			p2 = p1;
			p1 = tp;

			int tmp = tp2;
			tp2 = tp1;
			tp1 = tmp;

			double tmpz = z2;
			z2 = z1;
			z1 = tmpz;
		}

		if (p2.getY() > p3.getY()) {
			PointRGB tp = p3;
			p3 = p2;
			p2 = tp;

			int tmp = tp3;
			tp3 = tp2;
			tp2 = tmp;

			double tmpz = z3;
			z3 = z2;
			z2 = tmpz;
		}

		if (p1.getY() > p2.getY()) {
			PointRGB tp = p2;
			p2 = p1;
			p1 = tp;

			int tmp = tp2;
			tp2 = tp1;
			tp1 = tmp;

			double tmpz = z2;
			z2 = z1;
			z1 = tmpz;
		}

		Texture t = ((Texture) textures.get(tm.getTextureId() - 1));

		double tw = t.getW() - 1;
		double th = t.getH() - 1;

		int x1 = p1.getX();
		int y1 = p1.getY();
		double tx1 = textureCoords.get(tp1 - 1).getX1() * tw;
		double ty1 = th - textureCoords.get(tp1 - 1).getY1() * th;

		int x2 = p2.getX();
		int y2 = p2.getY();
		double tx2 = textureCoords.get(tp2 - 1).getX1() * tw;
		double ty2 = th - textureCoords.get(tp2 - 1).getY1() * th;

		int x3 = p3.getX();
		int y3 = p3.getY();
		double tx3 = textureCoords.get(tp3 - 1).getX1() * tw;
		double ty3 = th - textureCoords.get(tp3 - 1).getY1() * th;

		if (y1 > h)
			return;
		if (y3 < 0)
			return;
		if (x1 > w && x2 > w && x3 > w)
			return;
		if (x1 < 0 && x2 < 0 && x3 < 0)
			return;

		z1 = 1.0 / (focalLength + z1 * zoom);
		tx1 = tx1 * z1;
		ty1 = ty1 * z1;

		z2 = 1.0 / (focalLength + z2 * zoom);
		tx2 = tx2 * z2;
		ty2 = ty2 * z2;

		z3 = 1.0 / (focalLength + z3 * zoom);
		tx3 = tx3 * z3;
		ty3 = ty3 * z3;

		// Wspó³czynniki dla X
		double xkp = 0;
		double xkl = 0;
		// double xk3 = 0;

		// Wspó³czynniki dla Z
		double zkp = 0;
		double zkl = 0;

		// Wspó³czynniki dla tekstury
		double tkxp = 0;
		double tkyp = 0;
		double tkxl = 0;
		double tkyl = 0;
		// double ck3 = 0;

		double xl = x1;
		double xp = x1;

		double zl = z1;
		double zp = z1;

		double txl = tx1;
		double tyl = ty1;
		double txp = tx1;
		double typ = ty1;

		int tmem[][] = t.getMem();

		double y32 = y3 - y2;
		double y31 = y3 - y1;
		double y21 = y2 - y1;

		double xk21 = (x2 - x1) / y21;
		double xk31 = (x3 - x1) / y31;
		double xk32 = (x3 - x2) / y32;

		double tkx21 = (tx2 - tx1) / y21;
		double tky21 = (ty2 - ty1) / y21;
		double tkx31 = (tx3 - tx1) / y31;
		double tky31 = (ty3 - ty1) / y31;
		double tkx32 = (tx3 - tx2) / y32;
		double tky32 = (ty3 - ty2) / y32;

		double zk21 = (z2 - z1) / y21;
		double zk31 = (z3 - z1) / y31;
		double zk32 = (z3 - z2) / y32;

		if (y21 != 0) {
			if (xk21 > xk31) {
				xkl = xk31;
				xkp = xk21;

				tkxl = tkx31;
				tkyl = tky31;

				tkxp = tkx21;
				tkyp = tky21;

				zkl = zk31;
				zkp = zk21;
			} else {
				xkl = xk21;
				xkp = xk31;

				tkxl = tkx21;
				tkyl = tky21;

				tkxp = tkx31;
				tkyp = tky31;

				zkl = zk21;
				zkp = zk31;
			}

			// Clipping
			int id = 0;

			if (y1 >= 0)
				id = w * y1;
			else {
				xl = xl - xkl * y1;
				xp = xp - xkp * y1;

				zl = zl - zkl * y1;
				zp = zp - zkp * y1;

				txl = txl - tkxl * y1;
				tyl = tyl - tkyl * y1;

				txp = txp - tkxp * y1;
				typ = typ - tkyp * y1;

				y21 = y21 + y1;
				y31 = y31 + y1;
			}

			if (y2 > h)
				y21 = h - y1;
			// --- Clipping

			for (int i = 0; i < y21; i++) {
				if (xp >= 0 && xl < w) {
					double d = 1.0 / (xp - xl);
					double tkz = (zp - zl) * d;
					double tz = zl;

					double tkx = (txp - txl) * d;
					double tky = (typ - tyl) * d;
					double tx = txl;
					double ty = tyl;

					double xp_loop = xp;
					double xl_loop = xl;

					if (xp > w)
						xp_loop = w;
					if (xl < 0) {
						xl_loop = 0;
						tx = tx - tkx * xl;
						ty = ty - tky * xl;
						tz = tz - tkz * xl;
					}

					if (id + xp_loop > m)
						break;

					for (int j = (int) xl_loop; j < xp_loop; j++) {
						double onetz = 1.0 / tz;
						int ctx = (int) (tx * onetz);
						int cty = (int) (ty * onetz);

						if (ctx >= 0 && cty >= 0 && ctx <= tw && cty <= th)
							mem[id + j] = tmem[ctx][cty];

						tx = tx + tkx;
						ty = ty + tky;

						tz = tz + tkz;

					}
				}
				id = id + w;

				xl = xl + xkl;
				xp = xp + xkp;

				zl = zl + zkl;
				zp = zp + zkp;

				txl = txl + tkxl;
				tyl = tyl + tkyl;
				txp = txp + tkxp;
				typ = typ + tkyp;
			}

			if (xk21 > xk31) {
				xkp = xk32;
				tkxp = tkx32;
				tkyp = tky32;

				// zp = z2;

				zkp = zk32;
			} else {
				xkl = xk32;
				tkxl = tkx32;
				tkyl = tky32;

				// zl = z2;

				zkl = zk32;
			}
		} else {
			if (x1 < x2) {
				xp = x2;

				txp = tx2;
				typ = ty2;

				xkl = xk31;
				tkxl = tkx31;
				tkyl = tky31;

				xkp = xk32;
				tkxp = tkx32;
				tkyp = tky32;

				zp = z2;

				zkl = zk31;
				zkp = zk32;
			} else {
				xl = x2;

				txl = tx2;
				tyl = ty2;

				xkl = xk32;
				tkxl = tkx32;
				tkyl = tky32;

				xkp = xk31;
				tkxp = tkx31;
				tkyp = tky31;

				zl = z2;

				zkl = zk32;
				zkp = zk31;
			}
		}

		// Clipping
		int id = 0;

		if (y1 >= 0)
			id = (int) (w * (y1 + y21));
		else {
			id = (int) (w * y21);
		}

		if (y2 < 0) {
			id = 0;

			if (xk31 < xk32) {
				xkl = xk32;
				xkp = xk31;

				tkxl = tkx32;
				tkxp = tkx31;

				xl = x2 - xkl * y2;
				xp = x1 - xkp * y1;

				txl = tx2 - tkxl * y2;
				tyl = ty2 - tkyl * y2;

				txp = tx1 - tkxp * y1;
				typ = ty1 - tkyp * y1;

				zkl = zk32;
				zkp = zk31;

				zl = z2 - zkl * y2;
				zp = z1 - zkp * y1;
			} else {
				xkl = xk31;
				xkp = xk32;

				tkxl = tkx31;
				tkxp = tkx32;

				xl = x1 - xkl * y1;
				xp = x2 - xkp * y2;

				txl = tx1 - tkxl * y1;
				tyl = ty1 - tkyl * y1;

				txp = tx2 - tkxp * y2;
				typ = ty2 - tkyp * y2;

				zkl = zk31;
				zkp = zk32;

				zl = z1 - zkl * y1;
				zp = z2 - zkp * y2;
			}

			y31 = y31 + y2;
		}

		if (y3 > h)
			y31 = y31 - (y3 - h);
		// --- Clipping

		for (int i = (int) y21; i < y31; i++) {
			if (xp >= 0 && xl < w) {
				double d = 1.0 / (xp - xl);
				double tkz = (zp - zl) * d;
				double tz = zl;

				double tkx = (txp - txl) * d;
				double tky = (typ - tyl) * d;
				double tx = txl;
				double ty = tyl;

				double xp_loop = xp;
				double xl_loop = xl;

				if (xp > w)
					xp_loop = w;
				if (xl < 0) {
					xl_loop = 0;
					tx = tx - tkx * xl;
					ty = ty - tky * xl;
					tz = tz - tkz * xl;
				}

				if (id + xp_loop > m)
					break;

				for (int j = (int) xl_loop; j < xp_loop; j++) {
					double onetz = 1.0 / tz;
					int ctx = (int) (tx * onetz);
					int cty = (int) (ty * onetz);

					if (ctx >= 0 && cty >= 0 && ctx <= tw && cty <= th)
						mem[id + j] = tmem[ctx][cty];

					tx = tx + tkx;
					ty = ty + tky;

					tz = tz + tkz;
				}
			}
			id = id + w;

			xl = xl + xkl;
			xp = xp + xkp;

			zl = zl + zkl;
			zp = zp + zkp;

			txl = txl + tkxl;
			tyl = tyl + tkyl;
			txp = txp + tkxp;
			typ = typ + tkyp;
		}
	}

	public void drawTexturedFaceLight(PointRGB p1, PointRGB p2, PointRGB p3,
			double z1, double z2, double z3, FaceMap tm) {

		int tp1 = tm.getTc1();
		int tp2 = tm.getTc2();
		int tp3 = tm.getTc3();

		if (p1.getY() > p2.getY()) {
			PointRGB tp = p2;
			p2 = p1;
			p1 = tp;

			int tmp = tp2;
			tp2 = tp1;
			tp1 = tmp;

			double tmpz = z2;
			z2 = z1;
			z1 = tmpz;
		}

		if (p2.getY() > p3.getY()) {
			PointRGB tp = p3;
			p3 = p2;
			p2 = tp;

			int tmp = tp3;
			tp3 = tp2;
			tp2 = tmp;

			double tmpz = z3;
			z3 = z2;
			z2 = tmpz;
		}

		if (p1.getY() > p2.getY()) {
			PointRGB tp = p2;
			p2 = p1;
			p1 = tp;

			int tmp = tp2;
			tp2 = tp1;
			tp1 = tmp;

			double tmpz = z2;
			z2 = z1;
			z1 = tmpz;
		}

		Texture t = ((Texture) textures.get(tm.getTextureId() - 1));

		int tw = t.getW() - 1;
		int th = t.getH() - 1;

		int x1 = p1.getX();
		int y1 = p1.getY();
		double tx1 = textureCoords.get(tp1 - 1).getX1() * tw;
		double ty1 = th - textureCoords.get(tp1 - 1).getY1() * th;

		int x2 = p2.getX();
		int y2 = p2.getY();
		double tx2 = textureCoords.get(tp2 - 1).getX1() * tw;
		double ty2 = th - textureCoords.get(tp2 - 1).getY1() * th;

		int x3 = p3.getX();
		int y3 = p3.getY();
		double tx3 = textureCoords.get(tp3 - 1).getX1() * tw;
		double ty3 = th - textureCoords.get(tp3 - 1).getY1() * th;

		tx1 = tx1 / (focalLength + z1 * zoom);
		ty1 = ty1 / (focalLength + z1 * zoom);
		z1 = 1.0 / (focalLength + z1 * zoom);

		tx2 = tx2 / (focalLength + z2 * zoom);
		ty2 = ty2 / (focalLength + z2 * zoom);
		z2 = 1.0 / (focalLength + z2 * zoom);

		tx3 = tx3 / (focalLength + z3 * zoom);
		ty3 = ty3 / (focalLength + z3 * zoom);
		z3 = 1.0 / (focalLength + z3 * zoom);

		// Wspó³czynniki dla X
		double xkp = 0;
		double xkl = 0;
		// double xk3 = 0;

		// Wspó³czynniki dla Z
		double zkp = 0;
		double zkl = 0;

		// Wspó³czynniki dla tekstury
		double tkxp = 0;
		double tkyp = 0;
		double tkxl = 0;
		double tkyl = 0;
		// double ck3 = 0;

		double xl = x1;
		double xp = x1;

		double zl = z1;
		double zp = z1;

		double txl = tx1;
		double tyl = ty1;
		double txp = tx1;
		double typ = ty1;

		int iy1 = y2 - y1;

		int tmem[][] = t.getMem();

		// System.out.println("x: " + t.getW() + ", y: " + t.getH());

		// System.out.println(iy1);

		double xk3 = (x3 - x2) / (double) (y3 - y2);
		double zk3 = (z3 - z2) / (double) (y3 - y2);

		double tkx3 = (tx3 - tx2) / (double) (y3 - y2);
		double tky3 = (ty3 - ty2) / (double) (y3 - y2);

		if (iy1 != 0) {
			double xk1 = (x2 - x1) / (double) (y2 - y1);
			double xk2 = (x3 - x1) / (double) (y3 - y1);

			double zk1 = (z2 - z1) / (double) (y2 - y1);
			double zk2 = (z3 - z1) / (double) (y3 - y1);

			double tkx1 = (tx2 - tx1) / (double) (y2 - y1);
			double tky1 = (ty2 - ty1) / (double) (y2 - y1);
			double tkx2 = (tx3 - tx1) / (double) (y3 - y1);
			double tky2 = (ty3 - ty1) / (double) (y3 - y1);

			if (x2 > x1) {
				if (xk1 > xk2) {
					xkl = xk2;
					xkp = xk1;

					zkl = zk2;
					zkp = zk1;

					tkxl = tkx2;
					tkyl = tky2;
					tkxp = tkx1;
					tkyp = tky1;
				} else {
					xkl = xk1;
					xkp = xk2;

					zkl = zk1;
					zkp = zk2;

					tkxl = tkx1;
					tkyl = tky1;
					tkxp = tkx2;
					tkyp = tky2;
				}
			} else {
				if (xk1 < xk2) {
					xkl = xk1;
					xkp = xk2;

					zkl = zk1;
					zkp = zk2;

					tkxl = tkx1;
					tkyl = tky1;
					tkxp = tkx2;
					tkyp = tky2;
				} else {
					xkl = xk2;
					xkp = xk1;

					zkl = zk2;
					zkp = zk1;

					tkxl = tkx2;
					tkyl = tky2;
					tkxp = tkx1;
					tkyp = tky1;
				}
			}

			for (int i = 0; i < iy1; i++) {
				int id = w * (i + y1);

				if (id > 0 && id < m) {

					double tkz = (zp - zl) / (double) (xp - xl);
					double tz = zl;

					double tkx = (txp - txl) / (double) (xp - xl);
					double tky = (typ - tyl) / (double) (xp - xl);

					double tx = txl;
					double ty = tyl;

					// unsigned char
					for (int j = (int) xl; j < xp; j++) {
						int ctx = (int) (tx / tz);
						int cty = (int) (ty / tz);

						if (ctx >= 0 && cty >= 0 && ctx <= tw && cty <= th) {
							int txt = tmem[ctx][cty];
							
							double l = (1.0 / tz - focalLength) / zoom;
							
							// System.out.println(Integer.toBinaryString(txt));
							int r = (txt >> 16) & (0x000000FF);
							// System.out.println(Integer.toBinaryString(r));
							int g = (txt >> 8) & (0x000000FF);
							int b = (txt) & (0x000000FF);


							//System.out.println(l);
							
							int c = 255; //(int) (-l * 255.0);

							// if (c < 0)
							// c = 0;
							// if (c > 255)
							// c = 255;

							// System.out.println(l);

							//r = (r + c) / 2;
							r = (int) - (r * l);
							if (r < 0)
								r = 0;
							if (r > 255)
								r = 255;
							//g = (g + c) / 2;
							g = (int) - (g * l);
							if (g < 0)
								g = 0;
							if (g > 255)
								g = 255;
							//b = (b + c) / 2;
							b = (int) - (b * l);
							if (b < 0)
								b = 0;
							if (b > 255)
								b = 255;

							mem[id + j] = (255 << 24) | (r << 16) | (g << 8) | b;
						}

						tx = tx + tkx;
						ty = ty + tky;

						tz = tz + tkz;

					}
				}
				xl = xl + xkl;
				xp = xp + xkp;

				zl = zl + zkl;
				zp = zp + zkp;

				txl = txl + tkxl;
				tyl = tyl + tkyl;
				txp = txp + tkxp;
				typ = typ + tkyp;
			}

			if (xk1 > xk2) {
				xkp = xk3;

				zkp = zk3;

				tkxp = tkx3;
				tkyp = tky3;
			} else {
				xkl = xk3;

				zkl = zk3;

				tkxl = tkx3;
				tkyl = tky3;
			}

		} else {

			double xk = (x3 - x1) / (double) (y3 - y1);

			double zk1 = (z3 - z1) / (double) (y3 - y1);

			double tkx = (tx3 - tx1) / (double) (y3 - y1);
			double tky = (ty3 - ty1) / (double) (y3 - y1);

			if (x1 < x2) {
				xl = x1;
				xp = x2;

				txl = tx1;
				tyl = ty1;
				txp = tx2;
				typ = ty2;

				xkl = xk;
				tkxl = tkx;
				tkyl = tky;

				zl = z1;
				zp = z2;

				zkl = zk1;
				zkp = zk3;

				xkp = xk3;
				tkxp = tkx3;
				tkyp = tky3;

			} else {
				xl = x2;
				xp = x1;

				txl = tx2;
				tyl = ty2;
				txp = tx1;
				typ = ty1;

				xkl = xk3;
				tkxl = tkx3;
				tkyl = tky3;

				zl = z2;
				zp = z1;

				zkl = zk3;
				zkp = zk1;

				xkp = xk;
				tkxp = tkx;
				tkyp = tky;
			}
		}

		int iy2 = y3 - y1;

		for (int i = iy1; i < iy2; i++) {
			int id = w * (i + y1);

			if (id > 0 && id < m) {
				double tkz = (zp - zl) / (double) (xp - xl);
				double tz = zl;

				double tkx = (txp - txl) / (double) (xp - xl);
				double tky = (typ - tyl) / (double) (xp - xl);

				double tx = txl;
				double ty = tyl;

				for (int j = (int) xl; j < xp; j++) {

					int ctx = (int) (tx / tz);
					int cty = (int) (ty / tz);

					if (ctx >= 0 && cty >= 0 && ctx <= tw && cty <= th) {
						int txt = tmem[ctx][cty];
						// System.out.println(Integer.toBinaryString(txt));
						int r = (txt >> 16) & (0x000000FF);
						// System.out.println(Integer.toBinaryString(r));
						int g = (txt >> 8) & (0x000000FF);
						int b = (txt) & (0x000000FF);

						double l = (1.0 / tz - focalLength) / zoom;

						int c = 255; //(int) (-l * 255.0);

						// if (c < 0)
						// c = 0;
						// if (c > 255)
						// c = 255;

						// System.out.println(l);

						//r = (r + c) / 2;
						r = (int) - (r * l);
						if (r < 0)
							r = 0;
						if (r > 255)
							r = 255;
						//g = (g + c) / 2;
						g = (int) - (g * l);
						if (g < 0)
							g = 0;
						if (g > 255)
							g = 255;
						//b = (b + c) / 2;
						b = (int) - (b * l);
						if (b < 0)
							b = 0;
						if (b > 255)
							b = 255;
						mem[id + j] = new Color(r, g, b).getRGB();
					}

					tx = tx + tkx;
					ty = ty + tky;

					tz = tz + tkz;
				}
			}
			xl = xl + xkl;
			xp = xp + xkp;

			zl = zl + zkl;
			zp = zp + zkp;

			txl = txl + tkxl;
			tyl = tyl + tkyl;
			txp = txp + tkxp;
			typ = typ + tkyp;
		}

		// mis.newPixels();
	}

	public void drawFacePhong(PointRGB p1, PointRGB p2, PointRGB p3, Vertex n1,
			Vertex n2, Vertex n3) {

		if (p1.getY() > p2.getY()) {
			PointRGB tp = p2;
			p2 = p1;
			p1 = tp;

			Vertex tmp = n2;
			n2 = n1;
			n1 = tmp;
		}

		if (p2.getY() > p3.getY()) {
			PointRGB tp = p3;
			p3 = p2;
			p2 = tp;

			Vertex tmp = n3;
			n3 = n2;
			n2 = tmp;
		}

		if (p1.getY() > p2.getY()) {
			PointRGB tp = p2;
			p2 = p1;
			p1 = tp;

			Vertex tmp = n2;
			n2 = n1;
			n1 = tmp;
		}

		int x1 = p1.getX();
		int y1 = p1.getY();
		double tx1 = n1.getX();
		double ty1 = n1.getY();
		double tz1 = n1.getZ();

		int x2 = p2.getX();
		int y2 = p2.getY();
		double tx2 = n2.getX();
		double ty2 = n2.getY();
		double tz2 = n2.getZ();

		int x3 = p3.getX();
		int y3 = p3.getY();
		double tx3 = n3.getX();
		double ty3 = n3.getY();
		double tz3 = n3.getZ();

		// Wspó³czynniki dla X
		double xkp = 0;
		double xkl = 0;

		// Wspó³czynniki dla tekstury
		double tkxp = 0;
		double tkyp = 0;
		double tkzp = 0;

		double tkxl = 0;
		double tkyl = 0;
		double tkzl = 0;
		// double ck3 = 0;

		double xl = x1;
		double xp = x1;

		double txl = tx1;
		double tyl = ty1;
		double tzl = tz1;

		double txp = tx1;
		double typ = ty1;
		double tzp = tz1;

		int iy1 = y2 - y1;

		double xk3 = (x3 - x2) / (double) (y3 - y2);

		double tkx3 = (tx3 - tx2) / (double) (y3 - y2);
		double tky3 = (ty3 - ty2) / (double) (y3 - y2);
		double tkz3 = (tz3 - tz2) / (double) (y3 - y2);

		if (iy1 != 0) {
			double xk1 = (x2 - x1) / (double) (y2 - y1);
			double xk2 = (x3 - x1) / (double) (y3 - y1);

			double tkx1 = (tx2 - tx1) / (double) (y2 - y1);
			double tky1 = (ty2 - ty1) / (double) (y2 - y1);
			double tkz1 = (tz2 - tz1) / (double) (y2 - y1);
			double tkx2 = (tx3 - tx1) / (double) (y3 - y1);
			double tky2 = (ty3 - ty1) / (double) (y3 - y1);
			double tkz2 = (tz3 - tz1) / (double) (y3 - y1);

			if (x2 > x1) {
				if (xk1 > xk2) {
					xkl = xk2;
					xkp = xk1;

					tkxl = tkx2;
					tkyl = tky2;
					tkzl = tkz2;
					tkxp = tkx1;
					tkyp = tky1;
					tkzp = tkz1;
				} else {
					xkl = xk1;
					xkp = xk2;

					tkxl = tkx1;
					tkyl = tky1;
					tkzl = tkz1;
					tkxp = tkx2;
					tkyp = tky2;
					tkzp = tkz2;
				}
			} else {
				if (xk1 < xk2) {
					xkl = xk1;
					xkp = xk2;

					tkxl = tkx1;
					tkyl = tky1;
					tkzl = tkz1;
					tkxp = tkx2;
					tkyp = tky2;
					tkzp = tkz2;
				} else {
					xkl = xk2;
					xkp = xk1;

					tkxl = tkx2;
					tkyl = tky2;
					tkzl = tkz2;
					tkxp = tkx1;
					tkyp = tky1;
					tkzp = tkz1;
				}
			}

			for (int i = 0; i < iy1; i++) {
				int id = w * (i + y1);

				if ((id + xp) > 0 && (id + xp) < m) {
					double tkx = (txp - txl) / (double) (xp - xl);
					double tky = (typ - tyl) / (double) (xp - xl);
					double tkz = (tzp - tzl) / (double) (xp - xl);
					double tx = txl;
					double ty = tyl;
					double tz = tzl;
					for (int j = (int) xl; j < xp; j++) {
						double normlength = Math.sqrt(Math.pow(tx, 2)
								+ Math.pow(ty, 2) + Math.pow(tz, 2));
						// double normlength = Math.sqrt(Math.pow(tx,2) +
						// Math.pow(ty,2));
						// double normlength = Math.max(Math.max(tx, ty), tz);
						// double normlength = Math.max(Math.max(Math.abs(tx),
						// Math.abs(ty)), Math.abs(tz));
						double v = -tz / normlength;

						int d = (int) (255.0 * v);

						if (d < 0)
							d = 0;
						if (d > 255)
							d = 255;

						mem[id + j] = (int) 255 << 24 | d << 16 | d << 8 | d;

						tx = tx + tkx;
						ty = ty + tky;
						tz = tz + tkz;
					}
				}
				xl = xl + xkl;
				xp = xp + xkp;

				txl = txl + tkxl;
				tyl = tyl + tkyl;
				tzl = tzl + tkzl;

				txp = txp + tkxp;
				typ = typ + tkyp;
				tzp = tzp + tkzp;
			}

			if (xk1 > xk2) {
				xkp = xk3;
				tkxp = tkx3;
				tkyp = tky3;
				tkzp = tkz3;
			} else {
				xkl = xk3;
				tkxl = tkx3;
				tkyl = tky3;
				tkzl = tkz3;
			}

		} else {

			double xk = (x3 - x1) / (double) (y3 - y1);

			double tkx = (tx3 - tx1) / (double) (y3 - y1);
			double tky = (ty3 - ty1) / (double) (y3 - y1);
			double tkz = (tz3 - tz1) / (double) (y3 - y1);

			if (x1 < x2) {
				xl = x1;
				xp = x2;

				txl = tx1;
				tyl = ty1;
				tzl = tz1;
				txp = tx2;
				typ = ty2;
				tzp = tz2;

				xkl = xk;
				tkxl = tkx;
				tkyl = tky;
				tkzl = tkz;

				xkp = xk3;
				tkxp = tkx3;
				tkyp = tky3;
				tkzp = tkz3;
			} else {
				xl = x2;
				xp = x1;

				txl = tx2;
				tyl = ty2;
				tzl = tz2;
				txp = tx1;
				typ = ty1;
				tzp = tz1;

				xkl = xk3;
				tkxl = tkx3;
				tkyl = tky3;
				tkzl = tkz3;

				xkp = xk;
				tkxp = tkx;
				tkyp = tky;
				tkzp = tkz;
			}
		}

		int iy2 = y3 - y1;

		for (int i = iy1; i < iy2; i++) {
			int id = w * (i + y1);

			if ((id + xp) > 0 && (id + xp) < m) {
				double tkx = (txp - txl) / (double) (xp - xl);
				double tky = (typ - tyl) / (double) (xp - xl);
				double tkz = (tzp - tzl) / (double) (xp - xl);
				double tx = txl;
				double ty = tyl;
				double tz = tzl;

				for (int j = (int) xl; j < xp; j++) {
					double normlength = Math.sqrt(Math.pow(tx, 2)
							+ Math.pow(ty, 2) + Math.pow(tz, 2));
					// double normlength = Math.sqrt(Math.pow(tx,3) +
					// Math.pow(ty,3) + Math.pow(tz,3))/3;
					// double normlength = Math.sqrt(Math.pow(tx,2) +
					// Math.pow(ty,2));
					// double normlength = Math.max(Math.max(Math.abs(tx),
					// Math.abs(ty)), Math.abs(tz));
					double v = -tz / normlength;

					int d = (int) (255.0 * v);

					if (d < 0)
						d = 0;
					if (d > 255)
						d = 255;

					mem[id + j] = (int) 255 << 24 | d << 16 | d << 8 | d;

					tx = tx + tkx;
					ty = ty + tky;
					tz = tz + tkz;
				}
			}
			xl = xl + xkl;
			xp = xp + xkp;

			txl = txl + tkxl;
			tyl = tyl + tkyl;
			tzl = tzl + tkzl;
			txp = txp + tkxp;
			typ = typ + tkyp;
			tzp = tzp + tkzp;
		}
	}

	public double getTx() {
		return tx;
	}

	public void setTx(double tx) {
		this.tx = tx;
	}

	public double getTy() {
		return ty;
	}

	public void setTy(double ty) {
		this.ty = ty;
	}

	public double getRx() {
		return rx;
	}

	public void setRx(double rx) {
		this.rx = rx;
	}

	public double getRy() {
		return ry;
	}

	public void setRy(double ry) {
		this.ry = ry;
	}

	public double getS() {
		return s;
	}

	public void setS(double s) {
		this.s = s;
	}

	public int getMode() {
		return mode;
	}

	public void setMode(int mode) {
		this.mode = mode;
	}

	public int getAntialiasing() {
		return aa;
	}

	public void setAntialiasing(int aa) {
		this.aa = aa;
	}

	public double getFps() {
		return fps;
	}
}