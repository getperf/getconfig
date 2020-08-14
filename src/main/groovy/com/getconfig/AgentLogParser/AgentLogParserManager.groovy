package com.getconfig.AgentLogParser

import com.getconfig.Testing.TestUtil
import com.getconfig.Utils.DirUtils
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovy.util.logging.Slf4j
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

@TypeChecked
@CompileStatic
@Slf4j
class AgentLogParserManager {
    static class Parser {
        private final Object o;
        private final Method method;

        Parser(Object o, Method method) {
            this.o = o;
            this.method = method;
        }
    }

    class ParserKey {
        private final String platform;
        private final String metricFile;

        ParserKey(String platform, String metricFile) {
            this.platform = platform;
            this.metricFile = metricFile;
        }
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof ParserKey) {
                ParserKey condition = (ParserKey) obj;
                return this.platform == condition.platform &&
                        this.metricFile == condition.metricFile;
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(platform, metricFile);
        }
    }

    String parserLibPath
    private GroovyClassLoader gcl = new GroovyClassLoader();
    private Map<ParserKey, Parser> commanders = new HashMap<>();

    AgentLogParserManager(String parserLibPath) {
        this.parserLibPath = parserLibPath
    }

    private Collection<Class> loadClassFromFile(String path) throws IOException {
        gcl.clearCache();
        File[] files = DirUtils.ls(path, "groovy");

        List<Class> classes = new ArrayList<>();
        for (File file : files) {
            println "load file = ${file.getAbsolutePath()}"
            Class cls = gcl.parseClass(file);
            classes.add(cls);
        }
        return classes;
    }

    /**
     * プロトコルの読み込みはスレッドセーフではありません。マルチスレッドはばかげていますか？
     */
    private void load(Collection<Class> classes){
        for (Class cls : classes) {

            try {
                Object o = cls.newInstance();
                Method[] methods = cls.getDeclaredMethods();
                for (Method method : methods) {
                    com.getconfig.AgentLogParser.Parser cmd = method.getAnnotation(com.getconfig.AgentLogParser.Parser.class);
                    if(cmd != null) {
                        ParserKey key = new ParserKey(cls.getSimpleName(), cmd.value())
                        commanders.put(key, new Parser(o, method));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    void init() {
        Collection<Class> fileClasses = this.loadClassFromFile(parserLibPath);
        this.load(fileClasses);
    }

    /**
     * プロトコル呼び出し
     */
    void invoke(TestUtil t) {
        String platform = t.platform
        String metricFile = t.metricFile
        Parser commander = commanders.get(new ParserKey(platform, metricFile));
        if(commander != null) {
            try {
                commander.method.invoke(commander.o, t);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
}
