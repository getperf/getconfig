package com.getconfig.Utils

// Author: 巢鹏 chaopeng、住在北极，给GA4GH写登录
// URL : https://github.com/chaopeng/groovy-hotswap-demo
// About:
//  Using GroovyClassLoader to hotswap stateless business logic class

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.regex.Pattern

/**
 * パッケージスキャナー
 * @author chao
 *
 */
public class ClassPathScanner {

    private static final Logger logger = LoggerFactory
            .getLogger(ClassPathScanner.class);

    /**
     *パッケージをスキャン
     * @param basePackage基本パッケージ
     * @param recursiveサブパッケージを再帰的に検索するかどうか
     * @param excludeInner内部クラスを除外するかどうかtrue-> yes false-> no
     * @param checkInOrExフィルタールールアプリケーションtrue->ルールを満たすfalseを検索->ルールを満たすルールを除外
     * @param classFilterStrs List <java.lang.String>カスタムフィルタールール（nullまたは空の場合、つまり、すべてフィルターなしで満たされる場合）
     * @return　Set
     */
    public static Set<Class> scan(String basePackage, boolean recursive, boolean excludeInner, boolean checkInOrEx,
                                  List<String> classFilterStrs) {
        Set<Class> classes = new LinkedHashSet<>();
        String packageName = basePackage;
        List<Pattern> classFilters = toClassFilters(classFilterStrs);

        if (packageName.endsWith(".")) {
            packageName = packageName
                    .substring(0, packageName.lastIndexOf('.'));
        }
        String package2Path = packageName.replace('.', '/');

        Enumeration<URL> dirs;
        try {
            dirs = Thread.currentThread().getContextClassLoader()
                    .getResources(package2Path);
            while (dirs.hasMoreElements()) {
                URL url = dirs.nextElement();
                String protocol = url.getProtocol();
                if ("file".equals(protocol)) {
                    logger.debug("ファイル形式のクラスをスキャン....");
                    String filePath = URLDecoder.decode(url.getFile(), "UTF-8");
                    doScanPackageClassesByFile(classes, packageName, filePath,
                            recursive, excludeInner, checkInOrEx, classFilters);
                } else if ("jar".equals(protocol)) {
                    logger.debug("jarファイル形式のクラスをスキャン....");
                    doScanPackageClassesByJar(packageName, url, recursive,
                            classes, excludeInner, checkInOrEx, classFilters);
                }
            }
        } catch (IOException e) {
            logger.error("IOException error:", e);
        }

        return classes;
    }

    /**
     * jarモードでパッケージ内のすべてのClassファイルをスキャンします
     */
    private static void doScanPackageClassesByJar(String basePackage, URL url,
                                                  final boolean recursive, Set<Class> classes, boolean excludeInner, boolean checkInOrEx, List<Pattern> classFilters) {
        String packageName = basePackage;
        String package2Path = packageName.replace('.', '/');
        JarFile jar;
        try {
            jar = ((JarURLConnection) url.openConnection()).getJarFile();
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String name = entry.getName();
                if (!name.startsWith(package2Path) || entry.isDirectory()) {
                    continue;
                }

                // サブパッケージを再帰的に検索するかどうかを判断します
                if (!recursive
                        && name.lastIndexOf('/') != package2Path.length()) {
                    continue;
                }
                // 内部クラスをフィルタリングするかどうかを決定します
                if (excludeInner && name.indexOf('$') != -1) {
                    logger.debug("exclude inner class with name:" + name);
                    continue;
                }
                String classSimpleName = name
                        .substring(name.lastIndexOf('/') + 1);
                // フィルター条件が満たされているかどうかを判断します
                if (filterClassName(classSimpleName, checkInOrEx, classFilters)) {
                    String className = name.replace('/', '.');
                    className = className.substring(0, className.length() - 6);
                    try {
                        classes.add(Thread.currentThread()
                                .getContextClassLoader().loadClass(className));
                    } catch (ClassNotFoundException e) {
                        logger.error("Class.forName error:", e);
                    }
                }
            }
        } catch (IOException e) {
            logger.error("IOException error:", e);
        }
    }

    /**
     *パッケージ内のすべてのクラスファイルをファイルごとにスキャンします
     *
     * @param packageName
     * @param packagePath
     * @param recursive
     * @param classes
     */
    private static void doScanPackageClassesByFile(Set<Class> classes,
                                                   String packageName, String packagePath, boolean recursive, final boolean excludeInner, final boolean checkInOrEx, final List<Pattern> classFilters) {
        File dir = new File(packagePath);
        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }
        final boolean fileRecursive = recursive;
        File[] dirfiles = dir.listFiles(new FileFilter() {
            // カスタムファイルフィルタリングルール
            public boolean accept(File file) {
                if (file.isDirectory()) {
                    return fileRecursive;
                }
                String filename = file.getName();
                if (excludeInner && filename.indexOf('$') != -1) {
                    logger.debug("exclude inner class with name:" + filename);
                    return false;
                }
                return filterClassName(filename, checkInOrEx, classFilters);
            }
        });
        for (File file : dirfiles) {
            if (file.isDirectory()) {
                doScanPackageClassesByFile(classes,
                        packageName + "." + file.getName(),
                        file.getAbsolutePath(), recursive, excludeInner, checkInOrEx, classFilters);
            } else {
                String className = file.getName().substring(0,
                        file.getName().length() - 6);
                try {
                    classes.add(Thread.currentThread().getContextClassLoader()
                            .loadClass(packageName + '.' + className));

                } catch (ClassNotFoundException e) {
                    logger.error("IOException error:", e);
                }
            }
        }
    }

    /**
     * フィルタリングルールに従ってクラス名を決定する
     */
    private static boolean filterClassName(String className, boolean checkInOrEx, List<Pattern> classFilters) {
        if (!className.endsWith(".class")) {
            return false;
        }
        if (null == classFilters || classFilters.isEmpty()) {
            return true;
        }
        String tmpName = className.substring(0, className.length() - 6);
        boolean flag = false;
        for (Pattern p : classFilters) {
            if (p.matcher(tmpName).find()) {
                flag = true;
                break;
            }
        }
        return (checkInOrEx && flag) || (!checkInOrEx && !flag);
    }

    /**
     * @param pClassFilters the classFilters to set
     */
    private static List<Pattern> toClassFilters(List<String> pClassFilters) {
        List<Pattern> classFilters = new ArrayList<Pattern>();
        if(pClassFilters!=null){

            for (String s : pClassFilters) {
                String reg = "^" + s.replace("*", ".*") + "\$";
                Pattern p = Pattern.compile(reg);
                classFilters.add(p);
            }
        }
        return classFilters;
    }

}
