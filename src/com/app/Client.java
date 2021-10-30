package com.app.client;

import java.util.*;
import java.rmi.Naming;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.concurrent.Callable;

import com.lib.watch_dir.WatchDir;
import com.lib.interfaces.*;
import com.lib.peer_client.*;
import com.lib.index_server.Query;
import com.lib.index_server.QueryHit;
import com.lib.index_server.MessageID;
import com.lib.index_server.Invalidation;

import org.json.*;

public class Client {
  final static String rmiStr = "//localhost/peer/";
  final static String rmiServerStr = "//localhost/central_server";

  private static String dir;
  private static String peerIdStr; // used to identify peer at the server
  private static int myPeerId;
  private static WatchDir wd;
  private static RMIServerInterface centralServer;
  private static Vector<String> sharedFiles;
  private static PeerClient pc;

  private static String jsonConfig;
  private static int configId; // for using as an index into JSON config ONLY!
  private static int superpeerId;
  private static int timeToLive;
  private static int seq = 0;
  private static String validationMethod; // "push" or "pull"
  private static int timeToRefresh; // in minutes


  public static void PrintMessageLn(String str){
    System.out.println("PEER(" + myPeerId + "): " + str);
  }

  public static void PrintClientOptions(){
    PrintMessageLn("Enter the file name you would like to search. (type \"q\" to quit.)");
  }

  public static Vector<String> ReadSharedDirectory(String dir){
    System.out.println("Reading the shared directory: " + dir);
    File folder = new File(dir);

    if(!folder.isDirectory() || !folder.exists()) {
      System.err.println("ERROR: The folder you entered \"" + dir + "\" is not a valid directory.");
      System.exit(0);
    }

    // Read all the files in the directory and return
    Vector<String> filenames = new Vector<String>();

    for(File f : folder.listFiles()) {
      if(!f.isDirectory() && f.length() <= 1024*1024) {
        System.out.println("Sharing: " + f.getName());
        filenames.add(f.getName());
      }
    }

    return filenames;
  }

  public static void retrieveFile(final String filename, final int peerId, final RMIClientInterface peer) {
    PrintMessageLn("Retrieving file \"" + filename + "\" from 'peer " + peerId + "'. You'll be notified when it finishes.");

    try{
      Thread t_retrieve = new Thread(new Runnable() {
        @Override
        public void run() {
          try {
            System.out.println("Peer " + peerId + ": Retrieve start time =" + System.currentTimeMillis());
            // Get file
            FileInfo fileinfo = peer.retrieve(filename, peerId);
            System.out.println("Peer " + peerId + ": Retrieve end time =" + System.currentTimeMillis());

            /* --- start PA3 change --- */
            // add to file store
            System.out.println("Trying to add \"" + fileinfo.filename + "\" into the fileStore.");
            RetrievedFileInfo rfi = fileinfo.retrievedFileInfo;
            rfi.lastVerified = new Date();
            rfi.owner = false;
            pc.insertIntoFileStore(fileinfo.filename, rfi);
            /* ---- end PA3 change ---- */

            // Save file
            File f = new File(dir, fileinfo.filename);
            f.createNewFile();
            FileOutputStream out = new FileOutputStream(f,true);

            if(fileinfo.len > 0)
              out.write(fileinfo.data, 0, fileinfo.len);
            else
              out.write(fileinfo.data, 0, 0);

            out.flush();
            out.close();
            // PrintMessageLn("Done writing data...");

          }
          catch(Exception e){
            e.printStackTrace();
          }

          System.out.println("[####################] 100% : Download '" + filename + "'' complete!");
          return;
        }
      });
      t_retrieve.start();
    }
    catch (Exception ex) {
      System.err.println("EXCEPTION: Client Exception while CONNECTING to peer client: " + ex.toString());
      ex.printStackTrace();
    }
  }

  public static void main(String[] args) {
    // Check if the name to the shared directory is provided
    if(args.length != 3) {
      System.err.println("ARGS: [working dir] [json config file] [config id]");
      System.exit(0);
    }

    System.out.println("args[0] = " + args[0]);
    System.out.println("args[1] = " + args[1]);
    System.out.println("args[2] = " + args[2]);

    // get the command line args
    jsonConfig = args[0];
    configId = Integer.parseInt(args[1]);
    dir = args[2];

    // Get all non-directory files with file size less than MAX FILE SIZE. see retrieve().
    sharedFiles = ReadSharedDirectory(dir);

    // parse config
    try {
      Path filePath = Paths.get(jsonConfig);
      // String rawJson = Files.readString(filePath);
      String rawJson = new String(Files.readAllBytes(Paths.get(jsonConfig)));
      JSONObject json = new JSONObject(rawJson);
      superpeerId = json.getJSONArray("peers").getInt(configId);
      timeToLive = json.getInt("timeToLive");
      /* --- start PA3 change --- */
      validationMethod = json.getString("validationMethod");
      timeToRefresh = json.getInt("timeToRefresh");

      if(!(validationMethod.equals("push") || validationMethod.equals("pull"))) {
        // if the invalidation method is neither push nor pull
        System.err.println("Invalid validation method \"" + validationMethod + "\". Exiting.");
        System.exit(0);
      }
      System.out.println("Validation method: " + validationMethod);
      /* ---- end PA3 change ---- */
      System.out.println("Topology type: " + json.getString("topologyType"));
    } catch(Exception ex) {
      System.err.println("EXCEPTION: Client Exception while PARSING json config: " + ex.toString());
      ex.printStackTrace();
    }

    if(timeToLive <= 0) {
      System.err.println("ERROR: TTL should be a positive value.");
      System.exit(0);
    }

    try{
      centralServer = (RMIServerInterface)Naming.lookup(rmiServerStr + superpeerId); // connect to index server
    }
    catch (Exception ex) {
      System.err.println("EXCEPTION: Client Exception while CONNECTING to central sever: " + ex.toString());
      ex.printStackTrace();
    }

    try {
      peerIdStr = centralServer.register(rmiStr, sharedFiles, null);         // registering the files

      System.out.println("My peer identifier is " + peerIdStr);

      // need to get these strings dynamically
      myPeerId = Integer.parseInt(peerIdStr.split("_")[1]);
      pc = new PeerClient(rmiStr + superpeerId + "/", myPeerId, dir);

      /* --- start PA3 change --- */
      for(String fn : sharedFiles) {
        RetrievedFileInfo rfi = new RetrievedFileInfo();
        rfi.version = 1;
        rfi.originServerId = superpeerId;
        rfi.originPeerId = myPeerId;
        rfi.valid = true;
        rfi.lastVerified = new Date();
        rfi.timeToRefresh = timeToRefresh;
        rfi.owner = true;
        pc.insertIntoFileStore(fn, rfi);
      }
      /* ---- end PA3 change ---- */
    }
    catch (Exception ex) {
      System.err.println("EXCEPTION: Client Exception while REGISTERING with central server: " + ex.toString());
      ex.printStackTrace();
    }

    Path p = Paths.get(dir);
    wd = new WatchDir(p);
    // Watching the shared directory for changes on a separate thread.
    Thread t_watch = new Thread(new Runnable() {
      @Override
      public void run() {
        wd.BeginWatch(new Callable<Void>() {
          public Void call() {
            try{
              // If any change is detected, the peer needs to read the directory again
              System.out.println("Change detected in the shared directory.");
              sharedFiles = ReadSharedDirectory(dir);
              centralServer.register(rmiStr, sharedFiles, peerIdStr);
            }
            catch (Exception ex) {
              System.err.println("EXCEPTION: Client Exception while RE-REGISTERING files: " + ex.toString());
              ex.printStackTrace();
            }

            return null;
          }
        },
        /* --- start PA3 change --- */
        new WatchDir.ModifiedFileCallback() {
            @Override
            public void onFileModified(String filename) {
              // update the version in the store
              System.out.println("OnFileModified: " + filename);
              //System.out.println("OnFileModified: fileStore = " + pc.fileStore);

              if(validationMethod.equals("push")) {
              // the file is in the store so we know it was modified <== WRONG!!
              //if(pc.fileStore.containsKey(filename)) {
                RetrievedFileInfo rfi = pc.fileStore.get(filename);
                rfi.version++;

                // construct the invalidation message to send
                Invalidation inv = new Invalidation();
                inv.messageId = new MessageID();
                inv.messageId.superpeerId = superpeerId;
                inv.messageId.peerId = myPeerId;
                inv.messageId.seq = seq++;
                inv.timeToLive = timeToLive;
                inv.originServerId = superpeerId;
                inv.originPeerId = myPeerId;
                inv.filename = filename;
                inv.version = rfi.version; // get new version that will be in the file store
                try {
                  // only send the message and update the store if we're the owner
                  if(pc.fileStore.get(filename).owner == true) {
                    pc.fileStore.replace(filename, rfi);
                    centralServer.forwardInvalidation(inv);
                  }
                  else {
                    // if we are not the owner, we just self-invalidated a file
                    /*pc.fileStore.get(filename).valid = false;
                    try{
                      Vector<String> v = new Vector<String>();
                      v.add(filename);
                      centralServer.deregister(peerIdStr, v);
                    }
                    catch (Exception ex) {
                      System.err.println("EXCEPTION: Client Exception while DE-REGISTERING self-invalidated file: " + ex.toString());
                      ex.printStackTrace();
                    }*/
                    System.out.println("Pushed-based: YOU HAVE ILLEGALLY MODIFIED A FILE YOU DO NOT OWN. IT IS NOW INVALID AND DEREGISTERED.");
                  }
                }
                catch(Exception ex) {
                  System.err.println("EXCEPTION: Client Exception while calling forwardInvalidation: " + ex.toString());
                  ex.printStackTrace();
                }
              /*}
              // the file is not in the store, so it was created
              // this is just the call() code from above, but recontextualized
              else{
                try{
                  // If any change is detected, the peer needs to read the directory again
                  System.out.println("Change detected in the shared directory.");
                  sharedFiles = ReadSharedDirectory(dir);
                  centralServer.register(rmiStr, sharedFiles, peerIdStr);
                }
                catch (Exception ex) {
                  System.err.println("EXCEPTION: Client Exception while RE-REGISTERING files: " + ex.toString());
                  ex.printStackTrace();
                }
              }*/
              }
              // validationMethod == "pull"
              else {
                RetrievedFileInfo rfi = pc.fileStore.get(filename);
                if(rfi.owner) {
                  // update the version if we're the owner of the file
                  rfi.version++;
                  pc.fileStore.replace(filename, rfi);
                }
                else {
                  System.out.println("Pull-based: YOU HAVE ILLEGALLY MODIFIED A FILE YOU DO NOT OWN. IT IS NOW INVALID AND DEREGISTERED.");
                }
              }
            }
        /* ---- end PA3 change ---- */
        });
      }
    });
    t_watch.start(); // starting the thread

    /* --- start PA3 change --- */
    // if we are using the poll-based validation method, start this thread
    if(validationMethod.equals("pull")) {
      Thread t_poll = new Thread(new Runnable() {
        @Override
        public void run() {
          System.out.println("Starting the polling thread.");
          Calendar c = Calendar.getInstance();
          Calendar now = Calendar.getInstance();

          // keep checking forever
          while(true) {
            // for every file that we have in the store, if TTR has passed, then poll
            for (Map.Entry<String, RetrievedFileInfo> entry : pc.fileStore.entrySet()) {
              String filename = entry.getKey();
              RetrievedFileInfo rfi = entry.getValue();

              // calendar manipulation is the easiest way to add timestamps
              // it's clunky but it should work
              c.setTime(rfi.lastVerified);
              c.add(Calendar.SECOND, rfi.timeToRefresh);
              now.setTime(new Date());

              if(rfi.valid == true && rfi.owner == false && c.compareTo(now) < 0) { // if we are not the owner and lastVerfied + TTR < now, then poll
                System.out.print("Polling... ");
                try {
                  RMIClientInterface peer = (RMIClientInterface)Naming.lookup(rmiStr + rfi.originServerId + "/" + rfi.originPeerId);
                  if(peer != null) {
                    if(peer.poll(filename, rfi.version)) {
                      System.out.println("Valid :)");
                      // poll returns true so this version of the file is still valid
                      rfi.lastVerified = new Date();
                      rfi.valid = true;
                    }
                    else {
                      System.out.println("Not valid :(");
                      // poll returns false so we have an invalid copy. deregister it
                      rfi.lastVerified = new Date(); // should I do this here?
                      rfi.valid = false;
                      Vector<String> v = new Vector<String>();
                      v.add(filename);
                      try {
                        centralServer.deregister(peerIdStr, v);
                      }
                      catch(Exception e) {
                        System.err.println("EXCEPTION: Client Exception while CONNECTING to superpeer to DEREGISTER a POLL-INVALIDATED file: " + e.toString());
                        e.printStackTrace();
                      }
                    }
                  }
                  else {
                    PrintMessageLn("Unable to connect to peer " + rfi.originServerId + "/" + rfi.originPeerId + ".");
                  }
                }
                catch (Exception ex) {
                  System.err.println("EXCEPTION: Client Exception while CONNECTING AND POLLING to peer client: " + ex.toString());
                  ex.printStackTrace();
                }
              }
            }

            try { Thread.sleep(2000); } catch(Exception e){} // sleep for 2 seconds
          }
        }
      });
      t_poll.start(); // starting the thread
    }
    /* ---- end PA3 change ---- */

    Runtime.getRuntime().addShutdownHook(new Thread(){
      @Override
      public void run(){
        System.out.println("Shutting down the client.");
        try{
          centralServer.deregister(peerIdStr, sharedFiles);
        }
        catch (Exception ex) {
          System.err.println("EXCEPTION: Client Exception while DE-REGISTERING files during shutdown: " + ex.toString());
          ex.printStackTrace();
        }
      }
    });

    // User interface
    Scanner sc = new Scanner(System.in);
    String strInput;

    PrintMessageLn("Hello Client.");
    while(true){
      // Initial prompt
      PrintClientOptions();
      strInput = sc.nextLine();

      if(strInput.length() != 0){
        if(strInput.equals("f")){
          System.out.println("\n{");
          for (Map.Entry<String, RetrievedFileInfo> entry : pc.fileStore.entrySet()) {
            String filename = entry.getKey();
            RetrievedFileInfo rfi = entry.getValue();
            System.out.println("  {filename:      " + filename);
            System.out.println("  version:        " + rfi.version);
            System.out.println("  originServerId; " + rfi.originServerId);
            System.out.println("  originPeerId:   " + rfi.originPeerId);
            System.out.println("  valid:          " + rfi.valid);
            System.out.println("  lastVerified:   " + rfi.lastVerified);
            System.out.println("  timeToRefresh:  " + rfi.timeToRefresh);
            System.out.println("  owner:          " + rfi.owner + "},");
          }
          System.out.println("}\n");
          continue;
        }

        if(strInput.equals("q")){
          PrintMessageLn("Quitting.");
          System.exit(0); // This will trigger the shutdown hook so files will be regregistered.
        }

        try{
          // Get list of peer IDs who have desired file
          //ArrayList<Integer> clientList = centralServer.search(strInput);
          Query query = new Query();
          query.messageId = new MessageID();
          query.messageId.superpeerId = superpeerId;
          query.messageId.peerId = myPeerId;
          query.messageId.seq = seq++;
          query.timeToLive = timeToLive;
          query.filename = strInput;
          QueryHit queryHit = centralServer.forwardQuery(query);

          if(queryHit.peerId == null || queryHit.peerId.size() == 0){
            System.err.println("ERROR: OOPS! None of the clients have file " + strInput);
            continue;
          }

          if(queryHit.peerId.size() != queryHit.superpeerId.size()){
            System.err.println("ERROR: OOPS! Invalid number of peer response received for " + strInput);
            continue;
          }

          // PrintMessageLn("Client list start");
          String cliList = "";
          for(int i = 0; i < queryHit.peerId.size(); i++){
            // System.out.print(clientList.get(i));
            cliList += "Peer " + queryHit.peerId.get(i) + " at superpeer " + queryHit.superpeerId.get(i);
            if(i < queryHit.peerId.size() - 1)
              cliList += "\n";
          }
          PrintMessageLn("Following clients have '"+ strInput +"' :\n" + cliList);

          // prompt user to select a peer
          PrintMessageLn("Enter a client number (Download will proceed in background) \"b\" to go back:");
          int selectedClient;
          try {
            selectedClient = Integer.parseInt(sc.nextLine());
          } catch(Exception e) {
            continue;
          }

          PrintMessageLn("Enter the superpeer number:");
          int selectedClientSuperpeerId;
          try {
            selectedClientSuperpeerId = Integer.parseInt(sc.nextLine());
          } catch(Exception e) {
            continue;
          }


          if(selectedClient == myPeerId && selectedClientSuperpeerId == superpeerId){ // Check if the selected this client
            System.err.println("ERROR: File already exists.");
            continue;
          }
          else if(!queryHit.peerId.contains(selectedClient) || !queryHit.superpeerId.contains(selectedClientSuperpeerId)) {
            System.err.println("ERROR: Invalid client selection.");
            continue;
          }

          try{
            PrintMessageLn("Connecting to peer " + selectedClient);
            RMIClientInterface peer = (RMIClientInterface)Naming.lookup(rmiStr + selectedClientSuperpeerId + "/" + selectedClient);
            if(peer != null)
              retrieveFile(strInput, selectedClient, peer);
            else
              PrintMessageLn("Unable to connect to peer " + selectedClient + ".");
          }
          catch(Exception e){
            // e.printStackTrace();
          }
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
