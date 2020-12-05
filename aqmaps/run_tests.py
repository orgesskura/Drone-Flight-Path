import sys, os
import shlex
import traceback
from subprocess import Popen, PIPE
from math import sqrt

print(sys.argv)
os.chdir("ilp-results")

args = "{day} {month} {year} 55.944425 -3.188396 5678 8888"

year = 2020
path = os.path.join("..", sys.argv[1])

with open("log.txt", "wb") as log:
      for i in range(1,13):
       for j in range(1,32):
         day = j
         month= i
         print(f"Testing {day} {month} {year}")
         log.write(f"Log for {day} {month} {year}:\n\n".encode())
         try:
            proc = Popen(shlex.split(f"java -jar {path} " \
                                     + args.format(year=2020,
                                                   month=str(month).zfill(2),
                                                   day=str(day).zfill(2))),
                         stdout=PIPE)
            stdout, stderr = proc.communicate(None)
            log.write(stdout)
            if stderr:
                log.write(b"Error:\n")
                log.write(stderr)
            log.write(f"\nLast Line of test for {day} {month} {year}\n\n".encode())
         except KeyboardInterrupt:
            print(data)
            sys.exit()
         except:
            print(f"Error on {day} {month} {year}:")
            traceback.print_exc()
