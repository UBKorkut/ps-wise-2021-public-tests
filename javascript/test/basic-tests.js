// Maybe use .strict
const assert = require('assert');

const fs = require("fs");
const path = require("path");

const { stdin } = require('mock-stdin');


// Find your module using the --minesweeper_home variable and the default name 'minesweeper'
const minesweeperHome = process.env.npm_config_minesweeper_home.endsWith("/")
  ? process.env.npm_config_minesweeper_home
  : process.env.npm_config_minesweeper_home + "/";
const minesweeper = require(minesweeperHome + "minesweeper");

describe('Basic Tests', function() {
    
    describe('Runs correct version of node. Tag: Assignment1, Assignment2, Assignment3, Assignment4, Assignment5', function(){
        it('Version of node must be v10.23.0', function() {
            assert.strictEqual(process.version, 'v10.23.0');
        });
    });

    // https://stackoverflow.com/questions/21587122/mocha-chai-expect-to-throw-not-catching-thrown-errors
    // assert.throws( FunctionThatShouldThrow_AssertionError, assert.AssertionError )
    describe('Runs with non-null exit codes. Tag: Assignment1, Assignment2, Assignment3, Assignment4, Assignment5', function() {

        beforeEach("Ensure no simple.cfg before execution.", function(){
            if( fs.existsSync("simple.cfg")){
                fs.unlinkSync("simple.cfg");
            }
        });

        afterEach("Ensure no simple.cfg after execution", function(){
            if( fs.existsSync("simple.cfg")){
                fs.unlinkSync("simple.cfg");
            }
        });

        it('Minesweeper should exit with code 1 if input file is not provided', function() {
            var exitCode = minesweeper.main()
            assert.strictEqual(exitCode, 1, "Wrong exit code for missing board configuration file")
        });

        it('Minesweeper should exit with code 1 if input file does not exist is missing', function() {
            var exitCode = minesweeper.main("simple.cfg")
            assert.strictEqual(exitCode, 1, "Wrong exit code for not existent board configuration file")
        });
        
        it('Minesweeper should exit with code 2 if input file is empty', function() {
            // Make sure there's an empty "simple.cfg" file (see https://flaviocopes.com/how-to-create-empty-file-node/)
            fs.closeSync(fs.openSync("simple.cfg", 'w'))

            var stats = fs.statSync("simple.cfg")

            var exitCode = minesweeper.main("simple.cfg")

            assert.strictEqual(exitCode, 2, "Wrong exit code for invalid (empty) board configuration file")
        });
    });

    describe('Runs with null exit code.  Tag: Assignment1, Assignment2, Assignment3, Assignment4, Assignment5', function() {

        var stdin;

        beforeEach("Ensure no simple.cfg before execution", function(){
            if( fs.existsSync("simple.cfg")){
                fs.unlinkSync("simple.cfg");
            }
        });

        afterEach("Ensure no simple.cfg after execution", function(){
            if( fs.existsSync("simple.cfg")){
                fs.unlinkSync("simple.cfg");
            }
        });

        beforeEach("Start Mockin stdin", function(){
            stdin = require('mock-stdin').stdin();
        });
        
        afterEach("Stop Mocking stdin", function(){
            stdin.end();
        });

        it('Preys and Hunters should raise no exception', function() {
            // Creates the simple.cfg file
            const lines = ['..*', '...', '...'];
            fs.writeFileSync("simple.cfg", lines.join('\n'))
            // Prepare the inputs
            // stdin.send("1 1 R", "ascii");
            
            // Execute Minesweeper. This should pick up the mocked stdin
            var exitCode = minesweeper.main("simple.cfg")

            assert.strictEqual(exitCode, 0, "Wrong exit for valid (won) game")
        });
    });
});