#include "stdio.h"
int main()
{
    char* proc = "sleepingChild";
    char* argv[1];
    strcpy(argv[0], proc);
    strcat(proc,".coff");
    exec(proc, 1,argv);
    exit(0);
}
