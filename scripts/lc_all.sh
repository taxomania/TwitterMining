#!/bin/bash

DIR=`pwd`
if [[ $DIR == *scripts ]]
then
  cd ..
fi

wc -l `find pysrc -name *.py; find src -name *.java; find scripts -name *.sh`
