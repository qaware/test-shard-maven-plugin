/**
 * ﻿Copyright 2012, Deutsche Telekom AG, DTAG GHS GIS. All rights reserved.
 */

package com.telekom.gis.psa.test.shard.maven.plugin.cucumber;

import com.telekom.gis.psa.test.shard.maven.plugin.AbstractShardCreatorMojo;
import com.telekom.gis.psa.test.shard.maven.plugin.utils.ShardConstants;
import com.telekom.gis.psa.test.shard.maven.plugin.utils.TestClassFileFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;

/**
 * The "create-junit-shards" goal handles the input an creates test shards for junit tests.
 * These shards are called "junit-shardXX.txt"
 *
 * @author Patrick Fischer patrick.fischer@qaware.de
 */
@Mojo(name = "create-cucumber-shards", defaultPhase = LifecyclePhase.PROCESS_TEST_SOURCES)
public class CucumberShardCreatorMojo extends AbstractShardCreatorMojo {

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if(includes == null || includes.length == 0){
            includes = ShardConstants.DEFAULT_CUCUMBER_INCLUDE;
        }

        TestClassFileFilter fileFilter = new TestClassFileFilter(includes, excludes);
        createShards(ShardConstants.CUCUMBER_SHARD_PREFIX, fileFilter);
    }
}
