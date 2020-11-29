package com.craftinginterpreters.lox;

/*
 * prinitng abstract syntax tree in a string format that has an unambiguous
 * order of operation, applied to tokens or subexpressions contained in the
 * given expression.
 *
 * this also implements the visitor interface that is declared in Expr.java
 * This means that we need to have concrete implementation for each visit
 * methods in the interface.
 */

class AstPrinter implements Expr.Visitor<String> {
  String print(Expr expr) {
    return expr.accept(this);
  }

  @Override
  public String visitBinaryExpr(Expr.Binary expr) {
    return parenthesize(expr.operator.lexeme, expr.left, expr.right);
  }

  @Override
  public String visitGroupingExpr(Expr.Grouping expr) {
    return parenthesize("group", expr.expression);
  }

  /*
   * quite easy. only change value to string
   * or handle java null to become lox nil
   */
  @Override
  public String visitLiteralExpr(Expr.Literal expr) {
    if(expr.value == null) return "nil";
	return expr.value.toString();
  }

  @Override
  public String visitUnaryExpr(Expr.Unary expr) {
    return parenthesize(expr.operator.lexeme, expr.right);
  }

  /*
   * parenthesize helper function to handle other expr's
   * subexpression
   */
  private String parenthesize(String name, Expr... exprs) {
    StringBuilder builder = new StringBuilder();

	builder.append("(").append(name);
	for (Expr expr : exprs){
		builder.append(" ");
		// calls accept for each subexpression in and then passes
		// itself. It is a recursive step to print an entire tree
		builder.append(expr.accept(this));
	}
	builder.append(")");

	return builder.toString();
  }

  // public static void main(String[] args) {
  //   Expr expression = new Expr.Binary(
		// new Expr.Unary(
				// new Token(TokenType.MINUS, "-", null, 1),
				// new Expr.Literal(123)),
		// new Token(TokenType.STAR, "*", null, 1),
		// new Expr.Grouping(
				// new Expr.Literal(45.67)));

	// System.out.println(new AstPrinter().print(expression));
  // }
}
