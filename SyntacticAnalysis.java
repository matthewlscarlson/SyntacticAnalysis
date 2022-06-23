import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.StringTokenizer;

public class SyntacticAnalysis {
	public static class Parser {
		private StringBuilder buffer;
		private LexicalAnalyzer lexer = new LexicalAnalyzer();

		private class LexicalAnalyzer {
			// Only read tokens that *must* be read
			public String read() {
				if (buffer.length() == 0) return "";

				// Lexeme will be from beginning of buffer (location of current token) to where whitespace is
				int i = buffer.indexOf(" ");
				String lexeme = buffer.substring(0, i);

				// Delete lexeme from buffer since it is now stored in variable
				buffer.delete(0, i + 1);

				//Print what tokens are read for debugging purposes
				//System.out.println(lexeme);

				return lexeme;
			}

			/* Peek without deleting when choice is an option
			 * E.g., A factor token can be identifier, integer constant, or expression
             */
			public String lookAhead() {
				int i = buffer.indexOf(" ");
				String lexeme = buffer.substring(0, i);

				return lexeme;
			}
		}

		public Parser(File file) {
			Scanner sc = null;

			try {
				sc = new Scanner(file);
				buffer = new StringBuilder((int)file.length());

				while(sc.hasNext()) {
					String token = sc.next();

					// Since token character sequence may contain characters such as ()+-*/=;
					// We need to further break down token to separate special character and rest
                    // E.g., if(sum1<sum2) token will be further tokenized into if ( sum1 < sum2 )
					// This way spacing doesn't matter
					StringTokenizer tokens = new StringTokenizer(token, "()+-*/=;", true);

					while (tokens.hasMoreTokens())
						buffer.append(tokens.nextToken() + " ");
				}
			} catch (FileNotFoundException fnfe) {
				System.out.print("File not found.");
				fnfe.printStackTrace();
			} finally {
				sc.close();
			}
		}

		// Returns program() since <program> -> program begin <statement_list> end is checked first
		public boolean isValidCode() {
			return program();
		}

		private boolean program() {
			// Every program must have program followed by begin
			if (!lexer.read().toLowerCase().equals("program")) return false;
			if (!lexer.read().toLowerCase().equals("begin"))   return false;

			if (!statementList()) return false;

			// Every program must finish with end keyword
			if (!lexer.read().toLowerCase().equals("end")) return false;

			return true;
		}

		private boolean statementList() {
			// At least one statement must be present in a statement list
			if (!statement()) return false;

			// Semicolon must be present if additional statements follow
			while (lexer.lookAhead().equals(";")) {
				lexer.read();

				if (!statement()) return false;
			}

            return true;
		}

		private boolean statement() {
			// Check for assignment statement
			// Not using variable() method because we do not want to delete token
			if (lexer.lookAhead().matches("[a-zA-Z][a-zA-Z0-9]*") && !isReservedWord(lexer.lookAhead())) {
				if (!assignmentStatement()) return false;
			} else if (lexer.lookAhead().equals("if")) {   // Check for if statement
				if (!ifStatement()) return false;
			} else if (lexer.lookAhead().equals("loop")) { // Check for loop statement
				if (!loopStatement()) return false;
			} else {
				return false;                              // Can't be a statement if not any of the above
			}

			return true;
		}

		// <assignment_statement> -> <var> = <expr>
		private boolean assignmentStatement() {
			if (!variable()) return false;
			if (!lexer.read().equals("=")) return false;
			if (!expression()) return false;

            return true;
		}

		// <if_statement> -> if (<logic_expr>) then <stmnt>
		private boolean ifStatement() {
			if (!lexer.read().equals("if"))   return false;
			if (!lexer.read().equals("("))    return false;
			if (!logicExpression())           return false;
			if (!lexer.read().equals(")"))    return false;
			if (!lexer.read().equals("then")) return false;
			if (!statement())                 return false;

            return true;
		}

		// <loop_statement> -> loop (<logic_expr>) <statement>
		private boolean loopStatement() {
			if (!lexer.read().equals("loop")) return false;
			if (!lexer.read().equals("("))    return false;
			if (!logicExpression())           return false;
			if (!lexer.read().equals(")"))    return false;
			if (!statement())                 return false;

			return true;
		}

		// <expr> -> <term> {(+|-) <term>}
		private boolean expression() {
			if (!term()) return false;

			// Since + is a regex key symbol it must be escaped
			while (lexer.lookAhead().matches("\\+|-")) {
				lexer.read();

                if (!term()) return false;
			}

			return true;
		}

		// <logic_expr> -> <var> {(<|>) <var>}
		private boolean logicExpression() {
			if (!variable()) return false;
			if (!(lexer.lookAhead().matches("<|>"))) return false;

            lexer.read();

			if (!variable()) return false;

			return true;
		}

		// <term> -> <factor> {(*|/) <factor>}
		private boolean term() {
			if (!factor()) return false;

			// * is also a key symbol in regex
			while (lexer.lookAhead().matches("\\*|/")) {
				lexer.read();

				if (!factor()) return false;
			}

			return true;
		}

		// <factor> -> <identifier> | <int_const> | <expr>
		private boolean factor() {
			if (!identifier()) {
				if (!integerConstant()) {
					if (!lexer.lookAhead().equals("(")) return false;

					lexer.read();

					if (!expression()) return false;

					if (!lexer.lookAhead().equals(")")) return false;

					lexer.read();
				}

				return true;
			}

			return true;
		}

		// <var> -> <identifier>
		private boolean variable() {
			if (!identifier()) return false;

			return true;
		}

		private boolean identifier() {
			// Regular expression to determine valid identifier
			// [a-zA-Z] means starts with any letter
			// [a-zA-Z0-9]* means any amount of alphanumeric characters (0 - infinity)
			if (!lexer.lookAhead().matches("[a-zA-Z][a-zA-Z0-9]*") && !isReservedWord(lexer.lookAhead())) return false;

			lexer.read();

			return true;
		}

		// A keyword is a word that cannot be used as an identifier
		// This method is just as a safety measure and should be a feature of code parsers
		private boolean isReservedWord(String identifier) {
			// Regex will recognize these as valid identifiers when they really aren't
			final String[] reservedWords = {"program", "begin", "end", "if", "loop"};

			for (String rw: reservedWords) {
				// Identifier is invalid if it is any of the above
				if (identifier.toLowerCase().equals(rw)) return true;
			}

			return false;
		}

		// Maintaining consistency with methods for most symbols -- doesn't really need own method
		private boolean integerConstant() {
			// A regex to detect if string has only digits (i.e. is integer constant)
			// \\d is escape sequence for digit and .* means any amount
			if (!lexer.lookAhead().matches(".*\\d.*")) return false;

			lexer.read();

			return true;
		}
	}

	// Test method
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);

		System.out.print("\nEnter file name: ");
		String name = sc.next();

		File file = new File(name);

		Parser parser = new Parser(file);

		if (parser.isValidCode())
			System.out.println("\nThere are no syntax errors in the program.\n");
		else
			System.out.println("\nThe program contains one or more syntax errors.\n");
	}
}
