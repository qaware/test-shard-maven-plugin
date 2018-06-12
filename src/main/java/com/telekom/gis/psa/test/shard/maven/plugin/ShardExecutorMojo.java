/**
 * ﻿Copyright 2012, Deutsche Telekom AG, DTAG GHS GIS. All rights reserved.
 */

package com.telekom.gis.psa.test.shard.maven.plugin;

import com.telekom.gis.psa.test.shard.maven.plugin.cucumber.CucumberFeatureFileManager;
import com.telekom.gis.psa.test.shard.maven.plugin.utils.ShardConstants;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * The include mojo, loads one shard file an puts it into the surefire plugin
 *
 * @author Patrick Fischer patrick.fischer@qaware.de
 */
@Mojo(name = "execute-shard", defaultPhase = LifecyclePhase.PRE_INTEGRATION_TEST)
public class ShardExecutorMojo extends AbstractShardMojo {
    
    @Parameter(property = "shard.execute.cucumberWrapperClass")
    protected String cucumberWrapperClass;

    @Parameter(property = "shard.execute.shardIndex", required = true)
    private int shardIndex;

    /**
     * The execution function for this goal. It checks the given shardIndex and executes just the shard with the given
     * shard index
     *
     * @throws MojoFailureException if something wrong with the dependencies or sources of a the plugin
     * @throws MojoExecutionException if there is a problem in the properties
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        File outputFolder = new File(this.outputFolder);
        List<String> testShardPath = getTestShards(outputFolder);

        if(shardIndex < 0 || shardIndex >= testShardPath.size()){
            throw new MojoExecutionException("Invalid shard number, (shard count: " + testShardPath.size() + "; index: " +
                    shardIndex);
        }

        String shardName = testShardPath.get(shardIndex);

        if(shardName.matches(ShardConstants.CUCUMBER_SHARD_REGEX)){
            CucumberFeatureFileManager.disableOtherFeatures(outputFolder, shardName);

            if(cucumberWrapperClass == null || cucumberWrapperClass.isEmpty()){
                throw new MojoExecutionException("Failed to include cucumber file: missing cucumber wrapper class, use" +
                    " property <cucumberWrapperClass>");
            }

            project.getProperties().setProperty("test", cucumberWrapperClass);
            project.getProperties().setProperty("it.test", cucumberWrapperClass);
/*            String argLine = project.getProperties().getProperty("argLine");
            if(argLine == null){
                argLine = "";
            }
            argLine += " -Dcucumber.options=\"-plugin junit:target/failsafe-reports/TEST-" + cucumberWrapperClass + Integer.toString(shardIndex) + ".xml\"";
            project.getProperties().setProperty("argLine", argLine);*/
            project.getProperties().setProperty("surefire.reportNameSuffix", Integer.toString(shardIndex));
        }else if(shardName.matches(ShardConstants.JUNIT_SHARD_REGEX)){
            String includesFile = outputFolder + File.separator + shardName;
            project.getProperties().setProperty("surefire.includesFile", includesFile);
            project.getProperties().setProperty("failsafe.includesFile", includesFile);
        }else{
            getLog().warn(shardName + " does not match any shard regex, the file will be ignored");
            return;
        }

        getLog().info("Added test shard \"" + shardName + "\" to surefire.");
    }

    private List<String> getTestShards(File outputFolder) throws MojoExecutionException {
        String[] testShardArray = outputFolder.list((dir, name) -> ShardConstants.isShardFile(name));
        if(testShardArray == null || testShardArray.length == 0){
            throw new MojoExecutionException("No test shards found, shards must be created first.");
        }

        return Arrays.asList(testShardArray);
    }
}
