A collection of problems downloaded from http://old.sztaki.hu/~meszaros/public_ftp/lptestset/

Didn't get all of them. Specifically wanted some of the smaller files.

Compiled the c code to unpack the model files using:

gcc -std=c90 -o emps emps.c

Then do something like this:

gunzip ${modelName}.gz
./emps ${modelName} > ${modelName}.mps
rm ${modelName} 
