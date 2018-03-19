/**
 * ﻿Copyright 2012, Deutsche Telekom AG, DTAG GHS GIS. All rights reserved.
 */

package com.telekom.gis.psa.test.shard.maven.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * The include mojo, loads one shard file an puts it into the surefire plugin
 *
 * @author Patrick Fischer patrick.fischer@qaware.de
 */
@Mojo(name = "shard-include")
public class TestShardIncludeMojo extends AbstractTestShardMojo{

    @Parameter(property = "tests.shardIndex", required = true)
    private int shardIndex;

    /**
     * The execution function for this goal.
     *
     * @throws MojoFailureException if something wrong with the dependencies or sources of a the plugin
     * @throws MojoExecutionException if there is a problem in the properties
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        List<String> testShardPath = getTestShards();

        if(shardIndex < 0 || shardIndex >= testShardPath.size()){
            throw new MojoExecutionException("Invalid test number, (shard count: " + testShardPath.size() + "; index: " +
                    shardIndex);
        }

        project.getProperties().setProperty("surefire.includesFile", outputFolder + File.separator + testShardPath.get(shardIndex));
        getLog().info("Added test shard \"" + testShardPath.get(shardIndex) + "\" to surefire.");
    }

    private List<String> getTestShards() throws MojoExecutionException {
        File file = new File(outputFolder);
        String[] testShardArray = file.list();
        if(testShardArray == null || testShardArray.length == 0){
            throw new MojoExecutionException("No test shards found, shards must be created first.");
        }
        List<String> testShards = new ArrayList<>();
        Collections.addAll(testShards, testShardArray);
        return testShards;
    }
}
