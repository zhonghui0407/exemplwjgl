package fcampos.rawengine3D.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;

public final class TextFile {
	
	private TextFile()
	{
		
	}
	
	
	public final static BufferedReader openFile(String fileName)
	{
		BufferedReader reader = null;
		File fp = new File(fileName);
		System.out.println(fp.getAbsolutePath());
		if (fp.exists())
		{
			try
			{
			reader = new BufferedReader(new FileReader(fp)); // abre arquivo texto para leitura
			}catch(FileNotFoundException f)
			{
				f.getMessage();
			}
		}
		return reader;
	}

}
