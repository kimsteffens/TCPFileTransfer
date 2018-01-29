import java.io.*;
import java.net.*;
/************************************************************************
 * Client class allows the user to connect with the server at the 
 * given ip and port number. The user sends a request to the server for
 * a file to be sent back to the client. It uses TCP protocol. The user
 * may receive multiple files on the same connection.
 *
 * @author Kim Steffens
 * @version October, 3 2016
 ***********************************************************************/
class Client {
 public static void main(String args[]) {
  String file = "";
  BufferedReader inFromUser = new BufferedReader(
   new InputStreamReader(System.in));

  try {
   // asking for ip and port of server
   System.out.println("Enter an IP address: ");
   String ip = inFromUser.readLine();
   System.out.println("Enter a port number: ");
   String port = inFromUser.readLine();
   Socket clientSocket = null;

   // ensure ip and port are usable. 
   try {
    clientSocket = new Socket(ip, Integer.parseInt(port));
   } catch (Exception e) {
    System.out.println("Invalid IP or port number. Now quitting.");

    // end communications if not valid
    System.exit(0);
   }

   // input-output streams
   DataOutputStream outToServer = new DataOutputStream(
    clientSocket.getOutputStream());
   DataInputStream inFromServer = new DataInputStream(
    clientSocket.getInputStream());
   
   // retrieve file name from user
   System.out.println("Enter the file name you wish to send or " +
    "type 'quit' to leave: ");
   file = inFromUser.readLine();

   // continue looping until user quits
   myloop: while (!file.equalsIgnoreCase("Quit")) {

    outToServer.writeBytes(file + '\n');

    // receive file size
    int fileSize = inFromServer.readInt();

    // if there is no file
    if (fileSize == -1) {
     System.out.println("File does not exist, now quitting.");
     break myloop;
    }

    // array with large enough of space to use
    byte[] bytes = new byte[fileSize];
    DataInputStream inputStream =
     new DataInputStream(clientSocket.getInputStream());

    // put "My-" to differentiate from original file when saved
    file = "My-" + file;

    // obtaining file from server
    try {
     FileOutputStream foutput = new FileOutputStream(file);
     BufferedOutputStream boutput = new BufferedOutputStream(foutput);
     int bytesRead = inputStream.read(bytes, 0, bytes.length);
     int toSend = bytesRead;

     // write to file
     for (int i = fileSize; i >= 0; i--) {
      bytesRead = inputStream.read(bytes, toSend, (bytes.length - toSend));
      toSend += bytesRead;
     }

     boutput.write(bytes, 0, toSend);
     boutput.flush();

     System.out.println("File transfer complete.");

     // clean up and end
     boutput.close();
     System.out.println("Enter the file name you wish to send or " +
      "type 'quit' to leave: ");
     file = inFromUser.readLine();

    } catch (Exception e) {
     System.out.println("File could not transferred.");
     clientSocket.close();
    }
   }

   try {

    // inform server that client is quitting before leaving
    outToServer.writeBytes("quit");
    clientSocket.close();
   } catch (Exception e) {
    System.out.println("Cannot close socket.");
    System.exit(0);
   }

  } catch (Exception e) {
   System.out.println("IO exception. Now exitting.");
   System.exit(0);
  }
 }
}
