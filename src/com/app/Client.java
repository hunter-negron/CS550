package com.app.client;

import java.util.*;
import java.rmi.Naming;
import java.io.File;
import java.io.FileOutputStream;

import com.lib.interfaces.*;
import com.lib.peer_client.*;

public class Client {
  final static String hostname = "localhost";
  static PeerClient pc;

  public static void PrintMessageLn(String str){
    System.out.println("PEER(" + pc.getId() + "): " + str);
  }

  public static void PrintClientOptions(){
    PrintMessageLn("Enter the file name you would like to search.");
  }

  public static void main(String[] args) {
    // The one and only command line arg is the working directory
    if(args.length != 1) {
      System.err.println("ERROR: Please specify one directory.");
      System.exit(0);
    }

    try{
      // Create client
      pc = new PeerClient(hostname, args[0]); // need to get these strings dynamically
    }
    catch (Exception ex) {
      System.err.println("EXCEPTION: Client Exception while creating peer client: " + ex.toString());
      ex.printStackTrace();
    }

    // User interface
    Scanner sc = new Scanner(System.in);
    String strInput;

    PrintMessageLn("Hello Client.");
    while(true){
      // initial prompt
      PrintClientOptions();
      strInput = sc.nextLine();

      if(strInput.length() != 0){
        try{
          // Get list of peer IDs who have desired file
          ArrayList<Integer> clientList = pc.searchIndex(strInput);

          if(clientList == null || clientList.size() == 0){
            System.err.println("ERROR: OOPS! None of the clients have file " + strInput);
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

          // prompt user to select a peer
          PrintMessageLn("Select a client from which to retrieve the file.");
          int clientSelect;
          try {
            clientSelect = Integer.parseInt(sc.nextLine());
          }
          catch(Exception e) {
            continue;
          }
          if(!clientList.contains(clientSelect)) {
            System.err.println("ERROR: Invalid client selection.");
            continue;
          }

          // retrieve and save file
          pc.retrieveFile(strInput, clientSelect);
        }
        catch (Exception ex) {
          System.err.println("EXCEPTION: Client Exception while SEARCHING to central sever: " + ex.toString());
          ex.printStackTrace();
        }
      }
      else{
        System.err.println("ERROR: Empty string received in the input!");
      }
    }
  }
}
