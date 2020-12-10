package com.craftinginterpreters.lox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Lox {
  static boolean hadError = false;
  public static void main(String[] args) throws IOException {
    if (args.length > 1){
		System.out.println("Usage: jlox [script]");
		System.exit(64);
	} else if (args.length == 1){
		runFile(args[0]);
	} else{
		runPrompt();
	}
  }

  private static void runFile(String path) throws IOException {
    byte[] bytes = Files.readAllBytes(Paths.get(path));
	run(new String(bytes, Charset.defaultCharset()));
  }

  /*
   * main function for REPL.
   */
  private static void runPrompt() throws IOException {
    InputStreamReader input = new InputStreamReader(System.in);
	BufferedReader reader = new BufferedReader(input);

	for (;;) { // for loop forever which is the loop in REPL
		System.out.print("> ");
		String line = reader.readLine(); // read a line of input from the user then returns the result
		if (line == null) break; // check if a signal is ctrl-D which returns null and will break the loop
		run(line); // eval the command which is the E in REPL
		hadError = false; // add this hadError to false so that repl does not exit when user makes a mistake
	}
  }

  // Baby steps: printing the token only when running the interpreter
  // since the interpreter hasnt been implemented yet
  private static void run(String source) {
    // Indicate an error in the exit code.
    if (hadError) System.exit(65);
    Scanner scanner = new Scanner(source);
	List<Token> tokens = scanner.scanTokens();

	// instantiating the parser
    Parser parser = new Parser(tokens);
	Expr expression = parser.parse();

	// Stop when syntax error is detected
	if (hadError) return;

	System.out.println(new AstPrinter().print(expression));
  }

  /*
   * Error handling: Very important when things go wrong so that the user
   * of the language will be guided to what they __actually__ want to do
   *
   * err() and report() are helper fns that tell users about syntax errors
   * on a given line
   *
   * Good engineering practice to separate the code that generates error
   * and the code that reports error.
   */
  static void error(int line, String message) {
    report(line, "", message);
  }

  private static void report(int line, String where, String message) {
    System.err.println("[line " + line + "] Error" + where + ": " + message);
	hadError = true;
  }

  static void error(Token token, String message) {
		if (token.type == TokenType.EOF) {
		  report(token.line, " at end", message);
		} else {
		  report(token.line, " at '" + token.lexeme + "'", message);
		}
	}

}
