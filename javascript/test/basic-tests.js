// Maybe use .strict
const assert = require('assert');
const os = require('os');

const fs = require("fs");
const path = require("path");

const { stdin } = require('mock-stdin');
const { spawn } = require('child_process');
const readline = require('readline');

// taken and adapted from: https://stackoverflow.com/questions/18052762/remove-directory-which-is-not-empty
const deleteFolderWithFiles = function (dir) {
    if (fs.existsSync(dir)) {
        fs.readdirSync(dir).forEach((file, index) => {
            fs.unlinkSync(path.join(dir, file));
        });
        fs.rmdirSync(dir);
    }
};

// Find your module using the --minesweeper_home variable and the default name 'minesweeper'
const minesweeperHome = process.env.npm_config_minesweeper_home.endsWith("/")
    ? process.env.npm_config_minesweeper_home
    : process.env.npm_config_minesweeper_home + "/";
const minesweeper = require(minesweeperHome + "minesweeper");

describe('Basic Tests', function () {

    describe('Runs correct version of node. Tag: Assignment1, Assignment2, Assignment3, Assignment4, Assignment5', function () {
        it('Version of node must be v10.23.0', function () {
            assert.strictEqual(process.version, 'v10.23.0');
        });
    });

    // https://stackoverflow.com/questions/21587122/mocha-chai-expect-to-throw-not-catching-thrown-errors
    // assert.throws( FunctionThatShouldThrow_AssertionError, assert.AssertionError )
    describe('Runs with non-null exit codes. Tag: Assignment1, Assignment2, Assignment3, Assignment4, Assignment5', function () {

        var tempDir;
        var minesweeperProcess;

        beforeEach("Create temporary folder for file creation", function () {
            tempDir = fs.mkdtempSync(os.tmpdir() + path.sep);
        });

        afterEach("Remove temporary folder for file creation", function () {
            // storing the global variable into a local one for each test might be a bit of a hack,
            // but because of the asynchronous behavior there is not really another chance (to the best of my knowledge)
            // unless abstaining from the usage of before/afterEach.
            var tempDirLocal = tempDir;
            minesweeperProcess.on('close', () => {
                deleteFolderWithFiles(tempDirLocal);
            });
        });

        it('Minesweeper should exit with code 1 if input file is not provided', function () {
            minesweeperProcess = spawn('node', [minesweeperHome + "minesweeper.js"]);

            minesweeperProcess.on('close', (exitCode) => {
                assert.strictEqual(exitCode, 1, "Wrong exit code for missing board configuration file")
            });
        });

        it('Minesweeper should exit with code 1 if input file does not exist', function () {
            minesweeperProcess = spawn('node', [minesweeperHome + "minesweeper.js", "simple.cfg"]);

            minesweeperProcess.on('close', (exitCode) => {
                assert.strictEqual(exitCode, 1, "Wrong exit code for not existent board configuration file")
            });
        });

        it('Minesweeper should exit with code 2 if input file is invalid (empty)', function () {
            // Make sure there's an empty "simple.cfg" file (see https://flaviocopes.com/how-to-create-empty-file-node/)
            fs.closeSync(fs.openSync(tempDir + "/simple.cfg", 'w'))

            var stats = fs.statSync(tempDir + "/simple.cfg")

            minesweeperProcess = spawn('node', [minesweeperHome + "minesweeper.js", tempDir + "/simple.cfg"]);

            const rl = readline.createInterface({ input: minesweeperProcess.stdout });
            rl.on('line', line => {
                console.log(line)
            });

            minesweeperProcess.on('close', (exitCode) => {
                assert.strictEqual(exitCode, 2, "Wrong exit code for invalid (empty) board configuration file")
            });
        });
    });

    describe('Runs with null exit code.  Tag: Assignment1, Assignment2, Assignment3, Assignment4, Assignment5', function () {

        var stdin;
        var tempDir;
        var minesweeperProcess;

        beforeEach("Create temporary folder for file creation", function () {
            tempDir = fs.mkdtempSync(os.tmpdir() + path.sep);
        });

        afterEach("Remove temporary folder for file creation", function () {
            // storing the global variable into a local one for each test might be a bit of a hack,
            // but because of the asynchronous behavior there is not really another chance (to the best of my knowledge)
            // unless abstaining from the usage of before/afterEach.
            var tempDirLocal = tempDir;
            minesweeperProcess.on('close', () => {
                deleteFolderWithFiles(tempDirLocal);
            });
        });

        beforeEach("Start Mockin stdin", function () {
            stdin = require('mock-stdin').stdin();
        });

        afterEach("Stop Mocking stdin", function () {
            stdin.end();
        });

        it('Minesweeper should raise no exception', function () {
            // Creates the simple.cfg file
            const lines = ['..*', '...', '...'];
            fs.writeFileSync(tempDir + "/simple.cfg", lines.join('\n') + '\n')

            minesweeperProcess = spawn('node', [minesweeperHome + "minesweeper.js", tempDir + "/simple.cfg"]);

            // send inputs to the subprocess
            minesweeperProcess.stdin.write("1 1 R\n");

            minesweeperProcess.on('close', (exitCode) => {
                assert.strictEqual(exitCode, 0, "Wrong exit for valid (won) game")
            });
        });

        it('Win Minesweeper after first move', function () {
            // Creates the simple.cfg file
            const lines = ['..*', '...', '...'];
            fs.writeFileSync(tempDir + "/simple.cfg", lines.join('\n') + '\n')
            var lengthUI = (lines.length * 2 + 1) + 3; // three lines for the message box

            var expectedBoards = [
                "┌───┬───┬───┐\n"
                + "│   │   │   │\n"
                + "├───┼───┼───┤\n"
                + "│   │   │   │\n"
                + "├───┼───┼───┤\n"
                + "│   │   │   │\n"
                + "└───┴───┴───┘\n"
                + "╔═══════════╗\n"
                + "║           ║\n"
                + "╚═══════════╝\n",
                "┌───┬───┬───┐\n"
                + "│ ▓ │ 1 │   │\n"
                + "├───┼───┼───┤\n"
                + "│ ▓ │ 1 │ 1 │\n"
                + "├───┼───┼───┤\n"
                + "│ ▓ │ ▓ │ ▓ │\n"
                + "└───┴───┴───┘\n"
                + "╔═══════════╗\n"
                + "║You Won!   ║\n"
                + "╚═══════════╝\n"
            ]

            // spawn child process to execute Minesweeper instance
            minesweeperProcess = spawn('node', [minesweeperHome + "minesweeper.js", tempDir + "/simple.cfg"]);

            // send inputs to the subprocess
            minesweeperProcess.stdin.write("1 1 R\n");

            // observe stdout of the child process and verify the correctness of the board + message box
            // after each UI refresh
            var actualBoard = [];
            var moveCount = 0;

            const rl = readline.createInterface({ input: minesweeperProcess.stdout });
            rl.on('line', line => {
                if (!line.includes(">")) { // only check the board + message box in this test case
                    actualBoard.push(line);

                    if (actualBoard.length == lengthUI) {
                        assert.strictEqual(actualBoard.join('\n') + '\n', expectedBoards[moveCount], "Actual board after move " + (moveCount + 1) + " is wrong");
                        if (moveCount == expectedBoards.length - 1) { // end of the game is reached
                            rl.close();
                        } else {
                            actualBoard = [];
                            moveCount++;
                        }
                    }
                }
            });
        });
    });
});