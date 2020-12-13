package com.craftinginterpreters.lox;

// declaring hat it is a visitor
// return type is an object
class Interpreter implements Expr.Visitor<Object> {
		// Interpreter's public API
		void interpret(Expr expression) {
		  try {
		    Object value = evaluate(expression);
			System.out.println(stringify(value));
		  } catch (RuntimeError error) { // java catch Runtime error
		    Lox.runtimeError(error); // Lox prints out error message and let the user now
		  }
		}

		private String stringify(Object object) {
		  if (object == null) return "nil";

		  if (object instanceof Double) {
		    String text = object.toString();
			if (text.endsWith(".0")) {
		      text = text.substring(0, text.length() - 2);
			}
			return text;
		  }

		  return object.toString();
		}

		/*
		 * Converting a literal tree node into a runtime value
		 */
		@Override
		public Object visitLiteralExpr(Expr.Literal expr) {
		  return expr.value;
		}

		@Override
		public Object visitGroupingExpr(Expr.Grouping expr) {
		  return evaluate(expr.expression); // recursively evaluate the subexpression and return it
		}

		@Override
		public Object visitUnaryExpr(Expr.Unary expr) {
		  Object right = evaluate(expr.right);

		  switch (expr.operator.type) { // remember that expr operator is a token that contains the type field
		    case BANG:
		      return !isTruthy(right);
		    case MINUS:
		      checkNumberOperand(expr.operator, right);
			  return -(double)right; // casting it because we dont know that right is a double in java.
		  }

		  // unreachable
		  return null;
		}

		private boolean isTruthy(Object object) {
		  if (object == null) return false;
		  if (object instanceof Boolean) return (boolean)object;
		  return true;
		}

		/*
		 * HELPER FN: evaluate
		 * helper fn for visitGrouping Expr
		 * sends the expression back into the interpreter's visitor implementation
		 */
		private Object evaluate(Expr expr) {
		  return expr.accept(this);
		}

		@Override
		public Object visitBinaryExpr(Expr.Binary expr){
		  Object left = evaluate(expr.left);
		  Object right = evaluate(expr.right);

		  switch (expr.operator.type) {
		    case GREATER:
				checkNumberOperands(expr.operator, left, right);
				return (double)left > (double)right;
		    case GREATER_EQUAL:
				checkNumberOperands(expr.operator, left, right);
				return (double)left >= (double)right;
		    case LESS:
				checkNumberOperands(expr.operator, left, right);
				return (double)left < (double)right;
		    case LESS_EQUAL:
				checkNumberOperands(expr.operator, left, right);
				return (double)left <= (double)right;
		    case MINUS:
				checkNumberOperands(expr.operator, left, right);
				return (double)left - (double)right;
		    case PLUS:
				if (left instanceof Double && right instanceof Double) {
				  return (double)left + (double)right;
				}
				if (left instanceof String && right instanceof String) {
				  return (String)left + (String)right;
				}

				throw new RuntimeError(expr.operator, "Operands must be two numbers or two strings.");
		    case SLASH:
				checkNumberOperands(expr.operator, left, right);
				return (double)left / (double)right;
		    case STAR:
				checkNumberOperands(expr.operator, left, right);
				return (double)left * (double)right;
		    case BANG_EQUAL: return !isEqual(left, right); // supports any type
		    case EQUAL_EQUAL: return isEqual(left, right);
		  }

		  // unreachable
		  return null;
		}
      private void checkNumberOperand(Token operator, Object operand) {
		if (operand instanceof Double) return;
		throw new RuntimeError(operator, "Operand must be a number.");
	  }
      private void checkNumberOperands(Token operator, Object left, Object right) {
		if (left instanceof Double && right instanceof Double) return;

		throw new RuntimeError(operator, "Operands must be numbers.");
	  }

      private boolean isEqual(Object a, Object b) {
		if (a == null && b == null) return true;
		if (a == null) return false;

		return a.equals(b);
	  }

}
