import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class MethodTransformVisitor extends MethodVisitor implements Opcodes {
    int lineNumber = 0;
    int numParams = 0;
    int numVars = 0;
    String mName;

    public MethodTransformVisitor(final MethodVisitor mv, String methodname) {
        super(ASM5, mv);
        this.mName=methodname;
    }

    @Override
    public void visitParameter(String name, int access) {
        numParams++;
        super.visitParameter(name, access);
    }

    @Override
    public void visitEnd() {
        System.out.println("  Number of Arguments: " + numParams);
        System.out.println("  Number of Var Declarations: " + numVars);
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
        super.visitLineNumber(line, start);
    }

    @Override
    public void visitLocalVariable(String name, String desc, String signature,
            Label start, Label end, int index) {
        if (!"this".equals(name))
            numVars++;
        super.visitLocalVariable(name, desc, signature, start, end, index);
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
