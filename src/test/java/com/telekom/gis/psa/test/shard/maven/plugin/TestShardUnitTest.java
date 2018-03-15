/**
 * ﻿Copyright 2012, Deutsche Telekom AG, DTAG GHS GIS. All rights reserved.
 */

package com.telekom.gis.psa.test.shard.maven.plugin;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.io.File;

/**
 * Unit test for test shard mojo, just tests for java exceptions and basic result
 *
 * @author Patrick Fischer patrick.fischer@qaware.de
 */
public class TestShardUnitTest {

    private static Map<String, Object> properties;
    private static TestShardCreatorMojo testShardCreatorMojo;
    private static TestShardIncludeMojo testShardIncludeMojo;
    private static TestShardCleanerMojo testShardCleanerMojo;

    @BeforeClass
    public static void loadProperties() throws Exception {
        properties = new HashMap<>();

        properties.put("outputFolder", "test-shards");

        //Creator properties
        properties.put("shardCount", 5);
        properties.put("includes", new String[]{"**/*Test*.java"});

        String testFolder = TestShardUnitTest.class.getClassLoader().getResource("testClasses").getFile();
        testFolder = testFolder.replaceAll("%20", " ");

        properties.put("testFolders", new String[]{testFolder});
        properties.put("pathToPackage", "src\\test\\java");

        testShardCreatorMojo = new TestShardCreatorMojo();
        loadMojo(testShardCreatorMojo);

        testShardCleanerMojo = new TestShardCleanerMojo();
        loadMojo(testShardCleanerMojo);

        testShardCleanerMojo.execute();
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

    @Test
    public void testTestShardCreator() throws MojoFailureException, MojoExecutionException {
        testShardCreatorMojo.execute();
        Assert.assertEquals(10, testShardCreatorMojo.getReader().getTestFilePaths().size());

        File file = new File("test-shards");
        Assert.assertTrue(file.isDirectory());
        Assert.assertTrue(file.list((dir, name) -> name.startsWith("shard") && name.endsWith(".txt")).length == 5);
    }

    @AfterClass
    public static void testTestShardCleaner() throws MojoFailureException, MojoExecutionException {
        testShardCleanerMojo.execute();

        File file = new File("test-shards");
        if (file.isDirectory()) {
            Assert.assertTrue(file.list((dir, name) -> name.startsWith("shard") && name.endsWith(".txt")).length == 0);
            Assert.assertTrue(file.delete());
        }
    }

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
