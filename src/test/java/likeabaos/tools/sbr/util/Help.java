package likeabaos.tools.sbr.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Help {
    public static String readFile(String pathToFile) throws IOException {
        return new String(Files.readAllBytes(Paths.get(pathToFile)));
    }

    public static void deleteFilesInSubfolder(String path) {
        File folder = new File(path);
        File[] items = folder.listFiles();
        if (items != null) {
            for (File item : items) {
                if (item.isFile()) {
                    if (!item.delete())
                        item.deleteOnExit();
                } else {
                    deleteFilesInSubfolder(item.getAbsolutePath());
                }
            }
        }
    }
}
