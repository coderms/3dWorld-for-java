package pl.somapro.world3d;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;
import pl.somapro.world3d.common.Face;
import pl.somapro.world3d.common.FaceMap;
import pl.somapro.world3d.common.Obj;
import pl.somapro.world3d.common.Texture;
import pl.somapro.world3d.common.TextureCoord;
import pl.somapro.world3d.common.Vertex;

public class ObjectParser {

	private String fileName;
	private StringBuffer fileContent;
	private Obj object;
	
	public ObjectParser(String fileName) {
		this.fileName = fileName;
		
		read();
		parse();
	}
	
	private void read() {
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fileName));

	        fileContent = new StringBuffer();
	        String line = br.readLine();

	        while (line != null) {
	        	fileContent.append(line);
	        	fileContent.append("\n");
	            line = br.readLine();
	        }
	    } catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
	        try {
	        	if (br != null)
	        		br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	}
	
	private void parse() {
		System.out.println("Parsing file started!");
		
		String fc = fileContent.toString();
		
		object = new Obj();
		Vector<Vertex> v = new Vector<Vertex>();
		Vector<Face> f = new Vector<Face>();
		Vector<Texture> t = new Vector<Texture>();
		Vector<TextureCoord> tc = new Vector<TextureCoord>();
		
		int textureId = 0;
		boolean hasTexture = false;
		
		while(true) {
			int eol = fc.indexOf("\n")+1;
			String line = fc.substring(0, eol);
			
			if (line.length() == 0)
				break;
			
			int index = line.indexOf(" ");
			String p = "";
			
			if (index != -1)
				p = line.substring(0, index);
			else {
				fc = fc.substring(eol);
				
				continue;
			}
			
			if (p.equals("o")) {
				String name = line.substring(line.indexOf(" ")+1, line.length()-1);
				object.setName(name);
			} else if (p.equals("v")) {
				line = line.substring(line.indexOf(" ")+1, line.length()).trim();
				Double x = new Double(line.substring(0, line.indexOf(" ")));
				line = line.substring(line.indexOf(" ")+1, line.length()).trim();
				Double y = new Double(line.substring(0, line.indexOf(" ")));
				line = line.substring(line.indexOf(" ")+1, line.length()).trim();
				Double z = new Double(line.substring(0, line.length()));
				//System.out.println("x: " + x);
				//System.out.println("y: " + y);
				//System.out.println("z: " + z);
				
				Vertex vertex = new Vertex(x, y, z);
				v.add(vertex);
			} else if (p.equals("f")) {
				line = line.substring(line.indexOf(" ")+1, line.length()).trim();
				String v1s = line.substring(0, line.indexOf(" "));
				String[] v1st = v1s.split("//");
				
				if (v1st.length<2) {
					v1st = v1s.split("/");
				}
				
				int v1 = 0;
				int v1tc = 0;
				if (v1st.length>1) {
					v1 = new Integer(v1st[0].replaceAll("/", "").trim());
					v1tc = new Integer(v1st[1].replaceAll("/", "").trim());
				} else {
						v1 = new Integer(v1s.replaceAll("/", "").trim());
				}
				
				line = line.substring(line.indexOf(" ")+1, line.length()).trim();
				String v2s = line.substring(0, line.indexOf(" "));
				String[] v2st = v2s.split("//");
				if (v2st.length<2) {
					v2st = v2s.split("/");
				}
				int v2 = 0;
				int v2tc = 0;
				if (v2st.length>1) {
					v2 = new Integer(v2st[0].replaceAll("/", "").trim());
					v2tc = new Integer(v2st[1].replaceAll("/", "").trim());
				} else
					v2 = new Integer(v2s.replaceAll("/", "").trim());
				//int v2 = new Integer(line.substring(0, line.indexOf(" ")));
				line = line.substring(line.indexOf(" ")+1, line.length()).trim();
				String v3s = line.substring(0, line.length());
				String[] v3st = v3s.split("//");
				if (v3st.length<2) {
					v3st = v3s.split("/");
				}
				int v3 = 0;
				int v3tc = 0;
				if (v3st.length>1) {
					v3 = new Integer(v3st[0].replaceAll("/", "").trim());
					v3tc = new Integer(v3st[1].replaceAll("/", "").trim());
				} else
					v3 = new Integer(v3s.replaceAll("/", "").trim());
				//System.out.println("v1: " + v1);
				//System.out.println("v2: " + v2);
				//System.out.println("v3: " + v3);
				
				Face face = new Face(v1, v2, v3);
				
				if (v1tc != 0 && v2tc != 0 && v3tc != 0 && hasTexture) {
					FaceMap faceMap = new FaceMap(textureId, v1tc, v2tc, v3tc);
					face.setFaceMap(faceMap);
				}
				
				f.add(face);
			} else if (p.equals("usemtl")) {
				line = line.substring(line.indexOf(" ")+1, line.length());
				String path  = line.substring(0, line.length()).trim();
				
				if (path.length()>0 && new File(path).exists()) {
					textureId++;
					t.add(new Texture(textureId, path));
					hasTexture = true;
				} else
					hasTexture = false;
			} else if (p.equals("vt")) {
				line = line.substring(line.indexOf(" ")+1, line.length()).trim();
				Double x = new Double(line.substring(0, line.indexOf(" ")));
				
				line = line.substring(line.indexOf(" ")+1, line.length()).trim();
				int i = line.indexOf(" ");
				Double y = 0.0;
				
				if (i != -1)
					y = new Double(line.substring(0, i));
				else
					y = new Double(line.substring(0, line.length()-1));
				
				tc.add(new TextureCoord(x, y));
			}
			
			fc = fc.substring(eol);
		}
		
		System.out.println("Object: " + object.getName() + " loaded!");
		System.out.println("Vertex: " + v.size());
		System.out.println("Texture coords: " + tc.size());
		System.out.println("Faces: " + f.size());
		System.out.println("Textures: " + t.size());
		
		object.setVertices(v);
		object.setFaces(f);
		object.setTextures(t);
		object.setTextureCoords(tc);
	}
	
	public Obj getObject() {
		return object;
	}
	
	public StringBuffer getFileContent() {
		return fileContent;
	}
}
