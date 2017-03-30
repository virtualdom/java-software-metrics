import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class ClassParseVisitor extends ClassVisitor implements Opcodes
{

  public ClassParseVisitor() {
    super(ASM5, new ClassWriter(ClassWriter.COMPUTE_FRAMES));
  }

  @Override
  public void visit(int version, int access, String name, String signature,
      String superName, String[] interfaces) {
    System.out.println(name + " extends " + superName + " {");
    super.visit(version, access, name, signature, superName, interfaces);
  }
/*
  public void visitSource(String source, String debug) {
  }

  public void visitOuterClass(String owner, String name, String desc) {
  }

  public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
    return null;
  }

  public void visitAttribute(Attribute attr) {
  }

  public void visitInnerClass(String name, String outerName, String innerName,
      int access) {
  }

  public FieldVisitor visitField(int access, String name, String desc,
      String signature, Object value) {
    return null;
  }*/

  public MethodVisitor visitMethod(int access, String name, String desc,
      String signature, String[] exceptions) {
    System.out.println("    " + name + desc);
    MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
    return mv == null ? null : new MethodTransformVisitor(mv, name);
  }

  public void visitEnd() {
    System.out.println("}");
  }
}
