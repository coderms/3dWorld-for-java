package pl.somapro.world3d.common;

import java.util.Vector;

public class Manipulator {
	
	private Obj obj;
	
	public Manipulator(Obj obj) {
		this.obj = obj;
	}

	public Obj Translate(double tx, double ty, double tz) {
		Obj clone = obj.clone();
		
		Vector<Vertex> vertices = clone.getVertices();
		
		for (int i=0; i<vertices.size(); i++) {
			Vertex v = vertices.get(i);
			v.setX(v.getX() + tx);
			v.setY(v.getY() + ty);
			v.setZ(v.getZ() + tz);
		}
		
		return clone;
	}
}
