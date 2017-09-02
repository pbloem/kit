package nl.peterbloem.kit;

import static java.lang.String.format;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.commons.io.FileUtils;

public class FileIO {

	/**
	 * Runs a python script from the resources in the given directory.
	 * 
	 * @param dir
	 * @param name
	 * @throws InterruptedException 
	 */
	public static void python(File dir, String... scripts )
		throws IOException, InterruptedException
	{
		int i = 0;

		// * For each script:
		for(String script : scripts)
		{			
			String scriptName = script.split("/")[script.split("/").length-1];
			
			// * ... copy the script into the directory ./python/
			copy(script, dir); 
			
			// * Change directory
			Runtime runtime = Runtime.getRuntime();
			 
			// * ... run the script in python/
			Process p = runtime.exec(
					"python " + scriptName, 
					null,
					dir);
			
			BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
			BufferedReader ebr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			BufferedWriter bw = new BufferedWriter(new FileWriter(new File(dir, format("script%03d.log", i))));			
			BufferedWriter ebw = new BufferedWriter(new FileWriter(new File(dir, format("script%03d.err.log", i))));
			
			if(p.waitFor() == 0) 
				Global.log().info("Python script ("+script+") executed succesfully.");
			else
				Global.log().warning("Python script ("+script+") failed.");
			
			String line;
			while ( (line = br.readLine()) != null) 
				bw.write(line + "\n");
			while ( (line = ebr.readLine()) != null) 
				ebw.write(line + "\n");
			
			bw.close();
			ebw.close();
			
			i++;
		}
	}
	
	/**
	 * Copies all files and directories in the given classpath directory to 
	 * the given target directory in the filesystem.
	 * 
	 * @param cpDir
	 * @param target
	 */
	public static void copy(String cpDir, File target)
	{		
		URL sourcePath = Functions.class.getClassLoader().getResource(cpDir);
		Global.log().info("Copying static files from path " + sourcePath + " to " + target);
		
		// * Copy static files (css, js, etc)
		try
		{
			copyResources(sourcePath, target);
		} catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		Global.log().info("Finished copying");				
	}
	
	public static void copyResources(URL originUrl, File destination) 
			throws IOException 
	{
		System.out.println(originUrl);
	    URLConnection urlConnection = originUrl.openConnection();
	    
	    File file = new File(originUrl.getPath());
	    if (file.exists()) 
	    {	
	    	if(file.isDirectory())
	    		FileUtils.copyDirectory(new File(originUrl.getPath()), destination);
	    	else
	    		FileUtils.copyFile(file, new File(destination, file.getName()));
	    } else if (urlConnection instanceof JarURLConnection) 
	    {
	        copyJarResourcesRecursively(destination, (JarURLConnection) urlConnection);
	    } else {
	        throw new RuntimeException("URLConnection[" + urlConnection.getClass().getSimpleName() +
	                "] is not a recognized/implemented connection type.");
	    }
	}

	public static void copyJarResourcesRecursively(File destination, JarURLConnection jarConnection ) 
			throws IOException 
	{
	    JarFile jarFile = jarConnection.getJarFile();
	    
	    Enumeration<JarEntry> entries = jarFile.entries();
	    JarEntry targetEntry = jarConnection.getJarEntry();
	    
	    while(entries.hasMoreElements())
	    {
	    	JarEntry entry = entries.nextElement();
	    	
	        if (entry.getName().startsWith(jarConnection.getEntryName())) 
	        {
	        	
	        	// * Path of the target file relative to the destination 
	            String filePath;
	            if(targetEntry.isDirectory())
	            	filePath = removeStart(entry.getName(), jarConnection.getEntryName());
	            else 
	            	filePath = entry.getName().split("/")[entry.getName().split("/").length - 1];
	            
            	System.out.println("entry name: " + filePath);

	            if (! entry.isDirectory())
	            {
	                InputStream entryInputStream = null;
	                entryInputStream = jarFile.getInputStream(entry);
	                File targetFile = new File(destination, filePath);
					copyStream(entryInputStream, targetFile);
	               
	            } else
	            {
	                new File(destination, filePath).mkdirs();
	            }
	        }
	    }
	}

	private static void copyStream(InputStream in, File file) 
			throws IOException
	{
		OutputStream out;
		try { 
			out = new FileOutputStream(file);
		} catch(IOException e)
		{
			throw new RuntimeException("Tried to open " + file + " as outputstream. ", e);
		}
		int bt = in.read();
		while(bt != -1)
		{
			out.write(bt);
			bt = in.read();
		}
		out.flush();
		out.close();
	}	
	
	private static String removeStart(String string, String prefix)
	{
		if(string.indexOf(prefix) != 0)
			return null;
		
		return string.substring(prefix.length());
	}
	
	/**
	 * Delete the given file/directory, and everything in it.
	 * 
	 * @param file
	 */
	public static void rDelete(File file)
	{
		if(file.isDirectory())
			for(File sub : file.listFiles())
				rDelete(sub);
		
		file.delete();
	}
	
}
