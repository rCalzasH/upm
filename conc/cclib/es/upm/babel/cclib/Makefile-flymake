CLASSPATH=.:/home/angel/Asignaturas/CC/lib/cclib.jar:/home/angel/Asignaturas/CC/lib/net-datastructures-4-0.jar:/home/angel/Asignaturas/CC/lib/jcsp-1.1-rc4/jcsp-core.jar:/usr/share/java/junit4.jar
JAVAC=javac -cp $(CLASSPATH) -Xlint
JAVA=java -cp $(CLASSPATH)
sources = $(wildcard *.java)
classes = $(sources:.java=.class)

all: $(classes)

clean :
	rm -f *.class

%.class : %.java
	$(JAVAC) $<

run:
	$(JAVA) $(MAIN)

# To use with flymake
# Java syntax checking with Make
# This is the command that stock flymake.el runs on a Java source file:
# make -s -C ~/src/java/hello/ CHK_SOURCES=/tmp/../hello.java SYNTAX_CHECK_MODE=1 check-syntax
# To get this to work, add this stanza to your Makefile:

.PHONY: check-syntax

check-syntax:
	$(JAVAC) $(CHK_SOURCES)
