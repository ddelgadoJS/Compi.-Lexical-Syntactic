/*
 * @(#)Token.java                        2.1 2003/10/07
 *
 * Copyright (C) 1999, 2003 D.A. Watt and D.F. Brown
 * Dept. of Computing Science, University of Glasgow, Glasgow G12 8QQ Scotland
 * and School of Computer and Math Sciences, The Robert Gordon University,
 * St. Andrew Street, Aberdeen AB25 1HG, Scotland.
 * All rights reserved.
 *
 * This software is provided free for educational use only. It may
 * not be used for commercial purposes without the prior written permission
 * of the authors.
 */

package SyntacticAnalyzer;


final class Token extends Object {

  protected int kind;
  protected String spelling;
  protected SourcePosition position;

  public Token(int kind, String spelling, SourcePosition position) {

    if (kind == Token.IDENTIFIER) {
      int currentKind = firstReservedWord;
      boolean searching = true;

      while (searching) {
        int comparison = tokenTable[currentKind].compareTo(spelling);
        if (comparison == 0) {
          this.kind = currentKind;
          searching = false;
        } else if (comparison > 0 || currentKind == lastReservedWord) {
          this.kind = Token.IDENTIFIER;
          searching = false;
        } else {
          currentKind ++;
        }
      }
    } else
      this.kind = kind;

    this.spelling = spelling;
    this.position = position;

  }

  public static String spell (int kind) {
    return tokenTable[kind];
  }

  public String toString() {
    return "Kind=" + kind + ", spelling=" + spelling +
      ", position=" + position;
  }

  // Token classes...

  public static final int

    // literals, identifiers, operators...
    INTLITERAL	= 0,
    CHARLITERAL	= 1,
    IDENTIFIER	= 2,
    OPERATOR	= 3,

    // reserved words - must be in alphabetical order...
    ARRAY		= 4,
    BEGIN		= 5,
    CONST		= 6,
    DO			= 7,
    ELSE		= 8,
    ELSEIF              = 9, // se agrega elseif
    END			= 10,
    FOR                 = 11, //se agrego for
    FUNC		= 12,
    IF			= 13,
    IN			= 14,
    LET			= 15,
    LOOP                = 16, // Se agrego loop
    NOTHING             = 17, // se agrego nothing
    OF			= 18,
    PROC		= 19,
    RECORD		= 20,
    THEN		= 21,
    TO                  = 22,
    TYPE		= 23,
    UNTIL               = 24, // Se agrego until      
    VAR			= 25,
    WHILE		= 26,

    // punctuation...
    DOT			= 27,
    COLON		= 28,
    SEMICOLON	        = 29,
    COMMA		= 30,
    BECOMES		= 31,
    IS			= 32,

    // brackets...
    LPAREN		= 33,
    RPAREN		= 34,
    LBRACKET	        = 35,
    RBRACKET	        = 36,
    LCURLY		= 37,
    RCURLY		= 38,
    // special tokens...
    EOT			= 39,
    ERROR		= 40;
  
  private static String[] tokenTable = new String[] {
    "<int>",
    "<char>",
    "<identifier>",
    "<operator>",
    "array",
    "begin",
    "const",
    "do",
    "else",
    "elseif", //elseif
    "end",
    "for", //for
    "func",
    "if",
    "in",
    "let",
    "loop", //loop
    "nothing", //nothiny
    "of",
    "proc",
    "record",
    "then",
    "to",
    "type",
    "var",
    "while",
    ".",
    ":",
    ";",
    ",",
    ":=",
    "~",
    "(",
    ")",
    "[",
    "]",
    "{",
    "}",
    "",
    "<error>"
  };

  private final static int	firstReservedWord = Token.ARRAY,
  				lastReservedWord  = Token.WHILE;

}
