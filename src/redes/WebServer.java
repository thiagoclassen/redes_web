package redes;

import java.io.* ;
import java.net.* ;
import java.util.* ;


public final class WebServer
{
	public static void main(String argv[]) throws Exception
	{
		int port = 8789;		
		//String host = InetAddress.getByName(null);
		
		// Establish the skt
		Socket socket;
		
		while(true){
			// Listen for a TCP req
			socket = new ServerSocket(port).accept();
			HttpRequest request = new HttpRequest(socket);
			
			Thread thread = new Thread(request);
			thread.start();
		}
	}
}

final class HttpRequest implements Runnable
{

	final static String CRLF = "\r\n";
	Socket socket;
	
	public HttpRequest(Socket socket) throws Exception{
		this.socket = socket;
	}
	
	@Override
	public void run() {
		
		try{
			procesRequest();
		} catch(Exception e) {
			System.out.println(e);
		}
		
	}
	
	private void procesRequest() throws Exception
	{
		InputStream is = this.socket. getInputStream();
		
		DataOutputStream os = new DataOutputStream(socket.getOutputStream());
		
		BufferedReader br = new BufferedReader(new InputStreamReader(is, "UTF-8"));
		
		String requestLine = br.readLine();
		
		System.out.println();
		System.out.println(requestLine);
		
		String headerLine = null;
		while((headerLine = br.readLine()).length() != 0){
			System.out.println(headerLine);
		}
		
		
		StringTokenizer tokens = new StringTokenizer(requestLine);
		tokens.nextToken(); // skip over the method, which should be "GET"
		String fileName = tokens.nextToken();
		// Prepend a "." so that file request is within the current directory.
		fileName = "." + fileName;
		
		// Open the requested file.
		FileInputStream fis = null;
		boolean fileExists = true;
		try {
		fis = new FileInputStream(fileName);
		} catch (FileNotFoundException e) {
		fileExists = false;
		}
		
		// Construct the response message.
		String statusLine;
		String contentTypeLine = null;
		String entityBody = null;
		if (fileExists) {
		statusLine = "200";
		contentTypeLine = "Content-type: " +
		contentType( fileName ) + CRLF;
		} else {
		statusLine = "404";
		contentTypeLine = "Not Found";
		entityBody = "<HTML>" +
		"<HEAD><TITLE>Not Found</TITLE></HEAD>" +
		"<BODY>Not Found</BODY></HTML>";
		}
		
		// Send the status line.
		os.writeBytes(statusLine);
		// Send the content type line.
		os.writeBytes(contentTypeLine);
		// Send a blank line to indicate the end of the header lines.
		os.writeBytes(CRLF);
		
		// Send the entity body.
		if (fileExists) {
		sendBytes(fis, os);
		fis.close();
		} else {
		//os.writeBytes();
		}
		
		os.close();
		br.close();
		socket.close();
		
	}
	
	private static String contentType(String fileName)
	{
		if(fileName.endsWith(".htm") || fileName.endsWith(".html")) {
			return "text/html";
		}
		if(fileName.endsWith("gif")) {
			return "gif";
		}
		if(fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")) {
			return "image/jpeg";
		}
	return "application/octet-stream";
	}
	
	private static void sendBytes(FileInputStream fis, OutputStream os) throws Exception
		{
			 // Construct a 1K buffer to hold bytes on their way to the socket.
			 byte[] buffer = new byte[1024];
			 int bytes = 0;
			 // Copy requested file into the socket's output stream.
			 while((bytes = fis.read(buffer)) != -1 ) {
				 os.write(buffer, 0, bytes);
			 }
		}

}