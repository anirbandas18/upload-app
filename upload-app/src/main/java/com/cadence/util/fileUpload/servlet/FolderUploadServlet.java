package com.cadence.util.fileUpload.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.cadence.util.fileUpload.util.Config;
import com.cadence.util.fileUpload.util.Constants;

/**
 * Servlet implementation class SearchServlet
 */
public class FolderUploadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger logger = LogManager.getLogger(FolderUploadServlet.class);
	private static final String CONTENT_TYPE = "text/html; charset=windows-1252";
	// private static final String appPath = "/software/cdpapp/upload";
	public static final String appPath = Config.getValue(Constants.FILE_STOARGE_PATH);
	public static final String workPath = appPath + File.separator + "work";

	private Map<String, String> fileUploadStatus = new HashMap<String, String>();
	//private String baseDir;

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		System.out.println("initing..");
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		doPost(request, response);
	}

	/**
	 * handles file upload
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// gets absolute path of the web application
		// String appPath = request.getServletContext().getRealPath("");
		// System.out.println(appPath);
		int i = 1;
		for (Enumeration<String> e = request.getParameterNames(); e.hasMoreElements();) {
			String parameter = (String) e.nextElement();
			if (!"data".equals(parameter)) {
				System.out.println(i++ + " > name=" + parameter + ";value=" + request.getParameter(parameter));
			}
		}
		if ("listFiles".equals(request.getParameter("call"))) {
			listFiles(request, response);
		} else if ("findStatus".equals(request.getParameter("call"))) {
			findStatus(request, response);
		} else if ("listFileSrl".equals(request.getParameter("call"))) {
			listFileSrl(request, response);
		} else if ("saveFile".equals(request.getParameter("call"))) {
			saveFile(request);
		}

		/*
		 * response.setContentType("text/xml;charset=UTF-8"); PrintWriter out =
		 * response.getWriter(); StringBuffer sb=new StringBuffer();
		 * sb.append("<?xml version='1.0' encoding='UTF-8'?>");
		 * sb.append("<file>\n");
		 * sb.append("<message>").append(message).append("</message>\n");
		 * sb.append("</file>");
		 * 
		 * String str=sb.toString(); out.println(str); out.flush();
		 */
		// getServletContext().getRequestDispatcher("/message.jsp").forward(request,
		// response);
	}

	private void saveFile(HttpServletRequest request) throws IOException, ServletException {
		// constructs path of the directory to save uploaded file
		//System.out.println(baseDir);
		String filename = request.getParameter("file");
		int size = Integer.parseInt(request.getParameter("size"));
		String srl = request.getParameter("srl");
		int totalFiles = Integer.parseInt((String)request.getParameter("totalSize"));
		HttpSession session = request.getSession();
		String baseDir = (String) session.getAttribute("baseDir");
		
		System.out.println("got file" + filename);

		String destinationDirPath = workPath + File.separator + filename;
		String destinationFilePath = destinationDirPath + File.separator + srl;
		File destinationDir = createDir(destinationDirPath);
		File dir = createDir(appPath + File.separator + baseDir);
		File destinationFile = new File(destinationFilePath);
		
		if (destinationFile.exists()) {
			return;
		}
		//OutputStream out = null;

        InputStream filecontent = null;
	    try {
	        filecontent = request.getInputStream();

	        byte[] bytes = IOUtils.toByteArray(filecontent, size);
		    System.out.println("size=" + bytes.length);
		    FileUtils.writeByteArrayToFile(destinationFile, bytes);

		    System.out.println("New file " + filename + " created at " + appPath + ",size=" + bytes.length);
	    } catch (FileNotFoundException fne) {
	    	System.out.println("You either did not specify a file to upload or are "
	                + "trying to upload a file to a protected or nonexistent "
	                + "location.");
	    	System.out.println("<br/> ERROR: " + fne.getMessage());

	    } finally {
	       /* if (out != null) {
	            out.close();
	        }*/
	        if (filecontent != null) {
	            filecontent.close();
	        }
	    }
	    
		File[] files = destinationDir.listFiles();
		if (files.length == totalFiles) {
			System.out.println("Received final file");
			mergeFiles(destinationDir.getAbsolutePath(), dir.getAbsolutePath(), filename, totalFiles);
			// merge files
		}

	}
	
	/*public String getBody(HttpServletRequest request, File dir, String writeTofile, int totalFiles, String filename) throws IOException {

	    String body = null;
	    StringBuilder stringBuilder = new StringBuilder();
	    BufferedReader bufferedReader = null;

	    try {
	        InputStream inputStream = request.getInputStream();
	        if (inputStream != null) {
	            bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
	            byte[] charBuffer = new byte[128];
	            int bytesRead = -1;
	            while ((bytesRead = inputStream.read(charBuffer)) > 0) {
	        		File fileName = new File(dir, writeTofile);
	        		FileUtils.writeByteArrayToFile(fileName, charBuffer);
	            }
	        } else {
	            stringBuilder.append("");
	        }
			File[] files = dir.listFiles();
			if (files.length == totalFiles) {
				System.out.println("Received final file");
				String outputDir = appPath + File.separator + baseDir;
				String inputDir = dir.getAbsolutePath();
				mergeFiles(inputDir, outputDir, filename, totalFiles);
				// merge files
			}
	    } catch (IOException ex) {
	        throw ex;
	    } finally {
	        if (bufferedReader != null) {
	            try {
	                bufferedReader.close();
	            } catch (IOException ex) {
	                throw ex;
	            }
	        }
	    }
	    
		//File fileName = new File(dir, writeTofile);
		//FileUtils.writeByteArrayToFile(fileName, buffer);
	    // body = stringBuilder.toString();
	    return body;
	}*/

	private void mergeFiles(String inputDirPath, String outputDir, String outputFileName, long noOfFiles) throws FileNotFoundException {
		try {
		File outputfile = new File(outputDir + File.separator + outputFileName);
		outputfile.createNewFile();
		FileOutputStream fstream = new FileOutputStream(outputfile);
		for (long i = 0; i < noOfFiles; i++) {
			FileInputStream fis;
			try {
				byte[] buffer = new byte[1024];
				fis = new FileInputStream(inputDirPath + File.separator + i);

				int noOfBytes;
				while ((noOfBytes = fis.read(buffer)) != -1) {
		                fstream.write(buffer, 0, noOfBytes);
				}

				fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
			fstream.flush();
			fstream.close();
			File inputDir = new File(inputDirPath);
			File newInputDir = new File(inputDirPath + Config.getValue(Constants.FILE_MERGE_STATUS));
			boolean rename = inputDir.renameTo(newInputDir);
			System.out.println("Directory renamed " + rename);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private File createDir(String filename) throws IOException {
		File dir = new File(filename);
		if (dir.exists() && !dir.isDirectory()) {
			FileUtils.forceDelete(dir);
		}
		FileUtils.forceMkdir(dir);
		return dir;
	}

	/**
	 * Extracts file name from HTTP header content-disposition
	 */
	private String extractFileName(Part part) {
		String contentDisp = part.getHeader("content-disposition");
		String[] items = contentDisp.split(";");
		for (String s : items) {
			if (s.trim().startsWith("filename")) {
				return s.substring(s.indexOf("=") + 2, s.length() - 1);
			}
		}
		return "";
	}

	private void listFiles(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		String filename = request.getParameter("filename");
		File dir = new File(appPath);
		dir.mkdirs();
		PrintWriter out = response.getWriter();

		System.out.println("IsDirectory : " + dir.isDirectory() + " and Exists : " + dir.exists());
		
		// String[] files = dir.list();
		File[] files = dir.listFiles();
		JSONArray arr = new JSONArray();

		for (File aFile : files) {
			if (aFile.getName().equals(filename)) {
				JSONObject obj = new JSONObject();
				obj.put("Filename", aFile.getName());
				obj.put("size", aFile.length());
				arr.add(obj);
			}
		}

		System.out.println(arr.toString());
		// out.print(jsonContent);
		out.flush();

		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(arr.toString());
	}

	private void listFileSrl(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		String filename = request.getParameter("filename");
		String srl = request.getParameter("srl");HttpSession session = request.getSession();
		String baseDir = (String) session.getAttribute("baseDir");
		File theFile = new File(appPath + File.separator + baseDir + File.separator + filename + File.separator + srl);


		PrintWriter out = response.getWriter();

		// String[] files = dir.list();
		JSONArray arr = new JSONArray();



		if (theFile.exists()) {
			JSONObject obj = new JSONObject();
			obj.put("Filename", theFile.getName());
			obj.put("size", theFile.length());
			arr.add(obj);
		}

		System.out.println(arr.toString());
		// out.print(jsonContent);
		out.flush();

		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(arr.toString());
	}

	private void findStatus(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {

		String srl = (String) request.getParameter("srl");
		
		// String[] files = dir.list();
		JSONArray arr = new JSONArray();


		for (Object key : fileUploadStatus.keySet()) {
			JSONObject obj = new JSONObject();
			obj.put("Filename", key);
			arr.add(obj);
		}

		response.setContentType("text/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(arr.toString());
	}
}
