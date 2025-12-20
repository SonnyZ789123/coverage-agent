package com.kuleuven.CoverageAgent;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class CoverageMethodVisitor extends MethodVisitor {

    private final String className;
    private final String methodName;
    private final String desc;
    private final boolean isSUTMethod;

    CoverageMethodVisitor(MethodVisitor mv, String cls, String m, String d) {
        super(Opcodes.ASM9, mv);
        this.className = cls;
        this.methodName = m;
        this.desc = d;
        this.isSUTMethod = BlockRegistry.isInSUTScope(className, methodName, desc);
    }

    @Override
    public void visitCode() {
        if (isSUTMethod) {
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "com/kuleuven/CoverageAgent/CoverageRuntime",
                    "startPath",
                    "()V",
                    false
            );
        }

        super.visitCode();
    }

    @Override
    public void visitInsn(int opcode) {
        if (isSUTMethod) {
            /*
            In Opcodes:
            IRETURN = 172
            LRETURN = 173
            FRETURN = 174
            DRETURN = 175
            ARETURN = 176
            RETURN  = 177
            */
            if ((opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN)
                    || opcode == Opcodes.ATHROW) {

                mv.visitMethodInsn(
                        Opcodes.INVOKESTATIC,
                        "com/kuleuven/CoverageAgent/CoverageRuntime",
                        "endPath",
                        "()V",
                        false
                );
            }
        }

        super.visitInsn(opcode);
    }


    private void injectHit(int blockId) {
        /*
        Injects a call to CoverageRuntime.hit(blockId)
        ldc <blockId>
        invokestatic com/kuleuven/CoverageAgent/CoverageRuntime.hit (I)V
         */
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

