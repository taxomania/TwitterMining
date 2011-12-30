#!/bin/bash

DIR=`pwd`
if [[ $DIR == *scripts ]]
then
  cd ..
fi

LINEBREAK="---------------------"
echo $LINEBREAK

let "COUNT=`find pysrc -name *.py | wc -l`"
printf 'Python:\t%5d modules\n' $COUNT

let "COUNT2=`find src -name *.java | wc -l`"
printf 'Java:\t%5d classes\n' $COUNT2

echo $LINEBREAK

let "COUNT += COUNT2"
printf "Total:\t%5d   files\n" $COUNT

echo $LINEBREAK