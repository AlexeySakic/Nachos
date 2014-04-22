#include "syscall.h"
#include "stdio.h"

void testprint(char* m, int s){
  printf("%s: %s\n", m, (s ? "OK" : "Fail"));
}

int main(){
  int fd, state, i;
  char buf[16];

  *buf = 0;

  state = close(17);
  testprint("close unopened fd, should fail", state == -1);

  fd = creat("testfile");
  printf("file descriptor: %d\n", fd);
  testprint( "creat file `testfile`", fd > -1 );

  state = close( fd );
  testprint( "close file `testfile`", state == 0 );

  state = unlink( "testfile" );
  testprint( "unlink file `testfile`", state == 0 );

  fd = creat( "writefile" );
  printf("file descriptor: %d\n", fd);
  testprint( "creat file `writefile`", fd > -1 );

  state = write( fd, "foo", 3 );
  testprint( "write line `foo` to `writefile`", state == 3 );

  state = close( fd );
  testprint( "close file `writefile`", state == 0 );

  fd = open( "writefile" );
  printf("file descriptor: %d\n", fd);
  testprint( "open file `writefile`", fd > -1 );

  state = read( fd, buf, 16 );
  printf("read content: %c%c%c\n", buf[0], buf[1], buf[2]);
  testprint( "read from file `writefile`", state > 0 );

  state = close( fd );
  testprint( "close file `writefile`", state == 0 );

  state = 0;
  for (i = 0; i < 14; ++i){
    fd = open("writefile");
    if (fd < 0)
      state = -1;
  }
  testprint("opened 14 fds (+2 for io)", state == 0 );

  return 0;

}
