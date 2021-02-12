import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.function.Consumer;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

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
    public List<String> inputs;
    public List<String> expectedOutput;

    public ScenarioTest(String testName, String config, List<String> inputs, List<String> expectedOutput) {
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

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() throws Exception {

        ArrayList<Object[]> scenarioObjects = new ArrayList<>();
        // TODO Move to upper folder in system independent way
        File scenarioDirectory = new File("../test_scenarios");

        File[] scenarioList = scenarioDirectory.listFiles();
        for (File scenarioFolder : scenarioList) {

            if (!scenarioFolder.isDirectory())
                continue;

            File[] scenarioConfigFiles = scenarioFolder.listFiles();

            String testName = "";
            List<String> inputs = new ArrayList<>();
            List<String> expectedOutput = new ArrayList<>();
            StringBuilder configBuilder = new StringBuilder();
            for (File scenarioConfigFile : scenarioConfigFiles) {
                testName = scenarioConfigFile.getName();

                if (testName.endsWith("-input.txt")) {

                    // ******************************************************
                    // process scenario input file. Read the user inputs
                    // ******************************************************

                    try (Scanner inputScanner = new Scanner(scenarioConfigFile)) {
                        while (inputScanner.hasNext())
                            inputs.add(inputScanner.nextLine());
                    }

                } else if (testName.endsWith("-board.txt")) {

                    // ******************************************************
                    // process scenario input file
                    // ******************************************************

                    try (Scanner boardScanner = new Scanner(scenarioConfigFile)) {

                        // read board configuration inputs for the game
                        while (boardScanner.hasNext())
                            configBuilder.append(boardScanner.nextLine()).append('\n');
                    }

                } else if (testName.endsWith("-expectedOutput.txt")) {

                    // ******************************************************
                    // process scenario expected output file
                    // ******************************************************

                    try (Scanner expectedOutputScanner = new Scanner(scenarioConfigFile)) {
                        while (expectedOutputScanner.hasNext())
                            expectedOutput.add(expectedOutputScanner.nextLine());
                    }
                }
            }

            scenarioObjects.add(new Object[] { testName.substring(0, testName.indexOf('-')), configBuilder.toString(),
                    inputs, expectedOutput });
        }

        return scenarioObjects;
    }

    /**
     * This function takes three parameters and produce nothing
     * 
     * https://www.javatips.net/api/mdk-master/src/main/java/gov/nasa/jpl/mbee/mdk/api/function/TriConsumer.java
     * 
     * Created by igomes on 9/15/16.
     *
     * Represents an operation that accepts three input arguments and returns no
     * result. This is the three-arity specialization of {@link Consumer}. Unlike
     * most other functional interfaces, {@code TriConsumer} is expected to operate
     * via side-effects.
     * <p>
     * <p>
     * This is a <a href="package-summary.html">functional interface</a> whose
     * functional method is {@link #accept(Object, Object, Object)}.
     *
     * @param <T> the type of the first argument to the operation
     * @param <U> the type of the second argument to the operation
     * @param <V> the type of the third argument to the operation
     * @see Consumer
     * @since 1.8
     */
    @FunctionalInterface
    public interface TriConsumer<T, U, V> {

        /**
         * Performs this operation on the given arguments.
         *
         * @param t the first input argument
         * @param u the second input argument
         */
        void accept(T t, U u, V v);

        /**
         * Returns a composed {@code BiConsumer} that performs, in sequence, this
         * operation followed by the {@code after} operation. If performing either
         * operation throws an exception, it is relayed to the caller of the composed
         * operation. If performing this operation throws an exception, the
         * {@code after} operation will not be performed.
         *
         * @param after the operation to perform after this operation
         * @return a composed {@code BiConsumer} that performs in sequence this
         *         operation followed by the {@code after} operation
         * @throws NullPointerException if {@code after} is null
         */
        default TriConsumer<T, U, V> andThen(TriConsumer<? super T, ? super U, ? super V> after) {
            Objects.requireNonNull(after);

            return (l, r, s) -> {
                accept(l, r, s);
                after.accept(l, r, s);
            };
        }
    }

    /**
     * This method
     * 
     * @param <T1>
     * @param <T2>
     * @param c1
     * @param c2
     * @param consumer
     */
    private static <T1, T2> void iterateSimultaneously(Iterable<T1> c1, Iterable<T2> c2,
            TriConsumer<T1, T2, Integer> consumer) {
        Iterator<T1> i1 = c1.iterator();
        Iterator<T2> i2 = c2.iterator();
        int index = 0;
        while (i1.hasNext() && i2.hasNext()) {
            index = index + 1;
            consumer.accept(i1.next(), i2.next(), index);
        }
    }

    @Before
    public void createDefatulConfigIni() throws FileNotFoundException {
        File configIni = new File(MinesweeperTestUtils.getMinesweeper(), "config.ini");
        if (configIni.exists()) {
            boolean deleted = configIni.delete();
            if (!deleted) {
                Assert.fail("Cannot delete " + configIni.getAbsolutePath());
            }
        }

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

    @Test(timeout = 1000)
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

        List<String> actualOutputLines = Arrays.asList(stdOut.split("\n"));

        // Report only the first difference
        iterateSimultaneously(actualOutputLines, expectedOutput,
                (String actualLine, String expectedLine, Integer lineNumber) -> {
                    MatcherAssert.assertThat(
                            MinesweeperTestUtils.MINESWEEPER_CLASS_NAME
                                    + " did not produce the expected output on Line " + lineNumber,
                            actualLine, Matchers.equalTo(expectedLine));
                });

    }
}