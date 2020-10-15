package nl.han.ica.icss.parser;

import java.util.ArrayList;
import java.util.Collections;

import nl.han.ica.datastructures.HANStack;
import nl.han.ica.datastructures.IHANStack;
import nl.han.ica.icss.ast.*;
import nl.han.ica.icss.ast.literals.*;
import nl.han.ica.icss.ast.operations.AddOperation;
import nl.han.ica.icss.ast.operations.MultiplyOperation;
import nl.han.ica.icss.ast.operations.SubtractOperation;
import nl.han.ica.icss.ast.selectors.ClassSelector;
import nl.han.ica.icss.ast.selectors.IdSelector;
import nl.han.ica.icss.ast.selectors.TagSelector;

/**
 * This class extracts the ICSS Abstract Syntax Tree from the Antlr Parse tree.
 */
public class ASTListener extends ICSSBaseListener {

    //Accumulator attributes:
    private AST ast;

    //Use this to keep track of the parent nodes when recursively traversing the ast
    private IHANStack<ASTNode> currentContainer;

    // Stylesheet node to add style rules to
    private Stylesheet stylesheet;

    private ArrayList<ASTNode> stylerules;

    public ASTListener() {
        ast = new AST();
        currentContainer = new HANStack<>();
        stylesheet = new Stylesheet();
        stylerules = new ArrayList<>();
    }

    public AST getAST() {
        return ast;
    }

    @Override
    public void exitStylesheet(ICSSParser.StylesheetContext ctx) {
        ArrayList<ASTNode> variables = getGlobalVariables();
        while (variables.size() != 0) {
            stylesheet.addChild(variables.remove(0));
        }
        while (stylerules.size() != 0) {
            stylesheet.addChild(stylerules.remove(0));
        }
        ast.root = stylesheet;
    }

    private ArrayList<ASTNode> getGlobalVariables() {
        ArrayList<ASTNode> variables = new ArrayList<>();
        while (currentContainer.peek() != null) {
            variables.add(currentContainer.pop());
        }
        Collections.reverse(variables);
        return variables;
    }

    @Override
    public void exitStylerule(ICSSParser.StyleruleContext ctx) {
        ArrayList<ASTNode> attributes = new ArrayList<>();
        // While there's an item on the stack and this item is not an instanceof Selector add to attribute list.
        while (currentContainer.peek() != null && !(currentContainer.peek() instanceof Selector)) {
            attributes.add(currentContainer.pop());
        }
        Collections.reverse(attributes);

        ArrayList<ASTNode> selectors = new ArrayList<>();
        // While there's an item on the stack and this item is an instanceof Selector add to selector list.
        while (currentContainer.peek() != null && (currentContainer.peek() instanceof Selector)) {
            ASTNode selector = currentContainer.pop();
            selectors.add(selector);
        }
        Collections.reverse(selectors);

        ASTNode stylerule = new Stylerule();
        // It can be assumed a style rule always has one selector based on the grammar, so the first element of 'selectors' is present.

        while (attributes.size() > 0) {
            stylerule.addChild(attributes.remove(0));
        }
        // Whilst there are still elements in 'selectors' add them to the style rule.
        while (selectors.size() > 0) {
            stylerule.addChild(selectors.remove(0));
        }
        stylerules.add(stylerule);
    }

    @Override
    public void exitDeclaration(ICSSParser.DeclarationContext ctx) {
        ASTNode declaration = new Declaration();
        ASTNode expression = currentContainer.pop();
        declaration.addChild(currentContainer.pop());
        declaration.addChild(expression);
        currentContainer.push(declaration);
    }

    @Override
    public void exitTagSelector(ICSSParser.TagSelectorContext ctx) {
        ASTNode selector = new TagSelector(ctx.getChild(0).getText());
        currentContainer.push(selector);
    }

    @Override
    public void exitIdSelector(ICSSParser.IdSelectorContext ctx) {
        ASTNode selector = new IdSelector(ctx.getChild(0).getText());
        currentContainer.push(selector);
    }

    @Override
    public void exitClassSelector(ICSSParser.ClassSelectorContext ctx) {
        ASTNode selector = new ClassSelector(ctx.getChild(0).getText());
        currentContainer.push(selector);
    }

    @Override
    public void exitIfClause(ICSSParser.IfClauseContext ctx) {
        ASTNode elseClause = null;
        if (currentContainer.peek() instanceof ElseClause) {
            elseClause = currentContainer.pop();
        }

        ArrayList<ASTNode> body = new ArrayList<>();
        // If there is an item on the stack and this item is not an instanceof Expression add it to the body
        while (currentContainer.peek() != null && !(currentContainer.peek() instanceof Expression)) {
            body.add(currentContainer.pop());
        }
        Collections.reverse(body);

        ASTNode conditionalExpression = currentContainer.pop();
        currentContainer.push(createIfClauseNode(elseClause, body, conditionalExpression));
    }

    private ASTNode createIfClauseNode(ASTNode elseClause, ArrayList<ASTNode> body, ASTNode conditionalExpression) {
        ASTNode ifClause = new IfClause();
        ifClause.addChild(conditionalExpression);
        if (elseClause != null) {
            ifClause.addChild(elseClause);
        }
        while (body.size() > 0) {
            ifClause.addChild(body.remove(0));
        }
        return ifClause;
    }

    @Override
    public void exitElseClause(ICSSParser.ElseClauseContext ctx) {
        ArrayList<ASTNode> nodes = new ArrayList<>();
        // An ElseClause contains 3 useless elements in the grammer. By getting the child count and removing three elements provides the correct count of stack items
        for(int i = 0;i<ctx.getChildCount()-3;i++){
            nodes.add(currentContainer.pop());
        }
        Collections.reverse(nodes);
        ASTNode node = new ElseClause(nodes);
        currentContainer.push(node);
    }

    @Override
    public void exitPropertyName(ICSSParser.PropertyNameContext ctx) {
        ASTNode selector = new PropertyName(ctx.getChild(0).getText());
        currentContainer.push(selector);
    }

    @Override
    public void exitAdditionSubtraction(ICSSParser.AdditionSubtractionContext ctx) {
        ASTNode node;
        if (ctx.getChild(1).getText().equals("+")) {
            node = new AddOperation();
        } else {
            node = new SubtractOperation();
        }

        ASTNode rightLiteral = currentContainer.pop();
        node.addChild(currentContainer.pop());
        node.addChild(rightLiteral);
        currentContainer.push(node);
    }

    @Override
    public void exitMultiplication(ICSSParser.MultiplicationContext ctx) {
        ASTNode rightLiteral = currentContainer.pop();
        ASTNode multiplicationExpression = new MultiplyOperation();
        multiplicationExpression.addChild(currentContainer.pop());
        multiplicationExpression.addChild(rightLiteral);
        currentContainer.push(multiplicationExpression);
    }

    @Override
    public void exitPixelLiteal(ICSSParser.PixelLitealContext ctx) {
        ASTNode literal = new PixelLiteral(ctx.getChild(0).getText());
        currentContainer.push(literal);
    }

    @Override
    public void exitPercentageLiteral(ICSSParser.PercentageLiteralContext ctx) {
        ASTNode literal = new PercentageLiteral(ctx.getChild(0).getText());
        currentContainer.push(literal);
    }

    @Override
    public void exitScalarLiteral(ICSSParser.ScalarLiteralContext ctx) {
        ASTNode literal = new ScalarLiteral(ctx.getChild(0).getText());
        currentContainer.push(literal);
    }

    @Override
    public void exitColorLiteral(ICSSParser.ColorLiteralContext ctx) {
        ASTNode literal = new ColorLiteral(ctx.getChild(0).getText());
        currentContainer.push(literal);
    }

    @Override
    public void exitBooleanLiteral(ICSSParser.BooleanLiteralContext ctx) {
        ASTNode literal = new BoolLiteral(ctx.getChild(0).getText());
        currentContainer.push(literal);
    }

    @Override
    public void exitVariableReference(ICSSParser.VariableReferenceContext ctx) {
        ASTNode variable = new VariableReference(ctx.getChild(0).getText());
        currentContainer.push(variable);
    }

    @Override
    public void exitVariableAssignment(ICSSParser.VariableAssignmentContext ctx) {
        ASTNode variable = new VariableAssignment();
        ASTNode expression = currentContainer.pop();
        variable.addChild(currentContainer.pop());
        variable.addChild(expression);
        currentContainer.push(variable);
    }

}