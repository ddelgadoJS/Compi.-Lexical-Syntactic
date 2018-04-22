package Triangle.AbstractSyntaxTrees;

import Triangle.SyntacticAnalyzer.SourcePosition;

public class ProcFuncs extends Terminal {

  public ProcFuncs (String theSpelling, SourcePosition thePosition) {
    super (theSpelling, thePosition);
    type = null;
    decl = null;
  }

  public Object visit(Visitor v, Object o) {
    return v.visitIdentifier(this, o);
  }

  public TypeDenoter type;
  public AST decl; // Either a Declaration or a FieldTypeDenoter
}
