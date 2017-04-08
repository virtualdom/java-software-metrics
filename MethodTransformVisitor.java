import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.HashSet;

class MethodTransformVisitor extends MethodVisitor implements Opcodes {
    int numOperands = 0;
    int numOperators = 0;
    int numParams = 0;
    int numVars = 0;
    int lineCount = 0;
    int numCasts = 0;
    String mName;
    String className;
    ArrayList<String> params;
    HashSet<String> exceptions;
    HashSet<String> localMethodsCalled;
    HashSet<String> exterMethodsCalled;

    public MethodTransformVisitor(final MethodVisitor mv, String methodname, String className) {
        super(ASM5, mv);
        this.mName=methodname;
        this.className = className;
        this.params = new ArrayList<String>();
        this.exceptions = new HashSet<String>();
        this.localMethodsCalled = new HashSet<String>();
        this.exterMethodsCalled = new HashSet<String>();
    }

    @Override
    public void visitParameter(String name, int access) {
        params.add(name);
        numParams++;
        super.visitParameter(name, access);
    }

    @Override
    public void visitEnd() {
        System.out.println("    Number of Arguments: " + numParams);
        System.out.println("    Number of Var Declarations: " + numVars);
        System.out.println("    Number of Lines: " + lineCount);
        System.out.println("    Number of Arith/Bitwise Operators: " + numOperators);
        System.out.println("    Number of Arith/Bitwise Operands: " + numOperands);
        System.out.println("    Number of Casts: " + numCasts);

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

    // // method coverage collection
    // @Override
    // public void visitCode(){
    //     mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
    //     mv.visitLdcInsn("function " + mName + " executed");
    //     mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
    //     super.visitCode();
    // }

    // // statement coverage collection but not working well all the time
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
    public void visitIincInsn(int var, int increment){
       numOperators++;
       numOperands += increment == 1 || increment == -1 ? 1 : 2;
       super.visitIincInsn(var , increment);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if (className.equals(owner)) localMethodsCalled.add(name + desc);
        else exterMethodsCalled.add(owner + "/" + name + desc);
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }

    // @Override
    // public void visitLabel(Label l) {
    //     if (lineNumber != 0) {
    //         mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
    //         mv.visitLdcInsn("line " + lineNumber + " executed");
    //         mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
    //         super.visitLabel(l);
    //     }
    // }
}
