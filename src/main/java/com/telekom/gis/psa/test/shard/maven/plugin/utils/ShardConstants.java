/**
 * ﻿Copyright 2012, Deutsche Telekom AG, DTAG GHS GIS. All rights reserved.
 */

package com.telekom.gis.psa.test.shard.maven.plugin.utils;

/**
 * Some constants for test shards
 *
 * @author Patrick Fischer patrick.fischer@qaware.de
 */
public final class ShardConstants {

    private ShardConstants() {
        //No instances
    }

    /**
     * The shard prefix for cucumber shards. The shard prefix is the name of the file,
     * without out the shard index and the file type
     */
    public static final String CUCUMBER_SHARD_PREFIX = "cucumber-shard";

    /**
     * The shard prefix for junit test shards. The shard prefix is the name of the file,
     * without out the shard index and the file type
     */
    public static final String JUNIT_SHARD_PREFIX = "junit-shard";

    /**
     * The cucumber shard regex, which matches any cucumber-shards with <code>shard prefix + shard index + ".txt"</code>
     * as file name.
     */
    public static final String CUCUMBER_SHARD_REGEX = CUCUMBER_SHARD_PREFIX + "\\d+\\.txt";

    /**
     * The junit test shard regex, which matches any junit-test-shards with <code>shard prefix + shard index + ".txt"</code>
     * as file name.
     */
    public static final String JUNIT_SHARD_REGEX = JUNIT_SHARD_PREFIX + "\\d+\\.txt";

    /**
     * The default include file pattern for cucumber tests.
     */
    public static final String[] DEFAULT_CUCUMBER_INCLUDE = new String[]{"**/*.feature"};

    /**
     * The default include file pattern for junit tests.
     */
    public static final String[] DEFAULT_JUNIT_INCLUDE = new String[]{"**/*Test.java"};

    /**
     * A file name filter, which tests the fileName against all shard regex constants.
     *
     * @param fileName the name of the file
     * @return true if it matches any shard regex
     */
    public static boolean isShardFile(String fileName) {
        return  fileName.matches(ShardConstants.JUNIT_SHARD_REGEX) ||
                fileName.matches(ShardConstants.CUCUMBER_SHARD_REGEX);
    }
}
