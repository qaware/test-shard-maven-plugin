/**
 * ﻿Copyright 2012, Deutsche Telekom AG, DTAG GHS GIS. All rights reserved.
 */

package com.telekom.gis.psa.test.shard.maven.plugin;

import com.telekom.gis.psa.test.shard.maven.plugin.utils.ShardConstants;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.File;

/**
 * The cleaner mojo: deletes all test shards in the output folder. The mojo should be executed when the amount
 * of shards is decreased.
 *
 * @author Patrick Fischer patrick.fischer@qaware.de
 */
@Mojo(name = "shard-clean")
public class TestShardCleanerMojo extends AbstractShardMojo {

    /**
     * The execution function for this goal.
     *
     * @throws MojoFailureException   if something wrong with the dependencies or sources of a the plugin
     * @throws MojoExecutionException if there is a problem in the properties
     */
    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        File folder = new File(outputFolder);
        if (!folder.exists()) {
            return;
        }

        File[] shardFiles = folder.listFiles(this::isShardFile);

        if(shardFiles == null){
            return;
        }

        for (File file : shardFiles) {
            if(!file.delete()){
                getLog().warn("Failed to delete test shard: " + file.getPath());
            }
        }
        getLog().info("Deleted all test shards");
    }

    private boolean isShardFile(File dir, String fileName) {
        return  fileName.matches(ShardConstants.JUNIT_SHARD_REGEX) ||
                fileName.matches(ShardConstants.CUCUMBER_SHARD_REGEX);

    }
}
