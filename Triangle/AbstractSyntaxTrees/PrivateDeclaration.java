package Triangle.AbstractSyntaxTrees;

import Triangle.SyntacticAnalyzer.SourcePosition;

public class PrivateDeclaration extends Declaration {

  public PrivateDeclaration (Declaration dAST1, Declaration dAST2,
                    SourcePosition thePosition) {
    super (thePosition);
    D_1 = iAST;
    D_2 = eAST;
  }

  public Declaration D_1;
  public Declaration D_2;
}