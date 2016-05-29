/**
 * Décrivez votre classe RecopyFile ici.
 *
 * @author Crunchify.com
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class RecopyFile
{
    /**
     * Constructeur d'objets de classe RecopyFile
     */
    public RecopyFile(String fich1, String fich2)
    {
        File file1 =new File(fich1);
        File file2 =new File(fich2);
        try {
            fileCopy(file1, file2);
        } catch (IOException e) {
            e.printStackTrace();
        }
       // System.out.println ("File "+ fich1 + " on " + fich2);
    }       
	// Fastest way to Copy file in Java
	@SuppressWarnings("resource")
	public static void fileCopy ( File in, File out ) throws IOException
    {
        FileChannel inChannel = new FileInputStream( in ).getChannel();
        FileChannel outChannel = new FileOutputStream( out ).getChannel();
        try
        {
            // Try to change this but this is the number I tried.. for Windows, 64Mb - 32Kb)
            int maxCount = (64 * 1024 * 1024) - (32 * 1024);
            long size = inChannel.size();
            long position = 0;
            while ( position < size )
            {
               position += inChannel.transferTo( position, maxCount, outChannel );
            }
           // System.out.println("File Successfully Copied.." );
        }
        finally
        {
            if ( inChannel != null )
            {
               inChannel.close();
            }
            if ( outChannel != null )
            {
                outChannel.close();
            }
        }
    }
}