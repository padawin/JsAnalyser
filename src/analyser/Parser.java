package analyser;

import java.util.ArrayList;

class Parser
{
	protected boolean parsing = false;
	protected int currentCharIndex = -1;
	protected int state = -1;

	public void startParsing()
	{
		this.parsing = true;
		this.currentCharIndex = this.state = 0;
	}

	public void parseCodeChunk(String chunk)
	{
		int chunkSize = chunk.length(),
			localCurChar;
		for (localCurChar = 0; localCurChar < chunkSize; localCurChar++, this.currentCharIndex++) {
			this.parseChar(chunk.charAt(localCurChar));
		}
	}

	protected boolean compareState(final int flag)
	{
		return (1 << flag) == (this.state & (1 << flag));
	}

	protected void enableState(final int flag)
	{
		this.state = this.state | (1 << flag);
	}

	protected void disableState(final int flag)
	{
		this.state = this.state & ~(1 << flag);
	}

	protected void parseChar(final char c)
	{
	}

	public void endParsing()
	{
		this.parsing = false;
		this.reset();
	}

	protected void reset()
	{
		this.currentCharIndex = -1;
		this.state = -1;
	}

	public void printReport()
	{
		System.out.println("Report");
	}
}
