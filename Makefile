SRC := $(shell find src -name *.java)

all:
	javac -Xlint:unchecked src/analyser/Analyser.java -sourcepath src -d bin

