#!/bin/bash

DIR=`pwd`
if [[ $DIR == *scripts ]]
then
  cd ..
fi

echo "`(find src -name *.java; find pysrc -name *.py; find scripts -name *.sh) | wc -l` files"
