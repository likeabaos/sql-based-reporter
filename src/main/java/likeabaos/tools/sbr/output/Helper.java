package likeabaos.tools.sbr.output;

public class Helper {
    public static String getClassName(String className) {
	if (className == null || className.contains("."))
	    return className;

	String baseClass = BaseOutput.class.getCanonicalName();
	int pos = baseClass.lastIndexOf(".");
	baseClass = baseClass.substring(0, pos + 1);
	return baseClass + className;
    }
}
