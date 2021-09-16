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

import com.filesharer.ipeer.IPeer;

public class Peer extends UnicastRemoteObject implements IPeer {

    private static final long serialVersionUID = 1L;
    public static int peerId;

    protected Peer()
      throws RemoteException {
      super();
    }

    @Override
    public boolean requestFile(IPeer p)
      throws RemoteException {
      String response = "";

      try {
        File f1 = new File("sample.txt");
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
    public boolean sendFile(String filename, byte[] data, int len)
      throws RemoteException {
      System.err.println("sendFile " + peerId + " receiving file named " + filename);

      try {
      	File f = new File("___" + filename);
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
      peerId = Integer.parseInt(args[0]);
      Server s = new Server();
      Thread sThread = new Thread(s);
      sThread.start();

      try {
        Peer client = new Peer();
        String svr = JOptionPane.showInputDialog("Which peer?");
        IPeer look_up = (IPeer) Naming.lookup("//localhost/peer" + svr);
        look_up.requestFile(client);
      }
      catch(Exception e) {
        e.printStackTrace();
      }
    }
}
