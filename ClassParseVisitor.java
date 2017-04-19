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
  boolean atLeastOne = false;
  public ClassParseVisitor() {
    super(ASM5, new ClassWriter(ClassWriter.COMPUTE_FRAMES));
  }

  @Override
  public void visit(int version, int access, String name, String signature,
      String superName, String[] interfaces) {
    atLeastOne = false;
    this.className = name;
    System.out.println("{");
    System.out.println("  \"name\": \"" + name + "\",");
    System.out.println("  \"methods\": [");
    super.visit(version, access, name, signature, superName, interfaces);
  }

  public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
    if(atLeastOne) System.out.print(", {\n");
    else System.out.println("    {");
    atLeastOne = true;
    System.out.println("      \"name\": \"" + name +"\",");
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

    System.out.print("      \"modifiers\": [");

    for (int i = 0; i < modifiers.size() - 1; i++) {
        System.out.print("\"" + modifiers.get(i) + "\", ");
    }
    if (modifiers.size() > 0) System.out.print("\"" + modifiers.get(modifiers.size() - 1) + "\"");
    System.out.print("],\n");

    System.out.print("      \"exceptions_thrown\": [");
    if (exceptions != null) {
      for (int i = 0; i < exceptions.length - 1; i++)
        System.out.print("\"" + exceptions[i] + "\", ");

      if (exceptions.length > 0) System.out.print("\"" + exceptions[exceptions.length - 1] + "\"");
    }
    System.out.print("],\n");

    MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
    return mv == null ? null : new MethodTransformVisitor(mv, name, className);
  }

  public void visitEnd() {
    System.out.println("\n  ]");
    System.out.println("}");
  }
}
