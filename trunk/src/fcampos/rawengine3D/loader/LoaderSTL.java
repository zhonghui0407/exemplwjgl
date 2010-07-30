package fcampos.rawengine3D.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;

import fcampos.rawengine3D.MathUtil.Vector3f;
import fcampos.rawengine3D.io.LEDataInputStream;
import fcampos.rawengine3D.model.Face;
import fcampos.rawengine3D.model.ModelSTL;
import fcampos.rawengine3D.model.Object3d;

public class LoaderSTL {
	
	private LEDataInputStream inputStream;
	//private String header = null;
	private ModelSTL model;
	
	ArrayList<Vector3f> vertices;
	
	Hashtable<String, Integer> verticeHash;
	
	
	public LoaderSTL()
	{
		
	}
	
	public boolean importSTL(ModelSTL model, String fileName)
	{
		
		try
		{
			
			File file = new File(fileName);
			
			System.out.println(file.getAbsolutePath());
		
			FileInputStream inStream = new FileInputStream(file);
			
					
			inputStream = new LEDataInputStream(inStream);
			
			this.model = model;
						
			readSTLData(inputStream);
			
			return true;
			
		}catch (Exception e) {
			System.out.println(e.getMessage());
			System.out.println("[Error]: can't read " + fileName + " correctly.");
			return false;
		}
	}
	
	private void readSTLData(LEDataInputStream inputStream) throws IOException
	{
		
			
		Object3d object = new Object3d();
		inputStream.skipBytes(80);
		
		int numOfFaces = inputStream.readLEInt();
		
		vertices = new ArrayList<Vector3f>(numOfFaces * 2/3);
		
		verticeHash = new Hashtable<String, Integer>(numOfFaces * 2/3);
		
		System.out.println("Número de triângulos: "+ numOfFaces);
		
		object.setNumFaces(numOfFaces);
		object.setNumNormais(numOfFaces);
			
		for(int i=0; i < object.getNumFaces(); i++)
		{
			Vector3f normals = new Vector3f(inputStream.readLEFloat(), inputStream.readLEFloat(), inputStream.readLEFloat());
			Face face = object.getFace(i);
			object.setNormal(normals, i);
			for(int j=0; j < 3; j++)
			{
				Vector3f vertice = new Vector3f(inputStream.readLEFloat(), inputStream.readLEFloat(), inputStream.readLEFloat());
				int index = addToList(vertice, vertices, verticeHash);
				face.setVertices(j, index);
				face.setNormal(j, index);
			}
			inputStream.skipBytes(Short.SIZE/8);
			
		}
		
		vertices.trimToSize();
		System.out.println("Número de vértices: " + vertices.size());
		object.setNumVert(vertices.size());
		Vector3f[] array = new Vector3f[vertices.size()];
		array = vertices.toArray(array);
		object.setVertices(array);
		model.addObject(object);
		System.out.println("Objeto carregado");
		
	
	}
	
	private int addToList(Vector3f vertice, ArrayList<Vector3f> vertices, Hashtable<String, Integer> verticeHash)
	{
		String key = vertice.toString();
		
		if(verticeHash.containsKey(key))
		{
			return verticeHash.get(key);
		}else{
			vertices.add(vertice);
			int index = vertices.size()-1;
			verticeHash.put(key, index);
			return index;
		}
		/*
		for(int i=0; i < vertices.size(); i++)
		{
			Vector3f tempVertice = vertices.get(i);
			float distance = VectorMath.distance(tempVertice, vertice);
			System.out.println(distance);
			
			if(distance <= 0.01)
			{
				return i;
			}
		}
		
		*/
		
		
		//return vertices.size()-1;
	}

}
