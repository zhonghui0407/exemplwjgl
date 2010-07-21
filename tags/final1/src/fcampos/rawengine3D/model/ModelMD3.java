package fcampos.rawengine3D.model;


import fcampos.rawengine3D.loader.LoaderMD3;
import fcampos.rawengine3D.loader.LoaderMD3.TagMD3;

public class ModelMD3 extends Model3d {
	
	protected ModelMD3[]	links;				// This stores a list of pointers that are linked to this model
	protected TagMD3[]	tags;			// This stores all the tags for the model animations
	protected int numOfTags;						// This stores the number of tags in the model
	
	public LoaderMD3 loaderMD3;					// This object allows us to load the.md3 and .shader file

	
	public ModelMD3()
	{
		super();
		
	}
	
	/**
	 * @param pLinks the pLinks to set
	 */
	public void setLinks(ModelMD3[] pLinks) 
	{
		this.links = pLinks;
	}
	
	/**
	 * @param pLinks the pLinks to set
	 */
	public void setLinks(ModelMD3 pLinks, int index) 
	{
		this.links[index] = pLinks;
	}
	
	/**
	 * @param links the pLinks to set
	 */
	public void setNumLinks(int total) 
	{
		links = new ModelMD3[total];
		for(int i = 0; i < total; i++)
			links[i] = null;
	}


	/**
	 * @return the pLinks
	 */
	public ModelMD3[] getLinks() 
	{
		return links;
	}
	
	/**
	 * @return the pLinks
	 */
	public ModelMD3 getLinks(int index) 
	{
		return links[index];
	}


	/**
	 * @param pTags the pTags to set
	 */
	public void setTags(TagMD3[] pTags) 
	{
		this.tags = pTags;
	}
	
	/**
	 * @param pTags the pTags to set
	 */
	public void setTags(TagMD3 pTags, int index) 
	{
		this.tags[index] = pTags;
	}
	
	/**
	 * @param tags the pTags to set
	 */
	public void setNumTags(int total)
	{
		tags = new TagMD3[total];
				
	}


	/**
	 * @return the pTags
	 */
	public TagMD3[] getTags() 
	{
		return tags;
	}
	
	/**
	 * @return the pTags
	 */
	public TagMD3 getTags(int index) 
	{
		return tags[index];
	}
	
	
	
	/**
	 * @param numOfTags the numOfTags to set
	 */
	public void setNumOfTags(int numOfTags) 
	{
		this.numOfTags = numOfTags;
	}


	/**
	 * @return the numOfTags
	 */
	public int getNumOfTags() 
	{
		return numOfTags;
	}

	@Override
	public boolean load(String fileName)
	{
		loaderMD3 = new LoaderMD3();
		
		return loaderMD3.importMD3(this, fileName);
		
	}
	
	public boolean loadSkin(String fileSkin)
	{
		return loaderMD3.loadSkin(this, fileSkin);
	}
	
	public boolean loadShader(String fileShader)
	{
		return loaderMD3.loadShader(this, fileShader);
	}


	
	

}
