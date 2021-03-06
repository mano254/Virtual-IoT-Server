/*
  SmartDoorLock.java
  IoT Device for testing purpose
  Includes basic functions including lock, unlock, and change password.
*/

/*
  Usage Notes
  - Must hold button to trigger password input
  - Follow prompts when inputting password
*/

import mraa.Aio;
import mraa.Gpio;
import mraa.Pwm;
import java.util.concurrent.TimeUnit;
import java.util.Arrays;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import javax.net.ssl.SSLSocket;
import java.io.PrintWriter;

public class SmartDoorLock {
  private static String status;

  static {
    try {
      System.loadLibrary("mraajava");
    } catch (UnsatisfiedLinkError e) {
      System.err.println(
          "Native code library failed to load. See the chapter on Dynamic Linking Problems in the SWIG Java documentation for help.\n" +
          e);
      System.exit(1);
    }
  }

  public static void main(String[] args){

    //SSL

    SSLSocket sslSocket = null;
    SSLClientSocket mSSLClientSocket = new SSLClientSocket(args[0], Integer.parseInt(args[1]));
    if(mSSLClientSocket.checkAndAddCertificates()) {
      sslSocket = mSSLClientSocket.getSSLSocket();
    }
    else {
      return;
    }

    try {

      //Setting up input

      String userInput = "" , serverResponse = "";
      BufferedReader br = new BufferedReader(new InputStreamReader(sslSocket.getInputStream()));
      PrintWriter pw = new PrintWriter(sslSocket.getOutputStream());

      //Sending Virtual Service messages

      pw.println(args[2] + ":" + args[3] + "|DoorSensorPlugin");
      pw.flush();
      while(br.readLine().length() == 0) {
        pw.println(args[2] + ":" + args[3] + "|DoorSensorPlugin");
        pw.flush();
        try {
          TimeUnit.SECONDS.sleep(1);
        } catch (Exception e) {}
      }

      System.out.println("\033[1m\033[32mSuccessfully connected to secure server\033[0m");

      //Declare/instantiate the sensors/motor

      Gpio button = new Gpio(3);
      Aio light = new Aio(3);
      Pwm servo = new Pwm(6);

      //Setting default password

      double password = 1111;
      double enteredPassword;

      while(true) {
        serverResponse = br.readLine().trim();

        //Send packets of locked/unlocked to server

        pw.println(status);
        try {
          TimeUnit.SECONDS.sleep(1);
        } catch (Exception e) {}

        pw.flush();

        //Checks the password entered by button pattern

        if (button.read() == 1) {
          if (inputPassword(button) == password) {
            unlock(servo);
            System.out.println("Succesfully unlocked.");
          }
          else {
            lock(servo);
            System.out.println("Unsuccesful unlock. Please try again.");
          }
        }

        //Analyzes server's message

        if(serverResponse.equals("LOCK")) {
          lock(servo);
          pw.println("Succesfully locked.");
          pw.flush();
        }
        if(serverResponse.equals("UNLOCK")) {
          unlock(servo);
          pw.println("Succesfully unlocked.");
          pw.flush();
        }
        if(serverResponse.equals("CHANGE PASSWORD")) {
          //System.out.println("got here !!!!! ");
          password = changePassword(br,pw,password);
          pw.println("Succesful password change. New password is " + password);
          pw.flush();
        }
      }
    } catch(Exception e) {
      e.printStackTrace();
    }


  }

  //Unlocks the door. Called when server issues "UNLOCK".

  public static void unlock(Pwm door) {
    door.enable(true);
    door.period_ms(20);
    door.pulsewidth_us(500);
    try {
      TimeUnit.SECONDS.sleep(1);
    }catch (InterruptedException e) {
    }
    door.enable(false);
    status = "UNLOCKED";
  }

  //Locks the door. Called when server issues "LOCK".
  public static void lock(Pwm door) {
    door.enable(true);
    door.period_ms(20);
    door.pulsewidth_us(2500);
    try {
      TimeUnit.SECONDS.sleep(1);
    }catch (InterruptedException e) {
    }
    door.enable(false);
    status = "LOCKED";
  }

  //Allows user to enter a password through the button.

  public static double inputPassword(Gpio doorButton) {

    ArrayList<Integer> generatedPassword = new ArrayList<Integer>();
    int passLength = 0;
    int value = 0;
    double pass = 0;
    int shouldContinue = 0;

    BufferedReader length = new BufferedReader(new InputStreamReader(System.in));
    System.out.println("How long is the password?");

    try {
      passLength = Integer.parseInt(length.readLine());
    } catch (Exception e)  {}

    for (int i = 0; i < passLength; i++) {
      BufferedReader cont = new BufferedReader(new InputStreamReader(System.in));
      System.out.println("Type 1 to continue recording the password: ");

      try {
      shouldContinue = Integer.parseInt(cont.readLine());
      } catch (Exception e) {}

      if (shouldContinue == 1) {
        value = doorButton.read();
        generatedPassword.add(value);
      }
      else {
        break;
      }

    }

      for(int i = 0; i < passLength; i++){
      pass = pass + (Math.pow(10,(passLength-i-1)) * generatedPassword.get(i));
      }

    return pass;
  }

  //Changes the password. Called when server issues "CHANGE PASSWORD".

  public static double changePassword(BufferedReader br, PrintWriter pw, double currentPass) {
    pw.println("What is the new password?");
    double password = currentPass;
    try {
       password = Integer.parseInt(br.readLine());
    } catch (Exception e)  {}
    pw.flush();
    return password;
  }

}
