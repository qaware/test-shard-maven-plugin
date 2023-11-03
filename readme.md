# Test Shard Maven Plugin

The test shard maven plugin is a maven plugin to split tests into test shards (txt files). The main aim is to use these
shards for concurrent testing.

## Getting started

To include this plugin just add the following code snippet to the pom.xml of your project

```
<project>
	...	
	<build>
		...		
        <plugins>
			...	
            <plugin>
                <groupId>com.telekom.gis.psa</groupId>
                <artifactId>test-shard-maven-plugin</artifactId>
                <version>1.0-SNAPSHOT</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>[creator_goal]</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <shardCount>[shard_cound]</shardCount>
                </configuration>
            </plugin>			
			...
        </plugins>		
		...
    </build>	
	...
</project>
```

and replace [creator_goal] with either `create-junit-shards` or `create-cucumber-shards` and [shard_cound] with the
expected number of created shard files.

## Goals and plugin options

The maven shard creator plugin can create shards for basic junit test classes and cucumber feature files.
Make sure, that your project contains a cucumber junit wrapper class:

```
@RunWith(value = Cucumber.class)
@CucumberOptions(
     ...
)
public class PSACucumberItests {...}
```

The class should include all feature files.

### clean-shards

Default Phase: NONE

This goal deletes all test shards in the output directory. It is not necessary to call this goal on maven clean,
if the output folder is located in the target folder.

```
mvn com.telekom.gis.psa:test-shard-maven-plugin:clean-shards
```

### execute-shard

Default Phase: PRE_INTEGRATION_TEST

This goal executes a given test shard (gets the test shard by the given shardIndex) and includes it into surfire by
setting the property surefire.includesFile to the shard name.
Example command line call:

```
mvn com.telekom.gis.psa:test-shard-maven-plugin:execute-shard org.apache.maven.plugins:maven-surefire-plugin:test -Dtests.shardIndex=4
```

The shard index does not depend on the type of the test shard. For example, if you have 2 cucumber and 3 junit test
shards, the expected outcome
of the creator goals is:

```
- cucumber-shard0.txt
- cucumber-shard1.txt
- junit-shard0.txt
- junit-shard1.txt
- junit-shard2.txt
``` 

The index starts with 0 at `cucumber-shard0.txt`, continues with 2 at `junit-shard0.txt` and so on. So to execute
`junit-shard1.txt`, you have to execute this goal with shardIndex 3.

### create-junit-shards

Default Phase: PROCESS_TEST_SOURCES

This goal reads the test sources, splits them and creates the test shards.
For this goal test sources are supposed to be java files with junit tests
Example command line call:

```
mvn com.telekom.gis.psa:test-shard-maven-plugin:create-junit-shards -Dtests.shardCount=5
```

### create-cucumber-shards

Default Phase: PROCESS_TEST_SOURCES

This goal reads the test sources, splits them and creates the test shards.
For this goal test sources are supposed to be cucumber feature files.
Example command line call:

```
mvn com.telekom.gis.psa:test-shard-maven-plugin:create-junit-shards -Dtests.shardCount=5
```

### clean-cucumber-features

Default Phase: POST_INTEGRATION_TEST

This goal removes the .ignore extension from the feature files. This extension is used to disable feature files.

### Plugin options

If a parameter has no default value, it is required to set this parameter in the pom.xml of your project.

| Name                 | Parameter property                 | description                                                                                                                                                                                                                                                                                                                                       | goal                                                                 | default value                                              |
|----------------------|------------------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|----------------------------------------------------------------------|------------------------------------------------------------|
| cucumberWrapperClass | shard.execute.cucumberWrapperClass | The full class name (package and class name) of the wrapper class for cucumber tests                                                                                                                                                                                                                                                              | execute-shard                                                        | none, but only required if cucumber tests are executed     |
| shardIndex           | shard.execute.shardIndex           | The index of the shard to be loaded into surfire (see goal description)                                                                                                                                                                                                                                                                           | execute-shard                                                        |                                                            |
| outputFolder         | shard.outputFolder                 | The output directory for the test shards                                                                                                                                                                                                                                                                                                          | all                                                                  | `${project.build.directory}/test-shards`                   |                   
| shardCount           | shard.create.shardCount            | The amount of shards to be created (by each creator goal)                                                                                                                                                                                                                                                                                         | create-junit-shards, create-cucumber-shards                          |                                                            |
| includes             | shard.create.includes              | The path pattern for the test files to be included                                                                                                                                                                                                                                                                                                | create-junit-shards, create-cucumber-shards                          | `{**/*Test.java}` for junit, `{**/*.feature}` for cucumber | 
| excludes             | shard.create.excludes              | The path pattern for the test files to be excluded                                                                                                                                                                                                                                                                                                | create-junit-shards, create-cucumber-shards                          | []                                                         |
| testFolders          | shard.create.testFolders           | The directories, where to search for the the test files, do not include parts of package names                                                                                                                                                                                                                                                    | create-junit-shards, create-cucumber-shards, clean-cucumber-features | `${project.build.testSourceDirectory}`                     |
| testReportDirectory  | shard.create.testReportDirectory   | A directory that contains surefire report files of previous runs of the surefire plugin. If set, the test execution times are extracted from these reports and are used to distribute the tests among the configured number of shards for most even shard execution times. If this property is not set, the tests are simply partitioned by count | create-junit-shards, create-cucumber-shards                          | empty                                                      |
