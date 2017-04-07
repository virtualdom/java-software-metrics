# Java Software Metrics Project

## Description
Implement a static software metric tool based on ASM. The detailed list of metrics can be found [here](http://www.virtualmachinery.com/jhawkmetricslist.htm). The tool should include all the method-level metrics. Then, apply your tool to 10 real-world Java project (>1000 lines of code) from GitHub to measure them.

## Due dates
**Mid-term report - April 11.**
Submission materials: a 3 - 4 page report (extended from your proposal) specifying your progress and further plan

**In-class presentation - April 18.**
Ten minutes long.

**Final report - April 30.**
Submission materials: a 6-10 page full report (extended from your mid-term report) & code & data

## How to compile
```
javac -cp .:./asm-all-5.0.3.jar -g:vars,lines -parameters *.java
java -cp .:./asm-all-5.0.3.jar ParseClassFile TestMe.class
```
