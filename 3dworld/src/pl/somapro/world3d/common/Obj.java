package pl.somapro.world3d.common;

import java.util.Vector;

public class Obj {

	private String name;
	private Vector<Vertex> vertices;
	private Vector<Face> faces;
	private Vector<Texture> textures;
	private Vector<TextureCoord> textureCoords;
	
	public Obj() {
		
	}
	
	public Obj(String name, Vector<Vertex> vertices, Vector<Face> faces) {
		this.name = name;
		this.vertices = vertices;
		this.faces = faces;
	}

	public Vector<Vertex> getVertices() {
		return vertices;
	}

	public void setVertices(Vector<Vertex> vertices) {
		this.vertices = vertices;
	}

	public Vector<Face> getFaces() {
		return faces;
	}

	public void setFaces(Vector<Face> faces) {
		this.faces = faces;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Vector<Texture> getTextures() {
		return textures;
	}

	public void setTextures(Vector<Texture> textures) {
		this.textures = textures;
	}

	public Vector<TextureCoord> getTextureCoords() {
		return textureCoords;
	}

	public void setTextureCoords(Vector<TextureCoord> textureCoords) {
		this.textureCoords = textureCoords;
	}
	
	public Obj clone() {
		Obj clone = new Obj(name, (Vector<Vertex>) vertices.clone(), faces);
		
		clone.setTextures(textures);
		clone.setTextureCoords(textureCoords);
		
		return clone;
	}
}