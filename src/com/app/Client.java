package com.app.client;

import java.util.*;
import java.rmi.Naming;
import java.io.File;
import java.io.FileOutputStream;

import com.lib.interfaces.*;
import com.lib.peer_client.*;

// public int register(String ip, String lookupString, Vector<String> filenames)

public class Client {
  final static String rmiStr = "//localhost/peer";
  final static String rmiServerStr = "//localhost/central_server";
  static int myPeerId;

  private static RMIServerInterface centralServer;

  public static void PrintMessageLn(String str){
    System.out.println("PEER(" + myPeerId + "): " + str);
  }

  public static void PrintClientOptions(){
    System.out.println("Enter the file name you would like to search.");
  }

  public static void ReadSharedDirectory(){
    // Read all the files in the directory and return
  }

  public static void retrieveFile(String filename, int peerId) {
    try{
      System.out.println("Retrieving file \"" + filename + "\" from peer " + peerId);
      RMIClientInterface peer = (RMIClientInterface) Naming.lookup(rmiStr + peerId);
      FileInfo fileinfo = peer.retrieve(filename);
      try {
      	File f = new File(fileinfo.filename);
      	f.createNewFile();
      	FileOutputStream out = new FileOutputStream(f,true);
      	out.write(fileinfo.data,0,fileinfo.len);
      	out.flush();
      	out.close();
      	System.out.println("Done writing data...");
      }
      catch(Exception e){
      	e.printStackTrace();
      }
    }
    catch (Exception ex) {
      System.err.println("Client Exception while CONNECTING to peer client: " + ex.toString());
      ex.printStackTrace();
    }
  }

  public static void main(String[] args) {
    try{
      centralServer = (RMIServerInterface)Naming.lookup("//localhost/central_server");
    }
    catch (Exception ex) {
      System.err.println("Client Exception while CONNECTING to central sever: " + ex.toString());
      ex.printStackTrace();
    }

    // Read the files using ReadSharedDirectory() and put it into the vector here!
    Vector<String> files = new Vector<String>();
    files.add("foo");
    files.add("moo");
    files.add("boo");

    try{
      myPeerId = centralServer.register(rmiStr, files);
      System.out.println("Your peer id is : " + myPeerId);
    }
    catch (Exception ex) {
      System.err.println("Client Exception while REGISTERING to central sever: " + ex.toString());
      ex.printStackTrace();
    }

    try{
      PeerClient pc = new PeerClient(rmiStr, myPeerId); // need to get these strings dynamically
    }
    catch (Exception ex) {
      System.err.println("Client Exception while creating peer client: " + ex.toString());
      ex.printStackTrace();
    }

    Scanner sc = new Scanner(System.in);
    String strInput;

    PrintMessageLn("Hello Client.");
    while(true){
      PrintClientOptions();
      strInput = sc.nextLine();

      if(strInput.length() != 0){
        try{
          ArrayList<Integer> clientList = centralServer.search(strInput);

          if(clientList == null || clientList.size() == 0){
            PrintMessageLn("OOPS! None of the clients have file " + strInput);
            continue;
          }

          PrintMessageLn("Client list start");
          for(int i = 0; i < clientList.size(); i++){
            System.out.print(clientList.get(i));
            if(i < clientList.size() - 1)
              System.out.print(",");
          }
          System.out.println();
          PrintMessageLn("Client list end");

          System.out.println("Select a client from which to retrieve the file.");
          int clientSelect = Integer.parseInt(sc.nextLine());
          if(!clientList.contains(clientSelect)) {
            System.out.println("Invalid client selection.");
            continue;
          }

          retrieveFile(strInput, clientSelect);
        }
        catch (Exception ex) {
          System.err.println("Client Exception while SEARCHING to central sever: " + ex.toString());
          ex.printStackTrace();
        }
      }
      else{
        PrintMessageLn("Empty string received in the input!");
      }
    }
  }
}
