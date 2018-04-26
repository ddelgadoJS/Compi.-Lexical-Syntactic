package Triangle.AbstractSyntaxTrees;

import Triangle.SyntacticAnalyzer.SourcePosition;

public class PrivateDeclaration extends Declaration {

  public PrivateDeclaration (Declaration dAST1, Declaration dAST2,
                    SourcePosition thePosition) {
    super (thePosition);
    D = dAST1;
    D2 = dAST2;
  }

  public Declaration D;
  public Declaration D2;

    @Override
    public Object visit(Visitor v, Object o) {
        return v.visitPrivateDeclaration(this,o);
    }
}