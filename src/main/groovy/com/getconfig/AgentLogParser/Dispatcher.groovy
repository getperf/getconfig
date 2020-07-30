package com.getconfig.AgentLogParser

import groovy.util.logging.Slf4j
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

@Slf4j
class Dispatcher {
    static class Commander {
        private final Object o;
        private final Method method;

        Commander(Object o, Method method) {
            this.o = o;
            this.method = method;
        }
    }

    class CommandKey {
        private final String platform;
        private final String metricFile;

        CommandKey(String platform, String metricFile) {
            this.platform = platform;
            this.metricFile = metricFile;
        }
        @Override
        public boolean equals(Object obj) {
            if (obj instanceof CommandKey) {
                CommandKey condition = (CommandKey) obj;
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
    private Map<CommandKey, Commander> commanders = new HashMap<>();

    /**
     * プロトコルの読み込みはスレッドセーフではありません。マルチスレッドはばかげていますか？
     */
    public void load(Collection<Class> classes){
        for (Class cls : classes) {

            try {
                Object o = cls.newInstance();
                Method[] methods = cls.getDeclaredMethods();
                log.debug("CLASS NAME !!!!");
                log.debug(cls.getName());
                log.debug(cls.getSimpleName());
                for (Method method : methods) {
                    Parser cmd = method.getAnnotation(Parser.class);
                    if(cmd != null) {
                        CommandKey key = new CommandKey(cls.getSimpleName(), cmd.value())
                        commanders.put(key, new Commander(o, method));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
    }

    /**
     * プロトコル呼び出し
     */
    public void invoke(String platform, String metricFile){

        Commander commander = commanders.get(new CommandKey(platform, metricFile));
        if(commander != null) {
            try {
                commander.method.invoke(commander.o);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }
}
