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

package Triangle.SyntacticAnalyzer;


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
    INTLITERAL  = 0,
    CHARLITERAL = 1,
    IDENTIFIER  = 2,
    OPERATOR    = 3,

    // reserved words - must be in alphabetical order...
    ARRAY       = 4,
    //BEGIN       = 5,// begin deleted, index shifted
    CONST       = 5,
    DO          = 6,
    ELSE        = 7,
    ELSIF       = 8,  // ELSIF added.
    END         = 9,
    FOR         = 10, // FOR added.
    FUNC        = 11,
    IF          = 12,
    IN          = 13,
    LET         = 14,
    LOOP        = 15, // LOOP added.
    NOTHING     = 16, // NOTHING added.
    OF          = 17,
    PROC        = 18,
    RECORD      = 19,
    THEN        = 20,
    TYPE        = 21,
    UNTIL       = 22, // UNTIL added.      
    VAR         = 23,
    WHILE       = 24,
    AND         = 25, // AND added.
    PRIVATE     = 26, // PRIVATE added.
    REC         = 27, // AND added.
    TO          = 28, // TO added.
  
    // punctuation...
    DOT         = 29,
    TWO_DOTS    = 30, // TWO_DOTS added.
    COLON       = 31,
    SEMICOLON   = 32,
    COMMA       = 33,
    BECOMES     = 34,
    IS          = 35,

    // brackets...
    LPAREN      = 36,
    RPAREN      = 37,
    LBRACKET    = 38,
    RBRACKET    = 39,
    LCURLY      = 40,
    RCURLY      = 41,
    // special tokens...
    EOT         = 42,
    ERROR       = 43;
  
  private static String[] tokenTable = new String[] {
    "<int>",
    "<char>",
    "<identifier>",
    "<operator>",
    "array",
    //"begin",  // begin deleted
    "const",
    "do",
    "else",
    "elsif", //elsif
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
    "and", // and
    "private", // private
    "rec", // rec
    "to", // to
    ".",
    "..", // two_dots
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

  private final static int  firstReservedWord = Token.ARRAY,
                lastReservedWord  = Token.WHILE;

}
