/**
 * ﻿Copyright 2012, Deutsche Telekom AG, DTAG GHS GIS. All rights reserved.
 */

package com.telekom.gis.psa.test.shard.maven.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.File;

/**
 * The cleaner mojo: deletes all files in shard*.txt files in the output folder
 *
 * @author Patrick Fischer patrick.fischer@qaware.de
 */
@Mojo(name = "shard-clean")
public class TestShardCleanerMojo extends AbstractTestShardMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        File folder = new File(outputFolder);
        if (!folder.exists()) {
            return;
        }

        for (File file : folder.listFiles((dir, name) -> name.startsWith("shard") && name.endsWith(".txt"))) {
            file.delete();
        }
        getLog().info("Deleted all test shards");
    }
}
