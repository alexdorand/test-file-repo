package org.javaparser.examples.chapter2;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.visitor.ModifierVisitor;
import org.javaparser.samples.ReversePolishNotation;

import javax.tools.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.regex.Pattern;

public class ModifyingVisitorComplete {

    private static final String FILE_PATH_DIR = "src/main/java/org/javaparser/samples/";
    private static final String FILE_PATH_GENERATED_DIR = "target/generated-sources/annotations/org/javaparser/samples/";
    private static final String FILE_PATH = "src/main/java/org/javaparser/samples/ReversePolishNotation.java";
    private static final String FILE_PATH_GENERATED = "target/generated-sources/annotations/org/javaparser/samples/ReversePolishNotation.java";

    private static final Pattern LOOK_AHEAD_THREE = Pattern.compile("(\\d)(?=(\\d{3})+$)");

    public static void main(String[] args) throws Exception {

        CompilationUnit cu = JavaParser.parse(new FileInputStream(FILE_PATH));

        ModifierVisitor<?> numericLiteralVisitor = new IntegerLiteralModifier();
        numericLiteralVisitor.visit(cu, null);

        File parentDirectoryFile = new File(FILE_PATH_DIR);
        System.out.println(parentDirectoryFile.exists());
        //System.out.println(cu.toString());

        File dir = new File(FILE_PATH_GENERATED_DIR);
        dir.mkdirs();

        File dest = new File(FILE_PATH_GENERATED);
        dest.createNewFile();

        FileOutputStream fileOutputStream = new FileOutputStream(dest);
        fileOutputStream.write(cu.toString().getBytes());
        fileOutputStream.close();

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        File parentDirectory = parentDirectoryFile.getParentFile();
        fileManager.setLocation(StandardLocation.CLASS_OUTPUT, Arrays.asList(parentDirectory));
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(dest));
        compiler.getTask(null, fileManager, null, null, null, compilationUnits).call();
        fileManager.close();


        ReversePolishNotation reversePolishNotation = new ReversePolishNotation();
        System.out.println(reversePolishNotation.number());
    }

    private static class IntegerLiteralModifier extends ModifierVisitor<Void> {

        @Override
        public FieldDeclaration visit(FieldDeclaration fd, Void arg) {
            super.visit(fd, arg);
            fd.getVariables().forEach(v ->
                    v.getInitializer().ifPresent(i -> {
                        if (i instanceof IntegerLiteralExpr) {
                            v.setInitializer(formatWithUnderscores(((IntegerLiteralExpr) i).getValue()));
                        }
                    }));
            return fd;
        }
    }

    static String formatWithUnderscores(String value) {
        String withoutUnderscores = value.replaceAll("_", "");
        return LOOK_AHEAD_THREE.matcher(withoutUnderscores).replaceAll("$10");
    }
}
