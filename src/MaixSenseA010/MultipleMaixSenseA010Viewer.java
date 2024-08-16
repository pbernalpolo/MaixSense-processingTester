package MaixSenseA010;


import java.util.List;

import jssc.SerialPortException;
import maixsense.a010.MaixSenseA010Driver;
import maixsense.a010.MaixSenseA010Image;
import maixsense.a010.MaixSenseA010ImageConsumer;
import maixsense.a010.MaixSenseA010ImageQueue;
import numericalLibrary.types.Vector3;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PShape;
import processing.event.MouseEvent;
import sensorCalibrationLibrary.depthCameras.maixSenseA010.MaixSenseA010DefaultCalibration;
import util.MaixSenseA010DepthImageAdapter;



/**
 * Example on how to process and plot images and point clouds of multiple MaixSense-A010 at the same time.
 * <p>
 * In this example we can observe the effect of multi-machine interference.
 * <p>
 * Controls:
 * <ul>
 *  <li> Mouse position: camera viewpoint.
 *  <li> Mouse wheel: zoom.
 * </ul>
 */
public class MultipleMaixSenseA010Viewer
    extends PApplet
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
     * Driver that receives the images from the first MaixSense-A010 and stores them in {@link #imageQueue1}.
     */
    MaixSenseA010Driver tofCamera1;
    
    /**
     * Driver that receives the images from the second MaixSense-A010 and stores them in {@link #imageQueue2}.
     */
    MaixSenseA010Driver tofCamera2;
    
    /**
     * Queue that stores the images received by the first MaixSense-A010.
     */
    MaixSenseA010ImageQueue imageQueue1;
    
    /**
     * Queue that stores the images received by the second MaixSense-A010.
     */
    MaixSenseA010ImageQueue imageQueue2;
    
    /**
     * Consumes images from {@link #imageQueue1} and transforms them to a {@link PImage} and a point cloud.
     */
    DepthImageDataHolder dataHolder1;
    
    /**
     * Consumes images from {@link #imageQueue1} and transforms them to a {@link PImage} and a point cloud.
     */
    DepthImageDataHolder dataHolder2;
    
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
        PApplet.main("MaixSenseA010.MultipleMaixSenseA010Viewer");
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
        // Create data holders.
        this.dataHolder1 = new DepthImageDataHolder( color(0,255,0) );
        this.dataHolder2 = new DepthImageDataHolder( color(0,0,255) );
        
        // Create the image queues,
        this.imageQueue1 = new MaixSenseA010ImageQueue();
        this.imageQueue2 = new MaixSenseA010ImageQueue();
        // and add the listeners.
        this.imageQueue1.addListener( this.dataHolder1 );
        this.imageQueue2.addListener( this.dataHolder2 );
        
        // Create the driver,
        this.tofCamera1 = new MaixSenseA010Driver( "/dev/ttyUSB0" );
        this.tofCamera2 = new MaixSenseA010Driver( "/dev/ttyUSB2" );
        // and connect the queue so that received images are added to it.
        this.tofCamera1.connectQueue( this.imageQueue1 );
        this.tofCamera2.connectQueue( this.imageQueue2 );
        
        // Configure the MaixSense-A010 ToF cameras.
        try {
            this.configureCamera( this.tofCamera1 );
            this.configureCamera( this.tofCamera2 );
        } catch( SerialPortException e ) {
            e.printStackTrace();
        }
        
        // Initialize zoom variable.
        this.zoom = ( 1 << 8 );
    }
    
    
    /**
     * Sets the configuration of a MaixSenseA010Driver.
     * 
     * @param driver    {@link MaixSenseA010Driver} to be configured.
     * @throws SerialPortException  from {@link MaixSenseA010Driver} methods.
     */
    public void configureCamera( MaixSenseA010Driver driver )
            throws SerialPortException
    {
        driver.initialize();
        
        driver.setImageSignalProcessorOn();
        
        driver.setLcdDisplayOff();
        driver.setUsbDisplayOn();
        driver.setUartDisplayOff();
        
        driver.setBinning100x100();
        driver.setFps( 20 );
        
        driver.setQuantizationUnit( QUANTIZATION_UNIT );
        driver.setAntiMultiMachineInterferenceOff();
        //driver.setAntiMultiMachineInterferenceOn();
        driver.setExposureTimeAutoOn();
    }
    
    
    /**
     * Identical to "draw" in Processing IDE.
     */
    public void draw()
    {
        // Background initialization.
        background(0);
        
        // Here start the scope of 3d transformations on the scene.
        pushMatrix();
        // Set center to origin.
        translate( width/2 , height/2 , 0 );
        // Make reference frame right-handed.
        scale( 1 , -1 , 1 );
        // Scale using the zoom variable (controlled with mouse wheel).
        scale( this.zoom );
        // Rotate scene using mouse position.
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
        // Draw point cloud shape from first image.
        PShape shape1 = this.dataHolder1.pointCloudShape();
        if( shape1 != null ) {
            shape( shape1 );
        }
        // Draw point cloud shape from second image.
        PShape shape2 = this.dataHolder2.pointCloudShape();
        if( shape2 != null ) {
            shape( shape2 );
        }
        // Here ends the scope of the transformations made after pushMatrix.
        popMatrix();
        
        // Draw images on the corners of the drawing window.
        //  Draw image1.
        PImage image1 = this.dataHolder1.depthImage();
        if( image1 != null ) {
            image( image1 , 0 , height-height/4 , width/4 , height/4 );
        }
        //  Draw image 2.
        PImage image2 = this.dataHolder2.depthImage();
        if( image2 != null ) {
            image( image2 , width-width/4 , height-height/4 , width/4 , height/4 );
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
    
    
    /**
     * Overrides {@link PApplet#exit()} so that {@link MaixSenseA010Driver} is terminated and {@link MaixSenseA010ImageQueue} is stopped.
     */
    public void exit()
    {
        // Terminate serial communication.
        try {
            this.tofCamera1.terminate();
            this.tofCamera2.terminate();
        } catch( SerialPortException e ) {
            e.printStackTrace();
        }
        // Stop threads running in MaixSenseA010ImageQueue.
        this.imageQueue1.stop();
        this.imageQueue2.stop();
        // Finally, call the exit method of PApplet.
        super.exit();
    }
    
    
    
    ////////////////////////////////////////////////////////////////
    // INNER CLASSES
    ////////////////////////////////////////////////////////////////
    
    /**
     * Holds image data received through {@link MaixSenseA010ImageConsumer#consumeImage(MaixSenseA010Image)}.
     */
    class DepthImageDataHolder
        implements MaixSenseA010ImageConsumer
    {
        // PRIVATE VARIABLES
        
        /**
         * Calibration used to transform depth images to point clouds.
         */
        private MaixSenseA010DefaultCalibration depthCameraCalibration;
        
        /**
         * {@link PImage} that holds the last depth image received from the last received depth image.
         */
        private PImage depthImage;
        
        /**
         * {@link PShape} that holds the point cloud generated from the last received depth image.
         */
        private PShape pointCloudShape;
        
        /**
         * Color used to draw the point cloud.
         */
        int color;
        
        
        // PUBLIC CONSTRUCTORS
        
        /**
         * Constructs a {@link DepthImageDataHolder} whose point cloud is drawn with a specific color.
         * 
         * @param c     color used to draw the point cloud.
         */
        public DepthImageDataHolder( int c )
        {
            // Create DepthCameraCalibration; we take the default one.
            this.depthCameraCalibration = new MaixSenseA010DefaultCalibration();
            this.color = c;
        }
        
        
        // PUBLIC METHODS
        
        /**
         * Returns a {@link PImage} that contains the last received image.
         * <p>
         * If no image has been received so far, null is returned.
         * 
         * @return  {@link PImage} that contains the last received image.
         */
        public PImage depthImage()
        {
            if( this.depthImage != null ) {
                return this.depthImage.copy();
            } else {
                return null;
            }
        }
        
        /**
         * Returns the point cloud shape generated from the last received image.
         * <p>
         * If no image has been received so far, null is returned.
         * 
         * @return  point cloud shape generated from the last received image.
         */
        public PShape pointCloudShape()
        {
            return this.pointCloudShape;
        }
        
        /**
         * {@inheritDoc}
         */
        public void consumeImage( MaixSenseA010Image image )
        {
            // Adapt MaixSenseA010Image to be a DepthImage.
            MaixSenseA010DepthImageAdapter adaptedImage = new MaixSenseA010DepthImageAdapter( image );
            adaptedImage.setQuantizationUnit( QUANTIZATION_UNIT );
            // Generate point cloud from image.
            this.depthCameraCalibration.setImageSize( adaptedImage.cols() );
            List<Vector3> pointCloud = this.depthCameraCalibration.imageToPointCloud( adaptedImage );
            // Generate PShape from point cloud.
            PShape newPointCloudShape = createShape();
            newPointCloudShape.beginShape( POINTS );
            newPointCloudShape.strokeWeight( (float)2.0e0 );
            newPointCloudShape.stroke( this.color );
            for( Vector3 point : pointCloud ) {
                newPointCloudShape.vertex( (float)point.x() , (float)point.y() , (float)point.z() );
            }
            newPointCloudShape.endShape();
            // Update point cloud shape.
            this.pointCloudShape = newPointCloudShape;
            // Update image.
            if(  this.depthImage == null  ||  this.depthImage.width != image.cols()  ||  this.depthImage.height != image.rows()  ) {
                this.depthImage = createImage( image.cols() , image.rows() , RGB );
            }
            colorMode( RGB , (float)DEPTH_RANGE_MAX );
            for( int i=0; i<image.rows(); i++ ) {
                for( int j=0; j<image.cols(); j++ ) {
                    // Get depth value.
                    double depth = adaptedImage.depth( i , j );
                    // Set color in depth image.
                    this.depthImage.set( j,i , color( (float)(DEPTH_RANGE_MAX-depth) ) );
                }
            }
        }
        
    }
    
}
