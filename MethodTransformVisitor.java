import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

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
    HashSet<String> exceptions;
    HashSet<String> localMethodsCalled;
    HashSet<String> exterMethodsCalled;
    HashSet<Label> labelsVisited;
    HashMap<Integer, Integer> varReferences;

    public MethodTransformVisitor(final MethodVisitor mv, String methodname, String className) {
        super(ASM5, mv);
        this.mName=methodname;
        this.className = className;
        this.params = new ArrayList<String>();
        this.exceptions = new HashSet<String>();
        this.localMethodsCalled = new HashSet<String>();
        this.exterMethodsCalled = new HashSet<String>();
        this.labelsVisited = new HashSet<Label>();
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
        System.out.println("    No. of Arguments:                " + numParams);
        System.out.println("    No. of Var Declarations:         " + numVars);
        System.out.println("    No. of Lines:                    " + lineCount);
        System.out.println("    No. of Arith/Bitwise Operators:  " + numOperators);
        System.out.println("    No. of Arith/Bitwise Operands:   " + numOperands);
        System.out.println("    No. of Loops:                    " + numLoops);
        System.out.println("    No. of Casts:                    " + numCasts);
        System.out.println("    No. of Variables Referenced:     " + varReferences.size());

        System.out.println("    Local Method invocations: " + (localMethodsCalled.isEmpty() ? "None" : ""));
        for (String method:localMethodsCalled)
            System.out.println("      " + method);

        System.out.println("    External Method invocations: " + (exterMethodsCalled.isEmpty() ? "None" : ""));
        for (String method:exterMethodsCalled)
            System.out.println("      " + method);


        System.out.print("    Exceptions referenced: ");
        for(String exception:exceptions)
            System.out.print(exception + " ");
        System.out.print("\n");

        super.visitEnd();
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        lineCount++;
        super.visitLineNumber(line, start);
    }

    @Override
    public void visitLocalVariable(String name, String desc, String signature,
            Label start, Label end, int index) {
        if (!"this".equals(name) && !params.contains(name))
            numVars++;
        super.visitLocalVariable(name, desc, signature, start, end, index);
    }

    @Override
    public void visitInsn (int opcode) {
        if (opcode == IADD || opcode == ISUB ||
            opcode == IMUL || opcode == IDIV || opcode == IREM ||
            opcode == IOR || opcode == IAND || opcode == IXOR ||
            opcode == ISHR || opcode == ISHL || opcode == IUSHR) {
            numOperands += 2;
            numOperators++;
        } else if (opcode == INEG) {
            numOperators++;
            numOperands++;
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
