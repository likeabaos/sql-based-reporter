package likeabaos.tools.sbr.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class Help {
    public static String readFile(String pathToFile) throws IOException {
        return new String(Files.readAllBytes(Paths.get(pathToFile)));
    }
}
