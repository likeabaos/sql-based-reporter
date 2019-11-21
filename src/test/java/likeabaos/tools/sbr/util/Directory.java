package likeabaos.tools.sbr.util;

public class Directory {
    public static final String TEST_BASE_DIR = "src/test/resources";
    public static final String TEST_CONFIG_DIR = TEST_BASE_DIR + "/config";
    public static final String TEST_DATA_DIR = TEST_BASE_DIR + "/data";

    public static String getConfig(String item) {
        return TEST_CONFIG_DIR + "/" + item;
    }

    public static String getData(String item) {
        return TEST_DATA_DIR + "/" + item;
    }

    public static String getBase(String item) {
        return TEST_BASE_DIR + "/" + item;
    }
}
