#!/bin/bash

# cd src
# java -cp ./build com.app.server.CentralServer
# # java -cp ./build com.app.client.Client

# java -cp ./build com.test.test_cases.PeerTest1



OPTION=$1
SHARED_DIRECTORY=$2
CONFIG_FILE=$3
CONFIG_ID=$4

function exit_err {
  exit 1;
}

function client_usage {
  echo "Usage:"
  echo "    $0 c [SHARED_DIRECTORY_PATH]"
}

function usage {
  echo "Usage:"
  echo "    $0 [OPTION s|c]"
  echo "    s - run server."
  echo "    c - run client."
  echo "    $0 [OPTION] [SHARED_DIRECTORY] [CONFIG_FILE] [CONFIG_ID]"
  echo "    CONFIG_FILE must be in the same directory as $0"
}

if [ -z "$OPTION" ] || [ -z "$SHARED_DIRECTORY" ] || [ -z "$CONFIG_FILE" ] || [ -z "$CONFIG_ID" ]; then
  echo "Invalid: Parameters missing.";
  usage;
  exit_err;
fi

SHARED_DIRECTORY=$(cd $(dirname $SHARED_DIRECTORY); pwd)/$(basename $SHARED_DIRECTORY) # Convert relative path to abolute path
DIR=$PWD
cd build

if [ "$OPTION" = "s" ]; then
  java -cp $DIR/src/com/lib/thirdparty/json-20210307.jar: com.app.server.CentralServer $SHARED_DIRECTORY $DIR/$CONFIG_FILE $CONFIG_ID
fi

if [ "$OPTION" = "c" ]; then
  java -cp $DIR/src/com/lib/thirdparty/json-20210307.jar: com.app.client.Client $SHARED_DIRECTORY $DIR/$CONFIG_FILE $CONFIG_ID
fi
