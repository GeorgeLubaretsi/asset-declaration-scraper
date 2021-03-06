#!/bin/bash

BASEDIR=$PWD
SCRIPTS_FOLDER=$BASEDIR/scripts
PATH_TO_SCRAPPER=$BASEDIR/declarationScrapy
PDF_OUTPUT=$PATH_TO_SCRAPPER/output
XML_OUTPUT=$BASEDIR/xmloutput
OUTPUT=$BASEDIR/output

if ([ ! -d "$BASEDIR/archive" ]); then
    mkdir $BASEDIR/archive
    mkdir $BASEDIR/archive/declarationids
    mkdir $BASEDIR/archive/pdf
    mkdir $BASEDIR/archive/pdf/en
    mkdir $BASEDIR/archive/pdf/ka
    mkdir $BASEDIR/archive/xml
    mkdir $BASEDIR/archive/xml/en
    mkdir $BASEDIR/archive/xml/ka
    mkdir $BASEDIR/archive/output
    mkdir $BASEDIR/archive/output/xml
    mkdir $BASEDIR/archive/output/xml/en
    mkdir $BASEDIR/archive/output/xml/ka
    mkdir $BASEDIR/archive/output/csv
    mkdir $BASEDIR/archive/output/csv/en
    mkdir $BASEDIR/archive/output/csv/ka
fi

# Archiving the new declaration ids file, and also making it the new current declaration id file, to be used for the next scraping.
now=$(date +'%Y-%m-%d')
cp $BASEDIR/"declarationids-"$now $BASEDIR/archive/declarationids/
mv $BASEDIR/"declarationids-"$now $BASEDIR/"currentdeclarationids"

# Archiving downloaded PDF files
echo "Archiving PDF files..."
mv $PDF_OUTPUT/en/* $BASEDIR/archive/pdf/en/
mv $PDF_OUTPUT/ka/* $BASEDIR/archive/pdf/ka/

# Archiving XMl files, created from the PDF files
echo "Archiving XML files created from PDF files..."
cp $XML_OUTPUT/en/* $BASEDIR/archive/xml/en/
cp $XML_OUTPUT/ka/* $BASEDIR/archive/xml/ka/

# Archiving/replacing CSV and XML output files
#echo "Archiving output files..."
#cp -f $OUTPUT/csv/en/* $BASEDIR/archive/output/csv/en/
#cp -f $OUTPUT/csv/ka/* $BASEDIR/archive/output/csv/ka/
#cp -f $OUTPUT/xml/en/* $BASEDIR/archive/output/xml/en/
#cp -f $OUTPUT/xml/ka/* $BASEDIR/archive/output/xml/ka/
#cp -f $OUTPUT/csv/JoinResults.csv $BASEDIR/archive/output/csv/
#cp -f $OUTPUT/xml/JoinResults.xml $BASEDIR/archive/output/xml/

echo "Done. The files have been archived in "$BASEDIR"/archive"

echo "Archiving done."

