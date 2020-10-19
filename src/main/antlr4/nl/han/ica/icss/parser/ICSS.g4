grammar ICSS;

//--- LEXER: ---

// IF support:
IF: 'if';
ELSE: 'else';
BOX_BRACKET_OPEN: '[';
BOX_BRACKET_CLOSE: ']';


//Literals
TRUE: 'TRUE';
FALSE: 'FALSE';
PIXELSIZE: [0-9]+ 'px';
PERCENTAGE: [0-9]+ '%';
SCALAR: [0-9]+;


//Color value takes precedence over id idents
COLOR: '#' [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f];

//Specific identifiers for id's and css classes
ID_IDENT: '#' [a-z0-9\-]+;
CLASS_IDENT: '.' [a-z0-9\-]+;

//General identifiers
LOWER_IDENT: [a-z] [a-z0-9\-]*;
CAPITAL_IDENT: [A-Z] [A-Za-z0-9_]*;

//All whitespace is skipped
WS: [ \t\r\n]+ -> skip;

//
OPEN_BRACE: '{';
CLOSE_BRACE: '}';
SEMICOLON: ';';
COLON: ':';
PLUS: '+';
MIN: '-';
MUL: '*';
ASSIGNMENT_OPERATOR: ':=';

// Uitbreiding
GREATER_THAN: '>';
LOWER_THAN: '<';
EQUALS: '==';
GREATER_EQUAL_THAN: '>=';
LOWER_EQUAL_THAN: '<=';
REVERSE_BOOL: '!';
NOT_EQUAL: '!=';


//--- PARSER: ---
stylesheet: variableAssignment* stylerule+;
stylerule: selector+ OPEN_BRACE attribute+ CLOSE_BRACE;
attribute: variableAssignment | declaration | ifClause;
declaration: propertyName COLON expression SEMICOLON;

// CSS class selectors
selector: LOWER_IDENT #tagSelector
          | (ID_IDENT | COLOR) #idSelector
          | CLASS_IDENT #classSelector;

// IF ELSE clauses
ifClause: IF BOX_BRACKET_OPEN REVERSE_BOOL? (expression | boolOperation) BOX_BRACKET_CLOSE OPEN_BRACE attribute+ CLOSE_BRACE elseClause?;
elseClause: ELSE OPEN_BRACE attribute+ CLOSE_BRACE;

// Declaration attribute
propertyName: LOWER_IDENT;

// Value
expression: literal #expressionLiteral
            | expression MUL expression #multiplication
            | expression (PLUS | MIN) expression #additionSubtraction;
literal: PIXELSIZE #pixelLiteal
        | PERCENTAGE #percentageLiteral
        | SCALAR #scalarLiteral
        | COLOR #colorLiteral
        | bool #booleanLiteral
        | variableReference #variable;
bool: TRUE | FALSE;
boolOperation: expression EQUALS expression #equalsOperation
 | expression GREATER_THAN expression #greaterThanOperation
 | expression LOWER_THAN expression #lowerThanOperation
 | expression GREATER_EQUAL_THAN expression #greaterEqualOperation
 | expression LOWER_EQUAL_THAN expression #lowerEqualOperation
 | expression NOT_EQUAL expression #notEqualOperation;

// Variable
variableAssignment: variableReference ASSIGNMENT_OPERATOR expression SEMICOLON;
variableReference: CAPITAL_IDENT;