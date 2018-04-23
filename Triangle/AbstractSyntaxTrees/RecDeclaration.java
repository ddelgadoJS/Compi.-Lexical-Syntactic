package Triangle.AbstractSyntaxTrees;

import Triangle.SyntacticAnalyzer.SourcePosition;

public class RecDeclaration extends Declaration {

  public RecDeclaration (Declaration dAST,
                    SourcePosition thePosition) {
    super (thePosition);
    D = dAST;
  }

  public Declaration D;

    @Override
    public Object visit(Visitor v, Object o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}