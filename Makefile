SRC := $(shell find src -name *.java)

all:
	javac -Xlint:unchecked src/jsanalyser/AnalyserApp.java -sourcepath src -d bin

