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

    def "test create-pom"() {
        when:
        executeMvn('functional/create-pom', "org.grails:grails-maven-plugin:${grailsVersion}:create-pom", '-DgroupId=com.mycompany') 
        then:
        getOutput() isSuccessfulTestRun()
        verifyArtifactExists('functional/create-pom/pom.xml') 
    }
    
    //def "test create-pom without group id"() {
    //    when:
    //    executeMvn('functional/create-pom', "org.grails:grails-maven-plugin:${grailsVersion}:create-pom") 
    //    then:
    //    getOutput() isSuccessfulTestRun()
    //}    
    
    def "test create-controller"() {
        when:
        executeMvn('functional/test-application', 'grails:create-controller', "-DgrailsVersion=${grailsVersion}") 
        then:
        getOutput() isSuccessfulTestRun() 
        verifyArtifactExists('functional/test-application/grails-app/controllers/com/mycompany/ControllerTestController.groovy')
        verifyArtifactExists('functional/test-application/test/unit/com/mycompany/ControllerTestControllerTests.groovy')
    }
    
    def "test create-domain-class"() {
        when:
        executeMvn('functional/test-application', 'grails:create-domain-class', "-DgrailsVersion=${grailsVersion}") 
        then:
        getOutput() isSuccessfulTestRun()
        verifyArtifactExists('functional/test-application/grails-app/domain/com/mycompany/DomainTest.groovy')
        verifyArtifactExists('functional/test-application/test/unit/com/mycompany/DomainTestTests.groovy')
    }
    
    def "test create-integration-test"() {
        when:
        executeMvn('functional/test-application', 'grails:create-integration-test', "-DgrailsVersion=${grailsVersion}") 
        then:
        getOutput() isSuccessfulTestRun()    
        verifyArtifactExists('functional/test-application/test/integration/com/mycompany/IntegrationTestTests.groovy')
    }
    
    def "test create-script"() {
        when:
        executeMvn('functional/test-application', 'grails:create-script', "-DgrailsVersion=${grailsVersion}") 
        then:
        getOutput() isSuccessfulTestRun()    
        verifyArtifactExists('functional/test-application/scripts/TestScript.groovy')    
    }
    
    def "test create-service"() {
        when:
        executeMvn('functional/test-application', 'grails:create-service', "-DgrailsVersion=${grailsVersion}") 
        then:
        getOutput() isSuccessfulTestRun()
        verifyArtifactExists('functional/test-application/grails-app/services/com/mycompany/ServiceTestService.groovy')
        verifyArtifactExists('functional/test-application/test/unit/com/mycompany/ServiceTestServiceTests.groovy')    
    }
    
    def "test create-tag-lib"() {
        when:
        executeMvn('functional/test-application', 'grails:create-tag-lib', "-DgrailsVersion=${grailsVersion}") 
        then:
        getOutput() isSuccessfulTestRun()
        verifyArtifactExists('functional/test-application/grails-app/taglib/com/mycompany/TagLibTestTagLib.groovy')
        verifyArtifactExists('functional/test-application/test/unit/com/mycompany/TagLibTestTagLibTests.groovy')     
    }
    
    def "test create-unit-test"() {
        when:
        executeMvn('functional/test-application', 'grails:create-unit-test', "-DgrailsVersion=${grailsVersion}") 
        then:
        getOutput() isSuccessfulTestRun()
        verifyArtifactExists('functional/test-application/test/unit/com/mycompany/UnitTestTestTests.groovy')     
    }
}