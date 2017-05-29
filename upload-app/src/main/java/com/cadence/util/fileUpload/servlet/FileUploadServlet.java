package com.cadence.util.fileUpload.servlet;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.cadence.util.fileUpload.util.Config;
import com.cadence.util.fileUpload.util.Constants;

/**
 * Servlet implementation class SearchServlet
 */
public class FileUploadServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static Logger logger = LogManager.getLogger(FileUploadServlet.class);
	private static Properties gsaProperties = new Properties();
	private static final String CONTENT_TYPE = "text/html; charset=windows-1252";
	// private static final String appPath = "/software/cdpapp/upload";
	public static final String appPath = Config.getValue(Constants.FILE_STOARGE_PATH);
	public static final String workPath = appPath + File.separator + "work";

	private static Map<String, String> fileUploadStatus = new HashMap<>();

	public void init(ServletConfig config) throws ServletException {
		super.init(config);
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub

		doPost(request, response);
	}

	private String message;

	/**
	 * handles file upload
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		for (Enumeration<String> e = request.getParameterNames(); e.hasMoreElements();) {
			String parameter = (String) e.nextElement();
			if (!"data".equals(parameter)) {
				System.out.println("name=" + parameter + ";value=" + request.getParameter(parameter));
			}
		}
		if ("listFiles".equals(request.getParameter("call"))) {
			listFiles(request, response);
		} else if ("fileInfo".equals(request.getParameter("call"))) {
			listFiles(request, response, (String) request.getParameter("srl"));
		} else if ("findStatus".equals(request.getParameter("call"))) {
			findStatus(request, response, (String) request.getParameter("srl"));
		} else if ("saveFile".equals(request.getParameter("call"))) {
			saveFile(request, response);
		} else if("listAllFiles".equals(request.getParameter("call"))) {
			listAllFiles(request, response);
		} else if("fileChunkSize".equals(request.getParameter("call"))) {
			System.out.println("Found match");
			fileChunkSize(request, response);
		} 
	}

	

	private void fileChunkSize(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String chunkSizeInBytes = Config.getValue(Constants.FILE_CHUNK_SIZE);
		System.out.println(chunkSizeInBytes + " " + new Date());
		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		response.getWriter();
		PrintWriter out = response.getWriter();
		out.write(chunkSizeInBytes);
		out.flush();
	}

	private void listAllFiles(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		// TODO Auto-generated method stub
		HttpSession session = request.getSession();
		String baseDirName = (String) session.getAttribute("baseDir");
		JSONArray arr = new JSONArray();
		if(baseDirName != null) {
			File baseDir = createDir(appPath, baseDirName);
			File[] files = baseDir.listFiles();
			SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
			Arrays.sort(files);
			for(File f : files) {
				JSONObject ob = new JSONObject();
				ob.put("name", f.getName());
				ob.put("size", f.length());
				Date d = new Date();
				d.setTime(f.lastModified());
				ob.put("lastModified", sdf.format(d));
				arr.add(ob);
			}
		}
		response.setContentType("text/json");
		response.setCharacterEncoding("UTF-8");
		PrintWriter out = response.getWriter();//
		out.write(arr.toJSONString());
		out.flush();
	}

	private void saveFile(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		// constructs path of the directory to save uploaded file

		String filename = request.getParameter("filename");
		int totalFiles = Integer.parseInt(request.getParameter("totalFiles"));
		int size = Integer.parseInt(request.getParameter("size"));
		HttpSession session = request.getSession();
		String baseDir = (String) session.getAttribute("baseDir");
		String srl = request.getParameter("srl");
		File destinationDir = createDir(workPath, filename);
		String destinationDirPath = destinationDir.getAbsolutePath();
		String destinationFilePath = destinationDirPath + File.separator + srl;
		File dir = createDir(appPath, baseDir);
		File destinationFile = new File(destinationFilePath);
		
		if (destinationFile.exists()) {
			return;
		}
		
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
	       if (filecontent != null) {
	            filecontent.close();
	        }
	    }
	    
	    Boolean status = null;
		File[] files = destinationDir.listFiles();
		if (files.length == totalFiles) {
			System.out.println("Received final file");
			status = mergeFiles(destinationDir.getAbsolutePath(), dir.getAbsolutePath(), filename, totalFiles);
			// merge files
		} else {
			status = false;
		}
		
		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(status.toString());
	}
	
	public static String getBody(HttpServletRequest request, File dir, String writeTofile, int totalFiles, String filename, int size) throws IOException {

	    String body = null;
	    StringBuilder stringBuilder = new StringBuilder();
	    BufferedReader bufferedReader = null;

		OutputStream out = null;

        InputStream filecontent = null;
	    try {
	        filecontent = request.getInputStream();

	        int read = 0;
	        byte[] bytes = IOUtils.toByteArray(filecontent, size);
		    System.out.println("size=" + bytes.length);
    		File fileName = new File(dir, writeTofile);
		    FileUtils.writeByteArrayToFile(fileName, bytes);

		    System.out.println("New file " + filename + " created at " + appPath + ",size=" + bytes.length);
	    } catch (FileNotFoundException fne) {
	    	System.out.println("You either did not specify a file to upload or are "
	                + "trying to upload a file to a protected or nonexistent "
	                + "location.");
	    	System.out.println("<br/> ERROR: " + fne.getMessage());

	    } finally {
	        if (out != null) {
	            out.close();
	        }
	        if (filecontent != null) {
	            filecontent.close();
	        }
	    }

	   
	    return body;
	}

	private static boolean mergeFiles(String inputDirPath, String outputDirPath, String outputFileName, long noOfFiles) {
		boolean merge = false;
		FileOutputStream fstream = null;
		File inputDir = new File(inputDirPath);
		File newInputDir = new File(inputDirPath + Config.getValue(Constants.FILE_MERGE_STATUS));
		File outputfile = new File(outputDirPath + File.separator + outputFileName);
		try {
			fstream = new FileOutputStream(outputfile, true);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
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
		try {
			fstream.flush();
			fstream.close();
			boolean rename = inputDir.renameTo(newInputDir);
			System.out.println("Directory renamed " + rename);
			merge = true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return merge;
	}

	private File createDir(String parent, String filename) throws IOException {
		File dir = new File(parent, filename);
		/*
		if (dir.exists() && !dir.isDirectory()) {
			FileUtils.forceDelete(dir);
		}
		*/
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
		fileUploadStatus.clear();
		listFiles(request, response, "");
	}

	private void listFiles(HttpServletRequest request, HttpServletResponse response, String srl)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		String filename = request.getParameter("filename");
		File dir = createDir(workPath, filename);
		
		PrintWriter out = response.getWriter();

		// String[] files = dir.list();
		File[] files = dir.listFiles();
		JSONArray arr = new JSONArray();

		
		for (File aFile : files) {
			if (StringUtils.isEmpty(srl) || aFile.getName().equals(srl)) {
				JSONObject obj = new JSONObject();
				obj.put("Filename", aFile.getName());
				obj.put("size", aFile.length());
				arr.add(obj);
			}
		}

		System.out.println(arr.toJSONString());
		// out.print(jsonContent);
		out.flush();

		response.setContentType("text/plain");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(arr.toJSONString());
	}

	private void findStatus(HttpServletRequest request, HttpServletResponse response, String srl)
			throws ServletException, IOException {

		String jsonContent = "{\n";

		// String[] files = dir.list();
		JSONArray arr = new JSONArray();

		jsonContent += "\"files\":\t[\n";

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
