package com.kuleuven;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

public class CoverageAgent {
    public static void premain(String args, Instrumentation inst) {
        inst.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(
                    ClassLoader loader,
                    String name,
                    Class<?> cls,
                    ProtectionDomain domain,
                    byte[] bytes) {

                if (!shouldInstrument(name)) return bytes;

                ClassReader cr = new ClassReader(bytes);
                ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);

                ClassVisitor cv = new CoverageClassVisitor(cw);
                cr.accept(cv, 0);

                return cw.toByteArray();
            }
        });

    }

    private static boolean shouldInstrument(String internalClassName) {
        if (internalClassName == null) {
            return false;
        }

        // Never instrument JDK classes
        if (internalClassName.startsWith("java/") ||
                internalClassName.startsWith("javax/") ||
                internalClassName.startsWith("sun/") ||
                internalClassName.startsWith("jdk/")) {
            return false;
        }

        // Never instrument ASM or other libraries
        if (internalClassName.startsWith("org/objectweb/asm/") ||
                internalClassName.startsWith("com/google/gson/")) {
            return false;
        }

        // Never instrument your agent / runtime itself
        if (internalClassName.startsWith("com/kuleuven/coverage/")) {
            return false;
        }

        return internalClassName.startsWith("com/kuleuven/_examples/");
    }
}
