import spock.lang.*

/**
 * Functional tests for Grails Maven Plugin goals.
 *
 * This spec is inspired by/derived from the functional spec
 * found in the Grails Testing Tests project (https://github.com/grails/grails-testing-tests.git)
 * See http://grails.org/doc/latest/guide/4.%20The%20Command%20Line.html#4.5%20Ant%20and%20Maven for
 * more information regarding the goals available in the Grails Maven plugin.
 */
class FunctionalSpec extends BaseSpec {

	def setup() {
		//Clear the artifact list from the previous execution
		artifacts?.clear()
	}
	
	def cleanup() {
//		TODO Should we do this automatically?  This may
//		TODO mask issues in the Maven plugin if we clean after
//		TODO execution.  Might make more sense to NOT clean		
//		TODO so that we can see if any errors occur.						
//		/*
//		 * Clean the current working directory, in case it is
//		 * needed in the next test. Plus, it's always good
//		 * to clean up after yourself ;).
//		 */		
//		executeMvn('clean', "-DgrailsVersion=${grailsVersion}") 
		workingDir = ''
	}

    def "test create-pom"() {    	
        when:
		workingDir = 'functional/create-pom'
        artifacts << "${workingDir}/pom.xml"
        executeMvn("org.grails:grails-maven-plugin:${grailsVersion}:create-pom", '-DgroupId=com.mycompany') 
        then:
        getOutput() isSuccessfulTestRun()
    }
    
    //def "test create-pom without group id"() {
    //    when:
    //    executeMvn('functional/create-pom', "org.grails:grails-maven-plugin:${grailsVersion}:create-pom") 
    //    then:
    //    getOutput() isSuccessfulTestRun()
    //}    
    
    def "test create-controller"() {
        when:
		workingDir = 'functional/test-application'
        artifacts << "${workingDir}/grails-app/controllers/com/mycompany/ControllerTestController.groovy"
        artifacts << "${workingDir}/test/unit/com/mycompany/ControllerTestControllerTests.groovy"
        executeMvn('grails:create-controller', "-DgrailsVersion=${grailsVersion}") 
        then:
        getOutput() isSuccessfulTestRun() 
    }
    
    def "test create-domain-class"() {
        when:
		workingDir = 'functional/test-application'
        artifacts << "${workingDir}/grails-app/domain/com/mycompany/DomainTest.groovy"
        artifacts << "${workingDir}/test/unit/com/mycompany/DomainTestTests.groovy"
        executeMvn('grails:create-domain-class', "-DgrailsVersion=${grailsVersion}") 
        then:
        getOutput() isSuccessfulTestRun()
    }
    
    def "test create-integration-test"() {
        when:
		workingDir = 'functional/test-application'
        artifacts << "${workingDir}/test/integration/com/mycompany/IntegrationTestTests.groovy"
        executeMvn('grails:create-integration-test', "-DgrailsVersion=${grailsVersion}") 
        then:
        getOutput() isSuccessfulTestRun()    
    }
    
    def "test create-script"() {
        when:
		workingDir = 'functional/test-application'
        artifacts << "${workingDir}/scripts/TestScript.groovy"
        executeMvn('grails:create-script', "-DgrailsVersion=${grailsVersion}") 
        then:
        getOutput() isSuccessfulTestRun()     
    }
    
    def "test create-service"() {
        when:
		workingDir = 'functional/test-application'
        artifacts << "${workingDir}/grails-app/services/com/mycompany/ServiceTestService.groovy"
        artifacts << "${workingDir}/test/unit/com/mycompany/ServiceTestServiceTests.groovy"
        executeMvn('grails:create-service', "-DgrailsVersion=${grailsVersion}") 
        then:
        getOutput() isSuccessfulTestRun()  
    }
    
    def "test create-tag-lib"() {
        when:
		workingDir = 'functional/test-application'
        artifacts << "${workingDir}/grails-app/taglib/com/mycompany/TagLibTestTagLib.groovy"
        artifacts << "${workingDir}/test/unit/com/mycompany/TagLibTestTagLibTests.groovy"
        executeMvn('grails:create-tag-lib', "-DgrailsVersion=${grailsVersion}") 
        then:
        getOutput() isSuccessfulTestRun()    
    }
    
    def "test create-unit-test"() {
        when:
		workingDir = 'functional/test-application'
        artifacts << "${workingDir}/test/unit/com/mycompany/UnitTestTestTests.groovy"
        executeMvn('grails:create-unit-test', "-DgrailsVersion=${grailsVersion}") 
        then:
        getOutput() isSuccessfulTestRun()    
    }
    
    def "test generate-all"() {
        when:
		workingDir = 'functional/test-application'
        modifyPom("${workingDir}/pom.xml", '<domainClassName>com.mycompany.DomainTest</domainClassName>', '<domainClassName>com.mycompany.DomainTestGenAll</domainClassName>')
        artifacts << "${workingDir}/grails-app/domain/com/mycompany/DomainTestGenAll.groovy"
        artifacts << "${workingDir}/grails-app/controllers/com/mycompany/DomainTestGenAllController.groovy"
        artifacts << "${workingDir}/grails-app/views/domainTestGenAll/create.gsp"
        artifacts << "${workingDir}/test/unit/com/mycompany/DomainTestGenAllTests.groovy"
        artifacts << "${workingDir}/test/unit/com/mycompany/DomainTestGenAllControllerTests.groovy"
		executeMvn('grails:create-domain-class', "-DgrailsVersion=${grailsVersion}")
        executeMvn('grails:generate-all', "-DgrailsVersion=${grailsVersion}") 
        then:
        getOutput() isSuccessfulTestRun()     
    }
}