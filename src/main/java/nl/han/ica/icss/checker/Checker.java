package nl.han.ica.icss.checker;

import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.types.ExpressionType;
import nl.han.ica.datastructures.HANLinkedList;

import java.util.ArrayList;
import java.util.HashMap;


public class Checker {
    private final String COLOR_PROPERTY = "color";
    private final String BACKGROUND_COLOR_PROPERTY = "background-color";
    private final String WIDTH_PROPERTY = "width";
    private final String HEIGHT_PROPERTY = "height";
    private final String IF_STATEMENT = "ifClause";

    private IHANLinkedList<HashMap<String, ExpressionType>> variableTypes;
    private ArrayList<String> allowedProperties;

    public void check(AST ast) {
        variableTypes = new HANLinkedList<>();
        allowedProperties = new ArrayList<>();
        addAllowedProperties();
        ASTNode root = ast.root;
        check(root, 0);
    }

    private void addAllowedProperties() {
        allowedProperties.add(COLOR_PROPERTY);
        allowedProperties.add(BACKGROUND_COLOR_PROPERTY);
        allowedProperties.add(WIDTH_PROPERTY);
        allowedProperties.add(HEIGHT_PROPERTY);
    }

    private void check(ASTNode node, int scope) {
        enterScope(scope);
        ArrayList<ASTNode> children = node.getChildren();
        for (ASTNode child : children) {
            if (child instanceof Expression && node instanceof IfClause) {
                checkExpression(IF_STATEMENT, (Expression) child, scope);
            } else if (child instanceof Declaration) {
                checkDeclaration((Declaration) child, scope);
            } else if (child instanceof VariableAssignment) {
                initializeVariable((VariableAssignment) child, scope);
            } else if (child instanceof Stylerule || child instanceof IfClause || child instanceof ElseClause) {
                check(child, scope + 1);
                exitScope(scope + 1);
            }
        }
    }

    private void initializeVariable(VariableAssignment variableAssignment, int scope) {
        Expression expression = variableAssignment.expression;
        ExpressionType expressionType = getLiteralExpressionType(expression);

        VariableReference reference = variableAssignment.name;
        variableTypes.get(scope).put(reference.name, expressionType);
    }

    private ExpressionType getLiteralExpressionType(Expression expression) {
        if (expression instanceof BoolLiteral) {
            return ExpressionType.BOOL;
        } else if (expression instanceof ColorLiteral) {
            return ExpressionType.COLOR;
        } else if (expression instanceof PercentageLiteral) {
            return ExpressionType.PERCENTAGE;
        } else if (expression instanceof PixelLiteral) {
            return ExpressionType.PIXEL;
        } else if (expression instanceof ScalarLiteral) {
            return ExpressionType.SCALAR;
        } else {
            return ExpressionType.UNDEFINED;
        }
    }

    private void checkDeclaration(Declaration declaration, int scope) {
        String declarationProperty = declaration.property.name;
        if (allowedProperties.contains(declarationProperty)) {
            checkExpression(declarationProperty, declaration.expression, scope);
        } else {
            declaration.setError("Property '" + declarationProperty + "' is not allowed.");
        }
    }

    private void checkExpression(String property, Expression expression, int scope) {
        ExpressionType expressionType; // Used to check expression type of variable
        if (expression instanceof VariableReference) {
            expressionType = getVariableExpressionType(scope, (VariableReference) expression);
        } else {
            expressionType = getLiteralExpressionType(expression);
        }

        if (expression instanceof Literal || expression instanceof VariableReference) {
            checkIfProperyAndExceptionTypeMatch(property, expression, expressionType);
        } else if (expression instanceof Operation) {
            checkOperation(property, (Operation) expression, scope);
        }

        // If the expression is a conditional expression, check if the expression type is boolean.
        if (property.equals(IF_STATEMENT) && expressionType != ExpressionType.BOOL) {
            expression.setError("Conditional expression is not a boolean type.");
        }
    }

    private void checkIfProperyAndExceptionTypeMatch(String property, Expression expression, ExpressionType expressionType) {
        switch (property) {
            case COLOR_PROPERTY:
            case BACKGROUND_COLOR_PROPERTY:
                if (expressionType != ExpressionType.COLOR) {
                    // This if will prevent a previously set error from being overwritten
                    if (expression.getError() == null) {
                        expression.setError("Expression is not a color value");
                    }
                }
                break;
            case WIDTH_PROPERTY:
            case HEIGHT_PROPERTY:
                if (expressionType != ExpressionType.PIXEL && expressionType != ExpressionType.PERCENTAGE) {
                    // This if will prevent a previously set error from being overwritten
                    if (expression.getError() == null) {
                        expression.setError("Expression is neither a pixel value or a percentage");
                    }
                }
                break;
        }
    }

    private ExpressionType getVariableExpressionType(int scope, VariableReference variableReference) {
        ExpressionType expressionType = null;
        for (int i = 0; i < scope; i++) {
            expressionType = variableTypes.get(i).get(variableReference.name);
            if (expressionType != null) {
                break;
            }
        }

        // If expression type is null when code is reached it means the variable is not known in the hashmaps.
        if (expressionType == null) {
            variableReference.setError("Variable is not declared or out of scope.");
        }

        return expressionType;
    }

    private void checkOperation(String property, Operation operation, int scope) {
        Expression leftExpression = operation.lhs;
        Expression rightExpression = operation.rhs;

        if (leftExpression instanceof Operation) {
            checkOperation(property, (Operation) leftExpression, scope);
        }
        if (rightExpression instanceof Operation) {
            checkOperation(property, (Operation) rightExpression, scope);
        }

        if (leftExpression instanceof ColorLiteral || rightExpression instanceof ColorLiteral) {
            operation.setError("Color literal is not allowed in an operation.");
        }

        if (leftExpression instanceof ColorLiteral || rightExpression instanceof ColorLiteral) {
            operation.setError("A boolean cannot be used in an operation.");
        }

        if (operation instanceof MultiplyOperation) {
            if (!(leftExpression instanceof ScalarLiteral) && !(rightExpression instanceof ScalarLiteral)) {
                operation.setError("At least one of the expressions needs to be a scalar literal.");
            }
        } else if (operation instanceof AddOperation || operation instanceof SubtractOperation) {
            if (!expressionLiteralsMatch(leftExpression, rightExpression, scope)) {
                operation.setError("Operation expression do not match. In addition and subtraction expressions need to match.");
            }
        }
    }

    private boolean expressionLiteralsMatch(Expression leftExpression, Expression rightExpression, int scope) {
        ExpressionType leftExpressionType;
        ExpressionType rightExpressionType;

        // If the expression if a variable, check get variable expression type, else get literal expression type.
        if (leftExpression instanceof VariableReference) {
            leftExpressionType = getVariableExpressionType(scope, (VariableReference) leftExpression);
        } else {
            leftExpressionType = getLiteralExpressionType(leftExpression);
        }

        if (rightExpression instanceof VariableReference) {
            rightExpressionType = getVariableExpressionType(scope, (VariableReference) rightExpression);
        } else {
            rightExpressionType = getLiteralExpressionType(rightExpression);
        }

        return leftExpressionType == rightExpressionType;
    }

    private void enterScope(int scope) {
        variableTypes.insert(scope, new HashMap<>());
    }

    private void exitScope(int scope) {
        variableTypes.delete(scope);
    }
}
