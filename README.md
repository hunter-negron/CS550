# CS550
CS 550 Fall 2021 semester project


To compile the demo. Run the following command.

    javac src/com/rmitest/rmiinterface/RMIInterface.java src/com/rmitest/rmiserver/ServerOperation.java src/com/rmitest/rmiclient/ClientOperation.java

Make sure that you turn on your RMI registry. For most Linux based OS and Mac, following command works.

    rmiregistry &

Run server and client separately. Enter the `src` file and run the following commands.

    java com.rmitest.rmiserver.ServerOperation # for server
    java com.rmitest.rmiclient.ClientOperation # for client
