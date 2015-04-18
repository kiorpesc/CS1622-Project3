#!/bin/bash

java -cp "lib/java-cup-11a.jar:src/" MiniJavaCompiler $1 $2 $3

if [ -z "$3" ]
  then
    java -jar lib/Mars4_5.jar $2
  else
    java -jar lib/Mars4_5.jar $3
fi
