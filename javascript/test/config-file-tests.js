// Maybe use .strict
const assert = require('assert');
const os = require('os');

const fs = require("fs");
const path = require("path");

const { stdin } = require('mock-stdin');
const { spawnSync } = require('child_process');
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

describe('Config File Tests', function () {
    describe('Runs with 2 exit code. Tag: Assignment1, Assignment2, Assignment3, Assignment4, Assignment5', function () {

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

        it('Minesweeper exit because of invalid extension', function () {
            // Creates the simple.cfg file
            const lines = ['..*', '.,.', '...'];
            fs.writeFileSync(tempDir + "simple", lines.join('\n') + '\n')

            minesweeperProcess = spawnSync('node', [minesweeperHome + "minesweeper.js", tempDir + "simple"], { timeout: 3000 })
            assert.strictEqual(minesweeperProcess.status, 2, "Wrong exit code for invalid board configuration file extension")
        });

        it('Minesweeper exit because of invalid characters (,)', function () {
            // Creates the simple.cfg file
            const lines = ['..*', '.,.', '...'];
            fs.writeFileSync(tempDir + "simple.cfg", lines.join('\n') + '\n')

            minesweeperProcess = spawnSync('node', [minesweeperHome + "minesweeper.js", tempDir + "simple.cfg"], { timeout: 3000 })
            assert.strictEqual(minesweeperProcess.status, 2, "Wrong exit code for invalid (wrong characters) board configuration file")
        });

        it('Minesweeper exit because of new line character in last line missing', function () {
            // Creates the simple.cfg file
            const lines = ['..*', '...', '...'];
            fs.writeFileSync(tempDir + "simple.cfg", lines.join('\n'))

            minesweeperProcess = spawnSync('node', [minesweeperHome + "minesweeper.js", tempDir + "simple.cfg"], { timeout: 3000 })
            assert.strictEqual(minesweeperProcess.status, 2, "Wrong exit code for invalid (new line missing) board configuration file")
        });

        it('Minesweeper exit because of only mines', function () {
            // Creates the simple.cfg file
            const lines = ['***', '***', '***'];
            fs.writeFileSync(tempDir + "simple.cfg", lines.join('\n') + '\n')

            minesweeperProcess = spawnSync('node', [minesweeperHome + "minesweeper.js", tempDir + "simple.cfg"], { timeout: 3000 })
            assert.strictEqual(minesweeperProcess.status, 2, "Wrong exit code for invalid (only mines) board configuration file")
        });

        it('Minesweeper exit because of single square', function () {
            // Creates the simple.cfg file
            const lines = ['.'];
            fs.writeFileSync(tempDir + "simple.cfg", lines.join('\n') + '\n')

            minesweeperProcess = spawnSync('node', [minesweeperHome + "minesweeper.js", tempDir + "simple.cfg"], { timeout: 3000 })
            assert.strictEqual(minesweeperProcess.status, 2, "Wrong exit code for invalid (single square) board configuration file")
        });

        it('Minesweeper exit because board is no rectangle', function () {
            // Creates the simple.cfg file
            const lines = ['..*', '..'];
            fs.writeFileSync(tempDir + "simple.cfg", lines.join('\n') + '\n')

            minesweeperProcess = spawnSync('node', [minesweeperHome + "minesweeper.js", tempDir + "simple.cfg"], { timeout: 3000 })
            assert.strictEqual(minesweeperProcess.status, 2, "Wrong exit code for invalid (no rectangle) board configuration file")
        });

        it('Minesweeper exit because more than 20 columns', function () {
            // Creates the simple.cfg file
            const lines = ['....................*', '.....................'];
            fs.writeFileSync(tempDir + "simple.cfg", lines.join('\n') + '\n')

            minesweeperProcess = spawnSync('node', [minesweeperHome + "minesweeper.js", tempDir + "simple.cfg"], { timeout: 3000 })
            assert.strictEqual(minesweeperProcess.status, 2, "Wrong exit code for invalid (too many columns) board configuration file")
        });

        it('Minesweeper exit because more than 20 rows', function () {
            // Creates the simple.cfg file
            const lines = ['..*', '...', '...', '...', '...', '...', '...', '...', '...', '...', '...', '...'
                , '...', '...', '...', '...', '...', '...', '...', '...', '...'];
            fs.writeFileSync(tempDir + "simple.cfg", lines.join('\n') + '\n')

            minesweeperProcess = spawnSync('node', [minesweeperHome + "minesweeper.js", tempDir + "simple.cfg"], { timeout: 3000 })
            assert.strictEqual(minesweeperProcess.status, 2, "Wrong exit code for invalid (too many rows) board configuration file")
        });
    });

    describe('Runs with 0 exit code. Tag: Assignment1, Assignment2, Assignment3, Assignment4, Assignment5', function () {

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

        it('Minesweeper without mines', function () {
            // Creates the simple.cfg file
            const lines = ['...', '...', '...'];
            fs.writeFileSync(tempDir + "simple.cfg", lines.join('\n') + '\n')

            minesweeperProcess = spawnSync('node', [minesweeperHome + "minesweeper.js", tempDir + "simple.cfg"], { input: "1 1 R\n", timeout: 3000 });
            assert.strictEqual(minesweeperProcess.status, 0, "Wrong exit for valid (won) game")
        });

        it('Minesweeper with board 1x20', function () {
            // Creates the simple.cfg file
            const lines = ['...................*'];
            fs.writeFileSync(tempDir + "simple.cfg", lines.join('\n') + '\n')

            minesweeperProcess = spawnSync('node', [minesweeperHome + "minesweeper.js", tempDir + "simple.cfg"], { input: "1 1 R\n", timeout: 3000 });
            assert.strictEqual(minesweeperProcess.status, 0, "Wrong exit for valid (won) game")
        });

        it('Minesweeper with board 20x1', function () {
            // Creates the simple.cfg file
            const lines = ['.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.', '.'
                , '.', '*'];
            fs.writeFileSync(tempDir + "simple.cfg", lines.join('\n') + '\n')

            minesweeperProcess = spawnSync('node', [minesweeperHome + "minesweeper.js", tempDir + "simple.cfg"], { input: "1 1 R\n", timeout: 3000 });
            assert.strictEqual(minesweeperProcess.status, 0, "Wrong exit for valid (won) game")
        });
    });
});