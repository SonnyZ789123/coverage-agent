package com.kuleuven;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class CoverageClassVisitor extends ClassVisitor {
    private String className;

    CoverageClassVisitor(ClassVisitor cv) {
        super(Opcodes.ASM9, cv);
    }

    @Override
    public void visit(int v, int a, String name, String sig, String sup, String[] ifs) {
        this.className = name.replace('/', '.');
        super.visit(v, a, name, sig, sup, ifs);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name,
                                     String desc, String sig, String[] ex) {

        MethodVisitor mv = super.visitMethod(access, name, desc, sig, ex);
        return new CoverageMethodVisitor(mv, className, name, desc);
    }
}