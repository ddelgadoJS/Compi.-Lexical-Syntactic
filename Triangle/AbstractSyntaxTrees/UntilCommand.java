/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Usuario
 */
package Triangle.AbstractSyntaxTrees;

import Triangle.SyntacticAnalyzer.SourcePosition;

public class UntilCommand extends Command {

    
    public UntilCommand(Expression eAST, Command cAST, SourcePosition thePosition) {
        super(thePosition);
        E = eAST;
        C = cAST;
    }

    @Override
    public Object visit(Visitor v, Object o) {
        return v.visitUntilCommand(this, o);
    }
    
    public Expression E;
    public Command C;
}
