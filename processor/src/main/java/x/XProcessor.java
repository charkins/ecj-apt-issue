package x;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.JavaFileObject;
import javax.tools.Diagnostic.Kind;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class XProcessor extends AbstractProcessor {

    private final String suffix = "X";

    public XProcessor() {
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return new LinkedHashSet<>(Arrays.asList(X.class.getCanonicalName()));
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        for (TypeElement annotation : annotations) {
            Set<? extends Element> elements = env.getElementsAnnotatedWith(annotation);
            for (Element element : elements) {
                if (!(element instanceof TypeElement))
                    continue;

                try {
                    X x = element.getAnnotation(X.class);
                    boolean[] vals = x.value();

                    if (vals.length < 1)
                        return false;

                    boolean[] nvals = new boolean[vals.length - 1];
                    for (int i = 1; i < vals.length; i++)
                        nvals[i - 1] = vals[i];

                    String annotatedClassName = ((TypeElement) element).getQualifiedName().toString();
                    int dot = annotatedClassName.lastIndexOf(".");
                    String pkg = (dot>=0 ? annotatedClassName.substring(0,dot) : null);
                    String name = (dot>=0 ? annotatedClassName.substring(dot+1) : annotatedClassName ) + suffix;

                    if (vals[0])
                        generateJava(pkg, name, nvals);
                    else
                        generateClass(pkg, name, nvals);

                } catch (Exception e) {
                    processingEnv.getMessager().printMessage(Kind.ERROR, e.getMessage());
                }
            }
        }

        return false;
    }

    private void generateJava(String pkg, String name, boolean[] nextValues) throws IOException {
        String fqn = pkg!=null ? pkg + "." + name : name;

        JavaFileObject java = processingEnv.getFiler().createSourceFile(fqn);
        try (PrintWriter out = new PrintWriter(java.openWriter())) {
            if(pkg!=null) out.println("package " + pkg + ";");
            if (nextValues.length > 0) {
                out.print("@" + X.class.getName() + "({");
                for (int i = 0; i < nextValues.length; i++) {
                    if (i > 0)
                        out.print(",");
                    if (nextValues[i])
                        out.print("true");
                    else
                        out.print("false");
                }
                out.println("})");
            }

            out.println("public class " + name + " {");
            out.println("}");
        }
    }

    private void generateClass(String pkg, String name, boolean[] nextValues) throws IOException {
        String fqn = pkg!=null ? pkg + "." + name : name;

        ClassWriter cw = new ClassWriter(0);
        cw.visit(Opcodes.V1_6, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, fqn.replaceAll("\\.","/"), null, "java/lang/Object", null);
        if (nextValues.length > 0) {
            cw.visitAnnotation(Type.getType(X.class).getDescriptor(), true).visit("value", nextValues);
        }
        cw.visitEnd();

        JavaFileObject clazz = processingEnv.getFiler().createClassFile(fqn);
        try (OutputStream out = clazz.openOutputStream()) {
            out.write(cw.toByteArray());
        }
    }
}
