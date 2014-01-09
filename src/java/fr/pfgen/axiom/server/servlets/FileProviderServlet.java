package fr.pfgen.axiom.server.servlets;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import fr.pfgen.axiom.server.utils.IOUtils;

@SuppressWarnings("serial")
public class FileProviderServlet extends HttpServlet{

	/*void exe(HttpServletRequest req, HttpServletResponse resp)throws ServletException 
		{
		}
	
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		String s="/path/apache/web-app/appname/";//=config.getServletContext().getRealPath(arg0);
		
		}
	
	*/
	@Override
	protected synchronized void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException {
	
		String f = req.getParameter("file");
		if(f==null) throw new ServletException("Undefined file");
		File file=new File(f);
		if (!file.exists() || !file.isFile()) throw new ServletException("File not found on server");

		if (file.getName().toLowerCase().endsWith(".jpg")){
			resp.setHeader("Content-Type", "image/jpeg");
		}else if (file.getName().toLowerCase().endsWith(".png")){
			resp.setHeader("Content-Type", "image/png");
		}else if (file.getName().toLowerCase().endsWith(".pdf")){
			resp.setHeader("Content-Type", "application/pdf");
		}else if (file.getName().toLowerCase().endsWith(".txt")){
			resp.setHeader("Content-Type", "text/plain");
		}else{
			throw new ServletException("Can't find file extension");
		}
		
		FileInputStream reader=null;
		OutputStream out = null;
		try {
			reader = new FileInputStream(f);
			resp.setHeader("Content-Length", String.valueOf(file.length()));
			resp.setHeader("Content-disposition", "attachment;filename=\"" + file.getName() + "\"");
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
