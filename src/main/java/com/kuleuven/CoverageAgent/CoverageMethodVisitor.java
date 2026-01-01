package com.kuleuven.CoverageAgent;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class CoverageMethodVisitor extends MethodVisitor {

    private final String className;
    private final String methodName;
    private final String desc;
    private final boolean isSUTMethod;
    private int insnIdx = 0;

    CoverageMethodVisitor(MethodVisitor mv, String cls, String m, String d) {
        super(Opcodes.ASM9, mv);
        this.className = cls;
        this.methodName = m;
        this.desc = d;
        this.isSUTMethod = BlockRegistry.isInSUTScope(className, methodName, desc);
    }

    private void injectHit() {
        if (isSUTMethod) {
            mv.visitLdcInsn(insnIdx);
            mv.visitMethodInsn(
                    Opcodes.INVOKESTATIC,
                    "com/kuleuven/CoverageAgent/CoverageRuntime",
                    "hitInstruction",
                    "(I)V",
                    false
            );
        }
        insnIdx++;
    }

    private void injectBlockHit(int blockId) {
        mv.visitLdcInsn(blockId);
        mv.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "com/kuleuven/CoverageAgent/CoverageRuntime",
                "hitBlock",
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
            injectBlockHit(blockId);
        }

        super.visitLineNumber(line, start);
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
        injectHit();

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

    @Override
    public void visitVarInsn(int opcode, int var) {
        injectHit();
        super.visitVarInsn(opcode, var);
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        injectHit();
        super.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitTypeInsn(int opcode, String type) {
        injectHit();
        super.visitTypeInsn(opcode, type);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        injectHit();
        super.visitFieldInsn(opcode, owner, name, descriptor);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        injectHit();
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
        injectHit();
        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, bootstrapMethodArguments);
    }

    @Override
    public void visitJumpInsn(int opcode, Label label) {
        injectHit();
        super.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitLdcInsn(Object value) {
        injectHit();
        super.visitLdcInsn(value);
    }

    @Override
    public void visitIincInsn(int var, int increment) {
        injectHit();
        super.visitIincInsn(var, increment);
    }

    @Override
    public void visitTableSwitchInsn(int min, int max, Label dflt, Label... labels) {
        injectHit();
        super.visitTableSwitchInsn(min, max, dflt, labels);
    }

    @Override
    public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        injectHit();
        super.visitLookupSwitchInsn(dflt, keys, labels);
    }

    @Override
    public void visitMultiANewArrayInsn(String descriptor, int numDimensions) {
        injectHit();
        super.visitMultiANewArrayInsn(descriptor, numDimensions);
    }


}

