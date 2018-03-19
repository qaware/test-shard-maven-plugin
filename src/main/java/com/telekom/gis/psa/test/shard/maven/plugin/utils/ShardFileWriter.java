/**
 * ﻿Copyright 2012, Deutsche Telekom AG, DTAG GHS GIS. All rights reserved.
 */

package com.telekom.gis.psa.test.shard.maven.plugin.utils;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The file writer for test shards. Creates a test shard and writes to given test class into this file
 *
 * @author Patrick Fischer patrick.fischer@qaware.de
 */
public class ShardFileWriter {

    private String outputFolderPath;
    private List<String> testClasses;
    private FileWriter writer;

    /**
     * Default constructor, initializes not nullable field
     */
    public ShardFileWriter() {
        this.testClasses = new ArrayList<>();
    }

    /**
     * Creates the output folder, if it does not exist
     *
     * @param outputFolderPath the output folder path
     * @throws MojoExecutionException if the output folder exits but is not a directory
     * @throws MojoFailureException   if the output folder could not be created
     */
    public void createOutputFolder(String outputFolderPath) throws MojoExecutionException, MojoFailureException {
        File outputFolder = new File(outputFolderPath);

        if (outputFolder.exists() && !outputFolder.isDirectory()) {
            throw new MojoExecutionException("Invalid output folder: \"" + outputFolderPath + "\" is not a directory.");
        }

        if (!outputFolder.isDirectory() && !outputFolder.mkdirs()) {
            throw new MojoFailureException("Failed to create output folder (" + outputFolder.getAbsolutePath() + ").");
        }

        this.outputFolderPath = outputFolderPath;
    }

    /**
     * Opens a shard file and prepares the writer for writing
     *
     * @param shardName the shard name
     * @throws IOException
     */
    public void openShardFile(String shardName) throws IOException {
        File file = new File(outputFolderPath + File.separator + shardName);

        if (!file.exists()) {
            if(!file.createNewFile()){
                throw new IOException("Failed to create the shard file. Check if the folder exists.");
            }
        }

        writer = new FileWriter(file);
        testClasses.clear();
    }

    /**
     * Adds all test classes to a list (not saved to file)
     *
     * @param testClasses the test classes
     * @see ShardFileWriter#saveAndClose()
     */
    public void addTestClass(String... testClasses) {
        for (String testClass : testClasses) {
            addTestClass(testClass);
        }
    }

    /**
     * Adds a test class to a list (not saved to file)
     *
     * @param testClass the test classe
     * @see ShardFileWriter#saveAndClose()
     */
    public void addTestClass(String testClass) {
        if (testClasses.contains(testClass)) {
            return;
        }
        testClasses.add(testClass);
    }

    /**
     * Saves the temporary test class list to the opened file
     *
     * @throws IOException
     */
    public void saveAndClose() throws IOException {
        for (String line : testClasses) {
            writer.write(line + System.getProperty("line.separator"));
        }
        writer.flush();
        writer.close();
    }

    /**
     * Clears the temporary list, not the file itself
     */
    public void clear() {
        testClasses.clear();
    }
}
