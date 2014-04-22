#include "stdio.h"
int main(int argc, char** argv)
{
    if(argc == 1) exit(0);
    else exit(atoi(argv[1]));
}
