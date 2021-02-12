import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class CornerCaseInputTest {

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
    public void createDefatulConfigIni() throws FileNotFoundException {
        File configIni = new File(MinesweeperTestUtils.getMinesweeper(), "config.ini");
        try (PrintWriter pw = new PrintWriter(configIni)) {
            pw.println("plugin-name=default");
        }
    }

    @After
    public void deleteDefatulConfigIni() {
        File configIni = new File(MinesweeperTestUtils.getMinesweeper(), "config.ini");
        if (configIni.exists()) {
            boolean deleted = configIni.delete();
            if (!deleted) {
                Assert.fail("Cannot delete " + configIni.getAbsolutePath());
            }

        }
    }

    @Test(timeout = 3000)
    public void testThatAdditionalInputsAreNotConsideredWhenWinningGameAndReveal() throws Exception {
        // Setup: Provide the configuration file
        //
        // Create a temporary file.
        final File boardCfgFile = tempFolder.newFile("simple.cfg");

        // Write the simple 3x3 board to the file
        try (PrintWriter out = new PrintWriter(boardCfgFile)) {
            out.println("..*");
        }

        // Define the list of inputs to provide to Minesweeper
        List<String> inputSequence = new ArrayList<String>();
        //
        inputSequence.add("  1 2 R");
        inputSequence.add("  1 1 R");
        // game at this point is won, so the next input should not cause the game to
        // fail
        inputSequence.add("  3 1 R");

        // Execute Minesweeper in a separate process starting it with the boardCfgFile
        // and passing the inputSequence
        Map<String, Object> result = MinesweeperTestUtils.execute(boardCfgFile, inputSequence);

        int exitCode = (Integer) result.get("exitCode");
        String stdOut = (String) result.get("stdOut");
        String stdError = (String) result.get("stdError");

        // Assertions

        Assert.assertEquals(MinesweeperTestUtils.MINESWEEPER_CLASS_NAME + " did not exit normally. Error message: "
                + stdError + "\n", 0, exitCode);

        // The last three lines of the output must look like this:
        String[] lines = stdOut.split("\n");

        String messageBannerFirstLine = lines[lines.length - 3];
        String messageBannerSecondLine = lines[lines.length - 2];
        String messageBannerThirdLine = lines[lines.length - 1];

        //

        Assert.assertEquals(MinesweeperTestUtils.MINESWEEPER_CLASS_NAME + " did not produced the right output", //
                "╔═══════════╗", messageBannerFirstLine);
        Assert.assertEquals(MinesweeperTestUtils.MINESWEEPER_CLASS_NAME + " did not produced the right output", //
                "║You Won!   ║", messageBannerSecondLine);
        Assert.assertEquals(MinesweeperTestUtils.MINESWEEPER_CLASS_NAME + " did not produced the right output", //
                "╚═══════════╝", messageBannerThirdLine);

    }

    @Test(timeout = 3000)
    public void testThatAdditionalInputsAreNotConsideredWhenWinningGameAndFlag() throws Exception {
        // Setup: Provide the configuration file
        //
        // Create a temporary file.
        final File boardCfgFile = tempFolder.newFile("simple.cfg");

        // Write the simple 3x3 board to the file
        try (PrintWriter out = new PrintWriter(boardCfgFile)) {
            out.println("..*");
        }

        // Define the list of inputs to provide to Minesweeper
        List<String> inputSequence = new ArrayList<String>();
        //
        inputSequence.add("  1 2 R");
        inputSequence.add("  1 1 R");
        // game at this point is won, so the next input should not cause the game board
        // to change
        inputSequence.add("  3 1 F");

        // Execute Minesweeper in a separate process starting it with the boardCfgFile
        // and passing the inputSequence
        Map<String, Object> result = MinesweeperTestUtils.execute(boardCfgFile, inputSequence);

        int exitCode = (Integer) result.get("exitCode");
        String stdOut = (String) result.get("stdOut");
        String stdError = (String) result.get("stdError");

        // Assertions

        Assert.assertEquals(MinesweeperTestUtils.MINESWEEPER_CLASS_NAME + " did not exit normally. Error message: "
                + stdError + "\n", 0, exitCode);

        // The last three lines of the output must look like this:
        String[] lines = stdOut.split("\n");

        // -3 because the message banner takes always 3 lines
        String boardFirstLine = lines[lines.length - 3 - 3];
        String boardSecondLine = lines[lines.length - 2 - 3];
        String boardThirdLine = lines[lines.length - 1 - 3];

        // Note that because the console does not introduce a new line the first line in
        // the tests show the char '>'
        Assert.assertEquals(MinesweeperTestUtils.MINESWEEPER_CLASS_NAME + " did not produced the right output", //
                ">┌───┬───┬───┐", boardFirstLine);
        Assert.assertEquals(MinesweeperTestUtils.MINESWEEPER_CLASS_NAME + " did not produced the right output", //
                "│ ▓ │ 1 │   │", boardSecondLine);
        Assert.assertEquals(MinesweeperTestUtils.MINESWEEPER_CLASS_NAME + " did not produced the right output", //
                "└───┴───┴───┘", boardThirdLine);

    }

    @Test(timeout = 3000)
    public void testThatMissingInputsCauseTheGameToEndWithAMessage() throws Exception {
        // Setup: Provide the configuration file
        //
        // Create a temporary file.
        final File boardCfgFile = tempFolder.newFile("simple.cfg");

        // Write the simple 3x3 board to the file
        try (PrintWriter out = new PrintWriter(boardCfgFile)) {
            out.println("..*");
        }

        // Define the list of inputs to provide to Minesweeper
        List<String> inputSequence = new ArrayList<String>();
        //
        inputSequence.add("  1 2 R");
        // At this point the game is still on, but there are not more inputs, so we
        // expect the program to end with the
        // "Not enough inputs!" message

        // Execute Minesweeper in a separate process starting it with the boardCfgFile
        // and passing the inputSequence
        Map<String, Object> result = MinesweeperTestUtils.execute(boardCfgFile, inputSequence);

        int exitCode = (Integer) result.get("exitCode");
        String stdOut = (String) result.get("stdOut");
        String stdError = (String) result.get("stdError");

        // Assertions

        Assert.assertEquals(MinesweeperTestUtils.MINESWEEPER_CLASS_NAME + " did not exit normally. Error message: "
                + stdError + "\n", 0, exitCode);

        // The last three lines of the output must look like this:
        String[] lines = stdOut.split("\n");

        // Message banner takes always the last 3 lines
        String messageBannerFirstLine = lines[lines.length - 3];
        String messageBannerSecondLine = lines[lines.length - 2];
        String messageBannerThirdLine = lines[lines.length - 1];

        Assert.assertEquals(MinesweeperTestUtils.MINESWEEPER_CLASS_NAME + " did not produced the right output", //
                "╔══════════════════╗", messageBannerFirstLine);
        Assert.assertEquals(MinesweeperTestUtils.MINESWEEPER_CLASS_NAME + " did not produced the right output", //
                "║Not enough inputs!║", messageBannerSecondLine);
        Assert.assertEquals(MinesweeperTestUtils.MINESWEEPER_CLASS_NAME + " did not produced the right output", //
                "╚══════════════════╝", messageBannerThirdLine);

    }
}
