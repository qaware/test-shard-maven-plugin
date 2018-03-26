/**
 * ﻿Copyright 2012, Deutsche Telekom AG, DTAG GHS GIS. All rights reserved.
 */

package com.telekom.gis.psa.test.shard.maven.plugin.junit;

import com.telekom.gis.psa.test.shard.maven.plugin.AbstractShardCreatorMojo;
import com.telekom.gis.psa.test.shard.maven.plugin.utils.ShardConstants;
import com.telekom.gis.psa.test.shard.maven.plugin.utils.TestClassFileFilter;
import com.telekom.gis.psa.test.shard.maven.plugin.utils.TestFileReader;
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
@Mojo(name = "create-junit-shards", defaultPhase = LifecyclePhase.PROCESS_TEST_SOURCES)
public class JUnitShardCreatorMojo extends AbstractShardCreatorMojo {

    private JUnitFileReader reader;
    
    /**
     * Default constructor, initializes necessary fields
     */
    public JUnitShardCreatorMojo(){
        super();

        reader = new JUnitFileReader();
    }

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if(includes == null || includes.length == 0){
            includes = ShardConstants.DEFAULT_JUNIT_INCLUDE;
        }

        TestClassFileFilter fileFilter = new TestClassFileFilter(includes, excludes);
        createShards(ShardConstants.JUNIT_SHARD_PREFIX, fileFilter);
    }

    @Override
    public TestFileReader getReader() {
        return reader;
    }
}
