package pl.somapro.world3d.common;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Texture {

	private int w;
	private int h;
	int[][] mem;

	private String path;
	private int id;

	public Texture(int id, String path) {
		this.id = id;
		this.path = path;

		read();
		System.out.println(path);
	}

	private void read() {
		BufferedImage image = null;
        try {

              //you can either use URL or File for reading image using ImageIO
            File imagefile = new File(path);
            image = ImageIO.read(imagefile);
            
            convert(image);
        } catch (IOException e) {
              e.printStackTrace();
        }
	}

	private void convert(BufferedImage image) {
		w = image.getWidth();
		h = image.getHeight();
		mem = new int[w][h];
		for (int col = 0; col < w; col++) {
			for (int row = 0; row < h; row++) {
				mem[col][row] = image.getRGB(col, row);
			}
		}
	}

	public int getW() {
		return w;
	}

	public void setW(int w) {
		this.w = w;
	}

	public int getH() {
		return h;
	}

	public void setH(int h) {
		this.h = h;
	}

	public int[][] getMem() {
		return mem;
	}

	public void setMem(int[][] mem) {
		this.mem = mem;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
