package MaixSenseA010;


import jssc.SerialPortException;
import maixsense.a010.MaixSenseA010Driver;
import maixsense.a010.MaixSenseA010Image;
import maixsense.a010.MaixSenseA010ImageConsumer;
import maixsense.a010.MaixSenseA010ImageQueue;
import processing.core.PApplet;
import processing.core.PShape;
import processing.event.MouseEvent;
import util.MaixSenseA010DepthImageAdapter;



/**
 * Example on how to plot a mesh from a depth image captured with the MaixSenseA010 ToF camera.
 * <p>
 * Controls:
 * <ul>
 *  <li> Mouse position: camera viewpoint.
 *  <li> Mouse wheel: zoom.
 * </ul>
 */
public class MaixSenseA010Mesh3dViewer
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
    
    /**
     * Factor used to scale the pixel indices so that the mesh visualization is improved.
     */
    static final float XY_FACTOR = 1.0e-2f;
    
    
    
    ////////////////////////////////////////////////////////////////
    // VARIABLES
    ////////////////////////////////////////////////////////////////
    
    /**
     * {@link PShape} that holds the mesh generated from the last received depth image.
     */
    PShape meshShape;
    
    /**
     * Zoom set with the mouse wheel.
     */
    int zoom;
    
    
    
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
        PApplet.main("MaixSenseA010.MaixSenseA010Mesh3dViewer");
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
            a010.setQuantizationUnit( QUANTIZATION_UNIT );
            a010.setAntiMultiMachineInterferenceOff();
            a010.setExposureTimeAutoOn();
            
        } catch( SerialPortException e ) {
            e.printStackTrace();
        }
        
        // Initialize zoom variable.
        this.zoom = ( 1 << 8 );
    }
    
    
    /**
     * Identical to "draw" in Processing IDE.
     */
    public void draw()
    {
        // Background initialization.
        background(0);
        // Set center to origin.
        translate( width/2 , height/2 , 0 );
        // Make reference frame right-handed.
        scale( 1 , -1 , 1 );
        // Scale using the zoom variable (controlled with mouse wheel).
        scale( this.zoom );
        // Rotate scene using the mouse position.
        rotateX( (float)Math.PI );
        double theta = -( mouseY - height/2 ) * Math.PI / height;
        rotateX( (float)theta );
        double phi = ( mouseX - width/2 ) * Math.PI / width;
        rotateY( (float)phi );
        // Set lights for illumination.
        //pointLight( 100 , 100 , 100 , 0 , (float)( 2 * DEPTH_RANGE_MAX ) , 0 );
        directionalLight( 30 , 0 , 0 , -1 , 0 , 0 );
        directionalLight( 0 , 30 , 0 , 0 , -1 , 0 );
        directionalLight( 0 , 0 , 30 , 0 , 0 , -1 );
        ambientLight( 200 , 200 , 200 , 0 , -(float)DEPTH_RANGE_MAX , 0 );
        // Draw reference frame lines.
        strokeWeight( 1.0e-4f );
        stroke( color(255,0,0) );
        line( -100 , 0 , 0 , 100 , 0 , 0 );
        stroke( color(0,255,0) );
        line( 0 , -100 , 0 , 0 , 100 , 0 );
        stroke( color(0,0,255) );
        line( 0 , 0 , -100 , 0 , 0 , 100 );
        // Draw point cloud.
        synchronized( this ) {
            if( this.meshShape != null ) {
                shape( this.meshShape );
            }
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
        // Generate PShape from depth image.
        PShape newMeshShape = createShape();
        newMeshShape.beginShape( TRIANGLES );
        newMeshShape.stroke( 0 );
        int imageRowsHalf = adaptedImage.rows()/2;
        int imageColumnsHalf = adaptedImage.cols()/2;
        for( int i=0; i<adaptedImage.rows()-1; i++ ) {
            // Create lower triangles of the current strip.
            for( int j=0; j<adaptedImage.cols()-1; j++ ) {
                // Take pixel bytes, and convert them to depth in millimeters.
                double depthA = adaptedImage.depth( i , j );
                double depthB = adaptedImage.depth( i , j+1 );
                double depthC = adaptedImage.depth( i+1 , j );
                // Set triangle color.
                newMeshShape.fill( (float)( ( DEPTH_RANGE_MAX - ( depthA + depthB + depthC )/3 ) * 255/DEPTH_RANGE_MAX ) );
                // Create triangle.
                newMeshShape.vertex( (j-imageColumnsHalf)*XY_FACTOR , (i-imageRowsHalf)*XY_FACTOR , (float)depthA );
                newMeshShape.vertex( (j+1-imageColumnsHalf)*XY_FACTOR , (i-imageRowsHalf)*XY_FACTOR , (float)depthB );
                newMeshShape.vertex( (j-imageColumnsHalf)*XY_FACTOR , (i+1-imageRowsHalf)*XY_FACTOR , (float)depthC );
            }
            // Create upper triangles of the current strip.
            for( int j=0; j<image.cols()-1; j++ ) {
                // Take pixel bytes, and convert them to depth in millimeters.
                double depthA = adaptedImage.depth( i , j+1 );
                double depthB = adaptedImage.depth( i+1 , j );
                double depthC = adaptedImage.depth( i+1 , j+1 );
                // Set triangle color.
                newMeshShape.fill( (float)( ( DEPTH_RANGE_MAX - ( depthA + depthB + depthC )/3 ) * 255/DEPTH_RANGE_MAX ) );
                // Create triangle.
                newMeshShape.vertex( (j+1-imageColumnsHalf)*XY_FACTOR , (i-imageRowsHalf)*XY_FACTOR , (float)depthA );
                newMeshShape.vertex( (j-imageColumnsHalf)*XY_FACTOR , (i+1-imageRowsHalf)*XY_FACTOR , (float)depthB );
                newMeshShape.vertex( (j+1-imageColumnsHalf)*XY_FACTOR , (i+1-imageRowsHalf)*XY_FACTOR , (float)depthC );
            }
        }
        newMeshShape.endShape();
        // Update PShape.
        synchronized( this ) {
            this.meshShape = newMeshShape;
        }
    }
    
    
    /**
     * Behavior when a mouse wheel event is captured.
     */
    public void mouseWheel( MouseEvent event )
    {
        float e = event.getCount();
        if( e > 0 ) {
            this.zoom *= 2;
        } else {
            this.zoom /= 2;
        }
        if( this.zoom < 1 ) {
            this.zoom = 1;
        }
    }
    
}
