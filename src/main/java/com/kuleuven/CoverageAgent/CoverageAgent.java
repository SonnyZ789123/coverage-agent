package com.kuleuven.CoverageAgent;

import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;
import java.util.HashMap;
import java.util.Map;

public class CoverageAgent {
    public static void premain(String args, Instrumentation inst) {
        Map<String, String> options = parseArgs(args);

        String projectPrefix = options.get("projectPrefix");
        String outputPath = options.getOrDefault("outputPath", "out/coverage_paths.json");
        String blockMapPath = options.get("blockMapPath");

        CoverageRuntime.init(outputPath);
        BlockRegistry.init(blockMapPath);

        inst.addTransformer(new ClassFileTransformer() {
            @Override
            public byte[] transform(
                    ClassLoader loader,
                    String name,
                    Class<?> cls,
                    ProtectionDomain domain,
                    byte[] bytes) {

                if (!shouldInstrument(name, projectPrefix)) return bytes;

                ClassReader cr = new ClassReader(bytes);
                ClassWriter cw = new ClassWriter(cr, ClassWriter.COMPUTE_FRAMES);

                ClassVisitor cv = new CoverageClassVisitor(cw);
                cr.accept(cv, 0);

                return cw.toByteArray();
            }
        });

    }

    private static boolean shouldInstrument(String internalClassName, @Nullable String projectPrefix) {
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
                internalClassName.startsWith("com/google/gson/") ||
                internalClassName.startsWith("it/unimi/dsi/")) {
            return false;
        }

        // Never instrument coverage tracking classes
        if (internalClassName.startsWith("com/kuleuven/CoverageAgent/")) {
            return false;
        }

        if (projectPrefix != null && !projectPrefix.isEmpty()) {
            String safeProjectPrefix = projectPrefix.replace('.', '/');
            if (!internalClassName.startsWith(safeProjectPrefix)) {
                return false;
            }
        }

        return BlockRegistry.isInSUTScope(internalClassName.replace('/', '.'));
    }

    private static Map<String, String> parseArgs(String args) {
        Map<String, String> map = new HashMap<>();

        if (args == null || args.isEmpty()) {
            return map;
        }

        for (String part : args.split(",")) {
            String[] kv = part.split("=", 2);
            if (kv.length == 2) {
                map.put(kv[0], kv[1]);
            }
        }
        return map;
    }

}
