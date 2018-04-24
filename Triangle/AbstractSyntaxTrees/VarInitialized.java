package Triangle.AbstractSyntaxTrees;

import Triangle.SyntacticAnalyzer.SourcePosition;

public class VarInitialized extends Declaration {

    public VarInitialized(Identifier iAST, Expression eAST, SourcePosition thePosition) {
        super (thePosition);
        I = iAST;
        E = eAST;
    }

    public Identifier I;
    public Expression E;

    public Object visit(Visitor v, Object o) {
        return v.visitVarInitialized(this, o);
    }
}