/*
* Copyright (C) 2014 Andrew Reid and the ModelGUI Project <http://mgui.wikidot.com>
* 
* This file is part of ModelGUI[neuro] (mgui-neuro).
* 
* ModelGUI[neuro] is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* ModelGUI[neuro] is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with ModelGUI[neuro]. If not, see <http://www.gnu.org/licenses/>.
*/


package mgui.neuro.imaging.camino.tasks;

import java.io.*;
import misc.LoggedException;
import java.util.logging.Logger;

import data.OutputManager;

import tools.CL_Initializer;

/**
 *
 * Extracts periodic chunks from binary data. Call with command line:
 * <p> java apps.Shredder &ltoffset &rt &lt chunkSize &rt &lt space
 * &rt <p> Shredder makes an initial offset of <code>offset</code>
 * bytes. It then reads and outputs <code>chunkSize</code> bytes,
 * skips <code>space</code> bytes, and repeats until there is no more
 * input.  <p> If the <code>chunkSize</code> is negative, chunks of
 * size |<code>chunkSize</code>| are read and the byte ordering of
 * each chunk is reversed. If byte swapping is required, the
 * <code>chunkSize</code> should correspond to the size of the data
 * type.
 * 
 * @author Danny Alexander
 * @author Philip Cook
 * @version $Id: Shredder.java 445 2007-12-05 18:12:05Z ucacpco $
 */
public class Shredder2 {

    private static Logger logger = Logger.getLogger("camino.apps.Shredder");

    public static int FILEBUFFERSIZE = data.ExternalDataSource.FILEBUFFERSIZE;

    public static InputStream in; // =
	//new DataInputStream(new BufferedInputStream(System.in, FILEBUFFERSIZE));

    public static OutputStream out; // = 
	//new DataOutputStream(new BufferedOutputStream(System.out, FILEBUFFERSIZE));

    //  chunk size for skip method
    private static int skipChunkSize = FILEBUFFERSIZE;

    private static byte[] skipChunk = null;


    public static void main(String[] args) {

    	CL_Initializer.CL_init(args);
    	
        if (args.length != 7) {
            System.err
                    .println("Usage: java Shredder2 -inputfile <filename> -outputfile <filename> <offset> <chunk size> <space>.\n\n");
            //System.exit(0);
            return;
        }
        
        try{
        	in = new FileInputStream(new File(CL_Initializer.inputFile));
        	
        	out = new FileOutputStream(new File(OutputManager.outputFile));
        }catch (IOException e){
        	System.out.println("Shredder2: Exception encountered..");
        	throw new LoggedException(e);
        }

        int offset = Integer.parseInt(args[4]);
        int chunkSize = Integer.parseInt(args[5]);
        int space = Integer.parseInt(args[6]);

	skipChunk = new byte[skipChunkSize];

	if (chunkSize == 0) {
	    throw new LoggedException("Can't read zero byte chunks");
	}


        boolean reverse = false;
        if (chunkSize < 0) {
            reverse = true;
            chunkSize = -chunkSize;
        }

        byte[] chunk = new byte[chunkSize]; // stuff we want to output

	if ( !skip(offset) ) {
		System.out.println("Shredder2: Exception encountered..");
	    throw new LoggedException("Could not skip initial offset of " + offset + " bytes");
	}
	
        while (getChunk(chunk)) { // get chunk bytes
            
            if (reverse) {
                arrFlip(chunk);
            }

	    output(chunk); 
	    
	    if (!skip(space)) { // try to skip
		break;
	    }
        }

        // Close output stream.
        try {
            out.close();
        }
        catch (Exception e) {
        	System.out.println("Shredder2: Exception encountered..");
            throw new LoggedException(e);
        }


    }


    /**
     * Reads in the next chunk from the standard input into the array
     * chunk and returns a boolean indicating the success of the
     * operation.
     *
     * @param chunk An array to contain the chunks
     *
     * @return boolean indicating whether or not the chunk was read
     * successfully. If part of a chunk was read successfully, the return value is <code>true</code>.
     * Note that if only part of a chunk is read, the remaining bytes will contain the original
     * contents of <code>chunk</code>
     *
     */
    public static boolean getChunk(byte[] chunk) {

	int totalBytesRead = 0;

	try {
	    readBytes:
	    while (totalBytesRead < chunk.length) {
		int bytesRead = in.read(chunk, totalBytesRead, chunk.length - totalBytesRead);

		if (bytesRead == -1) {
		    // eof
		    break readBytes;
		}

		totalBytesRead += bytesRead;

	    }
	}
	catch (IOException e) {
	    // note: an exception is not thrown here for EOF
	    // in case of EOF, bytesRead is -1
	    throw new LoggedException(e);
	}

	// return true if we read at least one byte
	if (totalBytesRead > 0) {
	    return true;
	}
	
	return false;
    }

    
    /**
     * Skip bytes from the input stream.
     *
     * @param bytesToSkip the number of bytes to skip.
     * @return <code>true</code> if the bytes were skipped successfully, <code>false</code> if EOF was found 
     * before enough bytes could be skipped.
     *
     */
    public static boolean skip(int bytesToSkip) {

	if (bytesToSkip == 0) {
	    return true;
	}
	if (skipChunkSize == 0) {
	    throw new LoggedException("Internal error: skip chunk size is zero!");
	}

	// the skip method in InputStream simply reads and discards one 
	// byte at a time, so we won't use that
	int chunksToSkip = bytesToSkip / skipChunkSize;
	
       	int remainderSkip = bytesToSkip % skipChunkSize;

	// total bytes we read from the stream in this method
	int totalBytesRead = 0;

	try {
	    
	    	    
	    for (int i = 0; i < chunksToSkip; i++) {

		int bytesRead = 0;

		while (bytesRead < skipChunkSize) {
		    int bytesThisRead = in.read(skipChunk, bytesRead, skipChunkSize - bytesRead);

		    if (bytesThisRead == -1) {
			// EOF
			return false;
		    } 

		    bytesRead += bytesThisRead;
		}

		totalBytesRead += bytesRead;
	    }

	    
	    int bytesRead = 0;

	    while (bytesRead < remainderSkip) {
		int bytesThisRead = in.read(skipChunk, bytesRead, remainderSkip - bytesRead);
		
		if (bytesThisRead == -1) {
		    // EOF
		    return false;
		} 
		
		bytesRead += bytesThisRead;
	    }

	    totalBytesRead += bytesRead;
	    
	    if (totalBytesRead == bytesToSkip) {
		// success
		return true;
	    }
	    else {
		throw new 
		    LoggedException("Needed to skip " + bytesToSkip + " bytes, but skipped " + totalBytesRead);
	    }

	}
	catch (IOException e) {
	    throw new LoggedException(e);
	}
    }


    /**
     * Outputs the chunk to the standard output.
     *
     * @param chunk The chunk to output.
     */
    public static void output(byte[] chunk) {
	try {
	    out.write(chunk);
	}
	catch (IOException e) {
                throw new LoggedException(e);
	}
    }


    /**
     * Reverses the order of the bytes in the array.
     * 
     * @param The chunk to reverse.
     */
    public static void arrFlip(byte[] chunk) {
        for (int i = 0; i < chunk.length / 2; i++) {
            byte temp = chunk[i];
            chunk[i] = chunk[chunk.length - i - 1];
            chunk[chunk.length - i - 1] = temp;
        }
    }

}