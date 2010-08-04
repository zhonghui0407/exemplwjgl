package fcampos.rawengine3D.graficos;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Hashtable;


import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.*;

import java.nio.FloatBuffer;


import javax.imageio.ImageIO;

import org.lwjgl.util.glu.GLU;



/**
 * A utility class to load textures for JOGL. This source is based
 * on a texture that can be found in the Java Gaming (www.javagaming.org)
 * Wiki. It has been simplified slightly for explicit 2D graphics use.
 * 
 * OpenGL uses a particular image format. Since the images that are 
 * loaded from disk may not match this format this loader introduces
 * a intermediate image which the source image is copied into. In turn,
 * this image is used as source for the OpenGL texture.
 *
 * @author Kevin Glass
 * @author Brian Matzon
 */
public class TextureLoader {
    /** The table of textures that have been loaded in this loader */
    private HashMap<String, Texture> table = new HashMap<String, Texture>();
    
    /** The color model including alpha for the GL image */
    private ColorModel glAlphaColorModel;
    
    /** The color model for the GL image */
    private ColorModel glColorModel;
    
    /** 
     * Create a new texture loader based on the game panel
     *
     * @param gl The GL content in which the textures should be loaded
     */
    public TextureLoader() {
        glAlphaColorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
                                            new int[] {8,8,8,8},
                                            true,
                                            false,
                                            ComponentColorModel.TRANSLUCENT,
                                            DataBuffer.TYPE_BYTE);
                                            
        glColorModel = new ComponentColorModel(ColorSpace.getInstance(ColorSpace.CS_sRGB),
                                            new int[] {8,8,8,0},
                                            false,
                                            false,
                                            ComponentColorModel.OPAQUE,
                                            DataBuffer.TYPE_BYTE);
    }
    
    /**
     * Create a new texture ID 
     *
     * @return A new texture ID
     */
    private int createTextureID() 
    { 
       IntBuffer tmp = createIntBuffer(1); 
       GL11.glGenTextures(tmp); 
       return tmp.get(0);
    } 
    
    /**
     * Load a texture
     *
     * @param resourceName The location of the resource to load
     * @return The loaded texture
     * @throws IOException Indicates a failure to access the resource
     */
    public Texture getTexture(String resourceName, int x, int y, boolean mipmap, boolean useAnisotropicFilter) throws IOException {
        Texture tex = (Texture) table.get(resourceName);
        
        if (tex != null) {
            return tex;
        }
        
        
        
        tex = getTexture(resourceName,
                         GL11.GL_TEXTURE_2D, // target
                         GL11.GL_RGBA,     // dst pixel format
                          x, y, mipmap, useAnisotropicFilter);
        
        table.put(resourceName,tex);
        
        return tex;
    }
    
    /**
     * Load a texture into OpenGL from a image reference on
     * disk.
     *
     * @param resourceName The location of the resource to load
     * @param target The GL target to load the texture against
     * @param dstPixelFormat The pixel format of the screen
     * @param minFilter The minimising filter
     * @param magFilter The magnification filter
     * @return The loaded texture
     * @throws IOException Indicates a failure to access the resource
     */
    @SuppressWarnings("unused")
	public Texture getTexture(String resourceName, 
                              int target, 
                              int dstPixelFormat, 
                              int x, int y, boolean mipmap, boolean useAnisotropicFilter) throws IOException
    { 
        int srcPixelFormat = 0;
        int minFilter;
        int magFilter;
        int index;
        
        // create the texture ID for this texture 
        int textureID = createTextureID(); 
        Texture texture = new Texture(target,textureID);
        index = resourceName.lastIndexOf("/") + 1;
        String extension = resourceName.substring(resourceName.lastIndexOf(".") + 1);
        texture.setName(resourceName.substring(index));
        
        // bind this texture 
        GL11.glBindTexture(target, textureID); 
        
        ByteBuffer textureBuffer;
        int width;
        int height;
        int texWidth;
        int texHeight;
        if(!extension.equalsIgnoreCase("tga"))
        {
	        BufferedImage bufferedImage = loadImage(resourceName); 
	        texture.setWidth(bufferedImage.getWidth());
	        texture.setHeight(bufferedImage.getHeight());
	        
	        width = bufferedImage.getWidth();
	        height = bufferedImage.getHeight();
	        texWidth = bufferedImage.getWidth();
	        texHeight = bufferedImage.getHeight();
	        
	        if (bufferedImage.getColorModel().hasAlpha()) {
	            srcPixelFormat = GL11.GL_RGBA;
	        } else {
	            srcPixelFormat = GL11.GL_RGB;
	        }

        // convert that image into a byte buffer of texture data 
        textureBuffer = convertImageData(bufferedImage,texture, x, y);
        }else{
        	
             
             boolean hasAlpha;
             
         	textureBuffer = TGALoader.loadImage(new BufferedInputStream(getBufferedInputStream(resourceName)));
         	
         	width = TGALoader.getLastWidth();
         	height = TGALoader.getLastHeight();
         	hasAlpha = TGALoader.getLastDepth() == 32;
         	
         	texture.setTextureWidth(TGALoader.getLastTexWidth());
         	texture.setTextureHeight(TGALoader.getLastTexHeight());

             texWidth = texture.getImageWidth();
             texHeight = texture.getImageHeight();
             
             srcPixelFormat = hasAlpha ? GL11.GL_RGBA : GL11.GL_RGB;
             int componentCount = hasAlpha ? 4 : 3;
             
             texture.setWidth(width);
             texture.setHeight(height);

        }
        
        
        if(useAnisotropicFilter)
		{
			  // Due to LWJGL buffer check, you can't use smaller sized buffers (min_size = 16 for glGetFloat()).
			final FloatBuffer max_a = BufferUtils.createFloatBuffer(16);
			max_a.rewind();

			  // Grab the maximum anisotropic filter.
			GL11.glGetFloat(EXTTextureFilterAnisotropic.GL_MAX_TEXTURE_MAX_ANISOTROPY_EXT, max_a);

			  // Set up the anisotropic filter.
			GL11.glTexParameterf(target, EXTTextureFilterAnisotropic.GL_TEXTURE_MAX_ANISOTROPY_EXT, max_a.get(0));
			
			texture.setUseAnisotropic(true);
		}

        if(false)
        {
        	minFilter = GL11.GL_LINEAR;
        	magFilter = GL11.GL_NEAREST;
        }else{
        	minFilter = GL11.GL_LINEAR_MIPMAP_NEAREST;
        	magFilter = GL11.GL_LINEAR;
        }
        
        
        GL11.glTexParameteri(target, GL11.GL_TEXTURE_MIN_FILTER, minFilter); 
        GL11.glTexParameteri(target, GL11.GL_TEXTURE_MAG_FILTER, magFilter); 
        
        
        if (!mipmap) 
        { 
            
            
            // Check if GL_EXT_texture_compression_s3tc extension if available.
            
            ContextCapabilities cCap = GLContext.getCapabilities();
            int pixelFormat;
    		if(cCap.GL_EXT_texture_compression_s3tc)
    		{
    			  // If so, generate the texture using the DXT5 compression method (best copression).
    			  // Notice that we now use GL_RGBA to generate the texture.
    			System.out.println("GL_COMPRESSED_RGBA_S3TC_DXT5_EXT available !");
    			pixelFormat = EXTTextureCompressionS3TC.GL_COMPRESSED_RGBA_S3TC_DXT5_EXT;
    		}
    		else
    		{
    		 	  // If extension isn't available (should only be the case on old video cards), generate
    			  // an uncompressed texture.
    			System.out.println("GL_COMPRESSED_RGBA_S3TC_DXT5_EXT unavailable...");
    			// produce a texture from the byte buffer
    	        pixelFormat = dstPixelFormat;
    		}
    		
	    		GL11.glTexImage2D(target, 
	                    0, 
	                    pixelFormat, 
	                    get2Fold(width), 
	                    get2Fold(height), 
	                    0, 
	                    srcPixelFormat, 
	                    GL11.GL_UNSIGNED_BYTE, 
	                    textureBuffer); 
    		
	    		return texture;
       
        }else
        	{
   
            // produce a texture from the byte buffer
        	try{
        		
            GLU.gluBuild2DMipmaps(target, 
                              dstPixelFormat, 
                              texWidth, 
                              texHeight, 
                              srcPixelFormat, 
                              GL11.GL_UNSIGNED_BYTE, 
                              textureBuffer ); 
            return texture;
            
        	}catch(OpenGLException o)
        	{
        		o.toString();
        	}
        	}
        return null; 
    } 
    
    /**
     * Get the closest greater power of 2 to the fold number
     * 
     * @param fold The target number
     * @return The power of 2
     */
    private int get2Fold(int fold) {
        int ret = 2;
        while (ret < fold) {
            ret *= 2;
        }
        return ret;
    } 
    
    /**
     * Convert the buffered image to a texture
     *
     * @param bufferedImage The image to convert to a texture
     * @param texture The texture to store the data into
     * @return A buffer containing the data
     */
    private ByteBuffer convertImageData(BufferedImage bufferedImage,Texture texture, int x, int y) {
        ByteBuffer imageBuffer = null; 
        WritableRaster raster;
        BufferedImage texImage;
        
        int texWidth = 2;
        int texHeight = 2;
        
        // find the closest power of 2 for the width and height
        // of the produced texture
        while (texWidth < bufferedImage.getWidth()) {
            texWidth *= 2;
        }
        while (texHeight < bufferedImage.getHeight()) {
            texHeight *= 2;
        }
        
        texture.setTextureHeight(texHeight);
        texture.setTextureWidth(texWidth);
        
        // create a raster that can be used by OpenGL as a source
        // for a texture
        if (bufferedImage.getColorModel().hasAlpha()) {
            raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,texWidth,texHeight,4,null);
            texImage = new BufferedImage(glAlphaColorModel,raster,false,new Hashtable<Object, Object>());
        } else {
            raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,texWidth,texHeight,3,null);
            texImage = new BufferedImage(glColorModel,raster,false,new Hashtable<Object, Object>());
        }
            
        // copy the source image into the produced image
        //Graphics g = texImage.getGraphics();
        Graphics2D g = (Graphics2D) texImage.getGraphics();
        g.setColor(new Color(0f,0f,0f,0f));
        g.fillRect(0,0,texWidth,texHeight);
        AffineTransform transform = new AffineTransform();
        transform.scale(x, y);
        transform.translate(
            (x-1) * bufferedImage.getWidth() / 2,
            (y-1) * bufferedImage.getHeight() / 2);

        g.drawImage(bufferedImage,transform,null);
        
        // build a byte buffer from the temporary image 
        // that be used by OpenGL to produce a texture.
        byte[] data = ((DataBufferByte) texImage.getRaster().getDataBuffer()).getData(); 

        imageBuffer = ByteBuffer.allocateDirect(data.length); 
        imageBuffer.order(ByteOrder.nativeOrder()); 
        imageBuffer.put(data, 0, data.length); 
        imageBuffer.flip();
        
        return imageBuffer; 
    } 
    
    private BufferedInputStream getBufferedInputStream(String ref) throws IOException
    {
 
        
    	File file = new File(ref);
    	System.out.println(file.getAbsolutePath());
    	 InputStream is=null;
        
        try {
        	is = new FileInputStream(file);
        	
        } catch (IOException e) {
            e.toString();
        }
       
         BufferedInputStream buffer = new BufferedInputStream(is);
         
        return buffer;
       
    }
    
    /** 
     * Load a given resource as a buffered image
     * 
     * @param ref The location of the resource to load
     * @return The loaded buffered image
     * @throws IOException Indicates a failure to find a resource
     */
    @SuppressWarnings("unused")
	private BufferedImage loadImage(String ref) throws IOException 
    { 
    	
    	
        //URL url = TextureLoader.class.getClassLoader().getResource(ref);
        
    	File file = new File(ref);
    	System.out.println(file.getAbsolutePath());
    	 if (file == null) {
             throw new IOException("Cannot find: "+ref);
         }
    	
    	
        InputStream is=null;
        
        try {
        	is = new FileInputStream(file);
        	
        } catch (IOException e) {
            e.toString();
        }
       
         
         
        
       //if (url == null) {
       //     throw new IOException("Cannot find: "+ref);
      //  }
       
       
        
        BufferedImage bufferedImage = ImageIO.read(new BufferedInputStream(is)); 
        
        return bufferedImage;
       
    }
    
    /**
     * Creates an integer buffer to hold specified ints
     * - strictly a utility method
     *
     * @param size how many int to contain
     * @return created IntBuffer
     */
    protected IntBuffer createIntBuffer(int size) {
      ByteBuffer temp = ByteBuffer.allocateDirect(4 * size);
      temp.order(ByteOrder.nativeOrder());

      return temp.asIntBuffer();
    }    
}
