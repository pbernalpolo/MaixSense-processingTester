package MaixSenseA010;


import a010.MaixSenseA010Driver;
import a010.MaixSenseA010Image;
import a010.MaixSenseA010ImageConsumer;
import a010.MaixSenseA010ImageQueue;
import jssc.SerialPortException;
import processing.core.PApplet;
import processing.core.PShape;
import processing.event.MouseEvent;



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
            
        } catch( SerialPortException e ) {
            e.printStackTrace();
        }
        
        // Initialize zoom variable.
        this.zoom = 1;
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
        strokeWeight( 1.0e-1f );
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
        // Generate PShape from depth image.
        PShape newMeshShape = createShape();
        newMeshShape.beginShape( TRIANGLES );
        newMeshShape.stroke( 0 );
        int imageRowsHalf = image.rows()/2;
        int imageColumnsHalf = image.cols()/2;
        for( int i=0; i<image.rows()-1; i++ ) {
            // Create lower triangles of the current strip.
            for( int j=0; j<image.cols()-1; j++ ) {
                // Take pixel bytes and cast their unsigned representation to ints.
                int depthA = image.pixel(i,j) & 0xff;
                int depthB = image.pixel(i,j+1) & 0xff;
                int depthC = image.pixel(i+1,j) & 0xff;
                // Set triangle color.
                newMeshShape.fill( 255 - ( depthA + depthB + depthC )/3 );
                // Create triangle.
                newMeshShape.vertex( j-imageColumnsHalf , i-imageRowsHalf , depthA );
                newMeshShape.vertex( j+1-imageColumnsHalf , i-imageRowsHalf , depthB );
                newMeshShape.vertex( j-imageColumnsHalf , i+1-imageRowsHalf , depthC );
            }
            // Create upper triangles of the current strip.
            for( int j=0; j<image.cols()-1; j++ ) {
                // Take pixel bytes and cast their unsigned representation to ints.
                int depthA = image.pixel(i,j+1) & 0xff;
                int depthB = image.pixel(i+1,j) & 0xff;
                int depthC = image.pixel(i+1,j+1) & 0xff;
                // Set triangle color.
                newMeshShape.fill( 255 - ( depthA + depthB + depthC )/3 );
                // Create triangle.
                newMeshShape.vertex( j+1-imageColumnsHalf , i-imageRowsHalf , depthA );
                newMeshShape.vertex( j-imageColumnsHalf , i+1-imageRowsHalf , depthB );
                newMeshShape.vertex( j+1-imageColumnsHalf , i+1-imageRowsHalf , depthC );
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
