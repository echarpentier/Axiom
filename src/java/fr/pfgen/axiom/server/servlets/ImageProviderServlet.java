package fr.pfgen.axiom.server.servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import fr.pfgen.axiom.server.utils.IOUtils;

@SuppressWarnings("serial")
public class ImageProviderServlet extends HttpServlet {
	
	private Hashtable<String, File> appFiles; 
	
	@Override
	@SuppressWarnings("unchecked")
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		appFiles = (Hashtable<String, File>)getServletContext().getAttribute("ApplicationFiles");
	}
	
	@Override
	protected synchronized void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String f = req.getParameter("file");
		
		if(f==null) throw new ServletException("Undefined file");
		
		File file=new File(f);
		String imageType = "";
		
		if(!file.exists() || !file.isFile()) {
			imageType = "jpeg";
			file = new File(appFiles.get("imageNotFound").getAbsolutePath());
		} 
		if (file.getName().toLowerCase().endsWith(".jpg")){
			imageType = "jpeg";
		}else if (file.getName().toLowerCase().endsWith(".png")){
			imageType = "png";
		}else{
			throw new ServletException("Not an image file");
		}
		resp.setHeader("Content-Type", "image/"+imageType);
		
		FileInputStream reader=null;
		OutputStream out = null;
		try {
			reader = new FileInputStream(f);
			out = resp.getOutputStream();
			IOUtils.copyTo(reader, out);
			out.close();
			reader.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}finally{
			if (out!=null){try {out.close();} catch (IOException e) {} }
			if (reader!=null){try {reader.close();} catch (IOException e) {} }
		}
	}
}
