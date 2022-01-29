package miniJava.SyntacticAnalyzer;

public class Token {
	public TokenKind kind;
	public String spelling;

	public Token(TokenKind kind, String spelling) {
		this.kind = kind;
		this.spelling = spelling;
		// convert ID to keywords
		if(kind == TokenKind.ID) {
			switch(spelling) {
			case "boolean":
				kind = TokenKind.BOOLEAN;
				break;
			case "class":
				kind = TokenKind.CLASS;
				break;
			case "else":
				kind = TokenKind.ELSE;
				break;
			case "false":
				kind = TokenKind.FALSE;
				break;
			case "if":
				kind = TokenKind.IF;
				break;
			case "int":
				kind = TokenKind.INT;
				break;
			case "new":
				kind = TokenKind.NEW;
				break;
			case "private":
				kind = TokenKind.PRIVATE;
				break;
			case "public":
				kind = TokenKind.PUBLIC;
				break;
			case "return":
				kind = TokenKind.RETURN;
				break;
			case "static":
				kind = TokenKind.STATIC;
				break;
			case "this":
				kind = TokenKind.THIS;
				break;
			case "true":
				kind = TokenKind.TRUE;
				break;
			case "void":
				kind = TokenKind.VOID;
				break;
			case "while":
				kind = TokenKind.WHILE;
				break;
			default:
				
				break;
			
			}
				
		}
	}
}
