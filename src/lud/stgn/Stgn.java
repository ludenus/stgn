package lud.stgn;

import java.io.*;
import java.awt.image.*;
import javax.imageio.ImageIO;

public class Stgn {

	public static final int DEFAULT_MASK = 0x03030303; // 0b_00000011_00000011_00000011_000000011

	int mask;
	private int maskSetBitCount = -1;
	private int[] maskSetBitIndexes;
	private BufferedImage srcImage;
	private BufferedImage dstImage;

	public Stgn( BufferedImage img, int bitMask ) throws IOException {

		if ( 0 == bitMask ) {
			throw new IOException( "bitMask must not be null!" );
		}
		if ( null == img ) {
			throw new IOException( "img must not be null!" );
		}
		mask = bitMask;
		srcImage = img;
		maskSetBitCount = getSetBitCount( mask );
		maskSetBitIndexes = getSetBitIndexes( mask );

	}

	public Stgn( BufferedImage srcImg ) throws IOException {

		this( srcImg, DEFAULT_MASK );
	}

	public Stgn( File imgFile, int bitMask ) throws IOException {

		this( ImageIO.read( Util.validImageFile( imgFile ) ), bitMask );
	}

	public Stgn( File imgFile ) throws IOException {

		this( imgFile, DEFAULT_MASK );
	}

	public BufferedImage conceal( File secretFile )
													throws IOException {

		return conceal( new FileInputStream(
			Util.existingAndReadableFile( secretFile ) ) );
	}

	public BufferedImage conceal( InputStream secretData )
															throws IOException {

		if ( secretData.available() > getImageCapacity() / 8 ) {
			throw new IOException( "Image capacity ("
				+ ( getImageCapacity() / 8 )
				+ " bytes) is not enough to conceal given "
				+ secretData.available()
				+ " bytes of data" );
		}
		int height = srcImage.getHeight();
		int width = srcImage.getWidth();
		dstImage =
			new BufferedImage( width, height, BufferedImage.TYPE_INT_ARGB );
		BitArrayInputStream bitInS = new BitArrayInputStream( secretData );

		boolean[] bits = new boolean[ maskSetBitCount ];
		xyloop :
		for ( int y = 0; y < height; y++ )
			for ( int x = 0; x < width; x++ ) {

				if ( -1 == bitInS.read( bits, 0, maskSetBitCount ) ) {
					// set pixels to the end of the row
					int[] last_row =
						srcImage
							.getRGB( x, y, width - x - 1, 1, null, 0, width );
					dstImage
						.setRGB( x, y, width - x - 1, 1, last_row, 0, width );

					// set pixels from the next row till end of image
					int[] last_block =
						srcImage.getRGB(
							0,
							y + 1,
							width,
							height - y - 1,
							null,
							0,
							width );
					dstImage.setRGB(
						0,
						y + 1,
						width,
						height - y - 1,
						last_block,
						0,
						width );
					break xyloop;
				} else {
					dstImage.setRGB(
						x,
						y,
						setPixelBits( srcImage.getRGB( x, y ), bits ) );
				}

			}
		;
		if ( secretData.available() > 0 ) {
			throw new IOException(
				"All pixels filled, but data still available" );
		}
		return dstImage;
	}

	public OutputStream unveil( int len )
											throws IOException {

		len *= 8;
		int height = srcImage.getHeight();
		int width = srcImage.getWidth();
		if ( len > getImageCapacity() ) {
			throw new IOException( "Image capacity ("
				+ getImageCapacity()
				+ " bits) is less than amount of data requested("
				+ len
				+ " bits)" );
		}
		BitArrayOutputStream bitOutS =
			new BitArrayOutputStream( new ByteArrayOutputStream( len ) );
		xyloop :
		for ( int y = 0; y < height; y++ )
			for ( int x = 0; x < width; x++ ) {
				if ( 0 == len ) {
					break xyloop;
				}
				int pixel = srcImage.getRGB( x, y );
				boolean[] bits = getPixelBits( pixel );
				bitOutS.write( bits, 0, bits.length );
				len -= bits.length;
			}
		;
		return bitOutS.getByteStream();
	}

	int setPixelBits( int pixel, boolean[] bits ) {

		assert bits.length == maskSetBitCount;
		int new_pixel = 0;
		for ( int i = 0; i < bits.length; i++ ) {
			new_pixel +=
				( bits[ i ] ? 1 : 0 ) << maskSetBitIndexes[ maskSetBitIndexes.length
					- 1
					- i ];
		}

		return ( pixel & ( ~mask ) | new_pixel );
	}

	boolean[] getPixelBits( int pixel ) {

		boolean[] bits = new boolean[ maskSetBitCount ];

		for ( int i = 0; i < bits.length; i++ ) {
			bits[ i ] =
				( 0 != ( pixel & ( 1 << maskSetBitIndexes[ maskSetBitIndexes.length
					- 1
					- i ] ) ) );
		}

		return bits;
	}

	/**
	 * how many bits can be concealed into sourceImage with given bitmask
	 */
	int getImageCapacity() {

		return maskSetBitCount * srcImage.getWidth() * srcImage.getHeight();
	}

	/**
	 * 7 == getSetBitCount( 0b_00000001_00000011_00000011_000000011 )
	 * 
	 */
	int getSetBitCount( int n ) {

		int count = 0;
		for ( int i = 0; i < 32; i++ ) {
			if ( 0 != ( n & ( 1 << i ) ) )
				count++;
		}
		return count;
	}

	/**
	 * [0, 1, 8, 9, 16, 17, 24] == getSetBitIndexes(
	 * 0b_00000001_00000011_00000011_000000011 )
	 * 
	 */
	int[] getSetBitIndexes( int n ) {

		int[] indexes = new int[ getSetBitCount( n ) ];
		int j = 0;
		for ( int i = 0; i < 32; i++ ) {

			if ( 0 != ( n & ( 1 << i ) ) )
				indexes[ j++ ] = i;
			;
		}
		return indexes;
	}

}
