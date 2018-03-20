/**
 * ﻿Copyright 2012, Deutsche Telekom AG, DTAG GHS GIS. All rights reserved.
 */

package com.telekom.gis.psa.test.shard.maven.plugin;

import com.telekom.gis.psa.test.shard.maven.plugin.junit.JUnitShardCreatorMojo;
import com.telekom.gis.psa.test.shard.maven.plugin.utils.ShardConstants;
import com.telekom.gis.psa.test.shard.maven.plugin.utils.TestClassFileFilter;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit test for junit shard mojos, just tests for java exceptions and basic result
 *
 * @author Patrick Fischer patrick.fischer@qaware.de
 */
public class JUnitShardUnitTest {

    private static Map<String, Object> properties;
    private static JUnitShardCreatorMojo jUnitShardCreatorMojo;
    private static ShardCleanerMojo shardCleanerMojo;

    /**
     * Creates the needed mojos and loads the default properties.
     *
     * @throws Exception if an Exception occurs
     */
    @BeforeClass
    public static void loadProperties() throws Exception {
        properties = new HashMap<>();

        properties.put("outputFolder", "target/test-shards");

        //Creator properties
        properties.put("shardCount", 5);
        properties.put("includes", new String[]{"**/*Test*.java"});

        String testFolder = JUnitShardUnitTest.class.getClassLoader().getResource("testClasses").getFile();
        testFolder = testFolder.replaceAll("%20", " ");

        properties.put("testFolders", new String[]{testFolder});
        properties.put("pathToPackage", "src\\test\\java");

        jUnitShardCreatorMojo = new JUnitShardCreatorMojo();
        loadMojo(jUnitShardCreatorMojo);

        shardCleanerMojo = new ShardCleanerMojo();
        loadMojo(shardCleanerMojo);

        shardCleanerMojo.execute();
    }

    private static void loadMojo(Object mojo) throws Exception {
        loadMojo(mojo.getClass(), mojo);
    }

    private static void loadMojo(Class<?> type, Object mojo) throws Exception {
        for (Field field : type.getDeclaredFields()) {
            if (!properties.containsKey(field.getName())) {
                continue;
            }
            field.setAccessible(true);
            field.set(mojo, properties.get(field.getName()));
        }

        if (type.getSuperclass() != null) {
            loadMojo(type.getSuperclass(), mojo);
        }
    }

    /**
     * Cleans up everything and in doing so, it tests the clean goal
     *
     * @throws MojoFailureException if something wrong with the dependencies or sources of a the plugin
     * @throws MojoExecutionException if there is a problem in the properties
     */
    @AfterClass
    public static void testTestShardCleaner() throws MojoFailureException, MojoExecutionException {
        shardCleanerMojo.execute();

        File file = new File("target/test-shards");
        if (file.isDirectory()) {
            Assert.assertTrue(file.list((dir, name) ->  ShardConstants.isShardFile(name)).length == 0);
            Assert.assertTrue(file.delete());
        }
    }

    /**
     * Tests the creator goal
     *
     * @throws MojoFailureException if something wrong with the dependencies or sources of a the plugin
     * @throws MojoExecutionException if there is a problem in the properties
     */
    @Test
    public void testTestShardCreator() throws MojoFailureException, MojoExecutionException {
        jUnitShardCreatorMojo.execute();
        Assert.assertEquals(10, jUnitShardCreatorMojo.getReader().getTestFilePaths().size());

        File file = new File("target/test-shards");
        Assert.assertTrue(file.isDirectory());
        Assert.assertTrue(file.list((dir, name) -> name.matches(ShardConstants.JUNIT_SHARD_REGEX)).length == 5);
    }

    /**
     * Test the test class file filter, whether it accepts/excludes the right files according to the maven file path pattern
     *
     * @throws IOException if an exception occurs
     */
    @Test
    public void testTestClassFileFilter() throws IOException {
        String[] includes = new String[]{"**/*Test.java"};
        String[] excludes = new String[]{"**/*No*Test.java"};

        TestClassFileFilter testClassFileFilter = new TestClassFileFilter(includes, excludes);
        File file0 = new File("UnitTest.java");
        File file1 = new File("UnitNoTest.java");
        File file2 = new File("NoUnitTest.java");

        boolean b = true;
        if (!file0.exists()) {
            b = file0.createNewFile();
        }
        if (!file1.exists()) {
            b &= file1.createNewFile();
        }
        if (!file2.exists()) {
            b &= file2.createNewFile();
        }
        Assert.assertTrue(b);

        Assert.assertTrue(testClassFileFilter.accept(file0));
        Assert.assertTrue(!testClassFileFilter.accept(file1));
        Assert.assertTrue(!testClassFileFilter.accept(file2));

        b = file0.delete();
        b &= file1.delete();
        b &= file2.delete();
        Assert.assertTrue(b);
    }
}
