/**
 * ﻿Copyright 2012, Deutsche Telekom AG, DTAG GHS GIS. All rights reserved.
 */

package com.telekom.gis.psa.test.shard.maven.plugin;

import com.telekom.gis.psa.test.shard.maven.plugin.cucumber.CucumberFeatureFileManager;
import com.telekom.gis.psa.test.shard.maven.plugin.cucumber.CucumberShardCreatorMojo;
import com.telekom.gis.psa.test.shard.maven.plugin.cucumber.CucumberShardPostExecutorMojo;
import com.telekom.gis.psa.test.shard.maven.plugin.utils.ShardConstants;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Unit test for cucumber shard mojos, just tests for java exceptions and basic result
 *
 * @author Patrick Fischer patrick.fischer@qaware.de
 */
public class CucumberShardUnitTest {

    private static Map<String, Object> properties;

    private static CucumberShardCreatorMojo cucumberShardCreatorMojo;
    private static CucumberShardPostExecutorMojo cucumberShardPostExecutorMojo;
    private static ShardCleanerMojo shardCleanerMojo;

    private static String testFolder;

    /**
     * Initializes all necessary fields and loads mojo instances
     *
     * @throws Exception if an exception occurs
     */
    @BeforeClass
    public static void loadProperties() throws Exception {
        properties = new HashMap<>();

        properties.put("outputFolder", "target/test-shards");

        //Creator properties
        properties.put("shardCount", 2);
        properties.put("includes", new String[]{"**/*Cucumber*.feature"});

        testFolder = CucumberShardUnitTest.class.getClassLoader().getResource("testClasses").getFile();
        testFolder = testFolder.replaceAll("%20", " ");

        properties.put("testFolders", new String[]{testFolder});
        properties.put("pathToPackage", "src\\test\\java");

        cucumberShardCreatorMojo = new CucumberShardCreatorMojo();
        loadMojo(cucumberShardCreatorMojo);

        cucumberShardPostExecutorMojo = new CucumberShardPostExecutorMojo();
        loadMojo(cucumberShardPostExecutorMojo);

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
     * @throws MojoFailureException   if something wrong with the dependencies or sources of a the plugin
     * @throws MojoExecutionException if there is a problem in the properties
     */
    @AfterClass
    public static void testTestShardCleaner() throws MojoFailureException, MojoExecutionException {
        shardCleanerMojo.execute();

        File file = new File("target/test-shards");
        if (file.isDirectory()) {
            Assert.assertTrue(file.list((dir, name) -> ShardConstants.isShardFile(name)).length == 0);
            Assert.assertTrue(file.delete());
        }
    }

    /**
     * Tests the shard creator mojo
     * 
     * @throws MojoFailureException   if something wrong with the dependencies or sources of a the plugin
     * @throws MojoExecutionException if there is a problem in the properties
     */
    @Test
    public void testShardCreator() throws MojoFailureException, MojoExecutionException {
        cucumberShardCreatorMojo.execute();
        Assert.assertEquals(2, cucumberShardCreatorMojo.getReader().getTestFilePaths().size());

        File file = new File("target/test-shards");
        Assert.assertTrue(file.isDirectory());
        Assert.assertTrue(file.list((dir, name) -> name.matches(ShardConstants.CUCUMBER_SHARD_REGEX)).length == 2);
    }

    /**
     * Tests feature file disabling
     *
     * @throws MojoFailureException   if something wrong with the dependencies or sources of a the plugin
     * @throws MojoExecutionException if there is a problem in the properties
     */
    @Test
    public void testShardDisable() throws MojoExecutionException, MojoFailureException {
        File dummy1 = new File(testFolder, "DummyCucumber1.feature");
        File disabledDummy1 = new File(testFolder, "DummyCucumber1.feature.ignore");
        Assert.assertTrue(dummy1.exists());
        Assert.assertFalse(disabledDummy1.exists());

        CucumberFeatureFileManager.disableFeatureFile(dummy1);
        Assert.assertFalse(dummy1.exists());
        Assert.assertTrue(disabledDummy1.exists());

        CucumberFeatureFileManager.enableFeatureFile(disabledDummy1);
        Assert.assertTrue(dummy1.exists());
        Assert.assertFalse(disabledDummy1.exists());
    }
}
