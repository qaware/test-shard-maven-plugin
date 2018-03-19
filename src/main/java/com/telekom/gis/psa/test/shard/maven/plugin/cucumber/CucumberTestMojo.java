/**
 * ﻿Copyright 2012, Deutsche Telekom AG, DTAG GHS GIS. All rights reserved.
 */

package com.telekom.gis.psa.test.shard.maven.plugin.cucumber;

import com.telekom.gis.psa.test.shard.maven.plugin.AbstractShardMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

/**
 * The cucumber test mojo: runs all cucumber feature files. Before the mojo is executed
 * the feature files have to be sorted in feature-shards. To do so, run the test shard creator
 * mojo first.
 *
 * @author Patrick Fischer patrick.fischer@qaware.de
 */
public class CucumberTestMojo extends AbstractShardMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {

    }
}
