package Triangle.AbstractSyntaxTrees;

import Triangle.SyntacticAnalyzer.SourcePosition;

public class ArrayTypeDenoterStatic extends TypeDenoter {

    public ArrayTypeDenoterStatic(IntegerLiteral iAST1, IntegerLiteral iAST2,TypeDenoter tAST ,SourcePosition thePosition) {
        super (thePosition);
        IL = iAST1;
        IL2 = iAST2;
        T = tAST;
    }

    public Object visit(Visitor v, Object o) {
        return v.visitArrayTypeDenoterStatic(this, o);
    }

    public boolean equals(Object o){
        // This is missing.
        return true;
    }

    public IntegerLiteral IL;
    public IntegerLiteral IL2;
    public TypeDenoter T;

}