package com.cgj.web.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ChunkedUtils {

		/**
		 * Process the chunk data. Refer the HTTP-RFC2616 document[3.6.1 Chunked
		 * Transfer Coding], chunk data format is: 
		 *     Chunked-Body = *chunk
		 *                    last-chunk
		 *                    trailer
		 *                    CRLF
		 *     chunk        = chunk-size
		 *                    [ chunk-extension ]
		 *                    CRLF
		 *                    chunk-data
		 *                    CRLF
		 *     chunk-size   = 1*HEX
		 *     last-chunk   = 1*("0")
		 *                    [ chunk-extension ]
		 *                    CRLF
		 * @param in
		 *            : The input stream of proxied server
		 * @param out
		 *            : The output stream of client to which message will be sent.
		 */

		/**
		 * Read the chuck data size, the size is hexadecimal number.
		 * 
		 * @param in The input stream
		 * @return size of one chunk segment
		 * @throws IOException
		 */
		public static String readChunkSize(InputStream in) throws IOException {
			StringBuilder sb = new StringBuilder();
			int currChar = 0;
			while (((currChar = in.read()) != -1)
					&& ((currChar != Contants.INT_CR) || ((currChar = in.read()) != Contants.INT_LF))) {
				char ch = (char) currChar;
				if (isHexChar(ch)){
					sb.append(ch);
	            }
			}
			return sb.toString();
		}

		/**
		 * Read one chunk data segment to output stream based on the given size.
		 * 
		 * @param size
		 *            size of one chunk data segment
		 * @param out
		 *            output stream for output data
		 * @param in
		 *            input stream which contains the chunk data
		 * @param needToWrite
		 *            mark whether write data successfully
		 * @return
		 * @throws IOException
		 */
		public static int outputChunkData(int size, OutputStream out,
				InputStream in, boolean needToWrite) throws IOException {
			int len = size;
			int sizePerRead = 0;
			byte[] buf = new byte[Math.min(size,4096)];
			while (len > 0) {
				sizePerRead = in.read(buf, 0, Math.min(buf.length, len));
				if (sizePerRead == -1){
					throw new IOException(new StringBuilder("Unexpected EOF, expected to read: ")
							.append(size).append(" actually read: ")
							.append(sizePerRead).toString());
	            }
				if (needToWrite) {
					try {
						out.write(buf, 0, sizePerRead);
					} catch (IOException ioe) {
						needToWrite = false;
						throw ioe;
					}
				}
				len -= sizePerRead;
			}
			checkEOL(in);

			if (!needToWrite){
				return -1;
	        }
			return size;
		}

		/**
		 * Judge whether the given char is hexadecimal number.
		 * char[0|1|2|3|4|5|6|7|8|9|A|B|C|D|E|F|a|b|c|d|e|f]
		 * 
		 * @param c
		 *            given char
		 * @return true if given char is hexadecimal number char false otherwise
		 */
		public static final boolean isHexChar(int c) {
			switch (c) {
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
			case 'A':
			case 'B':
			case 'C':
			case 'D':
			case 'E':
			case 'F':
			case 'a':
			case 'b':
			case 'c':
			case 'd':
			case 'e':
			case 'f':
				return true;
			}
			return false;
		}

	/**
	 * Check whether the chunk data's EOL character is right.
	 * 
	 * @param in
	 *            input stream
	 * @throws IOException
	 *             if EOL character is not CRLF.
	 */
	private static void checkEOL(InputStream in) throws IOException {
		if (in.read() != Contants.INT_CR) {
			throw new IOException("Chunk data is not ended with CR");
		}
		if (in.read() != Contants.INT_LF) {
			throw new IOException("Chunk data is not ended with LF");
		}

	}
}
