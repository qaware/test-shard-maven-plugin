/**
 * ﻿Copyright 2012, Deutsche Telekom AG, DTAG GHS GIS. All rights reserved.
 */

package com.telekom.gis.psa.test.shard.maven.plugin.junit;

import com.telekom.gis.psa.test.shard.maven.plugin.AbstractShardExecutorMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;

import java.io.File;

/**
 * The execute mojo for junit shards, loads one shard file and includes it into the surefire plugin
 *
 * @author Patrick Fischer patrick.fischer@qaware.de
 */
@Mojo(name = "include-junit-shard")
public class JUnitShardIncludeMojo extends AbstractShardExecutorMojo {

    @Override
    public void executeShard(String shardName) throws MojoExecutionException, MojoFailureException {
        project.getProperties().setProperty("surefire.includesFile", outputFolder + File.separator + shardName);
        getLog().info("Added test shard \"" + shardName + "\" to surefire.");
    }

}
