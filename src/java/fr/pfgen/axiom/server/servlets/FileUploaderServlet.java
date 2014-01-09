package fr.pfgen.axiom.server.servlets;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.http.HttpStatus;

import fr.pfgen.axiom.server.utils.IOUtils;
import fr.pfgen.axiom.server.utils.ServerUtils;

@SuppressWarnings("serial")
public class FileUploaderServlet extends HttpServlet{

	private Hashtable<String, File> appFiles; 

	@Override
	@SuppressWarnings("unchecked")
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		appFiles = (Hashtable<String, File>)getServletContext().getAttribute("ApplicationFiles");
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException,IOException {
		PrintWriter pw = null;
		InputStreamReader in = null;

		if (!(ServletFileUpload.isMultipartContent(request))){
			throw new ServletException("Not a multipart request");
		}

		// Create a factory for disk-based file items
		FileItemFactory factory = new DiskFileItemFactory();

		// Create a new file upload handler
		ServletFileUpload upload = new ServletFileUpload(factory);

		// Parse the request
		try {
			@SuppressWarnings("unchecked")
			List<FileItem> items = upload.parseRequest(request);

			// Process the uploaded items
			@SuppressWarnings("rawtypes")
			Iterator iter = items.iterator();
			//String studyName = new String();
			String respString = new String();
			//String user = new String();
			byte[] data = null;
			while (iter.hasNext()) {
				FileItem item = (FileItem) iter.next();
				if (item.isFormField()) {  // Process a regular form field
					//String name = item.getFieldName();
					//String value = item.getString();
					//if (name.equals("studyName")){
					//	studyName = value;
					//}
					//if (name.equals("user")){
					//	user = value;
					//}
				} else {  // Process a file upload
					if (item.getSize()>50000000){
						//response.setStatus(HttpStatus.SC_BAD_REQUEST);
						respString = "Error: File greater than 50Mb";
					}else{
						data = item.get();
						File tmpFolder = appFiles.get("temporaryFolder");
						//File pedigreeFolder = new File(studyFolder, "Pedigree");
						if (!tmpFolder.exists()){
							tmpFolder.mkdir();
						}
						File uploadedFile = new File(tmpFolder, ServerUtils.randomHexString()+"_"+item.getName());
						pw = new PrintWriter(new FileWriter(uploadedFile));
						in = new InputStreamReader(new ByteArrayInputStream(data));
						IOUtils.copyTo(in, pw);

						pw.flush();
						pw.close();
						respString = uploadedFile.getAbsolutePath();
					}
				}
			}
			
			response.setContentType("text/html");
			response.setHeader("Pragma", "No-cache");
			response.setDateHeader("Expires", 0);
			response.setHeader("Cache-Control", "no-cache");
			pw = response.getWriter();
			pw.println("<html>");
			pw.println("<body>");
			pw.println("<script type=\"text/javascript\">");
			pw.println("if (parent.uploadComplete) parent.uploadComplete('" + respString + "');");
			pw.println("</script>");
			pw.println("</body>");
			pw.println("</html>");
			pw.flush();
			pw.close();
		} catch (FileUploadException e) {
			response.setStatus(HttpStatus.SC_BAD_REQUEST);
			response.getWriter().write("Error:"+e.getMessage());
			throw new IOException(e);
		} catch (IOException e) {
			response.setStatus(HttpStatus.SC_BAD_REQUEST);
			response.getWriter().write("Error:"+e.getMessage());
			throw e;
		} finally {
			IOUtils.safeClose(in);
			IOUtils.safeClose(pw);
		}
	}
}
