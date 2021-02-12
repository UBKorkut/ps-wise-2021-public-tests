
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
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
 * This class contains basic test cases that illustrate how we can test
 * Minesweeper
 * 
 * @author gambi
 *
 */
/*
 * This test must be executed only for Assignment3. This is exactly BasicTest but for the Fancy plugin. There should be no difference in the behavior
 */
@Category(Assignment3.class)
public class FancyTest {

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

    @Test(timeout = 1000)
    public void testFlagSymbol() throws Exception {
        // Create the board file
        final File boardCfgFile = tempFolder.newFile("simple.cfg");

        // Write the simple 3x3 board to the file
        try (PrintWriter out = new PrintWriter(boardCfgFile)) {
            out.println("..*");
            out.println("...");
            out.println("...");
        }

        // Define the list of inputs to provide to Minesweeper
        List<String> inputSequence = new ArrayList<String>();
        // Flag the square
        inputSequence.add("1 1 F");
        inputSequence.add("2 2 F");
        inputSequence.add("3 3 F");

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
        // Skip the first visualization of the board
        String contentOfCell = "" + lines[10 + 1].charAt(2);
        // Check that there's the F symbol in the expected position: 1 * 2 second line, 1 + 2 column
        Assert.assertEquals("Cannot find expected Flag at position (1, 1). " + stdOut, "F", contentOfCell);
        //
        contentOfCell = "" + lines[20 + 3].charAt(6);
        // Check that there's the F symbol in the expected position: 1 * 2 second line, 1 + 2 column
        Assert.assertEquals("Cannot find expected Flag at position (2, 2). " + stdOut, "F", contentOfCell);
        //
        contentOfCell = "" + lines[30 + 5].charAt(10);
        // Check that there's the F symbol in the expected position: 1 * 2 second line, 1 + 2 column
        Assert.assertEquals("Cannot find expected Flag at position (3, 3). " + stdOut, "F", contentOfCell);
    }
    
    @Test(timeout = 1000)
    public void testFlagUnflagSymbol() throws Exception {
        // Create the board file
        final File boardCfgFile = tempFolder.newFile("simple.cfg");

        // Write the simple 3x3 board to the file
        try (PrintWriter out = new PrintWriter(boardCfgFile)) {
            out.println("..*");
            out.println("...");
            out.println("...");
        }

        // Define the list of inputs to provide to Minesweeper
        List<String> inputSequence = new ArrayList<String>();
        // Flag the square
        inputSequence.add("1 1 F");
        inputSequence.add("1 1 F");
        inputSequence.add("1 1 F");

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
        // Skip the first visualization of the board
        String contentOfCell = "" + lines[10 + 1].charAt(2);
        // Check that there's the F symbol in the expected position when 1 1 F
        Assert.assertEquals("Cannot find expected Flag at position (1, 1). " + stdOut, "F", contentOfCell);
        //
        contentOfCell = "" + lines[20 + 1].charAt(2);
        // Check that there's the F symbol disappear from position 1 1 F (on the next page)
        Assert.assertEquals("Found unexpected Flag at position (1, 1). " + stdOut, " ", contentOfCell);
        //
        contentOfCell = "" + lines[30 + 1].charAt(2);
        // Check that there's the F symbol in the expected position: 1 * 2 second line, 1 + 2 column
        Assert.assertEquals("Cannot find expected Flag at position (1, 1). " + stdOut, "F", contentOfCell);

    }
    
    @Test(timeout = 1000)
    public void testMineSymbol() throws Exception {
        // Create the board file
        final File boardCfgFile = tempFolder.newFile("simple.cfg");

        // Write the simple 3x3 board to the file
        try (PrintWriter out = new PrintWriter(boardCfgFile)) {
            out.println("..*");
            out.println("...");
            out.println("...");
        }

        // Define the list of inputs to provide to Minesweeper
        List<String> inputSequence = new ArrayList<String>();
        // Find the Mine
        inputSequence.add("1 3 R");
        
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
        // Skip the first visualization of the board
        String contentOfCell = "" + lines[10 + 1].charAt(10);
        // Check that there's the F symbol in the expected position when 1 1 F
        Assert.assertEquals("Cannot find expected Mine at position (1, 3). " + stdOut, "M", contentOfCell);
    }
    
    @Test(timeout = 1000)
    public void testFineAllMines() throws Exception {
        // Create the board file
        final File boardCfgFile = tempFolder.newFile("simple.cfg");

        // Write the simple 3x3 board to the file
        try (PrintWriter out = new PrintWriter(boardCfgFile)) {
            out.println("***");
            out.println("*..");
            out.println("..*");
        }

        // Define the list of inputs to provide to Minesweeper
        List<String> inputSequence = new ArrayList<String>();
        // Find the Mine
        inputSequence.add("1 1 R");
        
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
        // Skip the first visualization of the board
        // Check that there's the F symbol in the expected position when 1 1 F
        Assert.assertEquals("Cannot find expected Mine at position (1, 1). " + stdOut, "M", "" + lines[10 + 1].charAt(2));
        // Check that there's the F symbol in the expected position when 1 1 F
        Assert.assertEquals("Cannot find expected Mine at position (1, 2). " + stdOut, "M", "" + lines[10 + 1].charAt(6));
        // Check that there's the F symbol in the expected position when 1 1 F
        Assert.assertEquals("Cannot find expected Mine at position (1, 3). " + stdOut, "M", "" + lines[10 + 1].charAt(10));
        // Check that there's the F symbol in the expected position when 1 1 F
        Assert.assertEquals("Cannot find expected Mine at position (2, 1). " + stdOut, "M", "" + lines[10 + 3].charAt(2));
        // Check that there's the F symbol in the expected position when 1 1 F
        Assert.assertEquals("Cannot find expected Mine at position (1, 3). " + stdOut, "M", "" + lines[10 + 5].charAt(10));
        
    }
}