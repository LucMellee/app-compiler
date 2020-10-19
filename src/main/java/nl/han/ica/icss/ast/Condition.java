package nl.han.ica.icss.ast;

import nl.han.ica.icss.ast.literals.BoolLiteral;

import java.util.ArrayList;

public class Condition extends Expression {
    BoolLiteral inverseCondition;
    Expression conditionalExpression;

    public Condition() {
        inverseCondition = new BoolLiteral(false);
    }

    public Condition(BoolLiteral literal) {
        this.inverseCondition = literal;
    }

    @Override
    public ASTNode addChild(ASTNode child) {
        if (child instanceof Expression) {
            this.conditionalExpression = (Expression) child;
        }
        return this;
    }

    @Override
    public ArrayList<ASTNode> getChildren() {
        ArrayList<ASTNode> children = new ArrayList<>();
        children.add(conditionalExpression);
        return children;
    }

    @Override
    public String getNodeLabel() {
        return "Condition";
    }
}
