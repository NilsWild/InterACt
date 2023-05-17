# InterACt
InterACt is an integration testing tool that integrates components based on their unit-tests. It observes the unit-test execution analyzes the observed data and generates new tests that validate if the expectations of a component towards its entvironment are fulfilled.
An example project that utilizes InterACt can be found here:
## How to use the project
### Installing InterACt
1. run ```mvn install``` to add InterACt to your local maven repo.

### Starting InterACt controller server
2. Start a neo4j database E.g. using docker: ```docker run --rm -d -p 7474:7474 -p 7687:7687 -e NEO4J_AUTH=neo4j/somePassword -e 'NEO4JLABS_PLUGINS=[\"apoc\"]' -e 'NEO4J_dbms_security_procedures_unrestricted=apoc.*,algo.*' neo4j:5.7-community```

3. Start the InterACt controller server ```java --% -Dloader.path=interact-binder-rest\target\interact-binder-rest-1.0-SNAPSHOT.jar -Dloader.debug=true -jar .\interact-controller\target\interact-controller-1.0-SNAPSHOT.jar```

### preparing your project
4. Add the following Maven Dependencies to your projects to be tested

        <dependency>
            <groupId>de.rwth.swc.interact</groupId>
            <artifactId>interact-junit5</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>de.rwth.swc.interact</groupId>
            <artifactId>interact-observer-spring-rest</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>
        <dependency>
            <groupId>de.rwth.swc.interact</groupId>
            <artifactId>interact-integrator-spring-rest</artifactId>
            <version>1.0-SNAPSHOT</version>
        </dependency>

5. Add ```@ExtendWith(ExIT.class)``` to your JUnit Test Class

6. For Spring Projects add ```@Import({TestConfiguration.class, SpringRestExITConfiguration.class, SpringRestExITIntegrationProxyConfiguration.class})```

And Configuration Class

    @Configuration
    public class TestConfiguration {

        @Bean
        public TestRestTemplate testRestTemplate(
                RestTemplateBuilder restTemplateBuilder,
                TestRestTemplateObservationInterceptor interceptor1,
                TestRestTemplateIntegrationInterceptor interceptor2
        ) {
            var testRestTemplate = new TestRestTemplate(restTemplateBuilder);
            testRestTemplate.getRestTemplate().getInterceptors()
                    .removeIf(it -> it instanceof RestTemplateObservationInterceptor || it instanceof RestTemplateIntegrationInterceptor);
    
    
            testRestTemplate.getRestTemplate().getInterceptors().add(interceptor2);
            testRestTemplate.getRestTemplate().getInterceptors().add(interceptor1);
            return testRestTemplate;
        }
    }

8. Create an interact.properties file in your test resources folder and add information about the component under test as such:
   ```
   # URL to controller
   broker.url=http://localhost:8080
   component.name=ServiceA
   component.version=1.0.0
   ```

9. Create an junit-platform.properties file to set the execution mode to UNIT or INTERACTION. Run in UNIT mode first for each project. Then re-execute the tests in INTERACTION mode until all interaction expectations are validated.

   ```interact.mode=UNIT```

### Inspect the observed data
Access the neo4j database on http://localhost:7474. The username is neo4j and the password is somePassword.
To delete everything in the database either restart the db or execute the following query ```MATCH (n) DETACH DELETE n```