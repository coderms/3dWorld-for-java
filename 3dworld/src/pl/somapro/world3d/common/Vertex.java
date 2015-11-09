package pl.somapro.world3d.common;

public class Vertex {

	private double x;
	private double y;
	private double z;
	
	private Vertex normal;
	private double count;
	
	public Vertex(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
		
		this.normal = null;
		this.count = 0;
	}

	public double getX() {
		return x;
	}

	public void setX(double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(double y) {
		this.y = y;
	}

	public double getZ() {
		return z;
	}

	public void setZ(double z) {
		this.z = z;
	}
	
	public Vertex getNormal() {		
		return new Vertex(this.normal.x / count, this.normal.y / count, this.normal.z / count);
	}

	public void setNormal(Vertex normal) {
		this.normal = normal;
		count = 0;
	}
	
	public void addToNormal(Vertex normal) {
		this.normal.x += normal.x;
		this.normal.y += normal.y;
		this.normal.z += normal.z;
		count++;
	}

	@Override
	public String toString() {
		return "X = " + x + ", Y = " + y + ", Z = " + z;
	}
}
