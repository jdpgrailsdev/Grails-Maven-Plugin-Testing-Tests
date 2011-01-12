import spock.lang.*

import org.junit.Rule
import org.junit.rules.TestName
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.BaseMatcher
import org.hamcrest.Description

/**
 * Base Spock specification for Grails Maven Plugin Testing Tests project.
 * This specification contains the basic execution and test verification
 * methods.
 * @author Jonathan Pearlin
 */
abstract class BaseSpec extends Specification {

    static PROCESS_TIMEOUT_MILLS = 1000 * 60 * 5 // 5 minutes

    //Required System Properties
    static grailsHome = requiredSysProp('grailsHome')
    static mvnHome = requiredSysProp('mvnHome')
    static projectWorkDir = requiredSysProp('projectWorkDir')
    static outputDir = requiredSysProp('outputDir')

    //Extracts the Grails Version from the Grails installation
    @Lazy static grailsVersion = {
        new File(BaseSpec.grailsHome, "build.properties").withReader { def p = new Properties(); p.load(it); p.'grails.version' }
    }()

    /**
     * Verifies the presence of the required system property
     * @param prop The name of the property to be verified
     * @return {@code True} if the property exists or {@code false}
     * 	if the property does not exist.
     */
    static requiredSysProp(prop) {
        assert System.getProperty(prop) != null
        System.getProperty(prop)
    }

    def command
    def exitStatus
    def output
    def workingDir
    def artifacts = []
    @Shared dumpCounter = 0

    @Rule testName = new TestName()

    /**
     * Creates the command/process to execute Maven.
     * @param workingDir The directory to execute Maven from.  This should
     * 	be the directory containing the project to build/use Maven on.
     * @param command The Maven goal(s) to be executed.
     * @return A {@code ProcessBuilder} object that can be executed.
     */
    def createMvnProcess(String workingDir, CharSequence[] command) {
        def completeCommand = [createCommand(mvnHome, 'mvn')]
        completeCommand.addAll(command.toList()*.toString())
        _createProcess(new File(projectWorkDir, workingDir), completeCommand as String[], "MAVEN_HOME", mvnHome)
    }

    /**
     * Creates the command/process to execute Grails.
     * @param projectDir The directory to execute the Grails
     * 	command line from.
     * @param command The Grails command to be executed.
     * @return A {@code ProcessBuilder} object that can be executed.
     */
    def createGrailsProcess(String projectDir, CharSequence[] command) {
        def completeCommand = [createCommand(grailsHome, 'grails')]
        completeCommand.addAll(command.toList()*.toString())
        _createProcess(new File(projectWorkDir, projectDir), completeCommand as String[], "GRAILS_HOME", grailsHome)
    }

    /**
     * Creates a {@code ProcessBuilder} object that can be used to run
     * the complete command in the supplied directory.
     * @param dir The directory where the command will be executed.
     * @param completeCommand The command to execute.
     * @param environmentVar The name of the environment variable.
     * @param environmentVarValue The value of the environment variable.
     * @return A {@code ProcessBuilder} object that wraps the command.
     */
    def _createProcess(File dir, String[] completeCommand, String environmentVar, String environmentVarValue) {
        new ProcessBuilder(completeCommand).with {
            redirectErrorStream(true)
            directory(dir)
            environment()[environmentVar] = environmentVarValue
            start()
        }
    }

    /**
     * Executes Maven.
     * @param command The Maven goal(s) to execute.
     * @return The exit status of running the Maven command.
     */
    def executeMvn(CharSequence[] command) {
        checkForUpgrade(workingDir)
        _execute(createMvnProcess(workingDir, *command), createCommandName(command))

    }

    /**
     * Executes Grails.
     * @param projectDir The project directory to run the Grails command from.
     * @param command The Grails command to execute.
     * @return The exit status of running the Grails command.
     */
    def executeGrails(String projectDir, CharSequence[] command) {
        _execute(createGrailsProcess(projectDir, *command), createCommandName(command))
    }

    /**
     * Executes the process.
     * @param process A {@code Process} implementation.
     * @param commandName The name of the command (used for naming the log file that contains
     * 	the output of the execution).
     * @return The exit status of the process.
     */
    def _execute(Process process, String commandName) {
        def outputBuffer = new StringBuffer()
        process.consumeProcessOutputStream(outputBuffer)
        process.waitForOrKill(PROCESS_TIMEOUT_MILLS)
        exitStatus = process.exitValue()
        output = outputBuffer.toString()
        dumpOutput(commandName)
        exitStatus
    }

    /**
     * Creates an unique name for a command by joining all
     * tokens in the command with underscores (_) and replacing
     * all forward-slashes (/), colons (:) and spaces ( ) with
     * underscores (_).
     * @param command The command to be executed.
     * @return A unique name for the command.
     */
    def createCommandName(CharSequence[] command) {
        command?.join('_').replace('/','_').replace(':','_').replace(' ','_')
    }

    /**
     * Saves the output of the process execution to a .txt file for
     * verifcation.
     * @param commandName The name of the command.
     */
    private dumpOutput(String commandName) {
        println commandName
        def outputLabel = "${this.class.simpleName}-${testName.methodName}-${dumpCounter++}-${commandName}"
        new File(outputDir, "${outputLabel}.txt") << output
        println outputLabel
        println output
    }

    /**
     * Verifies that the test specification ran successfully.  A
     * successfully test run verifies that the output does not contain
     * any errors and that any expected artifacts exist/have been created.
     * @return A {@code Matcher} that evaluates to {@code true} if all above
     * 	conditions are met or {@code false} if the conditions are not met.
     */
    def isSuccessfulTestRun() {
        allOf(looksLikeTestsDidRun(), hasNoTestFailures(), verifyArtifacts())
    }

    /**
     * Verifies that Maven executed all required goals.
     * @return A {@code Matcher} that evaluates to {@code true} if the
     * 	output contains the string '[INFO] BUILD SUCCESSFUL' or {@code false}
     * 	if the output does not contain the aforementioned string.
     */
    def looksLikeTestsDidRun() {
        matcher("should contain '[INFO] BUILD SUCCESSFUL'") { it.contains('[INFO] BUILD SUCCESSFUL') }
    }

    /**
     * Verifies that Maven successfully ran all goals without error.
     * @return A {@code Matcher} that evaluates to {@code true} if the
     * 	output does <b>NOT</b> contains the string '[ERROR] BUILD ERROR'
     *  or {@code false} if the output does contain the aforementioned string.
     */
    def hasNoTestFailures() {
        matcher("should not contain '[ERROR] BUILD ERROR'") { !(it.readLines().any { it ==~ ~/^[ERROR] BUILD ERROR.*$/ }) }
    }

    /**
     * Verifies that the requested artifact(s) exist(s) after executing
     * all of the Maven goals.
     * @return A {@code Matcher} that evaluates to {@code true} if the
     * 	requested artifact(s) exist(s) or {@code false} if the requested
     * 	artifact(s) do(es) not exist(s).
     */
    def verifyArtifacts() {
        matcher("should contain artifacts '${artifacts}'") {
            def result = true
            artifacts.each { artifact ->
                if(!new File(projectWorkDir,artifact).exists()) {
                    result = false
                }
            }
            result
        }
    }

    /**
     * Modifies the supplied POM file by replaces the "stringToReplace"
     * pattern with the string contained the "replaceString" parameter.
     * Note that this method will replace ALL occurrences of the
     * pattern to be replaced.
     * @param pomFile The POM file to be modified.
     * @param stringToReplace The pattern to be replaced.
     * @param replacementString The replacement value, if a match is found.
     */
    def modifyPom(pomFile, stringToReplace, replacementString) {
        def file = new File(projectWorkDir, pomFile)
        def contents = file?.text
        contents = contents?.replaceAll(stringToReplace, replacementString)
        file?.write(contents)
    }

    /**
     * Tests whether or not the tests are being executed in a flavor
     * of the Windows operating system.
     * @return {@code True} if the operating system is part of the
     * 	Windows family or {@code false} if it is not.
     */
    def isWindows() {
        System.getProperty("os.name").toLowerCase().indexOf('win') >= 0
    }

    /**
     * Creates the command string for running a command on the current
     * operating system.  This method assumes the binary to be executed
     * is in a folder named 'bin' and that it ends in '.bat' or '.sh' if
     * the operating system is Windows or Unix/-inux, respectively.
     * @param pathHome The path to the binary.
     * @param binaryName The name of the binary to execute.
     * @return The full command string for the requested binary for the
     * 	current operating system.
     */
    def createCommand(String pathHome, String binaryName) {
        "${pathHome}${File.separator}bin${File.separator}${binaryName}${isWindows() ? '.bat' : '.sh'}"
    }

    /**
     * Tests whether or not the Grails project found at the supplied
     * project directory needs to be upgraded to match the version of
     * Grails defined by the grails.home environment variable.  If an
     * upgrade is required, it will be performed by this method.
     * @param projectDir The directory path of a Grails project.
     */
    def checkForUpgrade(String projectDir) {
        def projectVersion
        new File(new File(projectWorkDir, projectDir), "application.properties").withReader {
            def p = new Properties();
            p.load(it);
            projectVersion = p.'app.grails.version'
        }

        if(projectVersion != grailsVersion) {
            upgradeProject(projectDir)
        }
    }

    /**
     * Upgrades the Grails project found at the supplied
     * project directory to match the Grails version of the
     * installation pointed to by the grails.home system property.
     * @param projectDir The directory path of a Grails project.
     */
    def upgradeProject(String projectDir) {
        assert executeGrails(projectDir, 'upgrade', '--non-interactive') == 0
        assert output.contains('Project upgraded')
    }

    /**
     * Creates a {@code Matcher} used to verify the outcome of a test.
     * @param describeTo The description to be displayed if the matcher
     * fails.
     * @param describeTo The information to be displayed if the matcher fails to match.
     * @param matches A closure to be executed to determine if a match has been made.
     * @return A {@code Matcher} used to verify the outcome of a test.
     */
    private Matcher matcher(String describeTo, Closure matches) {
        matcher({ Description description -> description.appendText(describeTo) }, matches)
    }

    /**
     * Creates a {@code Matcher} used to verify the outcome of a test.
     * @param describeTo A closure be executed in the event that a match is not
     * 	made to describe why a match did not occur.
     * @param matches A closure to be executed to determine if a match has been made.
     * @return A {@code Matcher} used to verify the outcome of a test.
     */
    private Matcher matcher(Closure describeTo, Closure matches) {
        [describeTo: describeTo, matches: matches] as BaseMatcher
    }

    /**
     * Verifies that all supplied {@code Matcher} instances resolve to
     * {@code true} when matched.
     * @param all A collection of {@code Matcher} instances.
     * @return A {@code Matcher} that evaluates to {@code true} if
     * 	all supplied matchers evaluate to {@code true} or evaluates
     * 	to {@code false} if all supplied matchers evaluate to {@code false}.
     */
    private Matcher allOf(Matcher[] all) {
        Matchers.allOf(all)
    }
}