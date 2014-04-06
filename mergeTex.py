import sys
import re
import string
#merge argv[1] -> argv[2] with replacement

def trim(line):
    return (re.sub(r'^\s*', '', line))
def getNum(line):
    compreg = re.compile(r'^\s*\\section{Task\s*' + r'\d' + r'}\s*')
    compmatch = compreg.match(line)
    if compmatch:
        newreg = re.compile(r'\d')
        match = newreg.search(line)
        return match.group()
    return -1
def trimEnding(line):
    return (re.sub(r'\\end{spacing}(.|\n)*', '', line))
##??why [.\n] is wrong
##return (re.sub(r'\\end{spacing}[.]*\n.*', '', line))
def writeEnding():
    print '\end{spacing}'
    print '\end{document}'
#update when f1 is printed
def update():
    global i, f1_pos, f1_strings, f2_strings
    global num1, num2
    f1_pos = f1_pos + 2
    if (f1_pos >= len(f1_strings)):
        tmp = i
        if (num1 == num2):
            tmp = i + 1;
        for j in range(tmp * 2 + 1, len(f2_strings)):
            sys.stdout.write(f2_strings[j])
        writeEnding()
        exit()
if (len(sys.argv) != 2):
    print """Usage:
mergeTex.py [file1] [file2]

This process will replace all the same parts (Tasks)
of file2 by its couterparts in file1.
Any Tasks in file1 only will also be added."""
    exit()

f1 = file(sys.argv[1], 'r')
f2 = file(sys.argv[2], 'r')

#read and split file1
all_text = f1.read()
all_text = trimEnding(all_text)
slp = re.compile(r'(\\section{Task\s*' + r'\d' + r'})')
f1_strings = slp.split(all_text)
f1.close()

#read and split file2
all_text = f2.read()
all_text = trimEnding(all_text)
f2_strings = slp.split(all_text)
f2.close()

#initialize
f1_pos = 1
sys.stdout.write(f2_strings[0])

#replace each file2's segment with file1's counterpart
for i in range(0, len(f2_strings) / 2):
    num1 = getNum(f1_strings[f1_pos])
    num2 = getNum(f2_strings[2 * i + 1])
##    print num1 + " " + num2
    while (num1 < num2):
        sys.stdout.write(f1_strings[f1_pos] +
                         f1_strings[f1_pos + 1])
        update()
#note that the return value can be -1, so there may exist a busy loop
        num1 = getNum(f1_strings[f1_pos])
    if (num1 == num2):
##        print '=='
        sys.stdout.write(f1_strings[f1_pos] +
                         f1_strings[f1_pos + 1])
        update()
    else:
##        print '!='
        sys.stdout.write(f2_strings[i * 2 + 1] +
                         f2_strings[i * 2 + 2])

#complete the file if f1 has added blocks
if (f1_pos < len(f1_strings)):
    for j in range(f1_pos, len(f1_strings)):
        sys.stdout.write(f1_strings[j])
    writeEnding()

