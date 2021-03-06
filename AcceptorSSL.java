/*
  AcceptorSSL.java
  Contains the thread that monitors for new encrypted connections
*/

import java.net.ServerSocket;
import java.net.Socket;
import javax.net.ssl.*;
import java.net.InetAddress;
import java.io.*;
import java.util.Arrays;
import java.lang.reflect.*;
import Plugins.*;

public class AcceptorSSL extends Thread {

  public void run(){ //Overwrites run

    PrintWriter out = null;
    BufferedReader in = null;

    int port = 6000;

    String line = "";

    SSLServerSocket serverSocket = null;
    SSLServerSocketFactory factory=(SSLServerSocketFactory) SSLServerSocketFactory.getDefault();

    while(true){ //Accepts client connection and establishes server connection

      SSLSocket sslsocket = null;

      try{
        serverSocket = (SSLServerSocket) factory.createServerSocket(port);
        sslsocket = (SSLSocket) serverSocket.accept();
        out = new PrintWriter(sslsocket.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(sslsocket.getInputStream()));

        line = in.readLine();
        System.out.println(line);
      }
      catch(Exception e){
        e.printStackTrace();
      }

      if(line.indexOf(":") != -1 && line.indexOf("|") != -1){ //parses the input from the client
        String serverIP = line.split(":")[0]; //IP of server
        int serverPort = Integer.parseInt(line.split(":")[1].split("\\|")[0]); //Port of Server
        String className = line.split("\\|")[1]; //name of the class

        Class clazz;
        IoTDevice device = null;

        try{ //Calls the constructor of the class
          clazz = Class.forName(className);

          Class[] cArg = new Class[2];
          cArg[0] = int.class;
          cArg[1] = String.class;

          device = (IoTDevice) clazz.getDeclaredConstructor(cArg).newInstance(serverPort, serverIP);
        }
        catch(Exception e){
          e.printStackTrace();
        }

        out.println("Successfuly connected");

        VirtualMachine virtualMachine = new VirtualMachine(sslsocket, "test.txt", device);
        virtualMachine.start();
      }

      port++;
      System.out.println("Now accepting new SSL connections on port " + port);

      try{
        Thread.sleep(10);
      }
      catch(Exception e){
        e.printStackTrace();
      }
    }

  }
}
