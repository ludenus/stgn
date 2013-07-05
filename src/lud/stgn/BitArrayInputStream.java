package lud.stgn;

import java.io.*;

public class BitArrayInputStream {

	InputStream byteStream;
	int bytesAvailable = 0;
	int bitsAvailable = 0;
	boolean[] bits;

	int bitIndex = -1;

	public BitArrayInputStream( InputStream btStrm ) {

		this.byteStream = btStrm;
	}

	public int available()
							throws IOException {

		if ( 0 == bitsAvailable ) {
			bytesAvailable = byteStream.available();
			bitsAvailable = ( 1 + bitIndex + 8 * bytesAvailable );
		}
		return bitsAvailable;
	}

	public boolean read()
							throws IOException {

		boolean bit;
		if ( 0 != available() % 8 ) {
			bit = bits[ bitIndex ];

		} else if ( 0 != bytesAvailable ) {
			bits = Util.byteToBooleanArray( byteStream.read() );
			bitIndex = 7;
			bit = bits[ bitIndex ];
			bytesAvailable--;
		} else {
			throw new IOException( "No bits available" );
		}
		bitIndex--;
		bitsAvailable--;
		return bit;
	}

	public long skip( long n )
								throws IOException {

		long count = 0;
		while ( ( count < n ) && ( available() > 0 ) ) {
			try {
				read();
			} catch ( IOException e ) {
				break;
			}
			count++;
		}
		return count;
	}

	public int read( boolean[] buf, int off, int len )
														throws IOException {

		int count = 0;

		if ( len > buf.length ) {
			throw new IOException( "Requested len exceeds buf.length" );
		}

		if ( off > available() ) {
			throw new IOException( "Offset exceeds available count" );
		} else {
			skip( off );
		}

		if ( available() > 0 ) {
			while ( count < len ) {
				try {
					buf[ count ] = read();
				} catch ( IOException e ) {
					break;
				}
				count++;
			}
		} else {
			count = -1;
		}
		return count;
	}

}// BitArrayInputStream
