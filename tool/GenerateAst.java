package com.craftinginterpreters.tool;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;

/*
 * This would be a java cmd tool that will generate Expr.java
 *
 * This class is a tool for us to generate base class for expressions.
 * Then for each kind of expression (unary or binary or grouping),
 * we create a subclass containing fields for their corresponding non terminals.
 *
 * so for example, if you want to access the second operand of a unary expression,
 * you would get a compile error. This is because unary expression would only have
 * one operand.
 */
public class GenerateAst {
  public static void main(String[] args) throws IOException {
    if (args.length != 1) {
		System.err.println("Usage: generate_ast <output directory>");
		System.exit(64);
	}
	String outputDir = args[0];
	defineAst(outputDir, "Expr", Arrays.asList(
		/*
		 * left hand side (name of the class) then
		 * semi-colon then
		 * right hand side (list of the fields)
		 */
		"Binary   : Expr left, Token operator, Expr right",
		"Grouping : Expr expression",
		"Literal  : Object value",
		"Unary    : Token operator, Expr right"
    ));

	defineAst(outputDir, "Stmt", Arrays.asList(
		"Expression : Expr expression",
		"Print      : Expr expression"
    ));
  }

  private static void defineAst( String outputDir, String baseName, List<String> types)
    throws IOException {
    String path = outputDir + "/" + baseName + ".java";
	PrintWriter writer = new PrintWriter(path, "UTF-8");

  /*
   * defineAst:
   * output base Expr class (first thing to do).
   */
    writer.println("package com.craftinginterpreters.lox;");
	writer.println();
	writer.println("import java.util.List;");
	writer.println();
	writer.println("abstract class " + baseName + " {"); // not hard coding because there will be separate family of classes later on

	/*
	 * This is the indirection that is needed to add extra methods to all class types
	 * without having to "touch" them one by one. Concrete implementation of the
	 * interface plus polymorphism would allow us to have separation of code.
	 */
    defineVisitor(writer, baseName, types); // defineVisitor that generates visitor interface.


	// AST Classes. These are each defined in the base class
	for (String type : types){
		String className = type.split(":")[0].trim(); //left hand side
		String fields = type.split(":")[1].trim(); //right hand side
		defineType(writer, baseName, className, fields); // defineType is written just below
	}

	// we define the abstract accept() method in the base class
	writer.println();
	writer.println("  abstract <R> R accept(Visitor<R> visitor);"); // return type of accept is generic

	// never forget the closing curly brackets
	writer.println("}");
    writer.close();

  }

  private static void defineVisitor( PrintWriter writer, String baseName, List<String> types) {
    writer.println("  interface Visitor<R> {");
	for (String type : types){
		String typeName = type.split(":")[0].trim();
		writer.println("      R visit" + typeName + baseName + "(" + typeName + " " + baseName.toLowerCase() + ");");
	}

	writer.println("  }");
  }
  /*
   * defineType HERE!!
   */
  private static void defineType( PrintWriter writer, String baseName, String className, String fieldList ){
    writer.println("  static class " + className + " extends " + baseName + " {");

	// Constructor for the class
	writer.println("    " + className + "(" + fieldList + ") {");

	// Store parameters in fields.
	String[] fields = fieldList.split(", ");
	for (String field: fields) {
		String name = field.split(" ")[1];
		writer.println("    this." + name + " = " + name + ";");
	}

	writer.println("    }");

    // Visitor pattern.
	writer.println();
	writer.println("    @Override");
	writer.println("    <R> R accept(Visitor<R> visitor) {");
	writer.println("     return visitor.visit" + className + baseName + "(this);");
	writer.println("    }");

	// Fields
	writer.println();
	for ( String field : fields) {
		writer.println("    final " + field + ";");
	}

	writer.println("  }");
  }

}
