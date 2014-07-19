package jsanalyser;

import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;

import jsanalyser.analyser.StringAnalyser;
import jsanalyser.analyser.RegexAnalyser;
import jsanalyser.analyser.TokenAnalyser;
import jsanalyser.analyser.NumericAnalyser;

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
	protected final int END_BLOCK_COMMENT = 8;
	protected final int IN_NUMERIC = 9;
	protected final int IN_TOKEN = 10;
	protected final int MAYBE_IN_REGEX = 11;
	protected final int IN_REGEX = 12;
	protected final int IN_REGEX_END = 13;
	protected final int END_PARENTHESIS = 14;

	protected StringAnalyser strings;
	protected String currentString;
	protected char currentStringDelimiter;

	protected NumericAnalyser numerics;
	protected String currentNumeric;

	protected RegexAnalyser regexes;
	protected String currentRegex;

	protected TokenAnalyser tokens;
	protected String currentToken;

	protected int currentCharIndex = -1;
	protected int state = 0;

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
		return this.compareState(this.IN_BLOCK_COMMENT)
			|| this.compareState(this.IN_INLINE_COMMENT)
			|| this.compareState(this.END_BLOCK_COMMENT);
	}

	protected boolean inRegex()
	{
		return this.compareState(this.IN_REGEX)
			|| this.compareState(this.IN_REGEX_END);
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
		this.parseRegex(c);
		this.parseString(c);

		if (this.inComment() || this.inRegex() || this.inString()) {
			return;
		}

		this.parseNumeric(c);

		if (this.compareState(this.IN_NUMERIC)) {
			return;
		}

		this.parseToken(c);

		if (c == '{') {
			this.currentScopeLevel++;
		}
		else if (c == '}') {
			this.currentScopeLevel--;
		}
		else if (c == ')') {
			this.enableState(this.END_PARENTHESIS);
		}
		else if (this.compareState(this.END_PARENTHESIS)) {
			this.disableState(this.END_PARENTHESIS);
		}
	}

	protected void parseRegex(final char c)
	{
		if (this.inString() || this.inComment()) {
			return;
		}

		String sC = String.valueOf(c);
		if (c == '\\' && !this.compareState(this.ESCAPED_CHAR)) {
			this.enableState(this.ESCAPED_CHAR);
		}
		else {
			// Entering regex
			if (
				!this.inRegex()
				&& !this.compareState(this.IN_NUMERIC)
				&& !this.compareState(this.IN_TOKEN)
				&& !this.compareState(this.END_PARENTHESIS)
				&& !this.inComment()
				&& c == '/'
			) {
				this.enableState(this.MAYBE_IN_REGEX);
			}
			else if (this.compareState(this.MAYBE_IN_REGEX)) {
				this.disableState(this.MAYBE_IN_REGEX);
				if (c != '*' && c != '/') {
					this.currentRegex = "/";
					this.enableState(this.IN_REGEX);
					this.disableState(this.MAYBE_START_COMMENT);
				}
			}
			else if (this.compareState(this.IN_REGEX)) {
				if (c == '/' && !this.compareState(this.ESCAPED_CHAR)) {
					this.disableState(this.IN_REGEX);
					this.enableState(this.IN_REGEX_END);
				}
			}
			else if (
				this.compareState(this.IN_REGEX_END)
				&& !Pattern.matches("[a-zA-Z]", sC)
			) {
				this.regexes.incElementOccurences(this.currentRegex);
				this.disableState(this.IN_REGEX_END);
			}

			if (this.compareState(this.ESCAPED_CHAR)) {
				this.disableState(this.ESCAPED_CHAR);
			}
		}

		if (this.inRegex()) {
			this.currentRegex = this.currentRegex.concat(sC);
		}
	}

	protected void parseComments(final char c)
	{
		if (this.inRegex() || this.inString()) {
			return;
		}

		if (c == '/') {
			// First /, means maybe the beginning of a comment
			if (!this.compareState(this.IN_INLINE_COMMENT)
				&& !this.compareState(this.IN_BLOCK_COMMENT)
				&& !this.compareState(this.MAYBE_START_COMMENT)
			) {
				this.enableState(this.MAYBE_START_COMMENT);
			}
			// second successive slash => inline comment
			else if (this.compareState(this.MAYBE_START_COMMENT)) {
				this.disableState(this.MAYBE_START_COMMENT);
				this.disableState(this.MAYBE_IN_REGEX);
				this.enableState(this.IN_INLINE_COMMENT);
			}
			// previous one was a *, so it's the end of a block comment
			else if (this.compareState(this.MAYBE_END_BLOCK_COMMENT)) {
				this.disableState(this.MAYBE_END_BLOCK_COMMENT);
				this.disableState(this.IN_BLOCK_COMMENT);
				this.enableState(this.END_BLOCK_COMMENT);
			}
		}
		else if (this.compareState(this.MAYBE_END_BLOCK_COMMENT)) {
			this.disableState(this.MAYBE_END_BLOCK_COMMENT);
		}
		else if (this.compareState(this.END_BLOCK_COMMENT)) {
			this.disableState(this.END_BLOCK_COMMENT);
		}

		if (c == '*') {
			if (this.compareState(this.MAYBE_START_COMMENT)) {
				this.disableState(this.MAYBE_START_COMMENT);
				this.disableState(this.MAYBE_IN_REGEX);
				this.enableState(this.IN_BLOCK_COMMENT);
			}
			else if (this.compareState(this.IN_BLOCK_COMMENT)) {
				this.enableState(this.MAYBE_END_BLOCK_COMMENT);
			}
		}
		else {
			if (c != '/' && this.compareState(this.MAYBE_START_COMMENT)) {
				this.disableState(this.MAYBE_START_COMMENT);
			}
			if ((c == '\n' || c == '\r') && this.compareState(this.IN_INLINE_COMMENT)) {
				this.disableState(this.IN_INLINE_COMMENT);
			}
		}
	}

	protected void parseNumeric(final char c)
	{
		String sC = String.valueOf(c);
		if (!this.compareState(this.IN_NUMERIC) && Pattern.matches("[0-9]", sC)) {
			this.enableState(this.IN_NUMERIC);
			this.currentNumeric = sC;
		}
		else if (this.compareState(this.IN_NUMERIC)) {
			if (Pattern.matches("[.0-9]", sC)) {
				this.currentNumeric = this.currentNumeric.concat(sC);
			}
			else {
				this.numerics.incElementOccurences(this.currentNumeric);
				this.disableState(this.IN_NUMERIC);
			}
		}
	}

	protected void parseString(final char c)
	{
		if (this.inRegex() || this.inComment()) {
			return;
		}

		if ((c == '"' || c == '\'')) {
			if (!this.compareState(this.IN_STRING)) {
				this.currentStringDelimiter = c;
				this.enableState(this.STRING_START);
				this.currentString = "";
			}
			else if (
				this.compareState(this.IN_STRING)
				&& !this.compareState(this.ESCAPED_CHAR)
				&& c == this.currentStringDelimiter
			) {
				this.strings.incElementOccurences(this.currentString);
				this.currentStringDelimiter = '\0';
				this.enableState(this.STRING_END);
				this.disableState(this.IN_STRING);
			}
		}

		if (this.compareState(this.IN_STRING)) {
			this.currentString = this.currentString.concat(String.valueOf(c));

			if (c == '\\' && !this.compareState(this.ESCAPED_CHAR)) {
				this.enableState(this.ESCAPED_CHAR);
			}
			else if (this.compareState(this.ESCAPED_CHAR)) {
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

	protected void parseToken(final char c)
	{
		if (Pattern.matches("[$_a-zA-Z0-9]", String.valueOf(c))) {
			if (!this.compareState(this.IN_TOKEN)) {
			this.currentToken = "";
				this.enableState(this.IN_TOKEN);
			}
			this.currentToken = this.currentToken.concat(String.valueOf(c));
		}
		else if (this.compareState(this.IN_TOKEN)) {
			this.tokens.incElementOccurences(this.currentToken);
			this.disableState(this.IN_TOKEN);
		}
	}

	protected void reset()
	{
		this.currentCharIndex = this.state = 0;
		this.regexes = new RegexAnalyser();
		this.strings = new StringAnalyser();
		this.numerics = new NumericAnalyser();
		this.tokens = new TokenAnalyser();
	}

	public void printReport()
	{
		System.out.println("Report");
		this.regexes.run(true);
		this.strings.run(true);
		this.numerics.run(true);
		this.tokens.run(true);
	}
}
