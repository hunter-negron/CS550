#!/bin/bash

mkdir build
javac -d build src/com/lib/interfaces/RMIServerInterface.java src/com/lib/index_server/RegisteredPeerInfo.java src/com/lib/index_server/IndexServer.java src/com/test/IndexTest.java
javac -d build src/com/lib/interfaces/RMIClientInterface.java src/com/lib/peer_client/PeerClient.java src/com/test/PeerTest.java