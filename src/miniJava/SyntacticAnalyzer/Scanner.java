package miniJava.SyntacticAnalyzer;

import java.io.*;

import miniJava.ErrorReporter;

public class Scanner{
	private InputStream inputStream;
	private ErrorReporter reporter;

	private char currentChar;
	private StringBuilder currentSpelling;
	
	private final static char eolUnix = '\n';
	private final static char eolWindows = '\r';
	
	// true when end of line is found
	private boolean eot = false; 

	public Scanner(InputStream inputStream, ErrorReporter reporter) {
		this.inputStream = inputStream;
		this.reporter = reporter;
		// initialize scanner state
		readChar();
	}

	//skip whitespace and scan next token
	public Token scan() {
		// skip whitespace
		while (!eot && currentChar == ' ')
			skipIt();
		// start of a token: collect spelling and identify token kind
		currentSpelling = new StringBuilder();
		TokenKind kind = scanToken();
		String spelling = currentSpelling.toString();
		// return new token
		return new Token(kind, spelling);
	}

	 //determine token kind
	public TokenKind scanToken() {
		if (eot)
			return TokenKind.EOT; 
		
		// scan Token
		switch (currentChar) {
        case 'a':  case 'b':  case 'c':  case 'd':  case 'e':
        case 'f':  case 'g':  case 'h':  case 'i':  case 'j':
        case 'k':  case 'l':  case 'm':  case 'n':  case 'o':
        case 'p':  case 'q':  case 'r':  case 's':  case 't':
        case 'u':  case 'v':  case 'w':  case 'x':  case 'y':
        case 'z':
        case 'A':  case 'B':  case 'C':  case 'D':  case 'E':
        case 'F':  case 'G':  case 'H':  case 'I':  case 'J':
        case 'K':  case 'L':  case 'M':  case 'N':  case 'O':
        case 'P':  case 'Q':  case 'R':  case 'S':  case 'T':
        case 'U':  case 'V':  case 'W':  case 'X':  case 'Y':
        case 'Z':
        	takeIt();
        	while(Character.isDigit(currentChar) 
        			|| Character.isLetter(currentChar) 
        			|| currentChar == '_') {
        		takeIt();
        	}
        	return TokenKind.ID;
        	
        case '0': case '1': case '2': case '3': case '4':
		case '5': case '6': case '7': case '8': case '9':
			takeIt();
			while (Character.isDigit(currentChar))
				takeIt();
			return TokenKind.NUM;
			
		case '>':
			takeIt();
			if(currentChar == '=') {
				takeIt();
				return TokenKind.GREATEQ;
			}
			return TokenKind.GREATER;
			
		case '<':
			takeIt();
			if(currentChar == '=') {
				takeIt();
				return TokenKind.LESSEQ;
			}
			return TokenKind.LESS;
			
		case '=':
			takeIt();
			if(currentChar == '=') {
				takeIt();
				return TokenKind.EQUAL;
			}
			return TokenKind.ASSIGN;
		
		case '!':
			takeIt();
			if(currentChar == '=') {
				takeIt();
				return TokenKind.NOTEQ;
			}
			return TokenKind.NOT;
		
		case '&':
			takeIt();
			if(currentChar != '&') {
				System.exit(4); //TODO: figure out better way
			}
			takeIt();
			return TokenKind.AND;
			
		case '|':
			takeIt();
			if(currentChar != '|') {
				System.exit(4); //TODO: figure out better way
			}
			takeIt();
			return TokenKind.OR;
			
		case '+':
			takeIt();
			return TokenKind.PLUS;
		
		case '-':
			takeIt();
			return TokenKind.MINUS;
			
		case '*':
			takeIt();
			return TokenKind.TIMES;
			
		case '/':
			skipIt();
			if(currentChar == '/') {
				skipIt();
				while(currentChar != '\n'
						&& currentChar != '\r'
						&& !eot) {
					skipIt();
				}
				skipIt();
				return scanToken();
			} else if(currentChar == '*') {
				skipIt();
				boolean loop = true;
				while(loop) {
					if(currentChar == '*') {
						skipIt();
						if(currentChar == '/') {
							skipIt();
							loop = false;
						}
					} else {
						skipIt();
					}
				}
				return scanToken();
			}
			currentSpelling.append('/');
			nextChar();
			return TokenKind.DIV;
			
		case '(':
			takeIt();
			return TokenKind.LPAREN;
			
		case ')':
			takeIt();
			return TokenKind.RPAREN;
			
		case '[':
			takeIt();
			return TokenKind.LBRACK;
			
		case ']':
			takeIt();
			return TokenKind.RBRACK;
			
		case '{':
			takeIt();
			return TokenKind.LCURLY;
			
		case '}':
			takeIt();
			return TokenKind.RCURLY;
			
		case '.':
			takeIt();
			return TokenKind.PERIOD;
			
		case ',':
			takeIt();
			return TokenKind.COMMA;
			
		case ';':
			takeIt();
			return TokenKind.SEMICOLON;
			
		case '\t':
			skipIt();
			return scanToken();
			
		case '\n':
			skipIt();
			return scanToken();
			
		case '\r':
			skipIt();
			return scanToken();
			
		case ' ':
			skipIt();
			return scanToken();
			
		default:
			scanError("Unrecognized character '" + currentChar + "' in input");
			return TokenKind.ERROR;
		}
	}

	private void takeIt() {
		currentSpelling.append(currentChar);
		nextChar();
	}

	private void skipIt() {
		nextChar();
	}

	private void scanError(String m) {
		reporter.reportError("Scan Error:  " + m);
	}

	//advance to next char in inputstream
	//detect end of file or end of line as end of input
	private void nextChar() {
		if (!eot)
			readChar();
	}

	private void readChar() {
		try {
			int c = inputStream.read();
			currentChar = (char) c;
			if (c == -1 || currentChar == eolUnix || currentChar == eolWindows) {
				eot = true;
			}
		} catch (IOException e) {
			scanError("I/O Exception!");
			eot = true;
		}
	}
}
