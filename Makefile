SRC := $(shell find src -name *.java)

all:
	javac -Xlint:unchecked src/analyser/AnalyserApp.java -sourcepath src -d bin

