#include "stdio.h"
#define MAXBUF 256
int main(int argc, char** argv)
{
    int exitStatus = 0, childCount = 1, i;
    char* proc = "exit";
    char agv[2][MAXBUF];
    strcpy(agv[0], proc);
    strcat(proc, ".coff");
    strcpy(agv[1], argv[1]);
    int exitStatNormal = atoi(argv[1]);
    if(argc > 2) childCount = atoi(argv[2]);
    for(i = 0; i < childCount; i ++)
    {
        int pid = exec(proc, 2, agv);
        int exitStat;
        join(pid, &exitStat);
        if(exitStatNormal != exitStat)
        {
            printf("%d\n", exitStat);
            exitStatus = -1;
        }
    }
    exit(exitStatus);
}
