package MaixSenseA010;


import processing.core.PApplet;



public class MaixSenseA010Viewer
    extends PApplet
{
    /**
     * Entry point.
     * <p>
     * The argument passed to "main" must match the class name.
     * 
     * @param args
     */
    public static void main(String[] args)
    {
        PApplet.main("MaixSenseA010.MaixSenseA010Viewer");
    }
    
    
    /**
     * Initial settings.
     * <p>
     * Needed in addition to "setup" to set the size of the window.
     */
    public void settings()
    {
        System.setProperty( "jogl.disable.opengles" , "false" );
        System.setProperty( "jogamp.gluegen.UseTempJarCache" , "false" );
        size( 1000 , 1000 , P3D );
        smooth(8);
    }
    
    
    /**
     * Identical to "setup" in Processing IDE except for "size" that must be set in "settings".
     */
    public void setup()
    {
    }
    
    
    /**
     * Identical to "draw" in Processing IDE.
     */
    public void draw()
    {
    }
    
}
