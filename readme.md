# Test Shard Maven Plugin

The test shard maven plugin is a maven plugin to split tests into test shards (txt files). The main aim is to use these shards for concurrent testing. The plugin reads all test classes, splits them into a given amount of
test shards in the the maven process-test-resources life-cycle phase. These shards can be included into surfire maven plugin by running the shard-include goal of this plugin. 

## Getting started

To include this plugin just add, it to the pom.xml of your project.
```
<project>
	...	
	<build>
		...		
        <plugins>
			...	
            <plugin>
                <artifactId>test-shard-maven-plugin</artifactId>
                <executions>
                    <execution>
                        <id>tests</id>
                        <goals>
                            <goal>shard-creator</goal>
                        </goals>
                    </execution>
                </executions>
                <configuration>
                    <includes>
                        <include>**/*Test.java</include>
                    </includes>
                    <excludes>
                        <exclude>**/*NoTest.java</exclude>
                    </excludes>
                    <testFolders>
                        <testFolder>src/test/java</testFolder>
                    </testFolders>
                    <shardCount>5</shardCount>
                    <outputFolder>target/test-shards</outputFolder>
                </configuration>
            </plugin>			
			...
        </plugins>		
		...
    </build>	
	...
</project>
```

## Goals and plugin options

The plugin contains three goals: shard-creator, shard-include and shard-clean. Just the shard-creator goal in executed in the maven build process. The other two can be executed by the command line.

### shard-creator

This goal reads the test sources, splits then and creates the test shards.
Example command line call:
```
mvn com.telekom.gis.psa:test-shard-maven-plugin:shard-creator -Dtests.shardCount=5
```

### shard-include

This goal includes a given test shard (gets the test shard by the given shardIndex) and sets the surfire property surefire.includesFile.
Example command line call:
```
mvn com.telekom.gis.psa:test-shard-maven-plugin:shard-include org.apache.maven.plugins:maven-surefire-plugin:test -Dtests.shardIndex=4
```

### shard-clean

This goal deletes all test shards in the output directory.
```
mvn com.telekom.gis.psa:test-shard-maven-plugin:shard-clean
```

### plugin options

If a parameter has no default value, it is required to set this parameter in the pom.xml of your project.

Name | Parameter property | description | goal | example | default value
--- | --- | --- | --- | --- | ---
outputFolder | tests.outputFolder | The output directory for the test shards | shard-creator, shard-include, shard-clean | `<outputFolder>target/test-shards</outputFolder>` | `${project.build.directory}/test-shards`
shardCount | tests.shardCount | The amount of shards to be created | shard-creator | `<shardCount>5</shardCount>` |
includes | tests.includes | The path pattern for the test files to be included | shard-creator | `<excludes><exclude>**/*Test.java</exclude></excludes>` | `**/*Test.java`
excludes | tests.excludes | The path pattern for the test files to be excluded | shard-creator | `<excludes><exclude>**/*NoTest.java</exclude></excludes>` | []
testFolders | tests.testFolders | The directories, where to search for the the test files | shard-creator | `<testFolders><testFolder>src/test/java</testFolder></testFolders>` | `${project.build.testSourceDirectory}`
shardIndex | tests.shardIndex | The index of the shard to be loaded into surfire (0 <= shardIndex < shardCount) | shard-include | `-Dtests.shardIndex=1` | 

