# Programming Styles -- WiSe 20-21
---------------
# Public Tests

This repository hosts the public tests for the assignments of the Programming Style WiSe 20-21

## Java Tests

Public tests for the `java` tasks are implemented using JUnit 4.13 and Hamcrest and are executed using `make test`. On your local machine you can execute them using the IDE or the command line (see for example this [StackOverflow question](https://stackoverflow.com/questions/2235276/how-to-run-junit-test-cases-from-the-command-line))

All the tests are meant to be **system tests**, that is, they will test end-to-end functionality of your code without considering its implementation. The tests call Minesweeper `main` method passing the location of the board configuration file, and then will input a sequence of input arguments while capturing the exit code (`exitCode`) of the program as well as its standard output (`stdOut`) and standard error (`stdErr`). After the program ends its execution, assertions can be defined over `exitCode`, `stdOut` and `stdErr`.

`BasicTest.java` is an example on how test cases are organized.

Before starting with the actual tests, using `@BeforeClass`, it checks the preconditions on the execution environment (java version, existence of the Minesweeper compiled class, etc.). It uses `Assumptions` (not `Assertions`!) to do so; hence, the tests will not fail if assumptions are not met, instead they will be skipped.

After checking preconditions, the actual test methods (marked with the `@Test` annotation) are executed. 

## Javascript Tests
Public tests for the `javascript` follows a similar philosophy but are implemented using Mocha. They can also be executed either using `make test` from the javascript folder.

All the test cases must be placed under the `test` folder otherwise Mocha will not be able to find them.

Javascript tests import the program as dependency of the test, define the arguments that must be passed to it, and makes assertions on exitCode, stdOut and stdErr.

> TODO: To capture log messages, check [here](https://glebbahmutov.com/blog/capture-all-the-logs/) and [here](https://medium.com/@the_teacher/how-to-test-console-output-console-log-console-warn-with-rtl-react-testing-library-and-jest-6df367736cf0)

## Filtering tests
Not all the assignments require the same test cases, so to select the right tests to use in each case we use JUnit categories and Mocha's grep functionality. That's why you see that some tests have the `@Category` annotation or a tag in their name.