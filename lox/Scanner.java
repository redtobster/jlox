package com.craftinginterpreters.lox;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.craftinginterpreters.lox.TokenType.*;

class Scanner {
  private final String source;
  private final List<Token> tokens = new ArrayList<>();
  private int start = 0; // points to the first char being considered
  private int current = 0; // points to the current char being considered
  private int line = 1; // what source line is current on right now

  // Map that stores keywords and its corresponding token type
  private static final Map <String, TokenType> keywords;
  static {
    keywords = new HashMap<>();
	keywords.put("and", AND);
	keywords.put("class", CLASS);
	keywords.put("else", ELSE);
	keywords.put("false", FALSE);
	keywords.put("for", FOR);
	keywords.put("fun", FUN);
	keywords.put("if", IF);
	keywords.put("nil", NIL);
	keywords.put("or", OR);
	keywords.put("print", PRINT);
	keywords.put("return", RETURN);
	keywords.put("super", SUPER);
	keywords.put("this", THIS);
	keywords.put("true", TRUE);
	keywords.put("var", VAR);
	keywords.put("while", WHILE);
  }

  Scanner(String source) {
    this.source = source;
  }

  List<Token> scanTokens() {
    while (!isAtEnd()) {
		// at the beginning of the next lexeme
		start = current;
		scanToken();
	}

	tokens.add(new Token(EOF, "", null, line));
	return tokens;
  }

  private void scanToken() {
    char c = advance();
	switch (c){
		case '(': addToken(LEFT_PAREN); break;
		case ')': addToken(RIGHT_PAREN); break;
		case '{': addToken(LEFT_BRACE); break;
		case '}': addToken(RIGHT_BRACE); break;
		case ',': addToken(COMMA); break;
		case '.': addToken(DOT); break;
		case '-': addToken(MINUS); break;
		case '+': addToken(PLUS); break;
		case ';': addToken(SEMICOLON); break;
		case '*': addToken(STAR); break;
		case '!':
		  addToken(match('=') ? BANG_EQUAL : BANG);
		  break;
		case '=':
		  addToken(match('=') ? EQUAL_EQUAL : EQUAL);
		  break;
		case '<':
		  addToken(match('=') ? LESS_EQUAL : LESS);
		  break;
		case '>':
		  addToken(match('=') ? GREATER_EQUAL : GREATER);
		  break;
		case '/':
		  if (match('/')) {
		    // comment goes until the end of line
			while (peek() != '\n' && !isAtEnd()) advance();
			// IMPORTANT:
			// when reach the end, we do not call addToken because comments are not important
		  } else if (match('*')){
		    while (peek() != '*' && !match('/') && !isAtEnd()) {
				if (peek() == '\n') line++;
				advance();
			}
			if (!isAtEnd()) {
				advance();
				advance();
		    }
		  } else {
		    addToken(SLASH);
		  }
		  break;
		case ' ':
		case '\r':
		case '\t':
		  break;
		case '\n':
		  line++;
		  break;
		// handling string literals they always begin with the " char
		case '"': string(); break;


		default:
		  if (isDigit(c)) {
		    number();
		  // assume lexeme starting with letter or underscore to be an identifier
		  } else if (isAlpha(c)){
		    identifier();
		  } else {
		    Lox.error(line, "Unexpected character."); // default if user keys in an invalid char like @
		  }
		  break;
		  /*
		   * IMPORTANT POINTS:
		   *   1. The erronous char still gets consumed by advance so that we dont get stuck in infinite loop
		   *   2. We keep scanning because there maybe other errors in the other program
		   *      we'd ideally report all errors instead of reporting just one error and stop scanning so that
		   *      the user can fix all errors at once rather than fixing one error, running and then
		   *      learning that there is another error.
		   */
	}
  }

  /*
   * HELPER FN 1: isAtEnd to tell us if we have consumed all chars
   */
  private boolean isAtEnd() {
    return current >= source.length();
  }

  /*
   * HELPER FN 2: advance is to go to the next char and return the next char to be consumed
   */
  private char advance() {
    current++;
	return source.charAt(current - 1);
  }

  /*
   * HELPER FN 3: addToken
   */
  private void addToken(TokenType type) {
    addToken(type, null);
  }

  private void addToken(TokenType type, Object literal) {
    String text = source.substring(start, current);
	tokens.add(new Token(type, text, literal, line));
  }

  /*
   * HELPER FN 4: match
   * see the expected char. If it matches, return true so that later on, add two character lexeme
   * otherwise, return false so that just add one char lexeme later on.
   */
  private boolean match(char expected) {
    if (isAtEnd()) return false;
	if (source.charAt(current) != expected) return false;

	// we only consume if it matches the expected char that we are looking for
	current++;
	return true;
  }

  /*
   * HELPER FN 5: peek
   * return the character at the current position of string without consuming the char
   */
  private char peek() {
    if (isAtEnd()) return '\0';
	return source.charAt(current);
  }

  /*
   * HELPER FN 6: string
   * handle string literal
   */
  private void string() {
    while (peek() != '"' && !isAtEnd()) {
		if(peek() == '\n') line++; // support multiline string
		advance();
	}

	if (isAtEnd()) {
		Lox.error(line, "Unterminated string."); // reporting an error of unterminated string
		return;
	}


	// The closing ".
	advance();

	// Trim the surrounding quotes (we do not want them when we add token)
	String value = source.substring(start + 1, current - 1);
	addToken(STRING, value);
  }

  /*
   * HELPER FN 7: isDigit
   * identify if the current character is a digit or not
   */
  private boolean isDigit(char c) {
    return c >= '0' && c <= '9';
  }

  /*
   * HELPER FN 8: number
   * method to consume the numbers literal
   */
  private void number() {
    while (isDigit(peek())) advance(); // if the next char is still digit, advance


	// Look for the fractional part
	// Remember that we do not support leading and trailing dot
	if (peek() == '.' && isDigit(peekNext())) {
		// consume the "."
		advance();

		while (isDigit(peek())) advance();
	}

	addToken(NUMBER, Double.parseDouble(source.substring(start,current)));
  }

  /*
   * HELPER FN 9: peekNext
   * this is to ensure that the char after "." of a fraction (subset of numbers)
   * is indeed a digit
   */
  private char peekNext() {
    if (current + 1 >= source.length()) return '\0';
    return source.charAt(current + 1);
  }

  /*
   * HELPER FN 10: identifier
   * member function to add lox reserved word as token
   */
  private void identifier() {
    while (isAlphaNumeric(peek())) advance();

    // check to see if it matches with anything in the map
	String text = source.substring(start, current);
	TokenType type = keywords.get(text);
	if (type == null) type = IDENTIFIER;
	addToken(IDENTIFIER);
  }

  /*
   * HELPER FN 11: isAlpha
   * check if its an alphabet that potentially might be a reserved word
   */
  private boolean isAlpha(char c) {
    return (c >= 'a' && c <= 'z') ||
		   (c >= 'A' && c <= 'z') ||
		   c == '_';
  }


  /*
   * HELPER FN 12: isAlphaNumeric
   * check if its an alphabet that potentially might be a reserved word
   */
  private boolean isAlphaNumeric(char c) {
    return isAlpha(c) || isDigit(c);
  }


}

