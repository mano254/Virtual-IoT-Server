/*
  Acceptor.java
  Contains the thread that monitors for new connection
*/

import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetAddress;
import java.io.*;

public class Acceptor extends Thread {

  public void run(){
    ServerSocket serverSocket = null;
    Socket clientSocket = null;
    PrintWriter out = null;
    BufferedReader in = null;

    int port = 2001;

    String line = "";

    try{
      serverSocket = new ServerSocket(2000);
    }
    catch(Exception e){
      e.printStackTrace();
    }

    while(true){
      try{
        clientSocket = serverSocket.accept();
        out = new PrintWriter(clientSocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
      }
      catch(Exception e){
        e.printStackTrace();
      }

      try{
        line = in.readLine();
      }
      catch(Exception e){
        e.printStackTrace();
      }

      if(line.indexOf(":") != -1){
        String serverIP = line.split(":")[0];
        int serverPort = Integer.parseInt(line.split(":")[1]);

        VirtualMachine virtualMachine = new VirtualMachine(serverIP, serverPort, port, "test.txt");
        virtualMachine.start();
        try{
          out.println("Adding you to virtual service. Connect to " + InetAddress.getLocalHost().getHostAddress() + ":" + port);
        }
        catch(Exception e){
          e.printStackTrace();
        }
      }

      try{
        clientSocket.close();
      }
      catch(Exception e){
        e.printStackTrace();
      }

      port++;

      try{
        Thread.sleep(10);
      }
      catch(Exception e){
        e.printStackTrace();
      }
    }

  }
}