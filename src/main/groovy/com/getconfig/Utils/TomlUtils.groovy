package com.getconfig.Utils

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import com.moandjiezana.toml.Toml
import com.moandjiezana.toml.TomlWriter

@TypeChecked
@CompileStatic
class TomlUtils {
    static Object read(String path, Class c) {
        def toml = new Toml().read(new File(path))
        return toml.to(c);
    }

    static String decode(Object obj) {
        TomlWriter writer = new TomlWriter()
        return writer.write(obj)
    }
}
