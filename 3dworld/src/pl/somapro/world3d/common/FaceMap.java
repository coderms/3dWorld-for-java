package pl.somapro.world3d.common;

public class FaceMap {
	
	private int textureId;
	private int tc1;
	private int tc2;
	private int tc3;
	
	public FaceMap(int textureId, int tc1, int tc2, int tc3) {

		this.textureId = textureId;
		this.tc1 = tc1;
		this.tc2 = tc2;
		this.tc3 = tc3;
	}

	public int getTextureId() {
		return textureId;
	}

	public void setTextureId(int textureId) {
		this.textureId = textureId;
	}

	public int getTc1() {
		return tc1;
	}

	public void setTc1(int tc1) {
		this.tc1 = tc1;
	}

	public int getTc2() {
		return tc2;
	}

	public void setTc2(int tc2) {
		this.tc2 = tc2;
	}

	public int getTc3() {
		return tc3;
	}

	public void setTc3(int tc3) {
		this.tc3 = tc3;
	}
}