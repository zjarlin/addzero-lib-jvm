package site.addzero.common.util.unuse

import java.io.File
import java.io.FileWriter
import java.net.URLClassLoader
import javax.tools.ToolProvider

object InMemoryClassLoader /*extends ClassLoader*/ {
    //    private final Map<String, byte[]> classes = new HashMap<>();
    //    public void addClass(String name, byte[] byteCode) {
    //        classes.put(name, byteCode);
    //    }
    //
    //    @Override
    //    protected Class<?> findClass(String name) throws ClassNotFoundException {
    //        byte[] byteCode = classes.get(name);
    //        if (byteCode == null) {
    //            throw new ClassNotFoundException(name);
    //        }
    //        return defineClass(name, byteCode, 0, byteCode.length);
    //    }
    @Throws(Exception::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val sourceCode = "public class DynamicClass { public String greet() { return \"Hello, World!\"; } }"

        val sourceFile = File("DynamicClass.java")
        FileWriter(sourceFile).use { writer ->
            writer.write(sourceCode)
        }
        val compiler = ToolProvider.getSystemJavaCompiler()
        compiler.run(null, null, null, sourceFile.path)

        val classLoader = URLClassLoader.newInstance(arrayOf(File("").toURI().toURL()))
        val dynamicClass = classLoader.loadClass("DynamicClass")

        val instance = dynamicClass.getDeclaredConstructor().newInstance()
        val greet = dynamicClass.getMethod("greet").invoke(instance)
        println(greet)
    }
}
