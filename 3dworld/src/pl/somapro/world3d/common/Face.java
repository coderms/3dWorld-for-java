package pl.somapro.world3d.common;

public class Face implements Comparable {

	private int v1;
	private int v2;
	private int v3;
	
	private boolean visible;
	
	private double z1;
	private double z2;
	private double z3;
	private double zSort;
	
	private FaceMap faceMap;
	private Vertex normal;
	
	public Face(int v1, int v2, int v3) {
		this.v1 = v1;
		this.v2 = v2;
		this.v3 = v3;
		
		this.faceMap = null;
		this.normal = null;
	}
	
	public int getV1() {
		return v1;
	}
	
	public void setV1(int v1) {
		this.v1 = v1;
	}
	
	public int getV2() {
		return v2;
	}
	
	public void setV2(int v2) {
		this.v2 = v2;
	}
	
	public int getV3() {
		return v3;
	}
	
	public void setV3(int v3) {
		this.v3 = v3;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public double getZSort() {
		return zSort;
	}

	public void calculateZvars(double z1, double z2, double z3) {
//		this.zSort = (z1 + z2 + z3) / 3.0;
		this.zSort = z1 + z2 + z3;
//		this.zSort = Math.min(Math.min(z1, z2),z3);
		this.z1 = z1;
		this.z2 = z2;
		this.z3 = z3;
	}
	
	public double getZ1() {
		return z1;
	}
	
	public double getZ2() {
		return z2;
	}

	public double getZ3() {
		return z3;
	}
	
	public FaceMap getFaceMap() {
		return faceMap;
	}

	public void setFaceMap(FaceMap faceMap) {
		this.faceMap = faceMap;
	}

	public Vertex getNormal() {
		return normal;
	}

	public void setNormal(Vertex normal) {
		this.normal = normal;
	}

	@Override
	public int compareTo(Object arg0) {
		Face compareToObject = (Face) arg0;
		
		if (zSort<compareToObject.zSort)
			return 1;
		else if (zSort>compareToObject.zSort)
			return -1;
		else 
			return 0;
		
	}
}