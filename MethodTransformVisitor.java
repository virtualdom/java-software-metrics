import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;

class MethodTransformVisitor extends MethodVisitor implements Opcodes {
    int numParams = 0;
    int numVars = 0;
    int lineCount = 0;
    int numCasts = 0;
	int numRefVar = 0;
    String mName;
    ArrayList<String> params;
    HashSet<String> exceptions;
	HashMap<Integer, Integer> varReferences;
	

    public MethodTransformVisitor(final MethodVisitor mv, String methodname) {
        super(ASM5, mv);
        this.mName=methodname;
        this.params = new ArrayList<String>();
        this.exceptions = new HashSet<String>();
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
        System.out.println("    Number of Arguments: " + numParams);
        System.out.println("    Number of Var Declarations: " + numVars);
        System.out.println("    Number of Lines: " + lineCount);
        System.out.println("    Number of Casts: " + numCasts);
		System.out.println("    Number of Var References: " + varReferences.size());

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
        if (I2L <= opcode && opcode <= I2S)
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
	public void visitVarInsn(int opcode, int var){
		if(varReferences.containsKey(var))
		{
			varReferences.put(var , varReferences.get(var) + 1);
		}
		else{
			varReferences.put(var , 1);
		}
		super.visitVarInsn(opcode , var);
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
