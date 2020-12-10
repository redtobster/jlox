package com.craftinginterpreters.lox;

import java.util.List;

import static com.craftinginterpreters.lox.TokenType.*;

class Parser {
  private static class ParseError extends RuntimeException {}

  private final List<Token> tokens; // we have tokens (in list) now instead of chars
  private int current = 0; // point to the next token that is to be parsed

  Parser(List<Token> tokens) {
    this.tokens = tokens;
  }

  // main parse method to kick off the parser
  Expr parse() {
    try {
		return expression();
	} catch (ParseError error) {
		return null; // when there is syntax error, returns null for now. Thats better than hanging
	}
  }

  /*
   * First and foremost rule that is simplest.
   * Expression expands to equality
   * Remember that each subexpression of a certain precedence must accept
   * itself an
   * So expression matching equality would match all
   */
  private Expr expression() {
    return equality();
  }

  /*
   * RULE:
   * equality --> comparison ( ( "!=" | "==" ) comparison )*;
   */
  private Expr equality() {
    Expr expr = comparison(); // first call to comparison;q

	// check if we see another bang equal or equal equal
	while (match(BANG_EQUAL, EQUAL_EQUAL)) {
		Token operator = previous();
		Expr right = comparison();
		expr = new Expr.Binary(expr, operator, right);
	}

	return expr;
  }


  /*
   * RULE 2:
   * comparison --> term ( ( ">" | ">=" | "<" | "<=" ) term )*;
   * this rule is hit when the match method does not match equal equal
   * or bang equal operator (so we didnt go to the while loop)
   */
  private Expr comparison() {
    Expr expr = term();
	while (match(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)){
		Token operator = previous();
		Expr right = term();
		expr = new Expr.Binary(expr, operator, right);
	}
	return expr;
  }

  /*
   * RULE 3:
   * term --> factor ( ( "+" | "-" ) factor )*;
   */
  private Expr term() {
    Expr expr = factor();
	while (match(MINUS, PLUS)){
		Token operator = previous();
		Expr right = factor();
		expr = new Expr.Binary(expr, operator, right);
	}
	return expr;
  }

  /*
   * RULE 4:
   * factor --> unary ( ( "/" | "*" ) unary )*;
   */
  private Expr factor() {
    Expr expr = unary();
	while (match(SLASH, STAR)){
		Token operator = previous();
		Expr right = unary();
		expr = new Expr.Binary(expr, operator, right);
	}
	return expr;
  }

  /*
   * RULE 5:
   * unary --> ( "!" | "-" ) unary
   * 		 | primary ;
   * check the current token if it contains either ! or -
   * If it does, then we can recursively call unary
   * until we reach the base case which is reaching the primary
   * expression
   */
  private Expr unary() {
	while (match(BANG, MINUS)){
		Token operator = previous();
		Expr right = unary();
		return new Expr.Unary(operator, right);
	}

	return primary();
  }

  /*
   * RULE 6:
   * primary --> NUMBER | STRING | "true" | "false" | "nil"
   * 		 | "(" expression ")" ;
   * parsing single literals such as number, string, ture false, nil is straight forward
   */
  private Expr primary() {
    if (match(FALSE)) return new Expr.Literal(false);
    if (match(TRUE)) return new Expr.Literal(true);
    if (match(NIL)) return new Expr.Literal(null);

	if (match(NUMBER, STRING)) {
		return new Expr.Literal(previous().literal);
	}

	if (match(LEFT_PAREN)) {
		Expr expr = expression();
		consume(RIGHT_PAREN, "Expect ')' after expression."); // must find right paren. Error otherwise
		return new Expr.Grouping(expr);
	}

	// we are at a token that does not start an expression.
	// the line below handles this error.
	throw error(peek(), "Expect expression.");
  }

  /*
   * HELPER FN: match
   * checks if the current token has any of the type that is passed in as param
   * if all of the token does not have the desired type, then return false
   */
	private boolean match(TokenType... types) {
		for (TokenType type : types){
		  if (check(type)){
		    advance();
			return true;
		  }
		}

		return false;
	}

	/*
	 * HELPER FN: consume
	 * check if the next expression is of the expected type
	 */
    private Token consume(TokenType type, String message) {
		if (check(type)) return advance();

		throw error(peek(), message);
	}

	/*
	 * HELPER FN: check
	 * returns true if the current token that we see is of a given type
	 */
	private boolean check(TokenType type) {
		if (isAtEnd()) return false; // end of token (no more to consume)
		return peek().type == type;
	}

	/*
	 * HELPER FN: advance
	 * consumes the current token and then returns the token
	 */
    private Token advance() {
		if (!isAtEnd()) current++;
		return previous();
	}

	/*
	 * HELPERFN: isAtEnd()
	 * end of the tokens? or still have some more?
	 */
    private boolean isAtEnd() {
		return peek().type == EOF;
	}

	private Token peek() {
		return tokens.get(current);
	}

	private Token previous() {
		return tokens.get(current - 1);
	}

	/*
	 * HELPER fn: error
	 * error method that returns a sentinel class ParseError
	 * that extends from RuntimeException.
	 *
	 * We return instead of throwing the class
	 * so that we can decide to unwind or not.
	 */
    private ParseError error(Token token, String message){
		Lox.error(token, message);
		return new ParseError();
	}


	/*
	 * HELPER fn: synchronize
	 * after calling error, we call this synchronize and discard
	 * the remaining tokens in the expression.
	 * After hitting semicolon, we hope that we are already at
	 * the end of the expression and we can start over parsing again
	 * and check if there is an error to be reported.
	 */
	private void synchronize() {
		advance();

		while (!isAtEnd()) {
		  if (previous().type == SEMICOLON) return;

		  switch (peek().type) {
				  case CLASS:
				  case FUN:
				  case VAR:
				  case FOR:
				  case IF:
				  case WHILE:
				  case PRINT:
				  case RETURN:
						  return;
		  }
				advance();
		}

	}

}
