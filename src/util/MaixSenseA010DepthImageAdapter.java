package util;


import maixsense.a010.MaixSenseA010Image;
import sensorCalibrationLibrary.depthCameras.DepthImage;



/**
 * Adapts a {@link MaixSenseA010Image} to a {@link DepthImage}.
 */
public class MaixSenseA010DepthImageAdapter
    implements DepthImage
{
    ////////////////////////////////////////////////////////////////
    // PRIVATE VARIABLES
    ////////////////////////////////////////////////////////////////
    
    /**
     * {@link MaixSenseA010Image} to be adapted to a {@link DepthImage}.
     */
    private MaixSenseA010Image image;
    
    /**
     * Quantization unit used to compute the depth from a pixel value.
     * 
     * @see #depth(byte)
     */
    private int quantizationUnit;
    
    
    
    ////////////////////////////////////////////////////////////////
    // PUBLIC CONSTRUCTORS
    ////////////////////////////////////////////////////////////////
    
    /**
     * Constructs a {@link MaixSenseA010DepthImageAdapter} taking a {@link MaixSenseA010Image}.
     * 
     * @param imageA010     {@link MaixSenseA010Image} to be adapted to a {@link DepthImage}.
     */
    public MaixSenseA010DepthImageAdapter( MaixSenseA010Image imageA010 )
    {
        this.image = imageA010;
    }
    
    
    
    ////////////////////////////////////////////////////////////////
    // PUBLIC METHODS
    ////////////////////////////////////////////////////////////////
    
    /**
     * Sets the quantization unit used to compute the depth from the pixel values.
     * 
     * @param unit  quantization unit.
     * 
     * @see #depth(byte)
     */
    public void setQuantizationUnit( int unit )
    {
        if( 0 <= unit  &&  unit <= 9 ) {
            this.quantizationUnit = unit;
        } else {
            this.quantizationUnit = 0;
            System.out.println( "Quantization unit must be in [0,10] interval; unit set to 0." );
        }
    }
    
    
    /**
     * Returns the {@link MaixSenseA010Image} being adapted.
     * 
     * @return  {@link MaixSenseA010Image} being adapted.
     */
    public MaixSenseA010Image getMaixSenseA010Image()
    {
        return this.image;
    }
    
    
    /**
     * {@inheritDoc}
     */
    public int rows()
    {
        return this.image.rows();
    }
    
    
    /**
     * {@inheritDoc}
     */
    public int cols()
    {
        return this.image.cols();
    }
    
    
    /**
     * {@inheritDoc}
     */
    public boolean checkPixel( int i , int j )
    {
        int pixelValueUnsignedByte = this.image.pixel( i , j );
        double r = Math.sqrt( ( i - 50 ) * ( i - 50 ) + ( j - 50 ) * (j - 50 ) );
        return ( 0 < pixelValueUnsignedByte  &&  pixelValueUnsignedByte < 255 /* && r < 50*/ );
    }
    
    
    /**
     * {@inheritDoc}
     */
    public double depth( int i , int j )
    {
        // Take pixel byte and cast its unsigned representation to an int.
        int pixelValueUnsignedByte = this.image.pixel( i , j ) & 0xFF ;
        // The depth value depends on the quantization strategy.
        if( this.quantizationUnit == 0 ) {
            double depthSqrt = pixelValueUnsignedByte / 5.1;
            double depthInMillimiters = depthSqrt * depthSqrt;
            return depthInMillimiters * 1.0e-3;
        } else {
            return ( this.quantizationUnit * pixelValueUnsignedByte );
        }
    }
    
}
