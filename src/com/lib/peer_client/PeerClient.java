package com.lib.peer_client;

import java.util.*;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.*;

import com.lib.interfaces.*;

public class PeerClient extends UnicastRemoteObject implements RMIClientInterface {
  final String rmiStr;
  final String rmiServerStr = "//localhost/central_server";
  private int id;
  private String dir;
  private RMIServerInterface centralServer;

  public PeerClient(String hostname, String directory) throws RemoteException {
    dir = directory;
    rmiStr = "//" + hostname + "/peer";

    // check that the directory is valid
    File folder = new File(dir);
    if(!folder.isDirectory() || !folder.exists()) {
      System.err.println("ERROR: The folder you entered \"" + dir + "\" is not a valid directory.");
      System.exit(0);
    }

    try {
      // connect to index server
      centralServer = (RMIServerInterface)Naming.lookup(rmiServerStr);
    }
    catch (Exception ex) {
      System.err.println("EXCEPTION: Client Exception while CONNECTING to central sever: " + ex.toString());
      ex.printStackTrace();
    }

    // Get all non-directory files with file size less than MAX FILE SIZE. see retrieve().
    Vector<String> files = ReadSharedDirectory(folder);

    try{
      // Register and receive peer ID
      id = centralServer.register(rmiStr, files);
    }
    catch (Exception ex) {
      System.err.println("EXCEPTION: Client Exception while REGISTERING to central sever: " + ex.toString());
      ex.printStackTrace();
    }

    try {
      // Binding to the RMI Interface
      System.out.println("Binding Client " + id + " RMI Interface to " + rmiStr + id);
      Naming.rebind(rmiStr + id, this);
    }
    catch (Exception ex) {
      System.err.println("EXCEPTION: PeerClient Exception while binding RMI Interface: " + ex.toString());
      ex.printStackTrace();
    }

    // Run this on its own thread
    try {
      // register directory and process its events
      Path path = Paths.get(dir);
      //new WatchDir(path, false, id, "").processEvents();
    }
    catch(Exception e) {
      e.printStackTrace();
    }
  }

  public Vector<String> ReadSharedDirectory(File folder){
    // Read all the files in the directory and return
    Vector<String> filenames = new Vector<String>();

    for(File f : folder.listFiles()) {
      if(!f.isDirectory() && f.length() <= 1024*1024) {
        filenames.add(f.getName());
      }
    }

    return filenames;
  }

  public void retrieveFile(String filename, int peerId) {
    try{
      System.out.println("Retrieving file \"" + filename + "\" from peer " + peerId);

      // Connect to peer
      RMIClientInterface peer = (RMIClientInterface) Naming.lookup(rmiStr + peerId);

      // Get file
      FileInfo fileinfo = peer.retrieve(filename);

      try {
        // Save file
      	File f = new File(dir, fileinfo.filename);
      	f.createNewFile();
      	FileOutputStream out = new FileOutputStream(f,true);
      	out.write(fileinfo.data,0,fileinfo.len);
      	out.flush();
      	out.close();
      }
      catch(Exception e){
      	e.printStackTrace();
      }
    }
    catch (Exception ex) {
      System.err.println("EXCEPTION: Client Exception while CONNECTING to peer client: " + ex.toString());
      ex.printStackTrace();
    }
  }

  public int getId() {
    return id;
  }

  public ArrayList<Integer> searchIndex(String q) {
    ArrayList<Integer> ret = new ArrayList<Integer>();
    try {
      ret = centralServer.search(q);
    }
    catch (Exception ex) {
      System.err.println("EXCEPTION: Client Exception while SEARCHING to central sever: " + ex.toString());
      ex.printStackTrace();
    }
    return ret;
  }

  @Override
  public FileInfo retrieve(String filename) throws RemoteException {
    System.out.println("Client " + id + " retrieve called: filename = " + filename);

    // All the info the client needs to know to save the file
    FileInfo ret = new FileInfo();
    ret.filename = filename;
    ret.data = new byte[1024*1024]; // MAX FILE SIZE 1024x1024
    ret.len = 0;

    try {
      // Read file
      File f1 = new File(dir, filename);
      FileInputStream in = new FileInputStream(f1);
      ret.len = in.read(ret.data);
    }
    catch(Exception e){
      e.printStackTrace();
    }

    return ret;
  }
}
