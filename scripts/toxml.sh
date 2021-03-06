#!/bin/bash

#Asset declaration scraper -- scrapes and processes http://declaration.ge
#Copyright 2012 Transparency International Georgia http://transparency.ge
#
#This program is free software: you can redistribute it and/or modify
#it under the terms of the GNU General Public License as published by
#the Free Software Foundation, either version 3 of the License, or
#(at your option) any later version.
#
#This program is distributed in the hope that it will be useful,
#but WITHOUT ANY WARRANTY; without even the implied warranty of
#MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#GNU General Public License for more details.
#
#You should have received a copy of the GNU General Public License
#along with this program.  If not, see <http://www.gnu.org/licenses/>.

# Convert reports downloaded from declarations.ge from PDF to HTML.
#
# It takes two parameters:
# $1 - directory to read PDF files from
# $2 - directory to write HTML files to
#############################################################################

BASEDIR=$PWD
#PDFTOHTML=/usr/local/bin/pdftohtml
XML_OUTPUT=$BASEDIR/xmloutput
#PDFTOHTML=poppler-0.16.7/utils/pdftohtml
#PDFTOHTML=/wherever/poppler-0.16.7/utils/pdftohtml
#############################################################################

if [ -z "$1" ]; then
    echo "Need directory to read PDF files from!"
    exit 1;
fi
if [ -z "$2" ]; then
    echo "Need directory to write XML files to!"
    exit 2;
fi

PDF=$1
XML_OUTPUT=$2

if [ ! -d "$XML_OUTPUT" ]; then
    mkdir $XML_OUTPUT
    mkdir "$XML_OUTPUT"/en
    mkdir "$XML_OUTPUT"/ka
fi

if [ ! -d "$XML_OUTPUT"/en ]; then
    mkdir "$XML_OUTPUT"/en
fi

if [ ! -d "$XML_OUTPUT"/ka ]; then
    mkdir "$XML_OUTPUT"/ka
fi

echo "Converting English documents..."
for f in `ls $PDF/en/*.pdf`; do
    name=$(basename $f .pdf)
    echo "Converting $name into XML file"
    pdftohtml -xml -enc "UTF-8" -i -q -c -hidden -noframes $f $XML_OUTPUT/test.xml
    cat $XML_OUTPUT/test.xml | grep -v '^<!DOCTYPE' > $XML_OUTPUT/en/$name.xml

    if xmllint --noout $XML_OUTPUT/en/$name.xml; then
        continue
    else
	rm $XML_OUTPUT/en/$name.xml
        echo "WARNING: "+$name+".xml (English version) was not formed properly! It was removed automatically, to avoid any further processing problems."
    fi

done
echo "Converting Georgian documents..."
for f in `ls $PDF/ka/*.pdf`; do
    name=$(basename $f .pdf)
    echo "Converting $name into XML file"
    pdftohtml -xml -enc "UTF-8" -i -q -c -hidden -noframes $f $XML_OUTPUT/test.xml
    cat $XML_OUTPUT/test.xml | grep -v '^<!DOCTYPE' > $XML_OUTPUT/ka/$name.xml

    if xmllint --noout $XML_OUTPUT/ka/$name.xml; then
        continue
    else
	rm $XML_OUTPUT/ka/$name.xml
        echo "WARNING: "+$name+".xml (Georgian version) was not formed properly! It was removed automatically, to avoid any further processing problems."
    fi
done
rm $XML_OUTPUT/test.xml

