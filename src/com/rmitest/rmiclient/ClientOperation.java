package com.rmitest.rmiclient;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import javax.swing.JOptionPane;

import com.rmitest.rmiinterface.RMIInterface;

public class ClientOperation {

    private static RMIInterface look_up;

    public static void main(String[] args)
        throws MalformedURLException, RemoteException, NotBoundException {

        look_up = (RMIInterface) Naming.lookup("//localhost/MyServer");
        String txt = JOptionPane.showInputDialog("What is your name?");
        String response = "";
        try {
    			File f1=new File("sample.txt");
    			FileInputStream in=new FileInputStream(f1);
    			byte [] mydata=new byte[1024*1024];
    			int mylen=in.read(mydata);
    			while(mylen>0){
            response += look_up.helloTo(txt,f1.getName(), mydata, mylen);
    				 mylen=in.read(mydata);
    			}
  	    }
         catch(Exception e){
  			      e.printStackTrace();
		    }

        JOptionPane.showMessageDialog(null, response);

    }

}
