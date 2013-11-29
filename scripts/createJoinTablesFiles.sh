#!/bin/bash

BASEDIR=$PWD
SCRIPTS_FOLDER=$BASEDIR/scripts
XQUERY_SCRIPTS_FOLDER=$SCRIPTS_FOLDER/XQueryTextMinerScripts


if [ -z "$1" ]; then
    echo "Need directory to read the XML files from!"
    exit 1;
fi
if [ -z "$2" ]; then
    echo "Need directory to write the output CSV and XML files to!"
    exit 2;
fi
if [ -z "$3" ]; then
    echo "Please specify the environment you are running this script from ('dev' or 'prod')!"
    exit 1;
fi
if [ -z "$4" ]; then
    echo "Please specify the action you wish to carry out ('main' to create main csv and xml files, or 'join' to create the files that gather names and declaration ids)!"
    exit 1;
fi

XML_OUTPUT=$1
OUTPUT=$2
ENVIRONMENT=$3
ACTION=$4

java -jar ./scripts/declarationXmlParsing.jar $XQUERY_SCRIPTS_FOLDER $XML_OUTPUT $OUTPUT $ENVIRONMENT $SCRIPTS_FOLDER/config.properties $ACTION

