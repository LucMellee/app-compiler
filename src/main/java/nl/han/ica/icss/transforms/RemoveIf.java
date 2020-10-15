package nl.han.ica.icss.transforms;

//BEGIN UITWERKING
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.BoolLiteral;

import java.util.ArrayList;
//EIND UITWERKING

public class RemoveIf implements Transform {

    ArrayList<ASTNode> declarations;

    @Override
    public void apply(AST ast) {
        declarations = new ArrayList<>();
        apply(ast.root);
    }

    public void apply(ASTNode node) {
        ArrayList<ASTNode> trashCan = new ArrayList<>(); // Used to store objects that need to be removed from the parent after for loop
        ArrayList<ASTNode> children = node.getChildren();
        for (ASTNode child : children) {
            if (child instanceof IfClause) {
                removeIfClause((IfClause) child);
                trashCan.add(child);
            } else if (child instanceof Declaration) {
                // Saving and removing non if-clause declaration to add them in the correct order at the end.
                declarations.add(child);
                trashCan.add(child);
            } else if (child instanceof Stylerule) {
                apply(child);
                emptyTrashcan(node, trashCan);
            }
        }
        emptyTrashcan(node, trashCan);
        addDeclarationsToParent(node);
    }

    private void emptyTrashcan(ASTNode parent, ArrayList<ASTNode> trashCan) {
        while (trashCan.size() > 0) {
            if (parent instanceof Stylerule) {
                ((Stylerule) parent).body.remove(trashCan.remove(0));
            }
        }
    }

    private void addDeclarationsToParent(ASTNode parent) {
        while (declarations.size() > 0) {
            parent.addChild(declarations.remove(0));
        }
    }

    private void removeIfClause(IfClause ifClause) {
        // If clauses can only have booleans for expressions at this stage, so casting to BoolLiteral is always possible.
        if (((BoolLiteral) ifClause.conditionalExpression).value) {
            scanBodyForIfClauses(ifClause.body);
        } else if (ifClause.elseClause != null) {
            removeElseClause(ifClause.elseClause);
        }
    }

    private void removeElseClause(ElseClause elseClause) {
        scanBodyForIfClauses(elseClause.body);
    }

    private void scanBodyForIfClauses(ArrayList<ASTNode> body) {
        for (ASTNode child : body) {
            if (child instanceof IfClause) {
                removeIfClause((IfClause) child);
            } else {
                declarations.add(child);
            }
        }
    }

}
