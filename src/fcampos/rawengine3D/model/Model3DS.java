package fcampos.rawengine3D.model;

import java.io.IOException;

import fcampos.rawengine3D.loader.Loader3DS;

public class Model3DS extends Model3d{
	
	protected Loader3DS loader3DS;
	
	public Model3DS()
	{
		super();
	}

	@Override
	public boolean load(String fileName)
	{
			loader3DS = new Loader3DS();
			try{
				loader3DS.import3DS(this, fileName);
				return true;
			}catch (IOException e) {
				System.out.println(e.getMessage());
				return false;
			}
	
	}
	
	

}
