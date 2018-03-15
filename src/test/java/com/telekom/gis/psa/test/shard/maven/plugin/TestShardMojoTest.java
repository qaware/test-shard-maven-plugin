/**
 * ﻿Copyright 2012, Deutsche Telekom AG, DTAG GHS GIS. All rights reserved.
 */

package com.telekom.gis.psa.test.shard.maven.plugin;

import org.apache.maven.plugin.testing.MojoRule;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;

/**
 * A mojo test, it includes getting parameters from pom or with maven variables.
 *
 * @author Patrick Fischer patrick.fischer@qaware.de
 */
public class TestShardMojoTest {

    @Rule
    public MojoRule rule = new MojoRule();

    @Test
    public void testShardWorkflow() throws Exception {
        String path = TestShardMojoTest.class.getResource("/test-project/pom.xml").getPath();
        File pom = new File(path);
        Assert.assertNotNull(pom);
        Assert.assertTrue(pom.exists());

        TestShardCreatorMojo creatorMojo = (TestShardCreatorMojo) rule.lookupMojo("shard-creator", pom);
        creatorMojo.execute();
        Assert.assertEquals(creatorMojo.getReader().getTestFilePaths().size(), 2);
    }
}
