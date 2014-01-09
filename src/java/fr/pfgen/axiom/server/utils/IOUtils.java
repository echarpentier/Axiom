package fr.pfgen.axiom.server.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.zip.GZIPInputStream;

public class IOUtils{
	
	static private final int BUFFER_SIZE=2048;
	
	
	public static void safeClose(InputStream in){
		try {
			if(in!=null) in.close();
		}
		catch (IOException e) {
		
		}	
	}
	
	public static void safeClose(Reader in){
		try {
			if(in!=null) in.close();
		}
		catch (IOException e) {

		}	
	}
	
	public static void safeClose(Writer out){
		try {
			if(out!=null) out.close();
		}
		catch (IOException e){
			
		}	
		}
	
	public static void safeClose(OutputStream out){
		try {
			if(out!=null) out.close();
		}
		catch (IOException e){
		
		}	
	}
	
	public static void copyTo(InputStream in,OutputStream out) throws IOException{
		byte buffer[]=new byte[BUFFER_SIZE];
		int nRead;
		while((nRead=in.read(buffer))!=-1){
			out.write(buffer, 0, nRead);
		}
		out.flush();
	}
	
	public static void copyTo(Reader in,Writer out) throws IOException{
		char buffer[]=new char[BUFFER_SIZE];
		int nRead;
		while((nRead=in.read(buffer))!=-1){
			out.write(buffer, 0, nRead);
		}
		out.flush();
	}
	
	public static void stringToFile(String s, File f) throws IOException{
		StringReader sr = new StringReader(s);
		FileWriter fw = new FileWriter(f, false);
		copyTo(sr, fw);
	}
	
	public static String copyToString(Reader in) throws IOException{
		StringWriter sw=new StringWriter(BUFFER_SIZE);
		copyTo(in, sw);
		return sw.toString();
	}
	
	public static String copyToString(File f) throws IOException{
		FileReader r=new FileReader(f);
		try {
			return copyToString(r);
		}finally{
			r.close();
		}
	}
	
	public static void copy(String fromFileName, String toFileName) throws IOException {
		File fromFile = new File(fromFileName);
		File toFile = new File(toFileName);

		if (!fromFile.exists())
			throw new IOException("FileCopy: " + "no such source file: "
					+ fromFileName);
		if (!fromFile.isFile())
			throw new IOException("FileCopy: " + "can't copy directory: "
					+ fromFileName);
		if (!fromFile.canRead())
			throw new IOException("FileCopy: " + "source file is unreadable: "
					+ fromFileName);

		if (toFile.isDirectory())
			toFile = new File(toFile, fromFile.getName());

		if (toFile.exists()) {
			if (!toFile.canWrite())
				throw new IOException("FileCopy: "
						+ "destination file is unwriteable: " + toFileName);
		} else {
			String parent = toFile.getParent();
			if (parent == null)
				parent = System.getProperty("user.dir");
			File dir = new File(parent);
			if (!dir.exists())
				throw new IOException("FileCopy: "
						+ "destination directory doesn't exist: " + parent);
			if (dir.isFile())
				throw new IOException("FileCopy: "
						+ "destination is not a directory: " + parent);
			if (!dir.canWrite())
				throw new IOException("FileCopy: "
						+ "destination directory is unwriteable: " + parent);
		}

		FileInputStream from = null;
		FileOutputStream to = null;
		try {
			from = new FileInputStream(fromFile);
			to = new FileOutputStream(toFile);
			byte[] buffer = new byte[4096];
			int bytesRead;

			while ((bytesRead = from.read(buffer)) != -1)
				to.write(buffer, 0, bytesRead); // write
		} finally {
			if (from != null)
				try {
					from.close();
				} catch (IOException e) {
					;
				}
				if (to != null)
					try {
						to.close();
					} catch (IOException e) {
						;
					}
		}
	}
	
	public static BufferedReader openFile(File file) throws IOException{
		BufferedReader in = null;
		if(file.getName().toLowerCase().endsWith(".gz")){
			in = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
		}else{
			in = new BufferedReader(new FileReader(file));
		}
		return in;
	}
	
	public static void chgrp(File f, String gr, boolean recursive) throws IOException{
		String exe;
		if (recursive){
			exe = "chgrp -R ";
		}else{
			exe = "chgrp ";
		}
		Runtime.getRuntime().exec(exe+gr+" "+f.getAbsolutePath());
	}
	
	public static void chmod(File f, String mode, boolean recursive) throws IOException{
		String exe;
		if (recursive){
			exe = "chmod -R ";
		}else{
			exe = "chmod ";
		}
		Runtime.getRuntime().exec(exe+mode+" "+f.getAbsolutePath());
	}
	
	public static void chown(File f, String user, boolean recursive) throws IOException{
		String exe;
		if (recursive){
			exe = "chown -R ";
		}else{
			exe = "chown ";
		}
		Runtime.getRuntime().exec(exe+user+" "+f.getAbsolutePath());
	}
}
