package jsanalyser;

import java.util.Map;
import java.util.HashMap;
import java.util.regex.Pattern;

import jsanalyser.analyser.StringAnalyser;
import jsanalyser.analyser.RegexAnalyser;
import jsanalyser.analyser.TokenAnalyser;
import jsanalyser.analyser.NumericAnalyser;

/**
 * Parser class.
 * <p>
 * This class is a state machine which will read a piece of code, chunk by .
 * chunk (The code to parse can be in a single chunk).
 * </p>
 * <p>
 * Depending the current parsed char and the machine's state, tokens, strings or
 * regex will be stored in some according {@link analyser.Analyser} classes.
 * </p>
 * <p>
 * <b>The parser does not checks the syntax, it assumes the code is valid.<b>
 * </p>
 * <p>
 * The machine's state is an integer in which each bit is a specific state (in
 * string, in regex, in comment...).
 * </p>
 * <p>
 * Each possible state is stored in an attribute of the class. Those attributes'
 * values are the rank of the bit they change in the global state.
 * </p>
 */
public class Parser
{
	/* STATES ATTRIBUTES */

	/**
	 * State attribute. Associated with the bit to know if the parser's current
	 * char is in a string.
	 */
	protected final int IN_STRING = 0;

	/**
	 * State attribute. Associated with the bit to know if the parser is parsing
	 * an escaped character (which means the previous one was a '\').
	 */
	protected final int ESCAPED_CHAR = 1;

	/**
	 * State attribute. Associated with the bit to know if the parser enters a
	 * string. Which means the current parsed char is a simple or double quote.
	 */
	protected final int STRING_START = 2;

	/**
	 * State attribute. Associated with the bit to know if the parser exits a
	 * string. Which means the current parsed char is a simple or double quote.
	 */
	protected final int STRING_END = 3;

	/**
	 * State attribute. Associated with the bit to know if the parser enters a
	 * comment. Which means the current character is a '/'. This does not mean
	 * it's actually a comment, It will be one if the next character is a '/' or
	 * a '*'.
	 */
	protected final int MAYBE_START_COMMENT = 4;

	/**
	 * State attribute. Associated with the bit to know if the parser reaches
	 * the end of a comment. Which means the parser is in a bloc comment and the
	 * current char is a '*'.
	 */
	protected final int MAYBE_END_BLOCK_COMMENT = 5;

	/**
	 * State attribute. Associated with the bit to know if the parser is
	 * currently parsing an inline comment (which started with "//"). This state
	 * is set back to 0 at the next line ending.
	 */
	protected final int IN_INLINE_COMMENT = 6;

	/**
	 * State attribute. Associated with the bit to know if the parser is
	 * currently parsing a block comment (which started with "/*"). This state
	 * is set back to 0 at the next "*\/".
	 */
	protected final int IN_BLOCK_COMMENT = 7;

	/**
	 * State attribute. Associated with the bit to know if the parser reached
	 * the end of a block comment. Which means the current char is a '/' and the
	 * previous one was a '*'.
	 */
	protected final int END_BLOCK_COMMENT = 8;

	/**
	 * State attribute. Associated with the bit to know if the parser is
	 * currently parsing a numeric value, not in a comment, nor in a string
	 * neither in a regex.
	 */
	protected final int IN_NUMERIC = 9;

	/**
	 * State attribute. Associated with the bit to know if the parser is
	 * currently parsing a token. A token can be a keyword, a method/function
	 * name, an attribute or a variable.
	 */
	protected final int IN_TOKEN = 10;

	/**
	 * State attribute. Associated with the bit to know if the parser enters a
	 * regex. Which means the current char is a '/' and the parser is not in a
	 * string neither in a comment and that the parser is not exiting a token or
	 * a numeric (in which case, the '/' would be a division. This does not mean
	 * it will actually be a regex, this will be confirmed if the next char is
	 * not a '/', nor a '*', neither a '(' (in which cases, it would be an
	 * inline comment, a bloc comment or a division).
	 */
	protected final int MAYBE_IN_REGEX = 11;

	/**
	 * State attribute. Associated with the bit to know if the parser is
	 * currently parsing a regex.
	 */
	protected final int IN_REGEX = 12;

	/**
	 * State attribute. Associated with the bit to know if the parser is
	 * currently parsing the options of a regex (after the ending '/').
	 */
	protected final int IN_REGEX_END = 13;

	/**
	 * State attribute. Associated with the bit to know if the parser is on a
	 * closing parenthesis, but not in a regex, nor in a comment neither in a
	 * string.
	 */
	protected final int END_PARENTHESIS = 14;

	/* PARSING ATTRIBUTES (TEMPORARY VARIABLES, ANALYSERS... */

	/**
	 * Analyser for the strings in the parsed code.
	 * All the detected strings will be stored in this attribute, to be then
	 * analysed.
	 */
	protected StringAnalyser strings;

	/**
	 * Current parsed string. Reinitialised to "" at the beginning of the next
	 * string.
	 */
	protected String currentString;

	/**
	 * Can be '\'' or '"'. To know when the current string will end.
	 */
	protected char currentStringDelimiter;


	/**
	 * Analyser for the numeric values in the parsed code.
	 * All the detected numeric values will be stored in this attribute, to be
	 * then analysed.
	 */
	protected NumericAnalyser numerics;

	/**
	 * Current parsed numeric value. Reinitialised to "" at the beginning of
	 * the next numeric.
	 */
	protected String currentNumeric;


	/**
	 * Analyser for the regexes in the parsed code.
	 * All the detected regexes will be stored in this attribute, to be
	 * then analysed.
	 */
	protected RegexAnalyser regexes;

	/**
	 * Current parsed regex. Reinitialised to "" at the beginning of the next
	 * regex.
	 */
	protected String currentRegex;


	/**
	 * Analyser for the tokens in the parsed code.
	 * All the detected tokens will be stored in this attribute, to be
	 * then analysed.
	 */
	protected TokenAnalyser tokens;

	/**
	 * Current parsed token. Reinitialised to "" at the beginning of the next
	 * token.
	 */
	protected String currentToken;


	/**
	 * Index of the current parsed char in the code to analyse.
	 * Not used yet.
	 */
	protected int currentCharIndex = -1;

	/**
	 * Current global state of the parser. Composition of different states bits.
	 */
	protected int state = 0;

	/**
	 * Reset the parser with the default values.
	 * <p>
	 * The analysers are reconstructed and the state and index of the current
	 * character are set to 0.
	 * </p>
	 */
	protected void reset()
	{
		this.currentCharIndex = this.state = 0;
		this.regexes = new RegexAnalyser();
		this.strings = new StringAnalyser();
		this.numerics = new NumericAnalyser();
		this.tokens = new TokenAnalyser();
	}

	/**
	 * This method loops on each characters of a code chunk and will parse it.
	 *
	 * @param chunk The chunk of code to parse. A chunk can be a subpart of a
	 * 		whole code, so does not need to be valid by itself.
	 */
	public void parseCodeChunk(String chunk)
	{
		int chunkSize = chunk.length(),
			localCurChar;
		for (localCurChar = 0; localCurChar < chunkSize; localCurChar++, this.currentCharIndex++) {
			this.parseChar(chunk.charAt(localCurChar));
		}
	}

	/**
	 * Method to parse the current character.
	 * <p>
	 * The parser is completely agnostic of the neighbour characters and analyse
	 * the current one only based on the current state.
	 * </p>
	 * <p>
	 * First, the parser will parse the comments, regex and strings states.
	 * Those processes are supposed to be interchangeable. Then if the current
	 * char is not in one of those 3 states, the numerics and tokens will be
	 * parsed.
	 * </p>
	 *
	 * @param c The character to parse.
	 */
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

		if (c == ')') {
			this.enableState(this.END_PARENTHESIS);
		}
		else if (this.compareState(this.END_PARENTHESIS)) {
			this.disableState(this.END_PARENTHESIS);
		}
	}

	/**
	 * Print the analyse's report.
	 * <p>
	 * Each analyser print their report, to know which elements are optimisable.
	 * </p>
	 */
	public void printReport()
	{
		System.out.println("Report");
		this.regexes.run(true);
		this.strings.run(true);
		this.numerics.run(true);
		this.tokens.run(true);
	}

	/**
	 * Compares the global state with a given flag.
	 *
	 * @param flag The flag to test
	 * @return true if the flag is set to 1 in the global state.
	 */
	protected boolean compareState(final int flag)
	{
		return (1 << flag) == (this.state & (1 << flag));
	}

	/**
	 * Sets a flag's bit to 1 in the global state.
	 *
	 * @param flag The flag to activate.
	 */
	protected void enableState(final int flag)
	{
		this.state = this.state | (1 << flag);
	}

	/**
	 * Sets a flag's bit to 0 in the global state.
	 *
	 * @param flag The flag to desactivate.
	 */
	protected void disableState(final int flag)
	{
		this.state = this.state & ~(1 << flag);
	}

	/**
	 * @return true if the parser is currently in a comment.
	 */
	protected boolean inComment()
	{
		return this.compareState(this.IN_BLOCK_COMMENT)
			|| this.compareState(this.IN_INLINE_COMMENT)
			|| this.compareState(this.END_BLOCK_COMMENT);
	}

	/**
	 * @return true if the parser is currently in a regex.
	 */
	protected boolean inRegex()
	{
		return this.compareState(this.IN_REGEX)
			|| this.compareState(this.IN_REGEX_END);
	}

	/**
	 * @return true if the parser is currently in a string.
	 */
	protected boolean inString()
	{
		return this.compareState(this.STRING_START)
			|| this.compareState(this.IN_STRING)
			|| this.compareState(this.STRING_END);
	}

	/**
	 * This method parses a regex. It will detect if the parser enters or exits
	 * a regex. At the end of a regex, the parsed regex will be stored in the
	 * regex analyser ({@link analyser.RegexAnalyser}.
	 *
	 * @param c The current character.
	 */
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

	/**
	 * This method parses a comment. It will detect if the parser enters or
	 * exits a comment.
	 * <p>For the moment, comments are ignored in the analyse.<p>
	 *
	 * @param c The current character.
	 */
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

	/**
	 * This method parses a numeric value. It will detect if the parser enters
	 * or exits a numeric value. At the end of a numeric, the parsed numeric
	 * will be stored in the numeric analyser ({@link analyser.NumericAnalyser}.
	 *
	 * @param c The current character.
	 */
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

	/**
	 * This method parses a string. It will detect if the parser enters or exits
	 * a string. At the end of a string, the parsed string will be stored in the
	 * string analyser ({@link analyser.StringAnalyser}.
	 *
	 * @param c The current character.
	 */
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

	/**
	 * This method parses a token. It will detect if the parser enters or exits
	 * a token. At the end of a token, the parsed token will be stored in the
	 * token analyser ({@link analyser.TokenAnalyser}.
	 *
	 * @param c The current character.
	 */
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
}
