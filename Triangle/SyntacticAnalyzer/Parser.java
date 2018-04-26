/*
 * @(#)Parser.java                        2.1 2003/10/07
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

import Triangle.ErrorReporter;
import Triangle.AbstractSyntaxTrees.*;

public class Parser {

  private Scanner lexicalAnalyser;
  private ErrorReporter errorReporter;
  private Token currentToken;
  private SourcePosition previousTokenPosition;

  public Parser(Scanner lexer, ErrorReporter reporter) {
    lexicalAnalyser = lexer;
    errorReporter = reporter;
    previousTokenPosition = new SourcePosition();
  }

  // accept checks whether the current token matches tokenExpected.
  // If so, fetches the next token.
  // If not, reports a syntactic error.

  void accept(int tokenExpected) throws SyntaxError {
    if (currentToken.kind == tokenExpected) {
      previousTokenPosition = currentToken.position;
      currentToken = lexicalAnalyser.scan();
    } else {
      syntacticError("\"%\" expected here", Token.spell(tokenExpected));
    }
  }

  void acceptIt() {
    previousTokenPosition = currentToken.position;
    currentToken = lexicalAnalyser.scan();
  }

  // start records the position of the start of a phrase.
  // This is defined to be the position of the first
  // character of the first token of the phrase.

  void start(SourcePosition position) {
    position.start = currentToken.position.start;
  }

  // finish records the position of the end of a phrase.
  // This is defined to be the position of the last
  // character of the last token of the phrase.

  void finish(SourcePosition position) {
    position.finish = previousTokenPosition.finish;
  }

  void syntacticError(String messageTemplate, String tokenQuoted) throws SyntaxError {
    SourcePosition pos = currentToken.position;
    errorReporter.reportError(messageTemplate, tokenQuoted, pos);
    throw (new SyntaxError());
  }

  ///////////////////////////////////////////////////////////////////////////////
  //
  // PROGRAMS
  //
  ///////////////////////////////////////////////////////////////////////////////

  public Program parseProgram() {

    Program programAST = null;

    previousTokenPosition.start = 0;
    previousTokenPosition.finish = 0;
    currentToken = lexicalAnalyser.scan();

    try {
      Command cAST = parseCommand();
      programAST = new Program(cAST, previousTokenPosition);
      if (currentToken.kind != Token.EOT) {
        syntacticError("\"%\" not expected after end of program", currentToken.spelling);
      }
    } catch (SyntaxError s) {
      return null;
    }
    return programAST;
  }

  ///////////////////////////////////////////////////////////////////////////////
  //
  // LITERALS
  //
  ///////////////////////////////////////////////////////////////////////////////

  // parseIntegerLiteral parses an integer-literal, and constructs
  // a leaf AST to represent it.

  IntegerLiteral parseIntegerLiteral() throws SyntaxError {
    IntegerLiteral IL = null;

    if (currentToken.kind == Token.INTLITERAL) {
      previousTokenPosition = currentToken.position;
      String spelling = currentToken.spelling;
      IL = new IntegerLiteral(spelling, previousTokenPosition);
      currentToken = lexicalAnalyser.scan();
    } else {
      IL = null;
      syntacticError("integer literal expected here", "");
    }
    return IL;
  }

  // parseCharacterLiteral parses a character-literal, and constructs a leaf
  // AST to represent it.

  CharacterLiteral parseCharacterLiteral() throws SyntaxError {
    CharacterLiteral CL = null;

    if (currentToken.kind == Token.CHARLITERAL) {
      previousTokenPosition = currentToken.position;
      String spelling = currentToken.spelling;
      CL = new CharacterLiteral(spelling, previousTokenPosition);
      currentToken = lexicalAnalyser.scan();
    } else {
      CL = null;
      syntacticError("character literal expected here", "");
    }
    return CL;
  }

  // parseIdentifier parses an identifier, and constructs a leaf AST to
  // represent it.

  Identifier parseIdentifier() throws SyntaxError {
    Identifier I = null;

    if (currentToken.kind == Token.IDENTIFIER) {
      previousTokenPosition = currentToken.position;
      String spelling = currentToken.spelling;
      I = new Identifier(spelling, previousTokenPosition);
      currentToken = lexicalAnalyser.scan();
    } else {
      I = null;
      syntacticError("identifier expected here", "");
    }
    return I;
  }

  // parseOperator parses an operator, and constructs a leaf AST to
  // represent it.

  Operator parseOperator() throws SyntaxError {
    Operator O = null;

    if (currentToken.kind == Token.OPERATOR) {
      previousTokenPosition = currentToken.position;
      String spelling = currentToken.spelling;
      O = new Operator(spelling, previousTokenPosition);
      currentToken = lexicalAnalyser.scan();
    } else {
      O = null;
      syntacticError("operator expected here", "");
    }
    return O;
  }

  ///////////////////////////////////////////////////////////////////////////////
  //
  // COMMANDS
  //
  ///////////////////////////////////////////////////////////////////////////////

  // parseCommand parses the command, and constructs an AST
  // to represent its phrase structure.

  Command parseCommand() throws SyntaxError {
    Command commandAST = null; // in case there's a syntactic error

    SourcePosition commandPos = new SourcePosition();

    start(commandPos);
    commandAST = parseSingleCommand();
    while (currentToken.kind == Token.SEMICOLON) {
      acceptIt();
      Command c2AST = parseSingleCommand();
      finish(commandPos);
      commandAST = new SequentialCommand(commandAST, c2AST, commandPos);
    }
    return commandAST;
  }

  Command parseSingleCommand() throws SyntaxError {
    Command commandAST = null; // in case there's a syntactic error

    SourcePosition commandPos = new SourcePosition();
    start(commandPos);

    switch (currentToken.kind) {

    case Token.IDENTIFIER: {
      Identifier iAST = parseIdentifier();
      if (currentToken.kind == Token.LPAREN) {
        acceptIt();
        ActualParameterSequence apsAST = parseActualParameterSequence();
        accept(Token.RPAREN);
        finish(commandPos);
        commandAST = new CallCommand(iAST, apsAST, commandPos);

      } else {

        Vname vAST = parseRestOfVname(iAST);
        accept(Token.BECOMES);
        Expression eAST = parseExpression();
        finish(commandPos);
        commandAST = new AssignCommand(vAST, eAST, commandPos);
      }
    }
      break;

    //Se agrega case Token.LOOP para loopCommand, casos de while, until, do y for
    case Token.LOOP: {
      acceptIt();
      switch (currentToken.kind) { // switch para casos en el LOOP

      case Token.WHILE: { // caso para while
        acceptIt(); // se acepta el token
        Expression eAST = parseExpression(); // parse para la expresion
        accept(Token.DO); // para comando definido se espera un do 
        Command cAST = parseCommand(); // parse para el comando
        accept(Token.END); // para comando definido se espera un end para indicar final del comando
        finish(commandPos);
        commandAST = new WhileCommand(eAST, cAST, commandPos); // se genera estructura para AST del tipo revisado
      }
        break;

      case Token.UNTIL: { // caso de UNTIL
        acceptIt(); // se acepta el token UNTIL
        Expression eAST = parseExpression(); // parse para expresion
        accept(Token.DO); // el comando espera un do 
        Command cAST = parseCommand(); // parse para comando
        accept(Token.END); // el token end indica el final del comando
        finish(commandPos);
        commandAST = new UntilCommand(eAST, cAST, commandPos); // se genera estructura para AST del tipo revisado
      }
        break;

      case Token.DO: { // caso de do
        acceptIt(); // se acepta token do
        Command cAST = parseCommand(); // parse para el comando

        // en este caso existen dos formas para el loop do
        if (currentToken.kind == Token.WHILE) { // caso de do while
          acceptIt(); 
          Expression eAST = parseExpression();
          accept(Token.END);
          finish(commandPos);
          commandAST = new DoWhileCommand(cAST, eAST, commandPos); // estructura para AST de DoWhile
        } else if (currentToken.kind == Token.UNTIL) { // caso de do until
          acceptIt();
          Expression eAST = parseExpression();
          accept(Token.END);
          finish(commandPos);
          commandAST = new DoUntilCommand(cAST, eAST, commandPos); // estructura para AST de DoUntil
        } else {
          syntacticError("\"%\" cannot create a doCommand", currentToken.spelling); // se genera error al encontrar otro token
        }
        
      }
        break;

      case Token.FOR: { // caso de loop for
        acceptIt();
        Identifier iAST = parseIdentifier(); // parse para identificador
        accept(Token.BECOMES); // se espera token :=
        Expression eAST = parseExpression(); // parse para primera expresion
        accept(Token.TO); // se espera token to
        Expression eAST1 = parseExpression(); // parse para segunda expresion
        accept(Token.DO); // se espera token do
        Command cAST = parseCommand(); // parse para command
        accept(Token.END); // se espera token end para indicar final del comando
        finish(commandPos);
        commandAST = new ForCommand(iAST, eAST, eAST1, cAST, commandPos); // se genera estructura para AST forCommand
      }
        break;
       // error para loop command al no encontrar ningunos de los tokens esperados
      default:
        syntacticError("\"%\" cannot create a loopCommand", currentToken.spelling);
        break;
      }
    }
      break;

    case Token.LET: { // caso para single-command let
      acceptIt();
      Declaration dAST = parseDeclaration(); // parse para declaracion
      accept(Token.IN); // se acepta el token in
      Command cAST = parseCommand(); // parser para comando 
      accept(Token.END); // se acepta token end que indica el final del comando 
      finish(commandPos);
      commandAST = new LetCommand(dAST, cAST, commandPos); //  se crea estructura para letCommand
    }
      break;
    //"if" Expression "then" Command ("elsif" Expression "then" Command)*"else" Command "end"
    case Token.IF: // caso del if command
      {
       Command c2AST = null;
       acceptIt(); // se acepta token if
       Expression eAST = parseExpression(); // parse para la expresion
       accept(Token.THEN); // se acepta token then
       Command c1AST = parseCommand(); // parse para comando
       
       // en caso de tener elsif se llama a funcion recursuva que genera estructura para el AST
       if(currentToken.kind == Token.ELSIF){
          c2AST = parserElsIf(); // se genera estructura del elsif
       }
       else if (currentToken.kind == Token.ELSE){
          acceptIt(); // 
          c2AST = parseCommand();
       }else {
           syntacticError("\"%\" cannot create a ifCommand", currentToken.spelling); // se reporta error de sintaxis
       }
       
       accept(Token.END); // se acpeta token end para final del comando
       finish(commandPos);
       commandAST = new IfCommand(eAST, c1AST,c2AST, commandPos); // se crea estructura para ifCommand
      }
      break;
      
    //Eliminar "begin" Command "end"

    /*   case Token.BEGIN:
      acceptIt();
      commandAST = parseCommand();
      accept(Token.END);
      break;
    //Eliminar "let" Declaration "in" single-Command
    
    case Token.LET:
      {
        acceptIt();
        Declaration dAST = parseDeclaration();
        accept(Token.IN);
        Command cAST = parseSingleCommand();
        finish(commandPos);
        commandAST = new LetCommand(dAST, cAST, commandPos);
      }
      break;
    // Eliminar "if" Expression "then" single-Command "else" | "while" Expression "do" single-Command
    case Token.IF:
      {
        acceptIt();
        Expression eAST = parseExpression();
        accept(Token.THEN);
        Command c1AST = parseSingleCommand();
        accept(Token.ELSE);
        Command c2AST = parseSingleCommand();
        finish(commandPos);
        commandAST = new IfCommand(eAST, c1AST, c2AST, commandPos);
      }
      break;
    */
    case Token.NOTHING: // caso para comando nothing
    {
        acceptIt(); // se acepta el token
        finish(commandPos);
        commandAST = new NothingCommand(commandPos); // crea el comando nothing
    }

    case Token.SEMICOLON:
    case Token.END:
    case Token.ELSE:
    case Token.IN:
    case Token.EOT:
     
      finish(commandPos);
      commandAST = new NothingCommand(commandPos); // se cambia emptyCommand por nothingCommand
      break;

    default:
      syntacticError("\"%\" cannot start a command", currentToken.spelling);
      break;

    }

    return commandAST;
  }
  
  // Recursion of IF
  Command parserElsIf() throws SyntaxError{
        SourcePosition commandPos = new SourcePosition();
        
        start(commandPos);
        Command commandAST = null;
       //Al menos una vez se cumple
        accept(Token.ELSIF);
        Expression eAST2= parseExpression();
        accept(Token.THEN);
        Command cAST2 = parseCommand();
        finish(commandPos);
              
        if ( currentToken.kind == Token.ELSIF ) {
              commandAST = new ElsIfCommand(eAST2, cAST2, parserElsIf(), commandPos);
          } else {
              accept(Token.ELSE);
              Command elseCommand = parseCommand();
              commandAST = new ElsIfCommand(eAST2, cAST2, elseCommand , commandPos);
          }
      return commandAST;
  }
  ///////////////////////////////////////////////////////////////////////////////
  //
  // EXPRESSIONS
  //
  ///////////////////////////////////////////////////////////////////////////////

  Expression parseExpression() throws SyntaxError {
    Expression expressionAST = null; // in case there's a syntactic error

    SourcePosition expressionPos = new SourcePosition();

    start(expressionPos);

    switch (currentToken.kind) {

    case Token.LET: {
      acceptIt();
      Declaration dAST = parseDeclaration();
      accept(Token.IN);
      Expression eAST = parseExpression();
      finish(expressionPos);
      expressionAST = new LetExpression(dAST, eAST, expressionPos);
    }
      break;

    case Token.IF: {
      acceptIt();
      Expression e1AST = parseExpression();
      accept(Token.THEN);
      Expression e2AST = parseExpression();
      accept(Token.ELSE);
      Expression e3AST = parseExpression();
      finish(expressionPos);
      expressionAST = new IfExpression(e1AST, e2AST, e3AST, expressionPos);
    }
      break;

    default:
      expressionAST = parseSecondaryExpression();
      break;
    }
    return expressionAST;
  }

  Expression parseSecondaryExpression() throws SyntaxError {
    Expression expressionAST = null; // in case there's a syntactic error

    SourcePosition expressionPos = new SourcePosition();
    start(expressionPos);

    expressionAST = parsePrimaryExpression();
    while (currentToken.kind == Token.OPERATOR) {
      Operator opAST = parseOperator();
      Expression e2AST = parsePrimaryExpression();
      expressionAST = new BinaryExpression(expressionAST, opAST, e2AST, expressionPos);
    }
    return expressionAST;
  }

  Expression parsePrimaryExpression() throws SyntaxError {
    Expression expressionAST = null; // in case there's a syntactic error

    SourcePosition expressionPos = new SourcePosition();
    start(expressionPos);

    switch (currentToken.kind) {

    case Token.INTLITERAL: {
      IntegerLiteral ilAST = parseIntegerLiteral();
      finish(expressionPos);
      expressionAST = new IntegerExpression(ilAST, expressionPos);
    }
      break;

    case Token.CHARLITERAL: {
      CharacterLiteral clAST = parseCharacterLiteral();
      finish(expressionPos);
      expressionAST = new CharacterExpression(clAST, expressionPos);
    }
      break;

    case Token.LBRACKET: {
      acceptIt();
      ArrayAggregate aaAST = parseArrayAggregate();
      accept(Token.RBRACKET);
      finish(expressionPos);
      expressionAST = new ArrayExpression(aaAST, expressionPos);
    }
      break;

    case Token.LCURLY: {
      acceptIt();
      RecordAggregate raAST = parseRecordAggregate();
      accept(Token.RCURLY);
      finish(expressionPos);
      expressionAST = new RecordExpression(raAST, expressionPos);
    }
      break;

    case Token.IDENTIFIER: {
      Identifier iAST = parseIdentifier();
      if (currentToken.kind == Token.LPAREN) {
        acceptIt();
        ActualParameterSequence apsAST = parseActualParameterSequence();
        accept(Token.RPAREN);
        finish(expressionPos);
        expressionAST = new CallExpression(iAST, apsAST, expressionPos);

      } else {
        Vname vAST = parseRestOfVname(iAST);
        finish(expressionPos);
        expressionAST = new VnameExpression(vAST, expressionPos);
      }
    }
      break;

    case Token.OPERATOR: {
      Operator opAST = parseOperator();
      Expression eAST = parsePrimaryExpression();
      finish(expressionPos);
      expressionAST = new UnaryExpression(opAST, eAST, expressionPos);
    }
      break;

    case Token.LPAREN:
      acceptIt();
      expressionAST = parseExpression();
      accept(Token.RPAREN);
      break;

    default:
      syntacticError("\"%\" cannot start an expression", currentToken.spelling);
      break;

    }
    return expressionAST;
  }

  RecordAggregate parseRecordAggregate() throws SyntaxError {
    RecordAggregate aggregateAST = null; // in case there's a syntactic error

    SourcePosition aggregatePos = new SourcePosition();
    start(aggregatePos);

    Identifier iAST = parseIdentifier();
    accept(Token.IS);
    Expression eAST = parseExpression();

    if (currentToken.kind == Token.COMMA) {
      acceptIt();
      RecordAggregate aAST = parseRecordAggregate();
      finish(aggregatePos);
      aggregateAST = new MultipleRecordAggregate(iAST, eAST, aAST, aggregatePos);
    } else {
      finish(aggregatePos);
      aggregateAST = new SingleRecordAggregate(iAST, eAST, aggregatePos);
    }
    return aggregateAST;
  }

  ArrayAggregate parseArrayAggregate() throws SyntaxError {
    ArrayAggregate aggregateAST = null; // in case there's a syntactic error

    SourcePosition aggregatePos = new SourcePosition();
    start(aggregatePos);

    Expression eAST = parseExpression();
    if (currentToken.kind == Token.COMMA) {
      acceptIt();
      ArrayAggregate aAST = parseArrayAggregate();
      finish(aggregatePos);
      aggregateAST = new MultipleArrayAggregate(eAST, aAST, aggregatePos);
    } else {
      finish(aggregatePos);
      aggregateAST = new SingleArrayAggregate(eAST, aggregatePos);
    }
    return aggregateAST;
  }

  ///////////////////////////////////////////////////////////////////////////////
  //
  // VALUE-OR-VARIABLE NAMES
  //
  ///////////////////////////////////////////////////////////////////////////////

  Vname parseVname() throws SyntaxError {
    Vname vnameAST = null; // in case there's a syntactic error
    Identifier iAST = parseIdentifier();
    vnameAST = parseRestOfVname(iAST);
    return vnameAST;
  }

  Vname parseRestOfVname(Identifier identifierAST) throws SyntaxError {
    SourcePosition vnamePos = new SourcePosition();
    vnamePos = identifierAST.position;
    Vname vAST = new SimpleVname(identifierAST, vnamePos);

    while (currentToken.kind == Token.DOT || currentToken.kind == Token.LBRACKET) {

      if (currentToken.kind == Token.DOT) {
        acceptIt();
        Identifier iAST = parseIdentifier();
        vAST = new DotVname(vAST, iAST, vnamePos);
      } else {
        acceptIt();
        Expression eAST = parseExpression();
        accept(Token.RBRACKET);
        finish(vnamePos);
        vAST = new SubscriptVname(vAST, eAST, vnamePos);
      }
    }
    return vAST;
  }

  ///////////////////////////////////////////////////////////////////////////////
//
// DECLARATIONS
//
///////////////////////////////////////////////////////////////////////////////

  // Updated Declaration
  // Old: Declaration ::= single-Declaration (; single-Declaration)*
  // New: Declaration ::= compound-Declaration (";" compound-Declaration)*
  Declaration parseDeclaration() throws SyntaxError {
    Declaration declarationAST = null; // in case there's a syntactic error

    SourcePosition declarationPos = new SourcePosition();
    start(declarationPos);
    declarationAST = parseCompoundDeclaration();
    while (currentToken.kind == Token.SEMICOLON) {
      acceptIt();
      Declaration d2AST = parseCompoundDeclaration();
      finish(declarationPos);
      declarationAST = new SequentialDeclaration(declarationAST, d2AST,
        declarationPos);
    }
    return declarationAST;
  }

  Declaration parseSingleDeclaration() throws SyntaxError {
    Declaration declarationAST = null; // in case there's a syntactic error

    SourcePosition declarationPos = new SourcePosition();
    start(declarationPos);

    switch (currentToken.kind) {

    case Token.CONST:
      {
        acceptIt();
        Identifier iAST = parseIdentifier();
        accept(Token.IS);
        Expression eAST = parseExpression();
        finish(declarationPos);
        declarationAST = new ConstDeclaration(iAST, eAST, declarationPos);
      }
      break;

    case Token.VAR:
      {
        acceptIt();
        Identifier iAST = parseIdentifier();

        if (currentToken.kind == Token.COLON)
        {
          acceptIt();
          TypeDenoter tAST = parseTypeDenoter();
          finish(declarationPos);
          declarationAST = new VarDeclaration(iAST, tAST, declarationPos);
        } else if (currentToken.kind == Token.BECOMES)
        {
          acceptIt();
          Expression eAST = parseExpression();
          finish(declarationPos);
          declarationAST = new VarInitialized(iAST, eAST, declarationPos);
        }
      }
      break;

    case Token.PROC:
      {
        acceptIt();
        Identifier iAST = parseIdentifier();
        accept(Token.LPAREN);
        FormalParameterSequence fpsAST = parseFormalParameterSequence();
        accept(Token.RPAREN);
        accept(Token.IS);
        Command cAST = parseCommand();
        accept(Token.END);
        finish(declarationPos);
        declarationAST = new ProcDeclaration(iAST, fpsAST, cAST, declarationPos);
      }
      break;

    case Token.FUNC:
      {
        acceptIt();
        Identifier iAST = parseIdentifier();
        accept(Token.LPAREN);
        FormalParameterSequence fpsAST = parseFormalParameterSequence();
        accept(Token.RPAREN);
        accept(Token.COLON);
        TypeDenoter tAST = parseTypeDenoter();
        accept(Token.IS);
        Expression eAST = parseExpression();
        finish(declarationPos);
        declarationAST = new FuncDeclaration(iAST, fpsAST, tAST, eAST,
          declarationPos);
      }
      break;

    case Token.TYPE:
      {
        acceptIt();
        Identifier iAST = parseIdentifier();
        accept(Token.IS);
        TypeDenoter tAST = parseTypeDenoter();
        finish(declarationPos);
        declarationAST = new TypeDeclaration(iAST, tAST, declarationPos);
      }
      break;

    default:
      syntacticError("\"%\" cannot start a declaration",
        currentToken.spelling);
      break;

    }
    return declarationAST;
  }

  // New rule.
  Declaration parseCompoundDeclaration() throws SyntaxError {
    Declaration declarationAST = null; // in case there's a syntactic error

    SourcePosition declarationPos = new SourcePosition();
    start(declarationPos);
    
    switch (currentToken.kind) {

    case Token.CONST:
    case Token.VAR:
    case Token.PROC:
    case Token.FUNC:
    case Token.TYPE:
      declarationAST = parseSingleDeclaration();
      break;

    case Token.REC:
    {
      acceptIt();
      Declaration dAST = parseProcFuncs();
      accept(Token.END);
      finish(declarationPos);
      declarationAST = new RecDeclaration(dAST, declarationPos);
    }
    break;

    case Token.PRIVATE:
      {
        acceptIt();
        Declaration dAST1 = parseDeclaration();
        accept(Token.IN);
        Declaration dAST2 = parseDeclaration();
        accept(Token.END);
        finish(declarationPos);
        declarationAST = new PrivateDeclaration(dAST1, dAST2, declarationPos);
      }
      break;
    
    default:
      syntacticError("\"%\" cannot start a declaration",
        currentToken.spelling);
      break;

    }
    return declarationAST;
  }

  // New rule.
  Declaration parseProcFunc() throws SyntaxError {
    Declaration declarationAST = null; // in case there's a syntactic error

    SourcePosition declarationPos = new SourcePosition();
    start(declarationPos);
    
    switch (currentToken.kind) {

    case Token.PROC:
    {
      acceptIt();
      // accept(Token.IDENTIFIER); // Do I have to accept it?
      Identifier iAST = parseIdentifier();
      accept(Token.LPAREN);
      FormalParameterSequence fpsAST = parseFormalParameterSequence();
      accept(Token.RPAREN);
      accept(Token.IS);
      Command cAST = parseSingleCommand();;
      finish(declarationPos);
      declarationAST = new ProcDeclaration(iAST, fpsAST, cAST, declarationPos);
    }
    break;

    case Token.FUNC:
    {
      acceptIt();
      // accept(Token.IDENTIFIER); // Do I have to accept it?
      Identifier iAST = parseIdentifier();
      accept(Token.LPAREN);
      FormalParameterSequence fpsAST = parseFormalParameterSequence();
      accept(Token.RPAREN);
      accept(Token.COLON);
      TypeDenoter tAST = parseTypeDenoter();
      accept(Token.IS);
      Expression eAST = parseExpression();
      finish(declarationPos);
      declarationAST = new FuncDeclaration(iAST, fpsAST, tAST, eAST,
        declarationPos);
    }
    break;

    default:
      syntacticError("\"%\" cannot start a declaration",
        currentToken.spelling);
      break;

    }
    return declarationAST;
  }

  // New rule.
  Declaration parseProcFuncs() throws SyntaxError {
    Declaration declarationAST = null; // in case there's a syntactic error

    SourcePosition declarationPos = new SourcePosition();
    start(declarationPos);
    declarationAST = parseProcFunc();

    // The first and & the second ProcFunc are needed at least one time.
    do {
      accept(Token.AND);
      Declaration dAST2 = parseProcFunc();
      finish(declarationPos);
      declarationAST = new ProcFuncs(declarationAST, dAST2, declarationPos);
    } while (currentToken.kind == Token.AND);

    return declarationAST;
  }

///////////////////////////////////////////////////////////////////////////////
//
// PARAMETERS
//
///////////////////////////////////////////////////////////////////////////////

  FormalParameterSequence parseFormalParameterSequence() throws SyntaxError {
    FormalParameterSequence formalsAST;

    SourcePosition formalsPos = new SourcePosition();

    start(formalsPos);
    if (currentToken.kind == Token.RPAREN) {
      finish(formalsPos);
      formalsAST = new EmptyFormalParameterSequence(formalsPos);

    } else {
      formalsAST = parseProperFormalParameterSequence();
    }
    return formalsAST;
  }

  FormalParameterSequence parseProperFormalParameterSequence() throws SyntaxError {
    FormalParameterSequence formalsAST = null; // in case there's a syntactic error;

    SourcePosition formalsPos = new SourcePosition();
    start(formalsPos);
    FormalParameter fpAST = parseFormalParameter();
    if (currentToken.kind == Token.COMMA) {
      acceptIt();
      FormalParameterSequence fpsAST = parseProperFormalParameterSequence();
      finish(formalsPos);
      formalsAST = new MultipleFormalParameterSequence(fpAST, fpsAST, formalsPos);

    } else {
      finish(formalsPos);
      formalsAST = new SingleFormalParameterSequence(fpAST, formalsPos);
    }
    return formalsAST;
  }

  FormalParameter parseFormalParameter() throws SyntaxError {
    FormalParameter formalAST = null; // in case there's a syntactic error;

    SourcePosition formalPos = new SourcePosition();
    start(formalPos);

    switch (currentToken.kind) {

    case Token.IDENTIFIER: {
      Identifier iAST = parseIdentifier();
      accept(Token.COLON);
      TypeDenoter tAST = parseTypeDenoter();
      finish(formalPos);
      formalAST = new ConstFormalParameter(iAST, tAST, formalPos);
    }
      break;

    case Token.VAR: {
      acceptIt();
      Identifier iAST = parseIdentifier();
      accept(Token.COLON);
      TypeDenoter tAST = parseTypeDenoter();
      finish(formalPos);
      formalAST = new VarFormalParameter(iAST, tAST, formalPos);
    }
      break;

    case Token.PROC: {
      acceptIt();
      Identifier iAST = parseIdentifier();
      accept(Token.LPAREN);
      FormalParameterSequence fpsAST = parseFormalParameterSequence();
      accept(Token.RPAREN);
      finish(formalPos);
      formalAST = new ProcFormalParameter(iAST, fpsAST, formalPos);
    }
      break;

    case Token.FUNC: {
      acceptIt();
      Identifier iAST = parseIdentifier();
      accept(Token.LPAREN);
      FormalParameterSequence fpsAST = parseFormalParameterSequence();
      accept(Token.RPAREN);
      accept(Token.COLON);
      TypeDenoter tAST = parseTypeDenoter();
      finish(formalPos);
      formalAST = new FuncFormalParameter(iAST, fpsAST, tAST, formalPos);
    }
      break;

    default:
      syntacticError("\"%\" cannot start a formal parameter", currentToken.spelling);
      break;

    }
    return formalAST;
  }

  ActualParameterSequence parseActualParameterSequence() throws SyntaxError {
    ActualParameterSequence actualsAST;

    SourcePosition actualsPos = new SourcePosition();

    start(actualsPos);
    if (currentToken.kind == Token.RPAREN) {
      finish(actualsPos);
      actualsAST = new EmptyActualParameterSequence(actualsPos);

    } else {
      actualsAST = parseProperActualParameterSequence();
    }
    return actualsAST;
  }

  ActualParameterSequence parseProperActualParameterSequence() throws SyntaxError {
    ActualParameterSequence actualsAST = null; // in case there's a syntactic error

    SourcePosition actualsPos = new SourcePosition();

    start(actualsPos);
    ActualParameter apAST = parseActualParameter();
    if (currentToken.kind == Token.COMMA) {
      acceptIt();
      ActualParameterSequence apsAST = parseProperActualParameterSequence();
      finish(actualsPos);
      actualsAST = new MultipleActualParameterSequence(apAST, apsAST, actualsPos);
    } else {
      finish(actualsPos);
      actualsAST = new SingleActualParameterSequence(apAST, actualsPos);
    }
    return actualsAST;
  }

  ActualParameter parseActualParameter() throws SyntaxError {
    ActualParameter actualAST = null; // in case there's a syntactic error

    SourcePosition actualPos = new SourcePosition();

    start(actualPos);

    switch (currentToken.kind) {

    case Token.IDENTIFIER:
    case Token.INTLITERAL:
    case Token.CHARLITERAL:
    case Token.OPERATOR:
    case Token.LET:
    case Token.IF:
    case Token.LPAREN:
    case Token.LBRACKET:
    case Token.LCURLY: {
      Expression eAST = parseExpression();
      finish(actualPos);
      actualAST = new ConstActualParameter(eAST, actualPos);
    }
      break;

    case Token.VAR: {
      acceptIt();
      Vname vAST = parseVname();
      finish(actualPos);
      actualAST = new VarActualParameter(vAST, actualPos);
    }
      break;

    case Token.PROC: {
      acceptIt();
      Identifier iAST = parseIdentifier();
      finish(actualPos);
      actualAST = new ProcActualParameter(iAST, actualPos);
    }
      break;

    case Token.FUNC: {
      acceptIt();
      Identifier iAST = parseIdentifier();
      finish(actualPos);
      actualAST = new FuncActualParameter(iAST, actualPos);
    }
      break;

    default:
      syntacticError("\"%\" cannot start an actual parameter", currentToken.spelling);
      break;

    }
    return actualAST;
  }

  ///////////////////////////////////////////////////////////////////////////////
  //
  // TYPE-DENOTERS
  //
  ///////////////////////////////////////////////////////////////////////////////

  TypeDenoter parseTypeDenoter() throws SyntaxError {
    TypeDenoter typeAST = null; // in case there's a syntactic error
    SourcePosition typePos = new SourcePosition();

    start(typePos);

    switch (currentToken.kind) {

    case Token.IDENTIFIER: {
      Identifier iAST = parseIdentifier();
      finish(typePos);
      typeAST = new SimpleTypeDenoter(iAST, typePos);
    }
      break;

    case Token.ARRAY: {
      acceptIt();
      IntegerLiteral ilAST1 = parseIntegerLiteral();
      if (currentToken.kind == Token.OF) {
        acceptIt();
        TypeDenoter tAST = parseTypeDenoter();
        finish(typePos);
        typeAST = new ArrayTypeDenoter(ilAST1, tAST, typePos);
      } else if (currentToken.kind == Token.TWO_DOTS) {
        acceptIt();
        IntegerLiteral ilAST2 = parseIntegerLiteral();
        accept(Token.OF);
        TypeDenoter tAST = parseTypeDenoter();
        finish(typePos);
        typeAST = new ArrayTypeDenoterStatic(ilAST1, ilAST2, tAST, typePos);
      }
    }
      break;

    case Token.RECORD: {
      acceptIt();
      FieldTypeDenoter fAST = parseFieldTypeDenoter();
      accept(Token.END);
      finish(typePos);
      typeAST = new RecordTypeDenoter(fAST, typePos);
    }
      break;

    default:
      syntacticError("\"%\" cannot start a type denoter", currentToken.spelling);
      break;

    }
    return typeAST;
  }

  FieldTypeDenoter parseFieldTypeDenoter() throws SyntaxError {
    FieldTypeDenoter fieldAST = null; // in case there's a syntactic error

    SourcePosition fieldPos = new SourcePosition();

    start(fieldPos);
    Identifier iAST = parseIdentifier();
    accept(Token.COLON);
    TypeDenoter tAST = parseTypeDenoter();
    if (currentToken.kind == Token.COMMA) {
      acceptIt();
      FieldTypeDenoter fAST = parseFieldTypeDenoter();
      finish(fieldPos);
      fieldAST = new MultipleFieldTypeDenoter(iAST, tAST, fAST, fieldPos);
    } else {
      finish(fieldPos);
      fieldAST = new SingleFieldTypeDenoter(iAST, tAST, fieldPos);
    }
    return fieldAST;
  }
}
