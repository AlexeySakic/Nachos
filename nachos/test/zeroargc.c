// This is a test of exec call with zero argc.
#include "syscall.h"

int main()
{
    char* proc = "halt.coff";
    int argc = 0;
    char** argv = 0;
    int pid = exec(proc, argc, argv);
    if(pid == -1)
    {
        printf("zeroargc test failed: exec executed normally without argc\n");
    }
    else
    {
        printf("zeroargc test passed\n");
    }
}
