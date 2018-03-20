/**
 * ﻿Copyright 2012, Deutsche Telekom AG, DTAG GHS GIS. All rights reserved.
 */

package com.telekom.gis.psa.test.shard.maven.plugin;

import com.telekom.gis.psa.test.shard.maven.plugin.utils.ShardFileWriter;
import com.telekom.gis.psa.test.shard.maven.plugin.utils.TestClassFileFilter;
import com.telekom.gis.psa.test.shard.maven.plugin.utils.TestFileReader;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The creator mojo, reads the test files and created the shard files
 *
 * @author Patrick Fischer patrick.fischer@qaware.de
 */
public abstract class AbstractShardCreatorMojo extends AbstractShardMojo {

    private ShardFileWriter writer;

    @Parameter(property = "shard.create.shardCount", required = true)
    protected int shardCount;

    @Parameter(property = "shard.create.includes")
    protected String[] includes;

    @Parameter(property = "shard.create.excludes")
    protected String[] excludes;

    @Parameter(property = "shard.create.testFolders")
    protected String[] testFolders;

    /**
     * Default constructor, initializes a reader for the test classes and a writer for the test shards.
     */
    public AbstractShardCreatorMojo() {
        writer = new ShardFileWriter();
    }

    /**
     * Creates the test shards.
     * <p>
     * <code>filename = shardNamePattern + index + ".txt"</code>
     *
     * @param shardNamePattern the name of the created test shards, without the index;
     * @throws MojoFailureException   if something wrong with the dependencies or sources of a the plugin
     * @throws MojoExecutionException if there is a problem in the properties
     */
    public void createShards(String shardNamePattern, TestClassFileFilter fileFilter) throws MojoExecutionException, MojoFailureException {
        getLog().info("Start creation of " + shardNamePattern + " files.");

        TestFileReader reader = getReader();

        writer.clear();
        reader.setFilenameFilter(fileFilter);
        
        if (testFolders == null || testFolders.length == 0) {
            reader.read(getLog(), getTestSources());
        } else {
            reader.read(getLog(), testFolders);
        }

        List<String> testClassList = reader.getTestFilePaths();
        if (testClassList.isEmpty()) {
            throw new MojoExecutionException("Failed to load test classes, no classes found.");
        }
        getLog().info("Test files loaded, found " + testClassList.size() + " test(s) to split into " +
                shardCount + " test shard(s)");

        //Calculate average test shard size (count of test files per shard). Added 1 or 0 to make sure,
        //that testShardSize * shardCount >= testClassList.size and all test can be added
        int testShardSize = (testClassList.size() / shardCount) + Math.min(testClassList.size() % shardCount, 1);
        List<String[]> testClassesPerShard = splitList(testClassList, testShardSize);

        getLog().info(
                "Creating " + testClassesPerShard.size() + " test shard(s) with an max size of " + testShardSize);

        writer.createOutputFolder(outputFolder);

        for (int testShardNumber = 0; testShardNumber < testClassesPerShard.size(); testShardNumber++) {
            String[] testClassArray = testClassesPerShard.get(testShardNumber);
            try {
                String shardName = shardNamePattern + testShardNumber + ".txt";

                writer.openShardFile(shardName);
                writer.addTestClass(testClassArray);
                writer.saveAndClose();
            } catch (IOException e) {
                throw new MojoFailureException("Failed to write " + shardNamePattern + ".", e);
            }
        }
    }

    private List<String[]> splitList(List<String> elements, int elementCount) throws MojoExecutionException {
        List<String[]> splitList = new ArrayList<>();
        if (elementCount == 0) {
            return splitList;
        }
        int addedCount = 0;
        while (addedCount + elementCount <= elements.size()) {
            int size = Math.min(elements.size() - addedCount, elementCount);
            String[] elementArray = new String[size];
            for (int i = 0; i < size; i++) {
                elementArray[i] = elements.get(addedCount + i);
            }
            splitList.add(elementArray);
            addedCount += size;
        }

        //Add the rest
        if (elements.size() - addedCount > 0) {
            String[] elementArray = new String[elements.size() - addedCount];
            for (int i = 0; i < elementArray.length; i++) {
                elementArray[i] = elements.get(addedCount + i);
            }

            splitList.add(elementArray);
        }

        //Adjust test shards to the configured test shard count
        if (splitList.size() < shardCount) {
            if (elementCount == 1) {
                throw new MojoExecutionException(
                        "Failed to create " + shardCount + " shard(s), there are not enough test classes to split.");
            }
            int diff = shardCount - splitList.size();
            for (int i = 0; i < diff; i++) {
                adjustSplitList(splitList, i);
            }
        }

        return splitList;
    }

    private void adjustSplitList(List<String[]> splitList, int i) {
        String[] elementArray = splitList.get(i);
        int div = elementArray.length / 2;
        int mod = elementArray.length % 2;
        String[] part0 = new String[div + mod];
        String[] part1 = new String[div];
        for (int j = 0; j < elementArray.length; j++) {
            if (j < part0.length) {
                part0[j] = elementArray[j];
            } else {
                part1[j - part0.length] = elementArray[j];
            }
        }
        splitList.set(i, part0);
        splitList.add(part1);
    }

    /**
     * Getter for the test file reader
     *
     * @return the test file reader
     */
    public abstract TestFileReader getReader();
}
