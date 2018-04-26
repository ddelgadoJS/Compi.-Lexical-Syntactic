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
        return v.visitRecDeclaration(this,o);
    }
}