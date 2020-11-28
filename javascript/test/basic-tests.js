// Maybe use .strict
const assert = require('assert');
const os = require('os');

const fs = require("fs");
const path = require("path");

const { spawnSync } = require('child_process');


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

describe('Basic Tests (Public)', function () {

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
            // mkdtempSync requires the trailing `path.sep` to ensure a directory is created
            tempDir = fs.mkdtempSync(os.tmpdir() + path.sep);
            // tempDir does not have the trailing 'path.sep`
            // TODO use path.join([...paths])
            tempDir += path.sep;
        });

        afterEach("Remove temporary folder for file creation", function () {
            deleteFolderWithFiles(tempDir);
        });

        it('Minesweeper should exit with code 1 if input file is not provided', function () {
            minesweeperProcess = spawnSync('node', [minesweeperHome + "minesweeper.js"], { timeout: 3000 })
            assert.strictEqual(minesweeperProcess.status, 1, "Wrong exit code for missing board configuration file")
        });

        it('Minesweeper should exit with code 1 if input file does not exist', function () {
            minesweeperProcess = spawnSync('node', [minesweeperHome + "minesweeper.js", tempDir + "simple.cfg"], { timeout: 3000 })
            assert.strictEqual(minesweeperProcess.status, 1, "Wrong exit code for not existent board configuration file")
        });

        it('Minesweeper should exit with code 2 if input file is invalid (empty)', function () {
            // Make sure there's an empty "simple.cfg" file (see https://flaviocopes.com/how-to-create-empty-file-node/)
            fs.closeSync(fs.openSync(tempDir + "simple.cfg", 'w'))
            var stats = fs.statSync(tempDir + "simple.cfg")
            minesweeperProcess = spawnSync('node', [minesweeperHome + "minesweeper.js", tempDir + "simple.cfg"], { timeout: 3000 })
            assert.strictEqual(minesweeperProcess.status, 2, "Wrong exit code for invalid (empty) board configuration file")
        });
    });

    describe('Runs with null exit code.  Tag: Assignment1, Assignment2, Assignment3, Assignment4, Assignment5', function () {

        var tempDir;
        var minesweeperProcess;

        beforeEach("Create temporary folder for file creation", function () {
            tempDir = fs.mkdtempSync(os.tmpdir() + path.sep);
            tempDir += path.sep;
        });

        afterEach("Remove temporary folder for file creation", function () {
            deleteFolderWithFiles(tempDir);
        });

        it('Minesweeper should raise no exception', function () {
            // Creates the simple.cfg file
            const lines = ['..*', '...', '...'];
            fs.writeFileSync(tempDir + "simple.cfg", lines.join('\n') + '\n')

            minesweeperProcess = spawnSync('node', [minesweeperHome + "minesweeper.js", tempDir + "simple.cfg"], { input: "1 1 R\n", timeout: 3000 });

            assert.strictEqual(minesweeperProcess.status, 0, "Wrong exit for valid (won) game")
        });

        it('Minesweeper should raise no exception with multiple input parameters', function () {
            // Creates the simple.cfg file
            const lines = ['..*', '...', '...'];
            fs.writeFileSync(tempDir + "simple.cfg", lines.join('\n') + '\n')

            minesweeperProcess = spawnSync('node', [minesweeperHome + "minesweeper.js", tempDir + "simple.cfg", "Ignore"], { input: "1 1 R\n", timeout: 3000 });

            assert.strictEqual(minesweeperProcess.status, 0, "Wrong exit for valid (won) game")
        });

        it('Win Minesweeper after first move', function () {
            // Creates the simple.cfg file
            const lines = ['..*', '...', '...'];
            fs.writeFileSync(tempDir + "simple.cfg", lines.join('\n') + '\n')

            var expectedOutput = [
                "┌───┬───┬───┐\n",
                "│   │   │   │\n",
                "├───┼───┼───┤\n",
                "│   │   │   │\n",
                "├───┼───┼───┤\n",
                "│   │   │   │\n",
                "└───┴───┴───┘\n",
                "╔═══════════╗\n",
                "║           ║\n",
                "╚═══════════╝\n",
                ">",
                "┌───┬───┬───┐\n",
                "│ ▓ │ 1 │   │\n",
                "├───┼───┼───┤\n",
                "│ ▓ │ 1 │ 1 │\n",
                "├───┼───┼───┤\n",
                "│ ▓ │ ▓ │ ▓ │\n",
                "└───┴───┴───┘\n",
                "╔═══════════╗\n",
                "║You Won!   ║\n",
                "╚═══════════╝\n",
            ]

            // spawn child process to execute Minesweeper instance
            // and send inputs to the subprocess
            minesweeperProcess = spawnSync('node', [minesweeperHome + "minesweeper.js", tempDir + "simple.cfg"], { input: "1 1 R\n", timeout: 3000 });

            assert.strictEqual(minesweeperProcess.status, 0, "Wrong exit for valid (won) game")
            // We cannot do '\n' join because of the console character '>'
            // Note that we need to call toString() on stdout
            assert.strictEqual(minesweeperProcess.stdout.toString(), expectedOutput.join(''), "Wrong output")
            // instead of checking one by one the boars, we assert over the entire output


        });
    });
});