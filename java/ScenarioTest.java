import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.PrintWriter;
import java.util.*;

/**
 * This class contains a simple parameterized test for testing scenarios
 * contained in the folder test_scenarios
 *
 * @author lechlm
 */
@RunWith(Parameterized.class)
public class ScenarioTest {
    public String testName;
    public String config;
    public ArrayList<String> inputs;
    public String expectedOutput;

    public ScenarioTest(String testName, String config, ArrayList<String> inputs, String expectedOutput) {
        this.testName = testName;
        this.config = config;
        this.inputs = inputs;
        this.expectedOutput = expectedOutput;
    }

    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @BeforeClass
    public static void checkPrecondition() {
        /*
         * Check that the required variables and configurations are set
         */
        MinesweeperTestUtils.validateTheExecutionEnvironment();
    }

    @Parameterized.Parameters(name="{0}")
    public static Collection<Object[]> data() throws Exception {
        ArrayList<Object[]> scenarioObjects = new ArrayList<>();
        File scenarioDirectory = new File("test_scenarios");

        File[] scenarioList = scenarioDirectory.listFiles();
        if(scenarioList != null) {
            for (File scenario : scenarioList) {
                if (!scenario.isDirectory())
                    continue;

                File[] scenarioConfigFiles = scenario.listFiles();
                if (scenarioConfigFiles == null ||
                        scenarioConfigFiles.length != 2) continue;

                String testName = "";
                ArrayList<String> inputs = new ArrayList<>();
                StringBuilder expectedOutputBuilder = new StringBuilder(), configBuilder = new StringBuilder();
                for (File scenarioConfigFile : scenarioConfigFiles) {
                    testName = scenarioConfigFile.getName();

                    if (testName.endsWith("-input.txt")) {

                        // ******************************************************
                        // process scenario input file
                        // ******************************************************

                        Scanner inputScanner = new Scanner(scenarioConfigFile);

                        // first line is the board size
                        int boardSize = inputScanner.nextInt();
                        inputScanner.nextLine(); // skip \n at the end of the board size line

                        // read config file
                        for (int i = 0; i < boardSize; i++) {
                            configBuilder.append(inputScanner.nextLine()).append('\n');
                        }

                        // read user inputs for the game
                        while (inputScanner.hasNext())
                            inputs.add(inputScanner.nextLine());

                    } else {

                        // ******************************************************
                        // process scenario expected output file
                        // ******************************************************

                        Scanner expectedOutputScanner = new Scanner(scenarioConfigFile);
                        while (expectedOutputScanner.hasNext())
                            expectedOutputBuilder.append(expectedOutputScanner.nextLine()).append('\n');
                    }
                }

                scenarioObjects.add(new Object[] {testName.substring(0, testName.indexOf('-')), configBuilder.toString(), inputs,
                        expectedOutputBuilder.toString()});
            }
        }

        return scenarioObjects;
    }

    @Test
    public void testScenario() throws Exception {
        // code adapted from the BasicTest class

        final File boardCfgFile = tempFolder.newFile("simple.cfg");

        try (PrintWriter out = new PrintWriter(boardCfgFile)) {
            out.print(config);
        }

        // Define the list of inputs to provide to Minesweeper
        List<String> inputSequence = new ArrayList<>(inputs);

        // Execute Minesweeper in a separate process starting it with the boardCfgFile
        // and passing the inputSequence
        Map<String, Object> result = MinesweeperTestUtils.execute(boardCfgFile, inputSequence);
        String stdOut = (String) result.get("stdOut");

        // Assertions

        // Did program produce the correct output?
        MatcherAssert.assertThat(MinesweeperTestUtils.MINESWEEPER_CLASS_NAME + " did not produce the expected output!", stdOut,
                Matchers.equalTo(expectedOutput));
    }
}