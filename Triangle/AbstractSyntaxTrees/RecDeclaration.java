package Triangle.AbstractSyntaxTrees;

import Triangle.SyntacticAnalyzer.SourcePosition;

public class RecDeclaration extends Declaration {

  public RecDeclaration (Declaration dAST,
                    SourcePosition thePosition) {
    super (thePosition);
    D = dAST;
  }

  public Declaration D;
}