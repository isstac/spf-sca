package sidechannel.util;

import gov.nasa.jpf.symbc.numeric.*;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

/**
 * Created by bang on 12/13/16.
 *
 *
 *
 */
public class MathematicaTranslator extends ConstraintExpressionVisitor {

    public HashSet<String> setOfSymVar;
    public Stack<String> mathStack = new Stack<String>();

    public MathematicaTranslator(HashSet<String> set){
        setOfSymVar = set;
    }

    @Override
    public void preVisit(SymbolicInteger expr) {
        String name = cleanSymbol(expr.toString());
        //System.out.println("Encountered symbolic integer to push: " + name);
        setOfSymVar.add(name);
        mathStack.push(name);
        // System.out.println("\nCurrent Stack :");
        // System.out.println(mathStack);
        // System.out.println("Model Collector Model");
        // System.out.println(name + " : " + expr.solution + "\n");
    }

    @Override
    public void preVisit(IntegerConstant expr){
        // System.out.println("Encountered integer constant to push: " + expr);
        mathStack.push(String.valueOf(expr.value));
        // System.out.println("Current Stack :");
        //System.out.println(mathStack);

    }

    @Override
    public void postVisit(BinaryLinearIntegerExpression expr){
        // System.out.println("Post Encountered Linear Integer Expression: " + expr);
        // System.out.println("Expression operator: " + expr.getOp());
        // TODO Check for empty stack
        String arg2 = mathStack.pop();
        String arg1 = mathStack.pop();

        String result = "( " + arg1 + expr.getOp() + arg2 + ")";
        // System.out.println("Expression result to push: " + result);
        mathStack.push(result);

    }

    public String translate(Comparator comp){
        //System.out.println("translating comparator: " + comp);
        if(comp.equals(Comparator.EQ)) {
            return "Equal";
        }
        else if(comp.equals(Comparator.NE)){
            return "Unequal";
        }
        else if(comp.equals(Comparator.LE)){
            return "LessEqual";
        }
        else if(comp.equals(Comparator.GE)){
            return "GreaterEqual";
        }
        else if(comp.equals(Comparator.GT)){
            return "Greater";
        }
        else if(comp.equals(Comparator.LT)){
            return "Less";
        }
        else{
            return "Unknown comparator " + comp;
        }
    }

    public String translate(PathCondition pc){

        Constraint c = pc.header;

        Vector<String> mathConstraintStrings = new Vector<String> ();

        while(c != null){
            c.accept(this);
            // TODO Check for empty stack
            String arg2 = mathStack.pop();
            String arg1 = mathStack.pop();
            String result =  (translate(c.getComparator()) + "[" + arg1 + ", " + arg2 + "]").replace('{','[').replace('}',']');
            //result = result + " && " + translate(c.getComparator()) + "[" + arg1 + ", " + arg2 + "]";
            mathConstraintStrings.add(result);
            c = c.getTail();
        }

//        System.out.println(mathConstraintStrings);

        String translation = "";
        boolean firstTime = true;
        for( String s : mathConstraintStrings){
            if(firstTime) {
                translation = translation + s;
                firstTime = false;
            }
            else{
                translation = translation + " && " + s;
            }
        }

        //System.out.println(translation);
        return translation;
    }

    public String translate(Vector<PathCondition> pathConditions){
        String translation = "";
        boolean firstTime = true;
        for(PathCondition pc : pathConditions){
            if(firstTime){
                translation = translate(pc);
                firstTime = false;
            }
            else {
                translation = translation + " , " + translate(pc);
            }
        }
        return "PathConditions = {" + translation + "}";
    }

    private static String cleanSymbol(String str) {
        return str.replaceAll("\\[(.*?)\\]", ""); // remove e.g. [-1000000]
    }

    public Set<String> getListOfVariables(){
        return setOfSymVar;
    }

    public int size(){
        return setOfSymVar.size();
    }
}



//    public String translate(Constraint c){
//        if(c instanceof LinearIntegerConstraint){
//            System.out.println("Handle Linear Integer Constraint");
//            System.out.println(c.getComparator());
//            System.out.println(c.getLeft() + " :: " + c.getLeft().getClass());
//            System.out.println(c.getRight() + " :: " + c.getRight().getClass());
//            return translate(c.getComparator()) + "[" + "arg1" + "," + "arg2" + "]";
//        }
//        else{
//            return "Constraint " + c + "of type " + c.getClass() + " not handled";
//        }
//
//    }


//    @Override
//    public void postVisit(LinearIntegerConstraint c){
//        System.out.println("post Encountered Linear Integer Constraint: " + c);
//
//    }


//    public String translate(SymbolicInteger si){
//        return si.getName();
//    }
//
//    public String translate(IntegerConstant ic){
//        return ic.value + "";
//    }

//    @Override
//    public void preVisit(LinearIntegerConstraint c){
//        System.out.println("Encountered Linear Integer Constraint: " + c);
//    }

//    @Override
//    public void preVisit(BinaryLinearIntegerExpression expr){
//        System.out.println("Pre Encountered Linear Integer Expression: " + expr);
//        System.out.println("Expression operator: " + expr.getOp());
//    }