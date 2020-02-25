package com.crowdin.cli.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.InputStream;
import java.nio.file.FileSystems;
import java.util.Optional;
import java.util.Properties;
import java.util.ResourceBundle;


public class Utils {

    private static final String APPLICATION_BASE_URL = "application.base_url";

    private static final String APPLICATION_NAME = "application.name";

    private static final String APPLICATION_VERSION = "application.version";

    private static final String PROPERTIES_FILE = "/crowdin.properties";

    /**
     * Path separator for use when concatenating or using non-regex findLanguage/replace methods.
     */
    public static final String PATH_SEPARATOR = FileSystems.getDefault().getSeparator();

    /**
     * Path separator for use in regex patterns, or in regex replacements, which use the same escaping.
     */
    public static final String PATH_SEPARATOR_REGEX = "\\".equals(PATH_SEPARATOR) ? "\\\\" : PATH_SEPARATOR;

    private static final ResourceBundle RESOURCE_BUNDLE = MessageSource.RESOURCE_BUNDLE;

    private static String userAgentString;

    private static Properties readProperties() {
        Properties properties = new Properties();
        try (InputStream in = Utils.class.getResourceAsStream(PROPERTIES_FILE);){
            properties.load(in);
        } catch (Exception e) {
            System.out.println(String.format(RESOURCE_BUNDLE.getString("error.read_resource_file"), PROPERTIES_FILE));
        }
        return properties;
    }

    public static String getAppName() {
        Properties properties = readProperties();
        String applicationName = null;
        if (properties != null && properties.get(APPLICATION_NAME) != null) {
            applicationName = properties.get(APPLICATION_NAME).toString();
        }
        return applicationName;
    }

    public static String getAppVersion() {
        Properties properties = readProperties();
        String applicationVersion = null;
        if (properties.get(APPLICATION_VERSION) != null) {
            applicationVersion = properties.get(APPLICATION_VERSION).toString();
        }
        return applicationVersion;
    }

    public static String getBaseUrl() {
        Properties properties = readProperties();
        String applicationBaseUrl = null;
        if (properties.get(APPLICATION_BASE_URL) != null) {
            applicationBaseUrl = properties.get(APPLICATION_BASE_URL).toString();
        }
        return applicationBaseUrl;
    }

    public static String replaceBasePath(String path, String basePath) {
        if (StringUtils.isEmpty(path)) {
            return "";
        }
        String result;
        if (StringUtils.isNotEmpty(basePath)) {
            path = path.replaceAll(PATH_SEPARATOR_REGEX + "+", PATH_SEPARATOR_REGEX);
            result = path.replace(basePath, PATH_SEPARATOR);
        } else {
            String[] nodes = path.split(PATH_SEPARATOR_REGEX);
            result = nodes[nodes.length-1];
        }
        result = result.replaceAll(PATH_SEPARATOR_REGEX + "+", PATH_SEPARATOR_REGEX);
        return result;
    }

    public static Boolean isWindows() {
        return SystemUtils.IS_OS_WINDOWS;
    }

    public static String buildUserAgent() {
        if (Utils.userAgentString != null) {
            return Utils.userAgentString;
        }
        Properties prop = readProperties();

        Utils.userAgentString = String.format("%s/%s java/%s/%s %s/%s",
            prop.getProperty("application.name"),
            prop.getProperty("application.version"),
            System.getProperty("java.vendor"),
            System.getProperty("java.version"),
            System.getProperty("os.name"),
            System.getProperty("os.version"));
        return Utils.userAgentString;
    }

    public static String commonPath(String[] paths){
        String commonPath = "";
        if (paths == null) {
            return commonPath;
        }
        if (Utils.isWindows()) {
            String[] winPath = new String[paths.length];
            for (int i=0; i<paths.length; i++) {
                if (paths[i].contains("/")) {
                    winPath[i] = paths[i].replaceAll("/+", PATH_SEPARATOR_REGEX);
                } else {
                    winPath[i] = paths[i];
                }
            }
            paths = winPath;
        }
        if (paths.length == 1) {
            if (paths[0].lastIndexOf(PATH_SEPARATOR) > 0) {
                commonPath = paths[0].substring(0, paths[0].lastIndexOf(PATH_SEPARATOR));
            }
            if (Utils.isWindows() && paths[0].lastIndexOf("\\") > 0) {
                commonPath = paths[0].substring(0, paths[0].lastIndexOf("\\"));
            }
        } else if (paths.length > 1) {
            String[][] folders = new String[paths.length][];
            for(int i = 0; i < paths.length; i++){
                folders[i] = paths[i].split(PATH_SEPARATOR_REGEX);
            }
            for(int j = 0; j < folders[0].length; j++){
                String thisFolder = folders[0][j];
                boolean allMatched = true;
                for(int i = 1; i < folders.length && allMatched; i++){
                    if(folders[i].length < j){
                        allMatched = false;
                        break;
                    }
                    allMatched &= folders[i][j].equals(thisFolder);
                }
                if(allMatched){
                    commonPath += thisFolder + PATH_SEPARATOR;
                } else{
                    break;
                }
            }
        }
        return commonPath;
    }

    public static String getEnvironmentVariable(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        return System.getenv(name);
    }
}