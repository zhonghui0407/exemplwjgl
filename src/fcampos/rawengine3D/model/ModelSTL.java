package fcampos.rawengine3D.model;


import fcampos.rawengine3D.loader.LoaderSTL;


public class ModelSTL extends Model3d {
	
	private LoaderSTL loader;
	
	public ModelSTL()
	{
		super();
	}
	
	public boolean load(String fileName)
	{
		loader = new LoaderSTL();
		try{
			loader.importSTL(this, fileName);
			//VectorMath.computeNormals(this);
			return true;
		}catch (Exception e) {
			return false;
			// TODO: handle exception
		}
		//System.out.println(pObject.get(0).getName());
		
	}
	
	public void draw()
	{
		//System.out.println(pObject.get(0).getName());
		for(int i=0; i< object.size(); i++)
		{
			Object3d obj = object.get(i);
			//System.out.println(obj.getName());
			obj.draw(this);
		}
	}
	

}
