import java.io.*;
import java.net.*;
/************************************************************************
 * Server class connects on a user-given port number, and waits 
 * for a client to connect. It sets up a ClientHandler thread, which 
 * deals with transferring files to the client. It uses TCP protocol.
 * After a file transfer, it will continue to stay connected until the
 * user notifiers the server that it is quitting. The Server will run 
 * indefinitely and will accept multiple clients.
 *
 * @author Kim Steffens
 * @version October 3, 2016
 ***********************************************************************/
class Server {
 public static void main(String args[]) throws Exception {

  // ask user for the port number.
  BufferedReader inFromUser = new BufferedReader
		             (new InputStreamReader(System.in));
  System.out.println("Enter a port number");
  String port = inFromUser.readLine();

  ServerSocket listenSocket = null;
  Socket clientSocket = null;

  // try to connect on port
  try {
   listenSocket = new ServerSocket(Integer.parseInt(port));
  }
  // if port cannot be used
  catch (Exception e) {
   System.out.println("Invalid port number. Shutting down.");
   System.exit(0);
  }

  System.out.println("Waiting for a client...");

  // accept client when found and begin ClientHandler tasks
  while (true) {
   clientSocket = listenSocket.accept();
   Runnable r = new ClientHandler(clientSocket);
   Thread t = new Thread(r);
   t.start();
   System.out.println("Found a client. Wait for client to request file.");
  }
 }
}

/************************************************************************
 * ClientHandler class deals with sending the file to the client.
 * Contains error checking, file transfer, and communications with client.
 *
 * @author Kim Steffens
 * @version October 3, 2016
 ***********************************************************************/
class ClientHandler implements Runnable {

/* The client's socket */
 Socket clientSocket;

// constructor 
 ClientHandler(Socket connection) {
  clientSocket = connection;
 }

 public void run() {

  // stay connected to user with while loop
  myloop: while (true) {

   // try to catch errors not found in other try/catches
   try {

    BufferedReader inFromUser = null;
    DataOutputStream outToClient = null;
    BufferedReader inFromClient = null;
    String sendFile = "";

    try {
     inFromUser = new BufferedReader(new InputStreamReader(System.in));
     outToClient = new DataOutputStream(clientSocket.getOutputStream());
     inFromClient = new BufferedReader
		   (new InputStreamReader(clientSocket.getInputStream()));

     System.out.println("Waiting for client to request file.");

     // obtain file name from client
     sendFile = inFromClient.readLine();
     if (sendFile.equalsIgnoreCase("quit"))
      break myloop;
     System.out.println("File to send: " + sendFile);

    } catch (IOException e) {
     System.out.println("Error in obtaining file name.");
     break myloop;
    }

    try {

     // convert sendFile from String to File
     File fileToSend = new File(sendFile);

     // check if file exists
     if (!fileToSend.exists()) {
      System.out.println("File does not exist. Now quitting.");

      // send -1 file size to indicate invalid file to client
      outToClient.writeInt(-1);
      break myloop;
     } else {

      // send file size to client
      outToClient.writeInt((int) fileToSend.length());
      outToClient.flush();

      byte[] bytes = new byte[(int) fileToSend.length()];
      FileInputStream finput = new FileInputStream(fileToSend);
      BufferedInputStream binput = new BufferedInputStream(finput);

      // gather file
      binput.read(bytes, 0, bytes.length);

      // sending to client
      outToClient.write(bytes, 0, bytes.length);
      System.out.println("Success: sent file to client.");

      // clean up
      binput.close();
      finput.close();
      outToClient.flush();
     }

     // error in transferring
    } catch (IOException e) {
     System.out.println("Sending file failed.");
     break myloop;
    }

    // error found elsewhere
   } catch (Exception e) {
    System.out.println("Unknown error.");
    System.exit(0);
   }
  }

  // disconnect
   try {
   clientSocket.close();
   System.out.println("Disconnected from a client.");
  } catch (Exception e) {
   System.out.println("Cannot close socket. Exitting.");
   System.exit(0);
  }

 }
}
