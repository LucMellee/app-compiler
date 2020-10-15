package nl.han.ica.icss.transforms;

import nl.han.ica.datastructures.IHANLinkedList;
import nl.han.ica.icss.ast.*;
import nl.han.ica.datastructures.HANLinkedList;
import nl.han.ica.icss.ast.literals.PercentageLiteral;
import nl.han.ica.icss.ast.literals.PixelLiteral;
import nl.han.ica.icss.ast.literals.ScalarLiteral;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;

import java.util.ArrayList;
import java.util.HashMap;

public class EvalExpressions implements Transform {

    private IHANLinkedList<HashMap<String, Literal>> variableValues;

    public EvalExpressions() {
        variableValues = new HANLinkedList<>();
    }

    @Override
    public void apply(AST ast) {
        apply(ast.root, 0);
    }

    public void apply(ASTNode node, int scope) {
        enterScope(scope);
        ArrayList<ASTNode> trashCan = new ArrayList<>();
        for (ASTNode child : node.getChildren()) {
            if (child instanceof VariableAssignment) {
                initializeVariable((VariableAssignment) child, scope);
                trashCan.add(child);
            } else if (child instanceof Declaration) {
                evaluateDeclaration((Declaration) child, scope);
            } else if (child instanceof IfClause) {
                evaluateIfClause((IfClause) child, scope);
                apply(child, scope + 1);
            } else if (child instanceof Stylerule || child instanceof ElseClause) {
                apply(child, scope + 1);
            }
        }
        emptyTrashcan(node, trashCan);
        exitScope(scope);
    }

    private void emptyTrashcan(ASTNode node, ArrayList<ASTNode> trashCan) {
        while (trashCan.size() > 0) {
            node.removeChild(trashCan.remove(0));
        }
    }

    private void evaluateIfClause(IfClause ifClause, int scope) {
        ifClause.conditionalExpression = evaluateExpression(ifClause.conditionalExpression, scope);
    }

    private void evaluateDeclaration(Declaration declaration, int scope) {
        declaration.expression = evaluateExpression(declaration.expression, scope);
    }

    private void initializeVariable(VariableAssignment variableAssignment, int scope) {
        Expression expression = variableAssignment.expression;
        VariableReference reference = variableAssignment.name;
        variableValues.get(scope).put(reference.name, evaluateExpression(expression, scope));
    }

    private Literal evaluateExpression(Expression expression, int scope) {
        if (expression instanceof Literal) {
            return (Literal) expression;
        } else if (expression instanceof VariableReference) {
            return getLiteralFromVariable((VariableReference) expression, scope);
        } else if (expression instanceof Operation) {
            return getLiteralFromOperation((Operation) expression, scope);
        } else {
            return null;
        }
    }

    private Literal getLiteralFromVariable(VariableReference variableReference, int scope) {
        Literal literal = null;
        for (int i = 0; i < scope; i++) {
            literal = variableValues.get(i).get(variableReference.name);
            if (literal != null) {
                return literal;
            }
        }
        return null;
    }

    private Literal getLiteralFromOperation(Operation operation, int scope) {
        Expression leftExpression = operation.lhs;
        Expression rightExpression = operation.rhs;

        // If either of the expressions is still an operation, process them again.
        if (leftExpression instanceof Operation) {
            leftExpression = getLiteralFromOperation((Operation) leftExpression, scope);
        }
        if (rightExpression instanceof Operation) {
            rightExpression = getLiteralFromOperation((Operation) rightExpression, scope);
        }

        // If either of the expressions in a variable reference, get the variable value.
        if (leftExpression instanceof VariableReference) {
            leftExpression = getLiteralFromVariable((VariableReference) leftExpression, scope);
        }
        if (rightExpression instanceof VariableReference) {
            rightExpression = getLiteralFromVariable((VariableReference) rightExpression, scope);
        }

        if (operation instanceof MultiplyOperation) {
            return getMultipliedValue(leftExpression, rightExpression);
        } else if (operation instanceof AddOperation) {
            return getAddedValue(leftExpression, rightExpression);
        } else if (operation instanceof SubtractOperation) {
            return getSubtractedValue(leftExpression, rightExpression);
        } else {
            return null;
        }
    }

    private Literal getMultipliedValue(Expression leftExpression, Expression rightExpression) {
        int value = getLiteralValue((Literal) leftExpression) * getLiteralValue((Literal) rightExpression);

        Literal literal;
        // If the left literal is a scalar, but right is not, make a new literal based on the right literal's type.
        // Else, just make a new literal with the value.
        if (leftExpression instanceof ScalarLiteral && !(rightExpression instanceof ScalarLiteral)) {
            literal = literalTypeFactory(rightExpression, value);
        } else {
            literal = literalTypeFactory(leftExpression, value);
        }
        return literal;
    }

    private Literal getAddedValue(Expression leftExpression, Expression rightExpression) {
        int value = getLiteralValue((Literal) leftExpression) + getLiteralValue((Literal) rightExpression);
        return literalTypeFactory(leftExpression, value); // When this code is reached, both expressions should be the same literal type
    }

    private Literal getSubtractedValue(Expression leftExpression, Expression rightExpression) {
        int value = getLiteralValue((Literal) leftExpression) - getLiteralValue((Literal) rightExpression);
        return literalTypeFactory(leftExpression, value); // When this code is reached, both expressions should be the same literal type
    }

    private Literal literalTypeFactory(Expression literal, int value) {
        if (literal instanceof PixelLiteral) {
            return new PixelLiteral(value);
        } else if (literal instanceof PercentageLiteral) {
            return new PercentageLiteral(value);
        } else if (literal instanceof ScalarLiteral) {
            return new ScalarLiteral(value);
        } else {
            return null;
        }
    }

    private int getLiteralValue(Literal literal) {
        if (literal instanceof PercentageLiteral) {
            return ((PercentageLiteral) literal).value;
        } else if (literal instanceof PixelLiteral) {
            return ((PixelLiteral) literal).value;
        } else if (literal instanceof ScalarLiteral) {
            return ((ScalarLiteral) literal).value;
        } else {
            return 0;
        }
    }

    private void enterScope(int scope) {
        variableValues.insert(scope, new HashMap<>());
    }

    private void exitScope(int scope) {
        variableValues.delete(scope);
    }
}
