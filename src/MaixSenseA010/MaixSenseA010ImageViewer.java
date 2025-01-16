package MaixSenseA010;


import jssc.SerialPortException;
import maixsense.a010.MaixSenseA010Driver;
import maixsense.a010.MaixSenseA010Image;
import maixsense.a010.MaixSenseA010ImageConsumer;
import maixsense.a010.MaixSenseA010ImageQueue;
import processing.core.PApplet;
import processing.core.PImage;
import util.MaixSenseA010DepthImageAdapter;



/**
 * Example on how to plot a depth image captured with the MaixSenseA010 ToF camera.
 */
public class MaixSenseA010ImageViewer
    extends PApplet
    implements MaixSenseA010ImageConsumer
{
    ////////////////////////////////////////////////////////////////
    // PARAMETERS
    ////////////////////////////////////////////////////////////////
    
    /**
     * Quantization unit used for both the calibration and the driver.
     */
    static final int QUANTIZATION_UNIT = 0;
    
    /**
     * Maximum depth that the sensor can measure in meters.
     */
    static final double DEPTH_RANGE_MAX = 2.5;
    
    
    
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
            
            a010.setLcdDisplayOff();
            a010.setUsbDisplayOn();
            a010.setUartDisplayOff();
            
            a010.setBinning100x100();
            //a010.setBinning50x50();
            //a010.setBinning25x25();
            a010.setFps( 20 );
            
            a010.setQuantizationUnit( QUANTIZATION_UNIT );
            a010.setAntiMultiMachineInterferenceOff();
            
            a010.setExposureTimeAutoOn();
            
            a010.setImageSignalProcessorOn();
            
        } catch( SerialPortException e ) {
            e.printStackTrace();
        }
    }
    
    
    /**
     * Identical to "draw" in Processing IDE.
     */
    public void draw()
    {
        if( this.depthImage != null ) {
            image( this.depthImage.copy() , 0 , 0 , width , height );
        }
    }
    
    
    /**
     * {@inheritDoc}
     */
    public void consumeImage( MaixSenseA010Image image )
    {
        // Adapt MaixSenseA010Image to be a DepthImage.
        MaixSenseA010DepthImageAdapter adaptedImage = new MaixSenseA010DepthImageAdapter( image );
        adaptedImage.setQuantizationUnit( QUANTIZATION_UNIT );
        // Update PImage.
        if(  this.depthImage == null  ||  this.depthImage.width != adaptedImage.cols()  ||  this.depthImage.height != adaptedImage.rows()  ) {
            this.depthImage = createImage( adaptedImage.cols() , adaptedImage.rows() , RGB );
        }
        colorMode( RGB , (float)DEPTH_RANGE_MAX );
        for( int i=0; i<adaptedImage.rows(); i++ ) {
            for( int j=0; j<adaptedImage.cols(); j++ ) {
                // Get depth value.
                double depth = adaptedImage.depth( i , j );
                // Set color in depth image.
                this.depthImage.set( j,i , color( (float)(DEPTH_RANGE_MAX-depth) ) );
            }
        }
    }
    
}
