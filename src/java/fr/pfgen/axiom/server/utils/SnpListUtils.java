package fr.pfgen.axiom.server.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class SnpListUtils {

	public static List<String> readSnpListFile(File snpListFile) {
		List<String> snpList = new ArrayList<String>();
		BufferedReader br = null;
		
		try{
			br = IOUtils.openFile(snpListFile);
			String line;
			while((line=br.readLine())!=null){
				snpList.add(line);
			}
			br.close();
			return snpList;
		}catch(IOException e){
			e.printStackTrace();
			return null;
		}finally{
			IOUtils.safeClose(br);
		}
	}

	public static boolean writeSnpList(File snpListFile, List<String> snpList) {
		PrintWriter pw = null;
		try {
			pw = new PrintWriter(new FileWriter(snpListFile));
			for (String snp : snpList) {
				pw.println(snp);
			}
			pw.flush();
			pw.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} finally {
			IOUtils.safeClose(pw);
		}
	}
}
