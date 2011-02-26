/*
* Copyright 2011 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
import spock.lang.*

/**
 * Functional tests for Grails Maven Plugin goals.
 *
 * This spec is inspired by/derived from the functional spec
 * found in the Grails Testing Tests project (https://github.com/grails/grails-testing-tests)
 * See http://grails.org/doc/latest/guide/4.%20The%20Command%20Line.html#4.5%20Ant%20and%20Maven for
 * more information regarding the goals available in the Grails Maven plugin.
 *
 * @author Jonathan Pearlin
 * @since 0.1
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
//		execute('clean', "-DgrailsVersion=${grailsVersion}")
        workingDir = ''
    }

    def "test create-pom"() {
        given:
        workingDir = 'functional/create-pom'
        artifacts << "${workingDir}/pom.xml"
        when:
        execute(false, "org.grails:grails-maven-plugin:${grailsVersion}:create-pom", '-DgroupId=com.mycompany')
        then:
        getOutput() isSuccessfulTestRun()
    }

    //def "test create-pom without group id"() {
    //    when:
    //    execute('functional/create-pom', "org.grails:grails-maven-plugin:${grailsVersion}:create-pom")
    //    then:
    //    getOutput() isSuccessfulTestRun()
    //}

    def "test grails-clean"() {
        given:
        workingDir = 'functional/test-application'
        when:
        execute(true, 'grails:clean', "-DgrailsVersion=${grailsVersion}")
        then:
        getOutput() isSuccessfulTestRun()
    }

    def "test create-controller"() {
        given:
        workingDir = 'functional/test-application'
        artifacts << "${workingDir}/grails-app/controllers/com/mycompany/ControllerTestController.groovy"
        artifacts << "${workingDir}/test/unit/com/mycompany/ControllerTestControllerTests.groovy"
        when:
        execute(true, 'grails:create-controller', "-DgrailsVersion=${grailsVersion}")
        then:
        getOutput() isSuccessfulTestRun()
    }

    def "test create-domain-class"() {
        given:
        workingDir = 'functional/test-application'
        artifacts << "${workingDir}/grails-app/domain/com/mycompany/DomainTest.groovy"
        artifacts << "${workingDir}/test/unit/com/mycompany/DomainTestTests.groovy"
        when:
        execute(true, 'grails:create-domain-class', "-DgrailsVersion=${grailsVersion}")
        then:
        getOutput() isSuccessfulTestRun()
    }

    def "test create-integration-test"() {
        given:
        workingDir = 'functional/test-application'
        checkForUpgrade(workingDir)
        artifacts << "${workingDir}/test/integration/com/mycompany/IntegrationTestTests.groovy"
        when:
        execute(true, 'grails:create-integration-test', "-DgrailsVersion=${grailsVersion}")
        then:
        getOutput() isSuccessfulTestRun()
    }

    def "test create-script"() {
        given:
        workingDir = 'functional/test-application'
        artifacts << "${workingDir}/scripts/TestScript.groovy"
        when:
        execute(true, 'grails:create-script', "-DgrailsVersion=${grailsVersion}")
        then:
        getOutput() isSuccessfulTestRun()
    }

    def "test create-service"() {
        given:
        workingDir = 'functional/test-application'
        artifacts << "${workingDir}/grails-app/services/com/mycompany/ServiceTestService.groovy"
        artifacts << "${workingDir}/test/unit/com/mycompany/ServiceTestServiceTests.groovy"
        when:
        execute(true, 'grails:create-service', "-DgrailsVersion=${grailsVersion}")
        then:
        getOutput() isSuccessfulTestRun()
    }

    def "test create-tag-lib"() {
        given:
        workingDir = 'functional/test-application'
        artifacts << "${workingDir}/grails-app/taglib/com/mycompany/TagLibTestTagLib.groovy"
        artifacts << "${workingDir}/test/unit/com/mycompany/TagLibTestTagLibTests.groovy"
        when:
        execute(true, 'grails:create-tag-lib', "-DgrailsVersion=${grailsVersion}")
        then:
        getOutput() isSuccessfulTestRun()
    }

    def "test create-unit-test"() {
        given:
        workingDir = 'functional/test-application'
        artifacts << "${workingDir}/test/unit/com/mycompany/UnitTestTestTests.groovy"
        when:
        execute(true, 'grails:create-unit-test', "-DgrailsVersion=${grailsVersion}")
        then:
        getOutput() isSuccessfulTestRun()
    }

    def "test exec"() {
        given:
        workingDir = 'functional/test-application'
        when:
        execute(true, 'grails:exec', "-DgrailsVersion=${grailsVersion}")
        then:
        getOutput() isSuccessfulTestRun()
    }

    def "test generate-all"() {
        given:
        workingDir = 'functional/test-application'
        modifyPom("${workingDir}/pom.xml", '<domainClassName>com.mycompany.DomainTest</domainClassName>', '<domainClassName>com.mycompany.DomainTestGenAll</domainClassName>')
        artifacts << "${workingDir}/grails-app/domain/com/mycompany/DomainTestGenAll.groovy"
        artifacts << "${workingDir}/grails-app/controllers/com/mycompany/DomainTestGenAllController.groovy"
        artifacts << "${workingDir}/grails-app/views/domainTestGenAll/create.gsp"
        artifacts << "${workingDir}/grails-app/views/domainTestGenAll/edit.gsp"
        artifacts << "${workingDir}/grails-app/views/domainTestGenAll/list.gsp"
        artifacts << "${workingDir}/grails-app/views/domainTestGenAll/show.gsp"
        artifacts << "${workingDir}/test/unit/com/mycompany/DomainTestGenAllTests.groovy"
        artifacts << "${workingDir}/test/unit/com/mycompany/DomainTestGenAllControllerTests.groovy"
        when:
        execute(true, 'grails:create-domain-class', "-DgrailsVersion=${grailsVersion}")
        execute('grails:generate-all', "-DgrailsVersion=${grailsVersion}")
        then:
        getOutput() isSuccessfulTestRun()
    }

    def "test generate-controller"() {
        given:
        workingDir = 'functional/test-application'
        modifyPom("${workingDir}/pom.xml", '<domainClassName>com.mycompany.DomainTestGenAll</domainClassName>', '<domainClassName>com.mycompany.DomainTestGenCtlr</domainClassName>')
        artifacts << "${workingDir}/grails-app/domain/com/mycompany/DomainTestGenCtlr.groovy"
        artifacts << "${workingDir}/grails-app/controllers/com/mycompany/DomainTestGenCtlrController.groovy"
        artifacts << "${workingDir}/test/unit/com/mycompany/DomainTestGenCtlrControllerTests.groovy"
        when:
        execute(true, 'grails:create-domain-class', "-DgrailsVersion=${grailsVersion}")
        execute('grails:generate-controller', "-DgrailsVersion=${grailsVersion}")
        then:
        getOutput() isSuccessfulTestRun()
    }

    def "test generate-views"() {
        given:
        workingDir = 'functional/test-application'
        artifacts << "${workingDir}/grails-app/domain/com/mycompany/DomainTestGenCtlr.groovy"
        artifacts << "${workingDir}/grails-app/views/domainTestGenCtlr/create.gsp"
        artifacts << "${workingDir}/grails-app/views/domainTestGenCtlr/edit.gsp"
        artifacts << "${workingDir}/grails-app/views/domainTestGenCtlr/list.gsp"
        artifacts << "${workingDir}/grails-app/views/domainTestGenCtlr/show.gsp"
        when:
        execute(true, 'grails:generate-views', "-DgrailsVersion=${grailsVersion}")
        then:
        getOutput() isSuccessfulTestRun()
    }

    def "test install-templates"() {
        given:
        workingDir = 'functional/test-application'
        artifacts << "${workingDir}/src/templates/artifacts/Controller.groovy"
        artifacts << "${workingDir}/src/templates/artifacts/DomainClass.groovy"
        artifacts << "${workingDir}/src/templates/artifacts/Filters.groovy"
        artifacts << "${workingDir}/src/templates/artifacts/hibernate.cfg.xml"
        artifacts << "${workingDir}/src/templates/artifacts/Script.groovy"
        artifacts << "${workingDir}/src/templates/artifacts/Service.groovy"
        artifacts << "${workingDir}/src/templates/artifacts/TagLib.groovy"
        artifacts << "${workingDir}/src/templates/artifacts/Tests.groovy"
        artifacts << "${workingDir}/src/templates/artifacts/WebTest.groovy"
        artifacts << "${workingDir}/src/templates/scaffolding/Controller.groovy"
        artifacts << "${workingDir}/src/templates/scaffolding/create.gsp"
        artifacts << "${workingDir}/src/templates/scaffolding/edit.gsp"
        artifacts << "${workingDir}/src/templates/scaffolding/list.gsp"
        artifacts << "${workingDir}/src/templates/scaffolding/renderEditor.template"
        artifacts << "${workingDir}/src/templates/scaffolding/show.gsp"
        artifacts << "${workingDir}/src/templates/war/web.xml"
        when:
        execute(true, 'grails:install-templates', "-DgrailsVersion=${grailsVersion}")
        then:
        getOutput() isSuccessfulTestRun()
    }

    def "test set-version"() {
        given:
        workingDir = 'functional/test-application'
        when:
        execute(true, 'grails:set-version', "-DgrailsVersion=${grailsVersion}")
        then:
        getOutput() isSuccessfulTestRun()
    }

    def "test test-app"() {
        given:
        workingDir = 'functional/test-application'
        when:
        execute(true, 'grails:test-app', "-DgrailsVersion=${grailsVersion}")
        then:
        getOutput() isSuccessfulTestRun()
    }

    def "test mvn-clean"() {
        given:
        workingDir = 'functional/test-application'
        when:
        execute(true, 'grails:maven-clean', "-DgrailsVersion=${grailsVersion}")
        then:
        getOutput() isSuccessfulTestRun()
    }

    def "test mvn-compile"() {
        given:
        workingDir = 'functional/test-application'
        when:
        execute(true, 'grails:maven-compile', "-DgrailsVersion=${grailsVersion}")
        then:
        getOutput() isSuccessfulTestRun()
    }

    def "test mvn-config-directories"() {
        given:
        workingDir = 'functional/test-application'
        when:
        execute(true, 'grails:config-directories', "-DgrailsVersion=${grailsVersion}")
        then:
        getOutput() isSuccessfulTestRun()
    }

    def "test mvn-functional-test"() {
        given:
        workingDir = 'functional/test-application'
        when:
        execute(true, 'grails:maven-functional-test', "-DgrailsVersion=${grailsVersion}")
        then:
        getOutput() isSuccessfulTestRun()
    }

    def "test mvn-functional-test-with-grails-skip"() {
        given:
        workingDir = 'functional/test-application'
        when:
        execute(true, 'grails:maven-functional-test', "-DgrailsVersion=${grailsVersion}",  "-Dgrails.test.skip=true")
        then:
        getOutput() isSuccessfulTestRun()
    }

    def "test mvn-functional-test-with-mvn-skip"() {
        given:
        workingDir = 'functional/test-application'
        when:
        execute(true, 'grails:maven-functional-test', "-DgrailsVersion=${grailsVersion}", "-Dmaven.test.skip=true")
        then:
        getOutput() isSuccessfulTestRun()
    }

    def "test mvn-initialize-with-existing-project"() {
        given:
        workingDir = 'functional/test-application'
        when:
        execute(true, 'grails:init', "-DgrailsVersion=${grailsVersion}", "-Dproject.artifactId=test-application", "-Dproject.version=1.0-SNAPSHOT")
        then:
        getOutput() isSuccessfulTestRun()
    }

    def "test mvn-test"() {
        given:
        workingDir = 'functional/test-application'
        when:
        execute(true, 'grails:maven-test', "-DgrailsVersion=${grailsVersion}")
        then:
        getOutput() isSuccessfulTestRun()
    }

    def "test mvn-test-with-grails-skip"() {
        given:
        workingDir = 'functional/test-application'
        when:
        execute(true, 'grails:maven-test', "-DgrailsVersion=${grailsVersion}", "-Dgrails.test.skip=true")
        then:
        getOutput() isSuccessfulTestRun()
    }

    def "test mvn-test-with-maven-skip"() {
        given:
        workingDir = 'functional/test-application'
        when:
        execute(true, 'grails:maven-test', "-DgrailsVersion=${grailsVersion}", "-Dmaven.test.skip=true")
        then:
        getOutput() isSuccessfulTestRun()
    }

    def "test mvn-validate"() {
        given:
        workingDir = 'functional/test-application'
        when:
        execute(true, 'grails:validate', "-DgrailsVersion=${grailsVersion}", "-Dproject.artifactId=test-application", "-Dproject.version=1.0-SNAPSHOT")
        then:
        getOutput() isSuccessfulTestRun()
    }
}