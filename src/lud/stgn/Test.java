package lud.stgn;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.Arrays;
import javax.imageio.ImageIO;

public class Test {

	static void testBitArray() {

		try {

			byte[] bytes = { 0, 127, -128, 7, -44 };

			BitArrayInputStream bInS =
				new BitArrayInputStream( new ByteArrayInputStream( bytes ) );

			boolean[] buf = new boolean[ bytes.length * 8 ];
			bInS.read( buf, 0, buf.length );

			BitArrayOutputStream bOutS =
				new BitArrayOutputStream( new ByteArrayOutputStream(
					bytes.length ) );

			bOutS.write( buf, 0, buf.length );

			ByteArrayOutputStream bytesOut =
				( ByteArrayOutputStream ) bOutS.getByteStream();
			byte[] bytes2 = bytesOut.toByteArray();

			for ( int i = 0; i < bytes.length; i++ ) {
				if ( bytes[ i ] != bytes2[ i ] ) {
					System.err.println( Arrays.toString( bytes ) );
					System.err.println( Arrays.toString( buf ) );
					System.err.println( Arrays.toString( bytes2 ) );
					throw new RuntimeException(
						"Array items not equal after convertion" );

				}
			}
			System.out.println( "testBitArray ok" );
		} catch ( Exception e ) {
			e.printStackTrace( System.err );
		}

	}

	static void testByteToBooleanArray() {

		try {
			boolean[] ba =
				{ true, true, false, false, true, false, false, false };
			byte b1 = ( byte ) Util.booleanArrayToByte( ba );
			boolean[] ba_recalc = Util.byteToBooleanArray( b1 );

			if ( ( byte ) 0xC8 != b1 ) {
				System.err.println( String.format( "%02x", b1 ) );
				System.err.println( Arrays.toString( ba ) );
				throw new RuntimeException( "booleanArrayToByte failed" );
			}

			for ( int i = 0; i < ba.length; i++ ) {
				if ( ba_recalc[ i ] != ba[ i ] ) {
					System.err.println( Arrays.toString( ba_recalc ) );
					System.err.println( Arrays.toString( ba ) );
					throw new RuntimeException( "byteToBooleanArray failed" );
				}

			}
			System.out.println( "testByteToBooleanArray ok" );
		} catch ( IOException e ) {
			e.printStackTrace( System.err );
		}
	}

	static void testStgn() {

		System.out.println( "testStgn() START : "
			+ new Timestamp( System.currentTimeMillis() ) );
		try {

			// File newFile = new File( "./source.png" );
			// RenderedImage rendImage = Stgn.myCreateImage();
			// ImageIO.write( rendImage, "png", newFile );

			File srcFile = new File( "./container.png" );
			BufferedImage pngFromDisk = ImageIO.read( srcFile );

			File textFile = new File( "./secret.jpg" );

			FileInputStream inStr = new FileInputStream( textFile );

			Stgn st1 = new Stgn( pngFromDisk, 0x03030303 );

			BufferedImage resImg = st1.conceal( inStr );
			File dstFile = new File( "./modified.png" );
			ImageIO.write( resImg, "png", dstFile );

			int len = ( int ) textFile.length();

			Stgn st2 =
				new Stgn(
					ImageIO.read( new File( "./modified.png" ) ),
					0x03030303 );

			ByteArrayOutputStream bOutS =
				( ByteArrayOutputStream ) st2.unveil( len );

			OutputStream revStr = new FileOutputStream( "./revealed.jpg" );
			byte[] r_buf = bOutS.toByteArray();
			revStr.write( r_buf, 0, len );
			revStr.close();
			long crc_hidden = Util.fileChecksum( "./secret.jpg" );
			long crc_revealed = Util.fileChecksum( "./revealed.jpg" );
			if ( crc_hidden != crc_revealed ) {
				System.err.println( "crc_hidden:" + crc_hidden );
				System.err.println( "crc_revealed:" + crc_revealed );
				throw new RuntimeException( "File checksums mismatch" );
			}
			System.out.println( "testStgn ok" );
		} catch ( IOException e ) {
			e.printStackTrace( System.err );
		} finally {
			System.out.println( "testStgn() END   : "
				+ new Timestamp( System.currentTimeMillis() ) );
		}
	}

	public static void main( String[] args ) {

		testByteToBooleanArray();
		testBitArray();
		// for (int i =0 ; i < 1000; i++ ){
		testStgn();
		// }

	}
}
