package MaixSenseA010;


import java.util.List;

import a010.MaixSenseA010Driver;
import a010.MaixSenseA010Image;
import a010.MaixSenseA010ImageConsumer;
import a010.MaixSenseA010ImageQueue;
import jssc.SerialPortException;
import processing.core.PApplet;
import processing.core.PShape;
import processing.event.MouseEvent;
import util.RealVector3;
import util.calibration.MaixSenseA010DefaultCalibration;



/**
 * Example on how to plot a point cloud from a depth image captured with the MaixSenseA010 ToF camera.
 * <p>
 * Controls:
 * <ul>
 *  <li> Mouse position: camera viewpoint.
 *  <li> Mouse wheel: zoom.
 * </ul>
 */
public class MaixSenseA010PointCloudViewer
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
    
    
    
    ////////////////////////////////////////////////////////////////
    // VARIABLES
    ////////////////////////////////////////////////////////////////
    
    /**
     * Calibration used to transform depth images to point clouds.
     */
    MaixSenseA010DefaultCalibration depthCameraCalibration;
    
    /**
     * {@link PShape} that holds the point cloud generated from the last received depth image.
     */
    PShape pointCloudShape;
    
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
        PApplet.main("MaixSenseA010.MaixSenseA010PointCloudViewer");
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
        // Create DepthCameraCalibration; we take the default one.
        this.depthCameraCalibration = new MaixSenseA010DefaultCalibration();
        this.depthCameraCalibration.setQuantizationUnit( QUANTIZATION_UNIT );
        
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
            if( this.pointCloudShape != null ) {
                shape( this.pointCloudShape );
            }
        }
    }
    
    
    /**
     * {@inheritDoc}
     */
    public void consumeImage( MaixSenseA010Image image )
    {
        // Generate point cloud from image.
        List<RealVector3> pointCloud = this.depthCameraCalibration.imageToPointCloud( image );
        // Generate PShape from point cloud.
        PShape newPointCloudShape = createShape();
        newPointCloudShape.beginShape( POINTS );
        newPointCloudShape.strokeWeight( (float)2.0e0 );
        newPointCloudShape.stroke( 255 );
        for( RealVector3 point : pointCloud ) {
            newPointCloudShape.vertex( (float)point.x() , (float)point.y() , (float)point.z() );
        }
        newPointCloudShape.endShape();
        // Update PShape.
        synchronized( this ) {
            this.pointCloudShape = newPointCloudShape;
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
