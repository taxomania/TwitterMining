#!/bin/bash
wc -l `find . -wholename './pysrc/guess_language/*' -prune -o -name *.py -print; find . -name *.java`