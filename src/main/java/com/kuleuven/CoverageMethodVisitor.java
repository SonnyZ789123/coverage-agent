package com.kuleuven;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.Map;

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

    private Integer lookupBlockId(
            String className,
            String methodName,
            String methodDesc,
            int lineNumber
    ) {
        String asmMethodDescriptor = methodName + methodDesc;

        for (Map.Entry<Integer, BlockInfo> e : BlockRegistry.getBlocks().entrySet()) {
            BlockInfo info = e.getValue();

            if (!info.className().equals(className)) {
                continue;
            }

            if (!info.methodDescriptor().equals(asmMethodDescriptor)) {
                continue;
            }

            if (info.lineNumber() == lineNumber) {
                return e.getKey();
            }
        }

        return null;
    }


    private void injectHit(int blockId) {
        mv.visitLdcInsn(blockId);
        mv.visitMethodInsn(
                Opcodes.INVOKESTATIC,
                "com/kuleuven/CoverageRuntime",
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

