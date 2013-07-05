package lud.stgn;

import java.io.*;
import java.util.zip.*;
import javax.imageio.ImageIO;

public class Util {

	static int booleanArrayToByte( boolean[] btSt )
													throws IOException {

		int aByte = 0;
		if ( btSt.length != 8 ) {
			throw new IOException(
				"Boolean array size is not 8. will not convert to byte." );
		}

		for ( int i = 0; i < 8; i++ ) {
			// aByte += btSt[ 8 - i - 1 ]
			aByte += btSt[ 8 - 1 - i ] ? ( 1 << i ) : 0;
		}
		return aByte;
	}

	static boolean[] byteToBooleanArray( int b ) {

		boolean[] btSt = new boolean[ 8 ];

		for ( int i = 0; i < 8; i++ ) {
			btSt[ 8 - 1 - i ] = ( 0 != ( b & ( 1 << i ) ) );
		}
		return btSt;
	}

	/**
	 * 
	 * @param filename
	 * @return crc32 checksum or -1 in case of exception
	 */
	static long fileChecksum( String filename ) {

		long checkSum = -1;
		try {
			RandomAccessFile file = new RandomAccessFile( filename, "r" );
			byte[] fileBytes = new byte[ ( int ) file.length() ];
			file.read( fileBytes );
			CRC32 crc = new CRC32();
			crc.update( fileBytes );
			checkSum = crc.getValue();
			file.close();
		} catch ( Exception e ) {
			e.printStackTrace( System.err );
		}

		return checkSum;
	}

	static File existingAndReadableFile( File f )
													throws IOException {

		if ( !f.exists() ) {
			throw new FileNotFoundException( "File not found '" + f + "'" );
		}
		if ( !f.canRead() ) {
			throw new IOException( "Cannot read file '" + f + "'" );
		}
		return f;
	}

	static File validImageFile( File fImg )
											throws IOException {

		if ( null == ImageIO.read( Util.existingAndReadableFile( fImg ) ) ) {
			throw new IOException( "Image format '"
				+ fImg
				+ "' is not recognized!" );
		}
		return fImg;
	}

	static int validMask( String strMask )
											throws IOException {

		int intMask = 0;
		try {
			// if ( 10 == strMask.length() && strMask.matches(
			// "0x[0-9A-Fa-f]{8}" ) ) {
			intMask =
				Integer.parseInt( strMask.toLowerCase().substring( 2, 10 ), 16 );
		} catch ( Exception e ) {
			throw new IOException( "Failed to parse mask: '"
				+ strMask
				+ "' expected format hex integer '0xAARRGGBB'" );
		}
		return intMask;
	}

}
