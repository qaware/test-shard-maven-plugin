/**
 * ﻿Copyright 2012, Deutsche Telekom AG, DTAG GHS GIS. All rights reserved.
 */

package com.telekom.gis.psa.test.shard.maven.plugin.cucumber;

import com.telekom.gis.psa.test.shard.maven.plugin.AbstractShardCreatorMojo;
import com.telekom.gis.psa.test.shard.maven.plugin.utils.ShardConstants;
import com.telekom.gis.psa.test.shard.maven.plugin.utils.TestClassFileFilter;
import com.telekom.gis.psa.test.shard.maven.plugin.utils.TestFileReader;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.file.Files.*;

/**
 * The "create-junit-shards" goal handles the input an creates test shards for junit tests.
 * These shards are called "junit-shardXX.txt"
 *
 * @author Patrick Fischer patrick.fischer@qaware.de
 */
@Mojo(name = "create-cucumber-shards", defaultPhase = LifecyclePhase.PROCESS_TEST_SOURCES)
public class CucumberShardCreatorMojo extends AbstractShardCreatorMojo {

    private CucumberFileReader reader;

    /**
     * Default constructor, initializes necessary fields
     */
    public CucumberShardCreatorMojo(){
        super();

        reader = new CucumberFileReader();
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if(includes == null || includes.length == 0){
            includes = ShardConstants.DEFAULT_CUCUMBER_INCLUDE;
        }

        TestClassFileFilter fileFilter = new TestClassFileFilter(includes, excludes);
        createShards(ShardConstants.CUCUMBER_SHARD_PREFIX, fileFilter);
    }

    @Override
    public TestFileReader getReader() {
        return reader;
    }

    private static final Pattern FEATURE_PATTERN = Pattern.compile("^(?:Feature|Funktionalität): *(.+)$");

    @Override
    public Map<String, String> getMapping(List<String> testClassList) {
        Map<String, String> result = new HashMap<>();
        for (String testClass : testClassList) {
            String featureName = Arrays.stream(readFile(testClass).split("\n"))
                    .map(line -> FEATURE_PATTERN.matcher(line.trim()))
                    .filter(Matcher::matches)
                    .map(matcher -> matcher.group(1))
                    .findFirst()
                    .orElse(testClass);
            if(result.get(featureName) != null) {
                throw new IllegalStateException("Feature names must be unique; " + featureName + " occurs more than once");
            }
            result.put(featureName, testClass);
        }
        getLog().info("Mapping: " + result);
        return result;
    }

    private String readFile(String fileName) {
        StringBuilder resultStringBuilder = new StringBuilder();

        try (InputStream is = newInputStream(Paths.get(fileName)); BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = br.readLine()) != null) {
                resultStringBuilder.append(line).append("\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return resultStringBuilder.toString();
    }
}
