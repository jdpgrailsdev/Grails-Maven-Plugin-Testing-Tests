import spock.lang.*

import org.junit.Rule
import org.junit.rules.TestName
import org.hamcrest.Matcher
import org.hamcrest.Matchers
import org.hamcrest.BaseMatcher
import org.hamcrest.Description

//
// TODO Do we really need the auto-grails version upgrade?
// TODO Add verification method to look at contents of files after execution
//
abstract class BaseSpec extends Specification {

	static PROCESS_TIMEOUT_MILLS = 1000 * 60 * 5 // 5 minutes
	
	static grailsHome = requiredSysProp('grailsHome')
	static mvnHome = requiredSysProp('mvnHome')
	static projectWorkDir = requiredSysProp('projectWorkDir')
	static outputDir = requiredSysProp('outputDir')

	@Lazy static grailsVersion = {
		new File(BaseSpec.grailsHome, "build.properties").withReader { def p = new Properties(); p.load(it); p.'grails.version' }
	}()

	static requiredSysProp(prop) {
		assert System.getProperty(prop) != null
		System.getProperty(prop)
	}
	
	def command
	def exitStatus
	def output
	@Shared dumpCounter = 0

	@Rule testName = new TestName()
	
	def createMvnProcess(String workingDir, CharSequence[] command) {
	    def completeCommand = [createCommand(mvnHome, 'mvn')]
	    completeCommand.addAll(command.toList()*.toString())
	    _createProcess(new File(projectWorkDir, workingDir), completeCommand as String[], "MAVEN_HOME", mvnHome)
	}
	
	def createGrailsProcess(String projectDir, CharSequence[] command) {
		def completeCommand = [createCommand(grailsHome, 'grails')]
		completeCommand.addAll(command.toList()*.toString())
		_createProcess(new File(projectWorkDir, projectDir), completeCommand as String[], "GRAILS_HOME", grailsHome)	
	}
	
	def _createProcess(File dir, String[] completeCommand, String environmentVar, String environmentVarValue) {
		new ProcessBuilder(completeCommand).with {
			redirectErrorStream(true)
			directory(dir)
			environment()[environmentVar] = environmentVarValue
			start()
		}	
	}
	
	def executeMvn(String workingDir, CharSequence[] command) {
	    checkForUpgrade(workingDir)
	    _execute(createMvnProcess(workingDir, *command), createCommandName(command))
    
	}
	
	def executeGrails(String projectDir, CharSequence[] command) {
	    _execute(createGrailsProcess(projectDir, *command), createCommandName(command))
	}
	
	def _execute(Process process, String commandName) {
		def outputBuffer = new StringBuffer()
		process.consumeProcessOutputStream(outputBuffer)
		process.waitForOrKill(PROCESS_TIMEOUT_MILLS)
		exitStatus = process.exitValue()
		output = outputBuffer.toString()
		dumpOutput(commandName)
		exitStatus		
	}
	
	def createCommandName(CharSequence[] command) {
	    command?.join('_').replace('/','_').replace(':','_').replace(' ','_')
	}
	
	private dumpOutput(String commandName) {
	    println commandName
		def outputLabel = "${this.class.simpleName}-${testName.methodName}-${dumpCounter++}-${commandName}"
		new File(outputDir, "${outputLabel}.txt") << output
		println outputLabel
		println output
	}
	
	def isSuccessfulTestRun() {
		allOf(looksLikeTestsDidRun(), hasNoTestFailures())
	}
	
	def looksLikeTestsDidRun() {
		matcher("should contain '[INFO] BUILD SUCCESSFUL'") { it.contains('[INFO] BUILD SUCCESSFUL') }
	}
	
	def hasNoTestFailures() {
		matcher("should not contain '[ERROR] BUILD ERROR'") { !(it.readLines().any { it ==~ ~/^[ERROR] BUILD ERROR.*$/ }) }
	}
	
	def verifyArtifactExists(String artifact) {
	    new File(projectWorkDir,artifact).exists()
	}
	
	def isWindows() {
	    System.getProperty("os.name").toLowerCase().indexOf('win') >= 0
	}
	
	def createCommand(String pathHome, String binaryName) {
	    "${pathHome}${File.separator}bin${File.separator}${binaryName}${isWindows() ? '.bat' : '.sh'}"
	}
	
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
	
	def upgradeProject(String projectDir) {
		assert executeGrails(projectDir, 'upgrade', '--non-interactive') == 0
		assert output.contains('Project upgraded')	
	}
	
	private Matcher matcher(String describeTo, Closure matches) {
		matcher({ Description description -> description.appendText(describeTo) }, matches)
	}	

	private Matcher matcher(Closure describeTo, Closure matches) {
		[describeTo: describeTo, matches: matches] as BaseMatcher
	}
	
	private Matcher allOf(Matcher[] all) {
		Matchers.allOf(all)
	}
}