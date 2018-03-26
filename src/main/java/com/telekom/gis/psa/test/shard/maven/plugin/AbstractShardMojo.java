/**
 * ﻿Copyright 2012, Deutsche Telekom AG, DTAG GHS GIS. All rights reserved.
 */

package com.telekom.gis.psa.test.shard.maven.plugin;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * The abstract mojo for the test shard mojos, containing all the shared data.
 *
 * @author Patrick Fischer patrick.fischer@qaware.de
 */
public abstract class AbstractShardMojo extends AbstractMojo {

    @Parameter(property = "shard.outputFolder", defaultValue = "${project.build.directory}/test-shards")
    protected String outputFolder;

    @Parameter(property = "project", readonly = true, defaultValue = "${project}")
    protected MavenProject project;
}
