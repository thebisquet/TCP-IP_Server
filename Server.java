import java.io.*;
import java.net.*;
import java.util.*;

public final class Server {
	public static void main(String[] args) throws Exception {
		
		//Get port number from command line
		int port = Integer.parseInt(args[0]);
		
		//Establish the listening socket
		try( ServerSocket socket = new ServerSocket(port)) {
			//Count the number of the thread
			int i = 1;
			
			//Process HTTP service requests in an infinite loop
			while( true ) {
				//Listen for a TCP connection request
				System.out.println("Listening for Connection on Port "+port+"...");
				Socket connection = socket.accept();
				System.out.println("Connection Successful!");
				
				//Create object to process HTTP request
				HTTP_Request request = new HTTP_Request(connection);
				
				//Create new thread to process request
				Thread thread = new Thread(request);
				
				//Begin thread
				thread.start();
				System.out.println("----------This is thread number "+i+"----------");
				i++;
			}
		}
	}
}
