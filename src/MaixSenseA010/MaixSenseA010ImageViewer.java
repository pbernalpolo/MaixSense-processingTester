package MaixSenseA010;


import a010.MaixSenseA010Driver;
import a010.MaixSenseA010Image;
import a010.MaixSenseA010ImageConsumer;
import a010.MaixSenseA010ImageQueue;
import jssc.SerialPortException;
import processing.core.PApplet;
import processing.core.PImage;



/**
 * Example on how to plot a depth image captured with the MaixSenseA010 ToF camera.
 */
public class MaixSenseA010ImageViewer
    extends PApplet
    implements MaixSenseA010ImageConsumer
{
    ////////////////////////////////////////////////////////////////
    // VARIABLES
    ////////////////////////////////////////////////////////////////
    
    /**
     * {@link PImage} that holds the last depth image received from the last received depth image.
     */
    PImage depthImage;
    
    
    
    ////////////////////////////////////////////////////////////////
    // MAIN: ENTRY POINT
    ////////////////////////////////////////////////////////////////
    
    /**
     * Entry point.
     * <p>
     * The argument passed to "main" must match the class name.
     * 
     * @param args
     */
    public static void main(String[] args)
    {
        PApplet.main("MaixSenseA010.MaixSenseA010ImageViewer");
    }
    
    
    
    ////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    ////////////////////////////////////////////////////////////////
    
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
        // Create the image queue,
        MaixSenseA010ImageQueue imageQueue = new MaixSenseA010ImageQueue();
        // and add the listener; in this case it is the MaixSenseA010Viewer itself.
        imageQueue.addListener( this );
        
        // Create the driver,
        MaixSenseA010Driver a010 = new MaixSenseA010Driver( "/dev/ttyUSB0" );
        // and connect the queue so that received images are added to it.
        a010.connectQueue( imageQueue );
        
        // Configure the MaixSense-A010 ToF camera.
        try {
            a010.initialize();
            
            a010.setImageSignalProcessorOn();
            
            a010.setLcdDisplayOff();
            a010.setUsbDisplayOn();
            a010.setUartDisplayOff();
            
            a010.setBinning100x100();
            a010.setFps( 20 );
            
        } catch( SerialPortException e ) {
            e.printStackTrace();
        }
    }
    
    
    /**
     * Identical to "draw" in Processing IDE.
     */
    public void draw()
    {
        synchronized( this ) {
            if( this.depthImage != null ) {
                image( this.depthImage , 0 , 0 , width , height );
            }
        }
    }
    
    
    /**
     * {@inheritDoc}
     */
    public void consumeImage( MaixSenseA010Image image )
    {
        synchronized( this ) {
            if(  this.depthImage == null  ||  this.depthImage.width != image.cols()  ||  this.depthImage.height != image.rows()  ) {
                this.depthImage = createImage( image.cols() , image.rows() , RGB );
            }
            for( int i=0; i<image.rows(); i++ ) {
                for( int j=0; j<image.cols(); j++ ) {
                    // Take pixel byte and cast its unsigned representation to an int.
                    int depth = image.pixel(i,j) & 0xff;
                    // Set color in depth image.
                    this.depthImage.set( j,i , color( 255-depth ) );
                }
            }
        }
    }
    
}
