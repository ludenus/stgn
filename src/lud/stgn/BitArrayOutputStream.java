package lud.stgn;

import java.io.*;

public class BitArrayOutputStream {

	private OutputStream byteStream;
	private int byteBuf = 0;
	private int bitIndex = 0;

	public OutputStream getByteStream()
										throws IOException {

		flush();
		return byteStream;
	}

	public BitArrayOutputStream( OutputStream btStrm ) {

		this.byteStream = btStrm;
	}

	public void write( boolean bit )
									throws IOException {

		byteBuf += ( ( bit ? 1 : 0 ) << ( bitIndex ) );
		bitIndex++;

		if ( bitIndex > 7 ) {
			flush();
		}
	}

	public void write( boolean[] bits, int off, int len )
															throws IOException {

		for ( int i = 0; i < len; i++ ) {
			write( bits[ off + i ] );
		}
	}

	public void flush()
						throws IOException {

		if ( 0 != bitIndex ) {
			byteStream.write( byteBuf );
		}
		byteStream.flush();
		byteBuf = 0;
		bitIndex = 0;
	}

}// BitArrayOutputStream
