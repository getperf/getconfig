package com.getconfig.Utils

// Author: 巢鹏 chaopeng、住在北极，给GA4GH写登录
// URL : https://github.com/chaopeng/groovy-hotswap-demo
// About:
//  Using GroovyClassLoader to hotswap stateless business logic class

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * Simple file directory operation package
 *
 * @author chao
 *
 */
public class DirUtils {

    private static final Logger logger = LoggerFactory.getLogger(DirUtils.class);

    /**
     * ディレクトリ内のファイルを返す、argsは正規表現のみを受け入れます
     */
    public static File[] ls(String path, String... args) {
        File dir = new File(path);

        if (args.length == 0) {
            return dir.listFiles();
        }

        final Pattern[] patterns = new Pattern[args.length];
        for (int i = 0; i < args.length; i++) {
            patterns[i] = Pattern.compile(args[i]);
        }

        return dir.listFiles(new FilenameFilter() {

            @Override
            public boolean accept(File dir2, String name) {
                for (Pattern pattern : patterns) {
                    Matcher matcher = pattern.matcher(name);
                    if (matcher.find()) {
                        return true;
                    }
                }
                return false;
            }
        });
    }

    /**
     * 再帰的なサブディレクトリは一致するすべてのファイルを返し、argsは正規表現のみを受け入れます
     */
    public static File[] recursive(String path, String... args) {
        List<File> resLs = new ArrayList<File>();
        Queue<File> dirs = new LinkedList<File>();

        File file = new File(path);
        dirs.offer(file);

        final Pattern[] patterns = new Pattern[args.length];
        for (int i = 0; i < args.length; i++) {
            patterns[i] = Pattern.compile(args[i]);
        }

        while (dirs.size() != 0) {
            File tempPath = dirs.poll();
            File[] files = tempPath.listFiles();
            // 遍历files,如果是目录继续搜索,文件则匹配正则表达式
            for (File tFile : files) {
                if (tFile.isDirectory()) {
                    dirs.offer(tFile);
                } else {
                    for (Pattern pattern : patterns) {
                        Matcher matcher = pattern.matcher(tFile.getName());
                        if (matcher.find()) {
                            resLs.add(tFile);
                            break;
                        }
                    }
                }
            }
        }

        File[] res = new File[resLs.size()];
        resLs.toArray(res);
        return res;
    }

    /**
     * 1つのファイルをコピーする
     *
     * @param to
     *            ディレクトリでなければなりません
     */
    public static boolean cp(String from, String to, String... args) {
        // buffer为2M
        int length = 2097152;
        File fromFile = new File(from);
        File toFile = new File(to);
        // 目录的话相当于cp -r * 正则表达式对每一层都有效
        if (fromFile.isDirectory()) {
            File[] childFiles = ls(from, args);
            for (File childFile : childFiles) {
                if (childFile.isDirectory()) {
                    mkdir(to + File.separator + childFile.getName());
                }
                cp(childFile.getPath(),
                        to + File.separator + childFile.getName(), args);
            }
            return true;
        } else {
            try {
                FileInputStream in2 = new FileInputStream(from);
                String toPath = to;
                if (toFile.isDirectory()) {
                    toPath += File.separator + fromFile.getName();
                }
                FileOutputStream out = new FileOutputStream(toPath);
                byte[] buffer = new byte[length];
                while (true) {
                    int ins = in2.read(buffer);
                    if (ins == -1) {
                        in2.close();
                        out.flush();
                        out.close();
                        return true;
                    } else
                        out.write(buffer, 0, ins);
                }
            } catch (Exception e) {
                logger.error("cp error", e);
                return false;
            }
        }
    }

    /**
     * 1つのファイルを削除する
     */
    public static void rm(String path, String... args) {
        File file = new File(path);

        // ディレクトリはrm -r path / *と同等です。フォルダーは削除されません。正規表現は1つのレイヤーでのみ有効です
        if (file.isDirectory()) {
            File[] childFiles = ls(path, args);
            for (File childFile : childFiles) {
                if (childFile.isDirectory()) {
                    rm(childFile.getPath());
                }

                if(!childFile.delete()){
                    logger.error("file delete failed. filename="+childFile.getAbsolutePath());
                }
            }
        } else {
            if(!file.delete()){
                logger.error("file delete failed. filename="+file.getAbsolutePath());
            }
        }
    }

    /**
     * フォルダーを作る
     */
    public static boolean mkdir(String path) {
        File file = new File(path);
        return file.mkdirs();
    }

    /**
     * 空のフォルダーを削除する
     */
    public static boolean rmdir(String path) {
        File file = new File(path);
        return file.delete();
    }
}
