package com.getconfig.AgentLogParser

import com.getconfig.Utils.DirUtils
import spock.lang.Specification

class DispatcherTest extends Specification {
    GroovyClassLoader gcl = new GroovyClassLoader();

    private Collection<Class> loadClassFromFile(GroovyClassLoader gcl, String path) throws IOException {
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

    def "Load"() {
        when:
        Dispatcher dispatcher = new Dispatcher();
        Collection<Class> fileClasses = loadClassFromFile(gcl, "./lib/handlers");
        println "reload handlers from files"
        dispatcher.load(fileClasses);

        then:
        dispatcher.invoke("Linux","uname");
        1 == 1
    }

    def "Invoke"() {
    }
}
