package likeabaos.tools.sbr.output;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestHelper {

    @Test
    public void testGenerateFullClassName() {
	String className = Helper.getClassName("Sample");
	String expected = TestHelper.class.getCanonicalName().replace(TestHelper.class.getSimpleName(), "Sample");
	assertEquals(expected, className);

	expected = "com.company.Class";
	className = Helper.getClassName(expected);
	assertEquals(expected, className);
    }
}
