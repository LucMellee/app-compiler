package nl.han.ica.icss.generator;


import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.ColorLiteral;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;

public class Generator {
	private final int AMOUNT_OF_SPACING = 2;

	public String generate(AST ast) {
		StringBuilder builder = new StringBuilder();
		ASTNode root = ast.root;
        for (ASTNode child : root.getChildren()) {
        	builder.append(generateStylerule((Stylerule) child));
        	builder.append("\n\n");
		}
		return builder.toString();
	}

	private String generateStylerule(Stylerule stylerule) {
		StringBuilder builder = new StringBuilder();
		for (Selector selector : stylerule.selectors) {
			builder.append(selector);
			builder.append(" ");
		}
		builder.append("{\n");
		// After transforming the AST should only have selectors and declaration remaining.
		for (ASTNode node : stylerule.body) {
			builder.append(" ".repeat(AMOUNT_OF_SPACING));
			if (node instanceof Declaration) {
				builder.append(generateDeclaration((Declaration) node));
				builder.append("\n");
			}
		}
		builder.append("}");
		return builder.toString();
	}

	private String generateDeclaration(Declaration declaration) {
		StringBuilder builder = new StringBuilder();
		builder.append(declaration.property.name);
		builder.append(": ");
		// Expression of declaration should after transform always be a literal, if not it will not add the propery value.
		if (declaration.expression instanceof Literal) {
			builder.append(getPropertyValue(declaration.expression));
			builder.append(";");
		}
		return builder.toString();
	}

	private String getPropertyValue(Expression expression) {
		if (expression instanceof ColorLiteral) {
			return ((ColorLiteral) expression).value;
		} else if (expression instanceof PixelLiteral) {
			return "" + ((PixelLiteral) expression).value + "px";
		} else if (expression instanceof PercentageLiteral) {
			return "" + ((PercentageLiteral) expression).value + "%";
		} else {
			return "";
		}
	}
}
