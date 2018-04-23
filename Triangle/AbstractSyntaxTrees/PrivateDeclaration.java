package Triangle.AbstractSyntaxTrees;

import Triangle.SyntacticAnalyzer.SourcePosition;

public class PrivateDeclaration extends Declaration {

  public PrivateDeclaration (Declaration dAST1, Declaration dAST2,
                    SourcePosition thePosition) {
    super (thePosition);
    D_1 = dAST1;
    D_2 = dAST2;
  }

  public Declaration D_1;
  public Declaration D_2;

    @Override
    public Object visit(Visitor v, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}