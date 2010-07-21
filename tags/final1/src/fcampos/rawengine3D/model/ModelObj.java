package fcampos.rawengine3D.model;


import fcampos.rawengine3D.loader.TObjectLoader;

public class ModelObj extends Model3d {
	
	private TObjectLoader loader;
	
	public ModelObj()
	{
		super();
	}
	
	public boolean load(String arqName, boolean mipmap, boolean useAnisotropicFilter)
	{
		loader = new TObjectLoader();
		try{
			loader.carregaObjeto(arqName, mipmap, useAnisotropicFilter, this);
			return true;
		}catch (Exception e) {
			return false;
			// TODO: handle exception
		}
		//System.out.println(pObject.get(0).getName());
		
	}
	
	public void draw(ModelObj world)
	{
		//System.out.println(pObject.get(0).getName());
		for(int i=0; i< object.size(); i++)
		{
			Object3d obj = object.get(i);
			obj.draw(world);
		}
	}

}
