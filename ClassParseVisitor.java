import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;

class ClassParseVisitor extends ClassVisitor implements Opcodes
{
  String className;
  public ClassParseVisitor() {
    super(ASM5, new ClassWriter(ClassWriter.COMPUTE_FRAMES));
  }

  @Override
  public void visit(int version, int access, String name, String signature,
      String superName, String[] interfaces) {
    this.className = name;
    System.out.println("Analyzing class: " + name);
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

  public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
    System.out.println("  Function name: " + name);
    ArrayList<String> modifiers = new ArrayList<String>();
    int decodeAccess = access;

    if (decodeAccess >= ACC_ABSTRACT) {
      modifiers.add("abstract");
      decodeAccess -= ACC_ABSTRACT;
    }

    if (decodeAccess >= ACC_VOLATILE) {
      modifiers.add("volatile");
      decodeAccess -= ACC_VOLATILE;
    } else if (decodeAccess >= ACC_SYNCHRONIZED) {
      modifiers.add("synchronized");
      decodeAccess -= ACC_SYNCHRONIZED;
    }

    if (decodeAccess >= ACC_FINAL) {
      modifiers.add("final");
      decodeAccess -= ACC_FINAL;
    }

    if (decodeAccess >= ACC_STATIC) {
      modifiers.add("static");
      decodeAccess -= ACC_STATIC;
    }

    if (decodeAccess >= ACC_PROTECTED) {
      modifiers.add("protected");
      decodeAccess -= ACC_PROTECTED;
    } else if (decodeAccess >= ACC_PRIVATE) {
      modifiers.add("private");
      decodeAccess -= ACC_PRIVATE;
    } else if (decodeAccess >= ACC_PUBLIC) {
      modifiers.add("public");
      decodeAccess -= ACC_PUBLIC;
    }

    System.out.print("    Modifiers: ");
    for (String modifier: modifiers) {
        System.out.print(modifier + " ");
    }
    System.out.print("\n");

    if (exceptions != null) {
      System.out.print("    Exceptions thrown: ");
      for (int i = 0; i < exceptions.length; i++) System.out.print(exceptions[i] + " ");
      System.out.print("\n");
  }

    MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
    return mv == null ? null : new MethodTransformVisitor(mv, name, className);
  }

  public void visitEnd() {
    System.out.println("");
  }
}
