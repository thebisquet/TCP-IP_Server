import java.io.*;
import java.net.*;
import java.util.*;

final class HTTP_Request implements Runnable {
	final static String CRLF = "\r\n";
	Socket socket;
	
	//Constructor
	public HTTP_Request(Socket socket) throws Exception{
		this.socket = socket;
	}
	
	//Implement the run() method of the Runnable interface
	public void run() {
		try {
			processRequest();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	private void processRequest() throws Exception {
		//Get reference to socket input and output streams
		InputStream is = socket.getInputStream();
		DataOutputStream os = new DataOutputStream(socket.getOutputStream());
		
		//Setup input stream filters
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		
		//Optional Server Address Information
		InetAddress sa = InetAddress.getLocalHost();
		//Client IP Address
		InetAddress ca = socket.getInetAddress();
		//Convert IP into string
		String IPClient = ca.toString();
		
		//Get Request line of the HTTP request message
		String requestLine = br.readLine();
		
		//Optional Starting Line
		long st = System.currentTimeMillis();
		Date start = new Date();
		
		//Extract filename from the request line
		StringTokenizer tokens = new StringTokenizer(requestLine);
		//Skip over the method, which should be "GET"
		tokens.nextToken();
		String fileName = tokens.nextToken();
		
		//Prepend a "." so the file request is within the current directory
		fileName = "." + fileName;
		
		//Open the requested file
		FileInputStream fis = null;
		boolean fileExists = true;
		try {
			fis = new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
			fileExists = false;
		}
		
		//Debug info for private use
		System.out.println("Incoming!!!");
		System.out.println(requestLine);
		String headerLine = null;
		while((headerLine = br.readLine()).length() != 0) {
			System.out.println(headerLine);
		}
		
		//Construct the response message
		String statusLine = null;
		String contentTypeLine = null;
		String entityBody = null;
		if(fileExists) {
			statusLine = "HTTP/1.0 200 OK" + CRLF;
			contentTypeLine = "Content-Type: " + contentType(fileName) + CRLF;
		} else {
			statusLine = "HTTP/1.0 404 Not Found" + CRLF;
			contentTypeLine = "Content-Type: text/html" + CRLF;
			entityBody = "<HTML>" + "<HEAD><TITLE>Not Found</TITLE></HEAD>" + "<BODY>Not Found</BODY></HTML>";
		}
		
		//Send the status line
		os.writeBytes(statusLine);
		//Send the content type
		os.writeBytes(contentTypeLine);
		//Send blank line to indicate end of header lines
		os.writeBytes(CRLF);
		
		//Send the entity body
		if(fileExists) {
			sendBytes(fis, os);
			fis.close();
		} else {
			os.writeBytes(entityBody);
		}
		
		//Close streams and socket
		os.close();
		br.close();
		socket.close();
	}
	
	private static void sendBytes(FileInputStream fis, OutputStream os) throws Exception {
		//Construct a 1K buffer to hold bytes on their way to socket
		byte[] buffer = new byte[1024];
		int bytes = 0;
		
		//Copy requested file into socket output stream
		while((bytes = fis.read(buffer)) != -1) {
			os.write(buffer, 0, bytes);
		}
	}
	
	private static String contentType(String fileName) {
		if(fileName.endsWith(".htm") || fileName.endsWith(".html")) {
			return "text/html";
		}
		if(fileName.endsWith(".ram") || fileName.endsWith(".ra")) {
			return "audio/x-pn-realaudio";
		}
		return "application/octet-stream";	
	}
}
