package miniJava.SyntacticAnalyzer;

import miniJava.ErrorReporter;

public class Parser {

	private Scanner scanner;
	private ErrorReporter reporter;
	private Token token;
	private boolean trace = true;

	public Parser(Scanner scanner, ErrorReporter reporter) {
		this.scanner = scanner;
		this.reporter = reporter;
	}

	//SyntaxError is used to unwind parse stack when parse fails
	class SyntaxError extends Error {
		private static final long serialVersionUID = 1L;	
	}

	//accept current token and advance to next token
	private void acceptIt() throws SyntaxError {
		accept(token.kind);
	}

/**
	 * verify that current token in input matches expected token and advance to next token
	 * @param expectedToken
	 * @throws SyntaxError  if match fails
*/
	private void accept(TokenKind expectedTokenKind) throws SyntaxError {
		if (token.kind == expectedTokenKind) {
			if (trace)
				pTrace();
			token = scanner.scan();
		}
		else
			parseError("expecting '" + expectedTokenKind +
					"' but found '" + token.kind + "'");
	}

	/**
	 * report parse error and unwind call stack to start of parse
	 * @param e  string with error detail
	 * @throws SyntaxError
	 */
	private void parseError(String e) throws SyntaxError {
		reporter.reportError("Parse error: " + e);
		throw new SyntaxError();
	}

	// show parse stack whenever terminal is  accepted
	private void pTrace() {
		StackTraceElement [] stl = Thread.currentThread().getStackTrace();
		for (int i = stl.length - 1; i > 0 ; i--) {
			if(stl[i].toString().contains("parse"))
				System.out.println(stl[i]);
		}
		System.out.println("accepting: " + token.kind + " (\"" + token.spelling + "\")");
		System.out.println();
	}
	//parse input, catch possible parse error
	public void parse() {
		token = scanner.scan();
		try {
			parseProgram();
		}
		catch (SyntaxError e) { }
	}

	private void parseProgram() throws SyntaxError {
		while(token.kind == TokenKind.CLASS) {
			parseClassDeclaration();
		}
		accept(TokenKind.EOT);
	}
	// TODO: Might have to identify specific ID using parseIdentifier()
	private void parseClassDeclaration() throws SyntaxError {
		// class id { (FieldDeclaration | MethodDeclaration)* }
		accept(TokenKind.CLASS);
		accept(TokenKind.ID);
		accept(TokenKind.LCURLY);
		
		while(isVisibilityStarter(token.kind)) {
			parseVisibility();
			parseAccess();
			if(token.kind == TokenKind.VOID) {
				parseMethodDeclaration();
			} else {
				parseType();
			}
			accept(TokenKind.ID);
			if(token.kind == TokenKind.SEMICOLON) {
				parseFieldDeclaration();
			} else {
				parseMethodDeclaration();
			}
		}
		
		accept(TokenKind.RCURLY);
	}
	
	private void parseFieldDeclaration() throws SyntaxError {
		// Visibility Access Type id ;
		accept(TokenKind.SEMICOLON);
	}
	
	private void parseMethodDeclaration() throws SyntaxError {
		// Visibility Access (Type | void) id ( ParameterList? ) { Statement* }
		accept(TokenKind.LPAREN);
		if(isParameterListStarter(token.kind)) {
			parseParameterList();
		}
		accept(TokenKind.RPAREN);
		accept(TokenKind.RBRACK);
		while (isStatementStarter(token.kind)) {
			parseStatement();
		}
		accept(TokenKind.LBRACK);
		
	}
	
	private void parseVisibility() throws SyntaxError {
		// (public | private)?
		if(token.kind == TokenKind.PUBLIC || token.kind == TokenKind.PRIVATE) {
			acceptIt();
		}
	}
	
	private void parseAccess() throws SyntaxError {
		// static?
		if(token.kind == TokenKind.STATIC) {
			accept(TokenKind.STATIC);
		}		
	}
	
	private void parseType() throws SyntaxError {
		// int | boolean | id | (int|id) []
		switch(token.kind) {
			case INT:
				acceptIt();
				if(token.kind == TokenKind.LBRACK) {
					acceptIt();
					accept(TokenKind.RBRACK);
				}
				break;
			case BOOLEAN:
				acceptIt();
				break;
			case ID:
				acceptIt();
				if(token.kind == TokenKind.LBRACK) {
					acceptIt();
					accept(TokenKind.RBRACK);
				}
			default:
				parseError("Invalid Type Starter: " + token.spelling);
				break;
		}
	}
	
	private void parseParameterList() throws SyntaxError {
		// Type id (, Type id)*
		parseType();
		accept(TokenKind.ID);
		while(token.kind == TokenKind.COMMA) {
			acceptIt();
			parseExpression();
			accept(TokenKind.ID);
		}
	}
	
	private void parseArgumentList() throws SyntaxError {
		// Expression (, Expression)*
		parseExpression();
		while(token.kind == TokenKind.COMMA) {
			acceptIt();
			parseExpression();
		}
	}
	
	private void parseReference() throws SyntaxError {
		// id | this | Reference . id
		switch(token.kind) {
			case ID:
				acceptIt();
				break;
			case THIS:
				acceptIt();
				break;
			default:
				parseError("Invalid Reference Starter: " + token.spelling);
				break;
		}
		while(token.kind == TokenKind.PERIOD) {
			acceptIt();
			accept(TokenKind.ID);
		}
	}
	
	private void parseStatement() throws SyntaxError {
		// Type id = Expression ;
		if(isTypeStarter(token.kind)) {
			parseType();
			accept(TokenKind.ID);
			accept(TokenKind.EQUAL);
			parseExpression();
			accept(TokenKind.SEMICOLON);
		} 
		
		if(isReferenceStarter(token.kind)) {
			parseReference();
			// Reference [ Expression ] = Expression ;
			if(token.kind == TokenKind.LBRACK) {
				acceptIt();
				parseExpression();
				accept(TokenKind.RBRACK);
				accept(TokenKind.EQUAL);
				parseExpression();
				accept(TokenKind.SEMICOLON);
			} 
			// Reference ( ArgumentList? ) ;
			else if(token.kind == TokenKind.LPAREN) {
				acceptIt();
				if(isArgumentListStarter(token.kind)) {
					parseArgumentList();
				}
				accept(TokenKind.RPAREN);
				accept(TokenKind.SEMICOLON);
			}
			// Reference = Expression ;
			else if(token.kind == TokenKind.EQUAL) {
				acceptIt();
				parseExpression();
				accept(TokenKind.SEMICOLON);
			}
		}
		
		switch(token.kind) {
			// { Statement* }
			case LCURLY:
				acceptIt();
				while(isStatementStarter(token.kind)) {
					parseStatement();
				}
				accept(TokenKind.RCURLY);
				break;
			// return Expression? ;
			case RETURN:
				acceptIt();
				if(isExpressionStarter(token.kind)) {
					parseExpression();
				}
				accept(TokenKind.SEMICOLON);
				break;
			// if ( Expression ) Statement (else Statement)?
			case IF:
				acceptIt();
				accept(TokenKind.LPAREN);
				parseExpression();
				accept(TokenKind.RPAREN);
				parseStatement();
				if(token.kind == TokenKind.ELSE) {
					acceptIt();
					parseStatement();
				}
				break;
			// while ( Expression ) Statement
			case WHILE:
				acceptIt();
				accept(TokenKind.LPAREN);
				parseExpression();
				accept(TokenKind.RPAREN);
				parseStatement();
				break;
				
			default:
				parseError("Invalid Statement Starter: " + token.spelling);
				break;
		}
	}
	
	private void parseExpression() throws SyntaxError {
		// Reference | Reference [ Expression ] | Reference ( ArgumentList ? )
		if(isReferenceStarter(token.kind)) {
			parseReference();
			if(token.kind == TokenKind.LBRACK) {
				acceptIt();
				parseExpression();
				accept(TokenKind.RBRACK);
			} else if(token.kind == TokenKind.LPAREN) {
				acceptIt();
				if(isArgumentListStarter(token.kind)) {
					parseArgumentList();
				}
				accept(TokenKind.LBRACK);
			}
		}
		
		switch(token.kind) {
			// unop Expression
			case NOT:
			case MINUS:
				acceptIt();
				parseExpression();
				break;	
			// ( Expression )
			case LPAREN:
				acceptIt();
				parseExpression();
				accept(TokenKind.RPAREN);
				break;
			// num | true | false
			case NUM:
			case TRUE:
			case FALSE:
				acceptIt();
				break;
			// new ( id() | int [ Expression ] | id [ Expression ] )
			case NEW:
				acceptIt();
				if(token.kind == TokenKind.ID) {
					acceptIt();
					if(token.kind == TokenKind.LPAREN) {
						acceptIt();
						parseExpression();
						accept(TokenKind.RPAREN);
					} else if(token.kind == TokenKind.LBRACK) {
						acceptIt();
						parseExpression();
						accept(TokenKind.RBRACK);
					} else {
						parseError("Bracket / Paren Expected instead of: " + token.spelling);
					}
				} else if(token.kind == TokenKind.INT) {
					acceptIt();
					accept(TokenKind.RBRACK);
					parseExpression();
					accept(TokenKind.LBRACK);
				}
				break;
			default:
				parseError("Invalid Expression Starter: " + token.spelling);
				break;
		}
		// Expression binop Expression
		while(isBinop(token.kind)) {
			acceptIt();
			parseExpression();
		}
	}
	
	private boolean isStatementStarter(TokenKind kind) {
		return kind == TokenKind.LCURLY
				|| kind == TokenKind.IF
				|| kind == TokenKind.WHILE
				|| kind == TokenKind.RETURN
				|| isTypeStarter(kind)
				|| isReferenceStarter(kind);
	}

	private boolean isArgumentListStarter(TokenKind kind) {
		return isExpressionStarter(kind);
	}

	private boolean isReferenceStarter(TokenKind kind) {
		return kind == TokenKind.THIS
				|| kind == TokenKind.ID;
	}
	
	private boolean isTypeStarter(TokenKind kind) {
		return kind == TokenKind.BOOLEAN
				|| kind == TokenKind.INT
				|| kind == TokenKind.ID;
	}
	
	private boolean isExpressionStarter(TokenKind kind) {
		return kind == TokenKind.NOT
				|| kind == TokenKind.MINUS
				|| kind == TokenKind.LPAREN
				|| kind == TokenKind.NUM
				|| kind == TokenKind.TRUE
				|| kind == TokenKind.FALSE
				|| kind == TokenKind.NEW
				|| isReferenceStarter(kind);
	}
	
	private boolean isVisibilityStarter(TokenKind kind) {
		return kind == TokenKind.LPAREN;
	}
	
	private boolean isParameterListStarter(TokenKind kind) {
		return isTypeStarter(kind);
	}
	
	private boolean isBinop(TokenKind kind) {
        return kind == TokenKind.GREATER
                || kind == TokenKind.LESS
                || kind == TokenKind.EQUAL 
                || kind == TokenKind.LESSEQ
                || kind == TokenKind.GREATEQ
                || kind == TokenKind.NOTEQ
                || kind == TokenKind.AND
                || kind == TokenKind.OR
                || kind == TokenKind.PLUS
                || kind == TokenKind.MINUS
                || kind == TokenKind.TIMES
                || kind == TokenKind.DIV;
	}
	
	private boolean isUnop(TokenKind kind) {
		return kind == TokenKind.NOT
				|| kind == TokenKind.MINUS;
	}

}
