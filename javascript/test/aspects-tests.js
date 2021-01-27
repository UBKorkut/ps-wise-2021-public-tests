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

describe('Aspects Tests', function () {
    describe('Runs with 0 exit code and valid board moves. Tag: Assignment3', function () {

        var tempDir;
        var minesweeperProcess;

        beforeEach("Create temporary folder for file creation", function () {
            // mkdtempSync requires the trailing `path.sep` to ensure a directory is created
            tempDir = fs.mkdtempSync(os.tmpdir() + path.sep);
            // tempDir does not have the trailing 'path.sep`
            // TODO use path.join([...paths])
            tempDir += path.sep;
            process.env.ASPECT = "True";
        });

        afterEach("Remove temporary folder for file creation", function () {
            deleteFolderWithFiles(tempDir);
            delete process.env.ASPECT;
        });

        it('Win Minesweeper after first move (1 mine)', function () {
            // Creates the simple.cfg file
            const lines = ['..*', '...', '...'];
            fs.writeFileSync(tempDir + "simple.cfg", lines.join('\n') + '\n')

            var expectedOutput = [
                "╔═══════════╗\n",
                "║Mines: 1   ║\n",
                "║Moves: 0   ║\n",
                "╚═══════════╝\n",
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
                "╔═══════════╗\n",
                "║Mines: 0   ║\n",
                "║Moves: 1   ║\n",
                "╚═══════════╝\n",
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

        it('Lose Minesweeper after first move (2 mines)', function () {
            // Creates the simple.cfg file
            const lines = ['..*', '.*.', '...'];
            fs.writeFileSync(tempDir + "simple.cfg", lines.join('\n') + '\n')

            var expectedOutput = [
                "╔═══════════╗\n",
                "║Mines: 2   ║\n",
                "║Moves: 0   ║\n",
                "╚═══════════╝\n",
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
                "╔═══════════╗\n",
                "║Mines: 0   ║\n",
                "║Moves: 1   ║\n",
                "╚═══════════╝\n",
                "┌───┬───┬───┐\n",
                "│   │   │ * │\n",
                "├───┼───┼───┤\n",
                "│   │ * │   │\n",
                "├───┼───┼───┤\n",
                "│   │   │   │\n",
                "└───┴───┴───┘\n",
                "╔═══════════╗\n",
                "║You Lost!  ║\n",
                "╚═══════════╝\n",
            ]

            // spawn child process to execute Minesweeper instance
            // and send inputs to the subprocess
            minesweeperProcess = spawnSync('node', [minesweeperHome + "minesweeper.js", tempDir + "simple.cfg"], { input: "1 3 R\n", timeout: 3000 });

            assert.strictEqual(minesweeperProcess.status, 0, "Wrong exit for valid (won) game")
            // We cannot do '\n' join because of the console character '>'
            // Note that we need to call toString() on stdout
            assert.strictEqual(minesweeperProcess.stdout.toString(), expectedOutput.join(''), "Wrong output")
            // instead of checking one by one the boars, we assert over the entire output
        });

        it('Win Minesweeper after invalid input (1 mine)', function () {
            // Creates the simple.cfg file
            const lines = ['..*', '...', '...'];
            fs.writeFileSync(tempDir + "simple.cfg", lines.join('\n') + '\n')

            var expectedOutput = [
                "╔═══════════╗\n",
                "║Mines: 1   ║\n",
                "║Moves: 0   ║\n",
                "╚═══════════╝\n",
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
                "╔═══════════╗\n",
                "║Mines: 1   ║\n",
                "║Moves: 0   ║\n",
                "╚═══════════╝\n",
                "┌───┬───┬───┐\n",
                "│   │   │   │\n",
                "├───┼───┼───┤\n",
                "│   │   │   │\n",
                "├───┼───┼───┤\n",
                "│   │   │   │\n",
                "└───┴───┴───┘\n",
                "╔════════════════════════════════╗\n",
                "║The provided input is not valid!║\n",
                "╚════════════════════════════════╝\n",
                ">",
                "╔═══════════╗\n",
                "║Mines: 0   ║\n",
                "║Moves: 1   ║\n",
                "╚═══════════╝\n",
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
            minesweeperProcess = spawnSync('node', [minesweeperHome + "minesweeper.js", tempDir + "simple.cfg"], { input: "1 1 r\n1 1 R\n", timeout: 3000 });

            assert.strictEqual(minesweeperProcess.status, 0, "Wrong exit for valid (won) game")
            // We cannot do '\n' join because of the console character '>'
            // Note that we need to call toString() on stdout
            assert.strictEqual(minesweeperProcess.stdout.toString(), expectedOutput.join(''), "Wrong output")
            // instead of checking one by one the boars, we assert over the entire output
        });

        it('Win Minesweeper after flagging two fields (1 mine)', function () {
            // Creates the simple.cfg file
            const lines = ['..*', '...', '...'];
            fs.writeFileSync(tempDir + "simple.cfg", lines.join('\n') + '\n')

            var expectedOutput = [
                "╔═══════════╗\n",
                "║Mines: 1   ║\n",
                "║Moves: 0   ║\n",
                "╚═══════════╝\n",
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
                "╔═══════════╗\n",
                "║Mines: 0   ║\n",
                "║Moves: 0   ║\n",
                "╚═══════════╝\n",
                "┌───┬───┬───┐\n",
                "│   │   │ ¶ │\n",
                "├───┼───┼───┤\n",
                "│   │   │   │\n",
                "├───┼───┼───┤\n",
                "│   │   │   │\n",
                "└───┴───┴───┘\n",
                "╔═══════════╗\n",
                "║           ║\n",
                "╚═══════════╝\n",
                ">",
                "╔═══════════╗\n",
                "║Mines: 0   ║\n",
                "║Moves: 0   ║\n",
                "╚═══════════╝\n",
                "┌───┬───┬───┐\n",
                "│   │ ¶ │ ¶ │\n",
                "├───┼───┼───┤\n",
                "│   │   │   │\n",
                "├───┼───┼───┤\n",
                "│   │   │   │\n",
                "└───┴───┴───┘\n",
                "╔═══════════╗\n",
                "║           ║\n",
                "╚═══════════╝\n",
                ">",
                "╔═══════════╗\n",
                "║Mines: 0   ║\n",
                "║Moves: 1   ║\n",
                "╚═══════════╝\n",
                "┌───┬───┬───┐\n",
                "│ ▓ │ 1 │ ¶ │\n",
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
            minesweeperProcess = spawnSync('node', [minesweeperHome + "minesweeper.js", tempDir + "simple.cfg"], { input: "1 3 F\n1 2 F\n1 1 R\n", timeout: 3000 });

            assert.strictEqual(minesweeperProcess.status, 0, "Wrong exit for valid (won) game")
            // We cannot do '\n' join because of the console character '>'
            // Note that we need to call toString() on stdout
            assert.strictEqual(minesweeperProcess.stdout.toString(), expectedOutput.join(''), "Wrong output")
            // instead of checking one by one the boars, we assert over the entire output
        });

        it('Win Minesweeper after revealing one field twice (1 mine)', function () {
            // Creates the simple.cfg file
            const lines = ['..*', '...', '...'];
            fs.writeFileSync(tempDir + "simple.cfg", lines.join('\n') + '\n')

            var expectedOutput = [
                "╔═══════════╗\n",
                "║Mines: 1   ║\n",
                "║Moves: 0   ║\n",
                "╚═══════════╝\n",
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
                "╔═══════════╗\n",
                "║Mines: 1   ║\n",
                "║Moves: 1   ║\n",
                "╚═══════════╝\n",
                "┌───┬───┬───┐\n",
                "│   │   │   │\n",
                "├───┼───┼───┤\n",
                "│   │ 1 │   │\n",
                "├───┼───┼───┤\n",
                "│   │   │   │\n",
                "└───┴───┴───┘\n",
                "╔═══════════╗\n",
                "║           ║\n",
                "╚═══════════╝\n",
                ">",
                "╔═══════════╗\n",
                "║Mines: 1   ║\n",
                "║Moves: 2   ║\n",
                "╚═══════════╝\n",
                "┌───┬───┬───┐\n",
                "│   │   │   │\n",
                "├───┼───┼───┤\n",
                "│   │ 1 │   │\n",
                "├───┼───┼───┤\n",
                "│   │   │   │\n",
                "└───┴───┴───┘\n",
                "╔═══════════╗\n",
                "║           ║\n",
                "╚═══════════╝\n",
                ">",
                "╔═══════════╗\n",
                "║Mines: 0   ║\n",
                "║Moves: 3   ║\n",
                "╚═══════════╝\n",
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
            minesweeperProcess = spawnSync('node', [minesweeperHome + "minesweeper.js", tempDir + "simple.cfg"], { input: "2 2 R\n2 2 R\n1 1 R\n", timeout: 3000 });

            assert.strictEqual(minesweeperProcess.status, 0, "Wrong exit for valid (won) game")
            // We cannot do '\n' join because of the console character '>'
            // Note that we need to call toString() on stdout
            assert.strictEqual(minesweeperProcess.stdout.toString(), expectedOutput.join(''), "Wrong output")
            // instead of checking one by one the boars, we assert over the entire output
        });

        it('Win Minesweeper after 3 steps where one increments both mines and moves', function () {
            // Creates the simple.cfg file
            const lines = ['..*', '...', '...'];
            fs.writeFileSync(tempDir + "simple.cfg", lines.join('\n') + '\n')

            var expectedOutput = [
                "╔═══════════╗\n",
                "║Mines: 1   ║\n",
                "║Moves: 0   ║\n",
                "╚═══════════╝\n",
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
                "╔═══════════╗\n",
                "║Mines: 0   ║\n",
                "║Moves: 0   ║\n",
                "╚═══════════╝\n",
                "┌───┬───┬───┐\n",
                "│   │   │   │\n",
                "├───┼───┼───┤\n",
                "│   │ ¶ │   │\n",
                "├───┼───┼───┤\n",
                "│   │   │   │\n",
                "└───┴───┴───┘\n",
                "╔═══════════╗\n",
                "║           ║\n",
                "╚═══════════╝\n",
                ">",
                "╔═══════════╗\n",
                "║Mines: 1   ║\n",
                "║Moves: 1   ║\n",
                "╚═══════════╝\n",
                "┌───┬───┬───┐\n",
                "│   │   │   │\n",
                "├───┼───┼───┤\n",
                "│   │ 1 │   │\n",
                "├───┼───┼───┤\n",
                "│   │   │   │\n",
                "└───┴───┴───┘\n",
                "╔═══════════╗\n",
                "║           ║\n",
                "╚═══════════╝\n",
                ">",
                "╔═══════════╗\n",
                "║Mines: 0   ║\n",
                "║Moves: 2   ║\n",
                "╚═══════════╝\n",
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
            minesweeperProcess = spawnSync('node', [minesweeperHome + "minesweeper.js", tempDir + "simple.cfg"], { input: "2 2 F\n2 2 R\n1 1 R\n", timeout: 3000 });

            assert.strictEqual(minesweeperProcess.status, 0, "Wrong exit for valid (won) game")
            // We cannot do '\n' join because of the console character '>'
            // Note that we need to call toString() on stdout
            assert.strictEqual(minesweeperProcess.stdout.toString(), expectedOutput.join(''), "Wrong output")
            // instead of checking one by one the boars, we assert over the entire output
        });
    });
});