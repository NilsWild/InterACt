# InterACt

InterACt is an integration testing tool that integrates components based on their unit-tests. It observes the unit-test
execution analyzes the observed data and generates new tests that validate if the expectations of a component towards
its entvironment are fulfilled.
An example project that utilizes InterACt can be found
here: [InteractionTestExample](https://github.com/NilsWild/InteractionTestExample)

## How to use the project

### Installing InterACt

1. run ```mvn install``` to add InterACt to your local maven repo.

### Starting InterACt controller server

2. Start a neo4j database E.g. using
   docker: ```docker run --rm -d -p 7474:7474 -p 7687:7687 -e NEO4J_AUTH=neo4j/somePassword -e 'NEO4JLABS_PLUGINS=[\"apoc\"]' -e 'NEO4J_dbms_security_procedures_unrestricted=apoc.*,algo.*' neo4j:5.7-community```

3. Start the InterACt controller
   server ```java --% -Dloader.path=interact-binder-rest\target\interact-binder-rest-1.0-SNAPSHOT.jar -Dloader.debug=true -jar .\interact-controller\target\interact-controller-1.0-SNAPSHOT.jar```

### preparing your project

4. Add the following Maven Dependencies to your projects to be tested. Currently InterACt only supports SpringWebFlux
   Clients.

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

5. Replace ```@Test``` with ```@InterACtTest``` as done in the example project and parameterize your tests by the
   stimulus and environment_response messages

6. Add the WebClientObserver Filter to any WebClient.
   For WebClients that are used to stimulate the component under test set the isTestHarness parameter to true.
   For those used by the component under test set the isTestHarness parameter to false.

7. Create an interact.properties file in your test resources folder and add information about the component under test
   as such:
   ```
   # URL to controller
   broker.url=http://localhost:8080
   component.name=ServiceA
   component.version=1.0.0
   ```

### Inspect the observed data

Access the neo4j database on http://localhost:7474. The username is neo4j and the password is somePassword.
To delete everything in the database either restart the db or execute the following
query ```MATCH (n) DETACH DELETE n```