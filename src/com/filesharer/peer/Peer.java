package com.filesharer.peer;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.swing.JOptionPane;

import com.filesharer.indexserver.IndexServerInterface;

public class Peer extends UnicastRemoteObject implements PeerInterface {

    private static final long serialVersionUID = 1L;
    public static int peerId;

    protected Peer() throws RemoteException {
      super();
    }

    @Override
    public boolean requestFile(PeerInterface p, String filename) throws RemoteException {
      String response = "";

      // Read specified file from disk and send to specified peer
      try {
        File f1 = new File(filename);
        FileInputStream in = new FileInputStream(f1);
        byte[] mydata = new byte[1024*1024];
        int mylen = in.read(mydata);

        while(mylen > 0) {
          response += p.sendFile(f1.getName(), mydata, mylen);
          mylen = in.read(mydata);
        }
      }
      catch(Exception e){
        e.printStackTrace();
      }

      System.out.println("requestFile " + peerId + " " + response);
      return true;
    }

    @Override
    public boolean sendFile(String filename, byte[] data, int len) throws RemoteException {
      System.err.println("sendFile " + peerId + " receiving file named " + filename);

      // Write the received data to disk with the given filename
      try {
      	File f = new File(filename);
      	f.createNewFile();
      	FileOutputStream out = new FileOutputStream(f,true);
      	out.write(data,0,len);
      	out.flush();
      	out.close();
      	System.out.println("Done writing data...");
      }
      catch(Exception e){
      	e.printStackTrace();
      }
      return true;
    }

    public static class Server extends Thread {
      // Basic server thread to wait for RMI requests
      public void run() {
        try {
          Naming.rebind("//localhost/peer" + peerId, new Peer());
          System.err.println("Server " + peerId + " ready");
        }
        catch (Exception e) {
          System.err.println("Server " + peerId + " exception: " + e.toString());
          e.printStackTrace();
        }
      }
    }

    public static void main(String args[]) {
      // Register with the index server
      try {
        IndexServerInterface index = (IndexServerInterface) Naming.lookup("//localhost/index");
        peerId = index.register(args); // only index files given as arguments for now
      }
      catch(Exception e) {
        e.printStackTrace();
      }

      // Start the server (which needs the peerId)
      Server s = new Server();
      Thread sThread = new Thread(s);
      sThread.start();

      // Primitive file retrieval: prompt until empty string is entered
      try {
        String svr, fn;
        Peer client = new Peer();

        svr = JOptionPane.showInputDialog("Which peer?");
        fn = JOptionPane.showInputDialog("Which file?");
        do {
          PeerInterface look_up = (PeerInterface) Naming.lookup("//localhost/peer" + svr);
          look_up.requestFile(client, fn);
          svr = JOptionPane.showInputDialog("Which peer?");
          fn = JOptionPane.showInputDialog("Which file?");
        } while(!svr.equals(""));
      }
      catch(Exception e) {
        e.printStackTrace();
      }
    }
}
