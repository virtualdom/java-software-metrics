import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.Math;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;

class MethodTransformVisitor extends MethodVisitor implements Opcodes {
    int numOperands = 0;
    int numOperators = 0;
    int numParams = 0;
    int numVars = 0;
    int lineCount = 0;
    int numCasts = 0;
    int numLoops = 0;
    int numRefVar = 0;
    String mName;
    String className;
    ArrayList<String> params;
    HashSet<String> classReferenced;
    HashSet<String> exceptions;
    HashSet<String> localMethodsCalled;
    HashSet<String> exterMethodsCalled;
    HashSet<Label> labelsVisited;
    HashSet<Integer> operators;
    HashMap<Integer, Integer> varReferences;

    public MethodTransformVisitor(final MethodVisitor mv, String methodname, String className) {
        super(ASM5, mv);
        this.mName=methodname;
        this.className = className;
        this.params = new ArrayList<String>();
        this.classReferenced = new HashSet<String>();
        this.exceptions = new HashSet<String>();
        this.localMethodsCalled = new HashSet<String>();
        this.exterMethodsCalled = new HashSet<String>();
        this.labelsVisited = new HashSet<Label>();
        this.operators = new HashSet<Integer>();
        this.varReferences = new HashMap<Integer, Integer>();
    }

    @Override
    public void visitParameter(String name, int access) {
        params.add(name);
        numParams++;
        super.visitParameter(name, access);
    }

    @Override
    public void visitEnd() {
        System.out.println("      \"arguments\": " + numParams + ",");            // Number of arguments
        System.out.println("      \"declarations\": " + numVars + ",");           // Number of declarations
        System.out.println("      \"lines\": " + lineCount + ",");                // Number of lines
        System.out.println("      \"operators\": " + numOperators + ",");         // Number of operators
        System.out.println("      \"operands\": " + numOperands + ",");           // Number of operands
        System.out.println("      \"lth\": " + checkNaN(LTH()) + ",");            // Halstead LTH
        System.out.println("      \"voc\": " + checkNaN(VOC()) + ",");            // Halstead VOC
        System.out.println("      \"dif\": " + checkNaN(DIF()) + ",");            // Halstead DIF
        System.out.println("      \"vol\": " + checkNaN(VOL()) + ",");            // Halstead VOL
        System.out.println("      \"eff\": " + checkNaN(EFF()) + ",");            // Halstead EFF
        System.out.println("      \"bug\": " + checkNaN(VOL()/3000.0) + ",");     // Halstead BUG
        System.out.println("      \"loops\": " + numLoops + ",");                 // Number of loops
        System.out.println("      \"casts\": " + numCasts + ",");                 // Number of casts
        System.out.println("      \"var_refs\": " + varReferences.size() + ",");  // Number of variable references

        System.out.print("      \"local_methods_invoked\": [");
        boolean atLeastOne = false;
        for (String method:localMethodsCalled) {
            if (atLeastOne)
                System.out.print(", ");
            System.out.print("\"" + prettyPrint(method) + "\"");
            atLeastOne = true;
        }
        System.out.print("],\n");

        System.out.print("      \"external_methods_invoked\": [");
        atLeastOne = false;
        for (String method:exterMethodsCalled) {
            if (atLeastOne)
                System.out.print(", ");
            System.out.print("\"" + prettyPrint(method) + "\"");
            atLeastOne = true;
        }
        System.out.print("],\n");

        System.out.print("      \"exception_refs\": [");
        atLeastOne = false;
        for (String exception:exceptions) {
            if (atLeastOne)
                System.out.print(", ");
            System.out.print("\"" + exception+ "\"");
            atLeastOne = true;
        }
        System.out.print("],\n");

        System.out.print("      \"class_refs\": [");
        atLeastOne = false;
        for (String classRef:classReferenced) {
            if (atLeastOne)
                System.out.print(", ");
            System.out.print("\"" + parseType(classRef) + "\"");
            atLeastOne = true;
        }
        System.out.print("]\n");
        System.out.print("    }");


        super.visitEnd();
    }

    private String parseType (String asmType) {
        String type = asmType;
        int numArrays = 0;

        switch (type.toUpperCase().charAt(0)) {
            case 'Z': return "boolean";
            case 'C': return "char";
            case 'B': return "byte";
            case 'S': return "short";
            case 'I': return "int";
            case 'F': return "float";
            case 'J': return "long";
            case 'D': return "double";
            case 'V': return "void";
            case '[':
                return parseType(type.substring(1)) + "[]";
            case 'L':
                return type.substring(1, type.length() - 1);
        }

        return "";
    }

    private String buildParameters (String asmParameters) {
        boolean atLeastOne = false;
        String parameters = "";
        int nextChar = 1;

        while (asmParameters.length() > 0) {
            switch (asmParameters.toUpperCase().charAt(0)) {
                case 'Z':
                case 'C':
                case 'B':
                case 'S':
                case 'I':
                case 'F':
                case 'J':
                case 'D':
                case 'V':
                    if (atLeastOne) parameters += ", ";
                    parameters += parseType(asmParameters.substring(0, 1));
                    asmParameters = asmParameters.substring(1);
                    atLeastOne = true;
                    break;
                case 'L':
                    if (atLeastOne) parameters += ", ";
                    parameters += parseType(asmParameters.substring(0, asmParameters.indexOf(";") + 1));
                    asmParameters = asmParameters.substring(asmParameters.indexOf(";") + 1);
                    atLeastOne = true;
                    break;
                case '[':
                    nextChar = 1;
                    if (atLeastOne) parameters += ", ";
                    while (asmParameters.charAt(nextChar) == '[')
                        nextChar++;
                    if (asmParameters.charAt(nextChar) == 'L') {
                        parameters += parseType(asmParameters.substring(0, asmParameters.indexOf(";") + 1));
                        asmParameters = asmParameters.substring(asmParameters.indexOf(";") + 1);
                    } else {
                        parameters += parseType(asmParameters.substring(0, nextChar + 1));
                        asmParameters = asmParameters.substring(nextChar + 1);
                    }

                    atLeastOne = true;
                    break;
                default:
                    asmParameters = asmParameters.substring(1);
            }
        }

        return parameters;
    }

    private String prettyPrint (String asmSignature) {
        String returnType = "";
        String methodName = "";
        String parameters = "";

        String asmParameters = "";

        int indexOfOpenParenthesis = asmSignature.indexOf("(");
        int indexOfCloseParenthesis = asmSignature.indexOf(")");

        methodName = asmSignature.substring(0, indexOfOpenParenthesis);
        returnType = parseType(asmSignature.substring(indexOfCloseParenthesis + 1));
        asmParameters = asmSignature.substring(indexOfOpenParenthesis + 1, indexOfCloseParenthesis);
        parameters = buildParameters(asmParameters);

        return returnType + " " + methodName + "(" + parameters + ")";
    }

    public double LTH () { return numOperands + numOperators; }
    public double VOC () { return operators.size() + varReferences.size(); }
    public double DIF () { return operators.size()/2.0 * (double)numOperands/varReferences.size(); }
    public double VOL () { return LTH() * (Math.log(VOC())/Math.log(2)); }
    public double EFF () { return DIF() * VOL(); }
    public String checkNaN (double value) { return Double.isNaN(value) ? "\"NaN\"" : Double.toString(value); }

    @Override
    public void visitLineNumber(int line, Label start) {
        lineCount++;
        super.visitLineNumber(line, start);
    }

    @Override
    public void visitLocalVariable(String name, String desc, String signature, Label start, Label end, int index) {
        if (!"this".equals(name) && !params.contains(name))
            numVars++;
        for(int i = 0 ; i < desc.length() ; i++){
            if(desc.charAt(i) == '[')
                continue;
            if(desc.charAt(i) == 'L'){
                classReferenced.add(desc);
                break;
            }
            break;
        }
        super.visitLocalVariable(name, desc, signature, start, end, index);
    }

    @Override
    public void visitInsn (int opcode) {
        if (opcode == IADD || opcode == ISUB ||
            opcode == IMUL || opcode == IDIV || opcode == IREM ||
            opcode == IOR || opcode == IAND || opcode == IXOR ||
            opcode == ISHR || opcode == ISHL || opcode == IUSHR ||
            opcode == DDIV || opcode == LADD || opcode == FADD ||
            opcode == DADD || opcode == LSUB || opcode == FSUB ||
            opcode == DSUB || opcode == LMUL || opcode == FMUL ||
            opcode == DMUL || opcode == LDIV || opcode == FDIV ||
            opcode == DDIV || opcode == LREM || opcode == FREM ||
            opcode == DREM || opcode == LSHL || opcode == LSHR ||
            opcode == LUSHR || opcode == LAND || opcode == LOR ||
            opcode == LXOR) {
            numOperands += 2;
            numOperators++;
            operators.add(opcode);
        } else if (opcode == LNEG || opcode == FNEG ||
            opcode == DNEG || opcode == INEG) {
            numOperators++;
            numOperands++;
            operators.add(opcode);
        }else if (I2L <= opcode && opcode <= I2S)
            numCasts++;
        super.visitInsn(opcode);
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        if (opcode == CHECKCAST)
            numCasts++;
        super.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
        exceptions.add(type);
        super.visitTryCatchBlock(start, end, handler, type);
    }
    @Override
    public void visitIincInsn(int var, int increment) {
        numOperators++;
        operators.add(-1);
        numOperands += increment == 1 || increment == -1 ? 1 : 2;

        if(varReferences.containsKey(var))
        {
            varReferences.put(var , varReferences.get(var) + 1);
        }
        else{
            varReferences.put(var , 1);
        }

        super.visitIincInsn(var , increment);
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        if(varReferences.containsKey(var)) {
            varReferences.put(var , varReferences.get(var) + 1);
        }
        else {
            varReferences.put(var , 1);
        }
        super.visitVarInsn(opcode , var);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if (className.equals(owner)) localMethodsCalled.add(name + desc);
        else exterMethodsCalled.add(owner + "/" + name + desc);
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        if (labelsVisited.contains(label)) numLoops++;
        super.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitLabel(Label l) {
        labelsVisited.add(l);
        super.visitLabel(l);
    }
}
