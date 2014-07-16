package analyser;

import java.util.Map;
import java.util.HashMap;

class Parser
{
	protected final int IN_STRING = 0;
	protected final int ESCAPED_CHAR = 1;
	protected final int STRING_START = 2;
	protected final int STRING_END = 3;
	protected final int MAYBE_START_COMMENT = 4;
	protected final int MAYBE_END_BLOCK_COMMENT = 5;
	protected final int IN_INLINE_COMMENT = 6;
	protected final int IN_BLOCK_COMMENT = 7;

	protected char currentStringDelimiter;
	protected String currentString;
	protected int currentStringStartIndex;

	protected Map<String, Integer> strings;

	protected boolean parsing = false;
	protected int currentCharIndex = -1;
	protected int state = -1;
	protected int currentScopeLevel = 0;

	public void startParsing()
	{
		this.parsing = true;
		this.reset();
	}

	public void parseCodeChunk(String chunk)
	{
		int chunkSize = chunk.length(),
			localCurChar;
		for (localCurChar = 0; localCurChar < chunkSize; localCurChar++, this.currentCharIndex++) {
			this.parseChar(chunk.charAt(localCurChar));
		}
	}

	protected boolean inComment()
	{
		return this.compareState(this.IN_BLOCK_COMMENT) || this.compareState(this.IN_INLINE_COMMENT);
	}

	protected boolean inString()
	{
		return this.compareState(this.STRING_START)
			|| this.compareState(this.IN_STRING)
			|| this.compareState(this.STRING_END);
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
		this.parseComments(c);

		if (this.inComment()) {
			return;
		}

		this.parseString(c);

		if (this.inString()) {
			return;
		}

		if (c == '{') {
			this.currentScopeLevel++;
		}
		else if (c == '}') {
			this.currentScopeLevel--;
		}
	}

	protected void parseComments(final char c)
	{
		if (c == '/') {
			if (!this.compareState(this.IN_INLINE_COMMENT)
				&& !this.compareState(this.IN_BLOCK_COMMENT)
				&& !this.compareState(this.MAYBE_START_COMMENT)
			) {
				this.enableState(this.MAYBE_START_COMMENT);
			}
			else if (this.compareState(this.MAYBE_START_COMMENT)) {
				this.disableState(this.MAYBE_START_COMMENT);
				this.enableState(this.IN_INLINE_COMMENT);
			}
			else if (this.compareState(this.MAYBE_END_BLOCK_COMMENT)) {
				this.disableState(this.MAYBE_END_BLOCK_COMMENT);
				this.disableState(this.IN_BLOCK_COMMENT);
			}
		}
		else if (this.compareState(this.MAYBE_END_BLOCK_COMMENT)) {
			this.disableState(this.MAYBE_END_BLOCK_COMMENT);
		}

		if (c == '*') {
			if (this.compareState(this.MAYBE_START_COMMENT)) {
				this.disableState(this.MAYBE_START_COMMENT);
				this.enableState(this.IN_BLOCK_COMMENT);
			}
			else if (this.compareState(this.IN_BLOCK_COMMENT)) {
				this.enableState(this.MAYBE_END_BLOCK_COMMENT);
			}
		}
		else if (c == '\n' && this.compareState(this.IN_INLINE_COMMENT)) {
			this.disableState(this.IN_INLINE_COMMENT);
		}
	}

	protected void parseString(final char c)
	{
		if (c == '\\' && !this.compareState(this.ESCAPED_CHAR)) {
			this.enableState(this.ESCAPED_CHAR);
		}
		else {
			if ((c == '"' || c == '\'') && !this.compareState(this.IN_STRING)) {
				this.currentStringDelimiter = c;
				this.enableState(this.STRING_START);
				this.currentStringStartIndex = this.currentCharIndex;
				this.currentString = "";
			}
			else if (
				(c == '"' || c == '\'') &&
				this.compareState(this.IN_STRING) && !this.compareState(this.ESCAPED_CHAR)
				&& c == this.currentStringDelimiter
			) {
				Integer stringOccurences = this.strings.get(this.currentString);
				stringOccurences = stringOccurences != null ? stringOccurences: 0;
				this.strings.put(this.currentString, stringOccurences + 1);
				this.currentStringDelimiter = '\0';
				this.enableState(this.STRING_END);
				this.disableState(this.IN_STRING);
			}

			if (this.compareState(this.IN_STRING)) {
				this.currentString = this.currentString.concat(String.valueOf(c));
				if (this.compareState(this.ESCAPED_CHAR)) {
					this.disableState(this.ESCAPED_CHAR);
				}
			}
			else if (this.compareState(this.STRING_START)) {
				this.disableState(this.STRING_START);
				this.enableState(this.IN_STRING);
			}
			else if (this.compareState(this.STRING_END)) {
				this.disableState(this.STRING_END);
			}
		}
	}

	public void endParsing()
	{
		this.parsing = false;
		this.reset();
	}

	protected void reset()
	{
		this.currentCharIndex = this.state = 0;
		this.strings = new HashMap<String, Integer>();
	}

	public void printReport()
	{
		System.out.println("Report");
		StringAnalyser stringAnalyser = new StringAnalyser(this.strings);
		stringAnalyser.run(true);
	}
}
