/**
 * ﻿Copyright 2012, Deutsche Telekom AG, DTAG GHS GIS. All rights reserved.
 */

package com.telekom.gis.psa.test.shard.maven.plugin.cucumber;

import com.telekom.gis.psa.test.shard.maven.plugin.AbstractShardMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * The cucumber test mojo: runs all cucumber feature files. Before the mojo is executed
 * the feature files have to be sorted in feature-shards. To do so, run the test shard creator
 * mojo first.
 *
 * @author Patrick Fischer patrick.fischer@qaware.de
 */
@Mojo(name = "execute-cucumber-shard", defaultPhase = LifecyclePhase.TEST)
public class CucumberExecuteMojo extends AbstractShardMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            cucumber.api.cli.Main.main(new String[0]);
        } catch (Throwable throwable) {
            throw new MojoExecutionException("Failed to run cucumber tests", throwable);
        }
    }
}
