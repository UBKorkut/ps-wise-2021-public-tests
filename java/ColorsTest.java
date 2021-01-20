
/**
 * Programming Styles WiSe 20-21 Basic Test Case
 * 
 * @author gambi
 */
//See https://www.baeldung.com/hamcrest-text-matchers
import static org.hamcrest.text.IsBlankString.blankOrNullString;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

//See https://junit.org/junit4/javadoc/latest/deprecated-list.html
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.TemporaryFolder;

/**
 * This class contains test cases to check whether the fancy plugin generates
 * the correct colored output Minesweeper
 * 
 * @author gambi
 *
 */
/*
 * This test must be executed only for Assignment3.
 */
@Category(Assignment3.class)
public class ColorsTest {

    public static final String ESCAPED_RESET = "\\u001B\\[0m";
    public static final String ESCAPED_BLUE_1 = "\\u001B\\[34m";

    public static final String ESCAPED_GREEN_2 = "\\u001B\\[32m";
    public static final String ESCAPED_RED_3 = "\\u001B\\[31m";
    public static final String ESCAPED_PURPLE_4 = "\\u001B\\[35m";
    public static final String ESCAPED_WHITE_5 = "\\u001B\\[37m";
    public static final String ESCAPED_YELLOW_6 = "\\u001B\\[33m";
    public static final String ESCAPED_GRAY_7 = "\\u001B\\[90m";
    public static final String ESCAPED_CYAN_8 = "\\u001B\\[36m";

    // TODO This does not seems to work
    public static Pattern matchAnyColor = Pattern.compile(".*" + "\\u001B\\[[;\\d]*m" + ".*");

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @BeforeClass
    public static void checkPrecondition() {
        /*
         * Check that the required variables and configurations are set
         */
        MinesweeperTestUtils.validateTheExecutionEnvironment();
    }

    @Before
    public void createFancyConfigIni() throws FileNotFoundException {
        File configIni = new File(MinesweeperTestUtils.getMinesweeper(), "config.ini");
        if (configIni.exists()) {
            boolean deleted = configIni.delete();
            if (!deleted) {
                Assert.fail("Cannot delete fancy plugin: " + configIni.getAbsolutePath());
            }
        }

        try (PrintWriter pw = new PrintWriter(configIni)) {
            pw.println("plugin-name=fancy");
            pw.println("mine-symbol=M");
            pw.println("flag-symbol=F");
        }
    }

    @After
    public void deleteFancyConfigIni() {
        File configIni = new File(MinesweeperTestUtils.getMinesweeper(), "config.ini");
        if (configIni.exists()) {
            boolean deleted = configIni.delete();
            if (!deleted) {
                Assert.fail("Cannot delete fancy plugin: " + configIni.getAbsolutePath());
            }
        }
    }

    // Return the pattern to check that a digit with the given color is found in a
    // given position (e.g., 3 column)
    public static Pattern genereatePatternForDigitInColumn(int digit, int column) {
        String escaped_color_regex = null;
        switch (digit) {
        case 1:
            escaped_color_regex = ESCAPED_BLUE_1;
            break;
        case 2:
            escaped_color_regex = ESCAPED_GREEN_2;
            break;
        case 3:
            escaped_color_regex = ESCAPED_RED_3;
            break;
        case 4:
            escaped_color_regex = ESCAPED_PURPLE_4;
            break;
        case 5:
            escaped_color_regex = ESCAPED_WHITE_5;
            break;
        case 6:
            escaped_color_regex = ESCAPED_YELLOW_6;
            break;
        case 7:
            escaped_color_regex = ESCAPED_GRAY_7;
            break;
        case 8:
            escaped_color_regex = ESCAPED_CYAN_8;
            break;

        default:
            break;
        }

        // Count how many cells are in front of it and for each create a sub patter to
        // Pay attention that the char │ is not the "pipe", even if it looks similar to
        // |
        // match whatever content they have
        String beforeTargetColumn = "│.*".repeat(column - 1);
        // We do not care what's after it, so anything workds
        String afterTargetColumn = ".*";
        //
        String thePattern = "^" + beforeTargetColumn + //
                "│.*" + escaped_color_regex + ".*" + digit + ".*" + ESCAPED_RESET + ".*│" + //
                afterTargetColumn + "$";

        return Pattern.compile(thePattern);
    }

    @Test(timeout = 3000)
    public void testThatNoColorsOnStartup() throws Exception {
        // Setup: Provide the configuration file
        //
        // Create a temporary file.
        final File boardCfgFile = tempFolder.newFile("simple.cfg");

        // Write the simple 3x3 board to the file
        try (PrintWriter out = new PrintWriter(boardCfgFile)) {
            out.println("..*");
            out.println("...");
            out.println("...");
        }

        // Define the list of inputs to provide to Minesweeper
        List<String> inputSequence = new ArrayList<String>();
        // Input 1: space space 1 space 1 space R
        // This should output a BLUE 1 in square 1 2 on second page
        inputSequence.add("  1 2 R");

        // Execute Minesweeper in a separate process starting it with the boardCfgFile
        // and passing the inputSequence
        Map<String, Object> result = MinesweeperTestUtils.execute(boardCfgFile, inputSequence);

        int exitCode = (Integer) result.get("exitCode");
        String stdOut = (String) result.get("stdOut");
        String stdError = (String) result.get("stdError");

        // Did the program exit normally?
        Assert.assertEquals(MinesweeperTestUtils.MINESWEEPER_CLASS_NAME + " did not exit normally. Error message: "
                + stdError + "\n", 0, exitCode);

        // Did program produce any output at all?
        MatcherAssert.assertThat(MinesweeperTestUtils.MINESWEEPER_CLASS_NAME + " did not produced any output!", stdOut,
                Matchers.not(blankOrNullString()));

        // no colors in the output
        Assert.assertFalse(matchAnyColor.matcher(stdOut).matches());

    }

    @Test(timeout = 3000)
    public void testThatOneIsCorrectlyColored() throws Exception {
        // Setup: Provide the configuration file
        //
        // Create a temporary file.
        final File boardCfgFile = tempFolder.newFile("simple.cfg");

        // Write the simple 3x3 board to the file
        try (PrintWriter out = new PrintWriter(boardCfgFile)) {
            out.println("..*");
            out.println("...");
            out.println("...");
        }

        // Define the list of inputs to provide to Minesweeper
        List<String> inputSequence = new ArrayList<String>();
        // Input 1: space space 1 space 1 space R
        // This should output a BLUE 1 in square 1 2 on second page
        inputSequence.add("  1 2 R");

        // Execute Minesweeper in a separate process starting it with the boardCfgFile
        // and passing the inputSequence
        Map<String, Object> result = MinesweeperTestUtils.execute(boardCfgFile, inputSequence);

        int exitCode = (Integer) result.get("exitCode");
        String stdOut = (String) result.get("stdOut");
        String stdError = (String) result.get("stdError");

        // Did the program exit normally?
        Assert.assertEquals(MinesweeperTestUtils.MINESWEEPER_CLASS_NAME + " did not exit normally. Error message: "
                + stdError + "\n", 0, exitCode);

        // Did program produce any output at all?
        MatcherAssert.assertThat(MinesweeperTestUtils.MINESWEEPER_CLASS_NAME + " did not produced any output!", stdOut,
                Matchers.not(blankOrNullString()));

        // Some colors in the output
        Assert.assertTrue("Cannot find colors in:\n " + stdOut.replaceAll("\\n", " "),
                matchAnyColor.matcher(stdOut.replaceAll("\\n", " ")).matches());

        String[] lines = stdOut.split("\n");
        String targetLine = lines[11];

        int digit = 1;
        int column = 2;

        Assert.assertTrue("Wrong string. I cannot find the right colored digit in the expected position (1,2): ",
                genereatePatternForDigitInColumn(digit, column).matcher(targetLine).matches());

    }

    @Test(timeout = 3000)
    public void testThatTwoIsCorrectlyColored() throws Exception {
        // Setup: Provide the configuration file
        //
        // Create a temporary file.
        final File boardCfgFile = tempFolder.newFile("simple.cfg");

        // Write the simple 3x3 board to the file
        try (PrintWriter out = new PrintWriter(boardCfgFile)) {
            out.println("*.*");
            out.println("...");
            out.println("...");
        }

        // Define the list of inputs to provide to Minesweeper
        List<String> inputSequence = new ArrayList<String>();
        // Input 1: space space 1 space 1 space R
        // This should output a BLUE 1 in square 1 2 on second page
        inputSequence.add("  1 2 R");

        // Execute Minesweeper in a separate process starting it with the boardCfgFile
        // and passing the inputSequence
        Map<String, Object> result = MinesweeperTestUtils.execute(boardCfgFile, inputSequence);

        int exitCode = (Integer) result.get("exitCode");
        String stdOut = (String) result.get("stdOut");
        String stdError = (String) result.get("stdError");

        // Did the program exit normally?
        Assert.assertEquals(MinesweeperTestUtils.MINESWEEPER_CLASS_NAME + " did not exit normally. Error message: "
                + stdError + "\n", 0, exitCode);

        // Did program produce any output at all?
        MatcherAssert.assertThat(MinesweeperTestUtils.MINESWEEPER_CLASS_NAME + " did not produced any output!", stdOut,
                Matchers.not(blankOrNullString()));

        // Some colors in the output
        Assert.assertTrue("Cannot find colors in:\n " + stdOut.replaceAll("\\n", " "),
                matchAnyColor.matcher(stdOut.replaceAll("\\n", " ")).matches());

        String[] lines = stdOut.split("\n");
        String targetLine = lines[11];

        int digit = 2;
        int column = 2;

        Assert.assertTrue("Wrong string. I cannot find the right colored digit in the expected position (1,2): ",
                genereatePatternForDigitInColumn(digit, column).matcher(targetLine).matches());

    }

    @Test(timeout = 3000)
    public void testThatThreeIsCorrectlyColored() throws Exception {
        // Setup: Provide the configuration file
        //
        // Create a temporary file.
        final File boardCfgFile = tempFolder.newFile("simple.cfg");

        // Write the simple 3x3 board to the file
        try (PrintWriter out = new PrintWriter(boardCfgFile)) {
            out.println("*.*");
            out.println(".*.");
            out.println("...");
        }

        // Define the list of inputs to provide to Minesweeper
        List<String> inputSequence = new ArrayList<String>();
        // Input 1: space space 1 space 1 space R
        // This should output a BLUE 1 in square 1 2 on second page
        inputSequence.add("  1 2 R");

        // Execute Minesweeper in a separate process starting it with the boardCfgFile
        // and passing the inputSequence
        Map<String, Object> result = MinesweeperTestUtils.execute(boardCfgFile, inputSequence);

        int exitCode = (Integer) result.get("exitCode");
        String stdOut = (String) result.get("stdOut");
        String stdError = (String) result.get("stdError");

        // Did the program exit normally?
        Assert.assertEquals(MinesweeperTestUtils.MINESWEEPER_CLASS_NAME + " did not exit normally. Error message: "
                + stdError + "\n", 0, exitCode);

        // Did program produce any output at all?
        MatcherAssert.assertThat(MinesweeperTestUtils.MINESWEEPER_CLASS_NAME + " did not produced any output!", stdOut,
                Matchers.not(blankOrNullString()));

        // Some colors in the output
        Assert.assertTrue("Cannot find colors in:\n " + stdOut.replaceAll("\\n", " "),
                matchAnyColor.matcher(stdOut.replaceAll("\\n", " ")).matches());

        String[] lines = stdOut.split("\n");
        String targetLine = lines[11];

        int digit = 3;
        int column = 2;

        Assert.assertTrue("Wrong string. I cannot find the right colored digit in the expected position (1,2): ",
                genereatePatternForDigitInColumn(digit, column).matcher(targetLine).matches());

    }

    @Test(timeout = 3000)
    public void testThatFourIsCorrectlyColored() throws Exception {
        // Setup: Provide the configuration file
        //
        // Create a temporary file.
        final File boardCfgFile = tempFolder.newFile("simple.cfg");

        // Write the simple 3x3 board to the file
        try (PrintWriter out = new PrintWriter(boardCfgFile)) {
            out.println("*.*");
            out.println("**.");
            out.println("...");
        }

        // Define the list of inputs to provide to Minesweeper
        List<String> inputSequence = new ArrayList<String>();
        // Input 1: space space 1 space 1 space R
        // This should output a BLUE 1 in square 1 2 on second page
        inputSequence.add("  1 2 R");

        // Execute Minesweeper in a separate process starting it with the boardCfgFile
        // and passing the inputSequence
        Map<String, Object> result = MinesweeperTestUtils.execute(boardCfgFile, inputSequence);

        int exitCode = (Integer) result.get("exitCode");
        String stdOut = (String) result.get("stdOut");
        String stdError = (String) result.get("stdError");

        // Did the program exit normally?
        Assert.assertEquals(MinesweeperTestUtils.MINESWEEPER_CLASS_NAME + " did not exit normally. Error message: "
                + stdError + "\n", 0, exitCode);

        // Did program produce any output at all?
        MatcherAssert.assertThat(MinesweeperTestUtils.MINESWEEPER_CLASS_NAME + " did not produced any output!", stdOut,
                Matchers.not(blankOrNullString()));

        String[] lines = stdOut.split("\n");
        String targetLine = lines[11];

        int digit = 4;
        int column = 2;

        Assert.assertTrue("Wrong string. I cannot find the right colored digit in the expected position (1,2): ",
                genereatePatternForDigitInColumn(digit, column).matcher(targetLine).matches());

    }

    @Test(timeout = 3000)
    public void testThatFiveIsCorrectlyColored() throws Exception {
        // Setup: Provide the configuration file
        //
        // Create a temporary file.
        final File boardCfgFile = tempFolder.newFile("simple.cfg");

        // Write the simple 3x3 board to the file
        try (PrintWriter out = new PrintWriter(boardCfgFile)) {
            out.println("*.*");
            out.println("***");
            out.println("...");
        }

        // Define the list of inputs to provide to Minesweeper
        List<String> inputSequence = new ArrayList<String>();
        // Input 1: space space 1 space 1 space R
        // This should output a BLUE 1 in square 1 2 on second page
        inputSequence.add("  1 2 R");

        // Execute Minesweeper in a separate process starting it with the boardCfgFile
        // and passing the inputSequence
        Map<String, Object> result = MinesweeperTestUtils.execute(boardCfgFile, inputSequence);

        int exitCode = (Integer) result.get("exitCode");
        String stdOut = (String) result.get("stdOut");
        String stdError = (String) result.get("stdError");

        // Did the program exit normally?
        Assert.assertEquals(MinesweeperTestUtils.MINESWEEPER_CLASS_NAME + " did not exit normally. Error message: "
                + stdError + "\n", 0, exitCode);

        // Did program produce any output at all?
        MatcherAssert.assertThat(MinesweeperTestUtils.MINESWEEPER_CLASS_NAME + " did not produced any output!", stdOut,
                Matchers.not(blankOrNullString()));

        // Some colors in the output
        Assert.assertTrue("Cannot find colors in:\n " + stdOut.replaceAll("\\n", " "),
                matchAnyColor.matcher(stdOut.replaceAll("\\n", " ")).matches());

        String[] lines = stdOut.split("\n");
        String targetLine = lines[11];

        int digit = 5;
        int column = 2;

        Assert.assertTrue("Wrong string. I cannot find the right colored digit in the expected position (1,2): ",
                genereatePatternForDigitInColumn(digit, column).matcher(targetLine).matches());

    }

    @Test(timeout = 3000)
    public void testThatSixIsCorrectlyColored() throws Exception {
        // Setup: Provide the configuration file
        //
        // Create a temporary file.
        final File boardCfgFile = tempFolder.newFile("simple.cfg");

        // Write the simple 3x3 board to the file
        try (PrintWriter out = new PrintWriter(boardCfgFile)) {
            out.println("*.*");
            out.println("*.*");
            out.println("*.*");
        }

        // Define the list of inputs to provide to Minesweeper
        List<String> inputSequence = new ArrayList<String>();
        // Input 1: space space 1 space 1 space R
        // This should output a BLUE 1 in square 1 2 on second page
        inputSequence.add("  2 2 R");

        // Execute Minesweeper in a separate process starting it with the boardCfgFile
        // and passing the inputSequence
        Map<String, Object> result = MinesweeperTestUtils.execute(boardCfgFile, inputSequence);

        int exitCode = (Integer) result.get("exitCode");
        String stdOut = (String) result.get("stdOut");
        String stdError = (String) result.get("stdError");

        // Did the program exit normally?
        Assert.assertEquals(MinesweeperTestUtils.MINESWEEPER_CLASS_NAME + " did not exit normally. Error message: "
                + stdError + "\n", 0, exitCode);

        // Did program produce any output at all?
        MatcherAssert.assertThat(MinesweeperTestUtils.MINESWEEPER_CLASS_NAME + " did not produced any output!", stdOut,
                Matchers.not(blankOrNullString()));

        // Some colors in the output
        Assert.assertTrue("Cannot find colors in:\n " + stdOut.replaceAll("\\n", " "),
                matchAnyColor.matcher(stdOut.replaceAll("\\n", " ")).matches());

        String[] lines = stdOut.split("\n");
        String targetLine = lines[13];

        int digit = 6;
        int column = 2;

        Assert.assertTrue("Wrong string. I cannot find the right colored digit in the expected position (1,2): ",
                genereatePatternForDigitInColumn(digit, column).matcher(targetLine).matches());

    }

    @Test(timeout = 3000)
    public void testThatSevenIsCorrectlyColored() throws Exception {
        // Setup: Provide the configuration file
        //
        // Create a temporary file.
        final File boardCfgFile = tempFolder.newFile("simple.cfg");

        // Write the simple 3x3 board to the file
        try (PrintWriter out = new PrintWriter(boardCfgFile)) {
            out.println("***");
            out.println("*.*");
            out.println("*.*");
        }

        // Define the list of inputs to provide to Minesweeper
        List<String> inputSequence = new ArrayList<String>();
        // Input 1: space space 1 space 1 space R
        // This should output a BLUE 1 in square 1 2 on second page
        inputSequence.add("  2 2 R");

        // Execute Minesweeper in a separate process starting it with the boardCfgFile
        // and passing the inputSequence
        Map<String, Object> result = MinesweeperTestUtils.execute(boardCfgFile, inputSequence);

        int exitCode = (Integer) result.get("exitCode");
        String stdOut = (String) result.get("stdOut");
        String stdError = (String) result.get("stdError");

        // Did the program exit normally?
        Assert.assertEquals(MinesweeperTestUtils.MINESWEEPER_CLASS_NAME + " did not exit normally. Error message: "
                + stdError + "\n", 0, exitCode);

        // Did program produce any output at all?
        MatcherAssert.assertThat(MinesweeperTestUtils.MINESWEEPER_CLASS_NAME + " did not produced any output!", stdOut,
                Matchers.not(blankOrNullString()));

        // Some colors in the output
        Assert.assertTrue("Cannot find colors in:\n " + stdOut.replaceAll("\\n", " "),
                matchAnyColor.matcher(stdOut.replaceAll("\\n", " ")).matches());

        String[] lines = stdOut.split("\n");
        String targetLine = lines[13];

        int digit = 7;
        int column = 2;

        Assert.assertTrue("Wrong string. I cannot find the right colored digit in the expected position (1,2): ",
                genereatePatternForDigitInColumn(digit, column).matcher(targetLine).matches());

    }

    @Test(timeout = 3000)
    public void testThatEightIsCorrectlyColored() throws Exception {
        // Setup: Provide the configuration file
        //
        // Create a temporary file.
        final File boardCfgFile = tempFolder.newFile("simple.cfg");

        // Write the simple 3x3 board to the file
        try (PrintWriter out = new PrintWriter(boardCfgFile)) {
            out.println("***");
            out.println("*.*");
            out.println("***");
        }

        // Define the list of inputs to provide to Minesweeper
        List<String> inputSequence = new ArrayList<String>();
        // Input 1: space space 1 space 1 space R
        // This should output a BLUE 1 in square 1 2 on second page
        inputSequence.add("  2 2 R");

        // Execute Minesweeper in a separate process starting it with the boardCfgFile
        // and passing the inputSequence
        Map<String, Object> result = MinesweeperTestUtils.execute(boardCfgFile, inputSequence);

        int exitCode = (Integer) result.get("exitCode");
        String stdOut = (String) result.get("stdOut");
        String stdError = (String) result.get("stdError");

        // Did the program exit normally?
        Assert.assertEquals(MinesweeperTestUtils.MINESWEEPER_CLASS_NAME + " did not exit normally. Error message: "
                + stdError + "\n", 0, exitCode);

        // Did program produce any output at all?
        MatcherAssert.assertThat(MinesweeperTestUtils.MINESWEEPER_CLASS_NAME + " did not produced any output!", stdOut,
                Matchers.not(blankOrNullString()));

        // Some colors in the output
        Assert.assertTrue("Cannot find colors in:\n " + stdOut.replaceAll("\\n", " "),
                matchAnyColor.matcher(stdOut.replaceAll("\\n", " ")).matches());

        String[] lines = stdOut.split("\n");
        String targetLine = lines[13];

        int digit = 8;
        int column = 2;

        Assert.assertTrue("Wrong string. I cannot find the right colored digit in the expected position (1,2): ",
                genereatePatternForDigitInColumn(digit, column).matcher(targetLine).matches());

    }

}
