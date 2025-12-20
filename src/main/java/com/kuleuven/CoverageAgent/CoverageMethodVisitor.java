package com.kuleuven.CoverageAgent;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class CoverageMethodVisitor extends MethodVisitor {

    private final String className;
    private final String methodName;
    private final String desc;

    CoverageMethodVisitor(MethodVisitor mv, String cls, String m, String d) {
        super(Opcodes.ASM9, mv);
        this.className = cls;
        this.methodName = m;
        this.desc = d;
    }

    @Override
    public void visitLabel(Label label) {
        super.visitLabel(label);
    }


    private void injectHit(int blockId) {
        mv.visitLdcInsn(blockId);
        mv.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "com/kuleuven/CoverageAgent/CoverageRuntime",
                "hit",
                "(I)V",
                false
        );
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        Integer blockId = BlockRegistry.lookupByLine(
                className,
                methodName,
                desc,
                line
        );

        if (blockId != null) {
            injectHit(blockId);
        }

        super.visitLineNumber(line, start);
    }

}

