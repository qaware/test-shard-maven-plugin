/**
 * ﻿Copyright 2012, Deutsche Telekom AG, DTAG GHS GIS. All rights reserved.
 */

package com.telekom.gis.psa.test.shard.maven.plugin.cucumber;

import com.telekom.gis.psa.test.shard.maven.plugin.utils.ShardConstants;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * The Cucumber feature file manager handles the .feature files. They can be disable by appending .ignore to the file
 * name. This manager renames the .feature files.
 *
 * @author Patrick Fischer patrick.fischer@qaware.de
 */
public final class CucumberFeatureFileManager {

    private CucumberFeatureFileManager() {
        //No instances
    }

    /**
     * Renames all cucumber shards, which where not used. The result file as the file type .ignore and will not be
     * recognized buy cucumber
     *
     * @param shardDirectory the directory of the test shards
     * @param shardName      the name of the excluded cucumber shard
     * @throws MojoExecutionException if there is a problem in the properties
     */
    public static void disableOtherFeatures(File shardDirectory, String shardName) throws MojoExecutionException {
        File[] otherShards = shardDirectory
                .listFiles((dir, name) -> name.matches(ShardConstants.CUCUMBER_SHARD_REGEX) && !name.equals(shardName));

        if (otherShards == null) {
            throw new MojoExecutionException("Shard directory " + shardDirectory.getPath() + " does not exist.");
        }

        for (File shard : otherShards) {
            disableAllFeaturesOf(shard);
        }
    }

    private static void disableAllFeaturesOf(File shard) throws MojoExecutionException {
        List<String> featureFiles = getAllFeatureFileOf(shard);

        for (String featureFileName : featureFiles) {
            disableFeatureFile(new File(featureFileName));
        }
    }

    /**
     * Disables a given feature file by appending .ignore to the file name
     *
     * @param featureFile the feature file
     * @throws MojoExecutionException if an exception occurs
     */
    public static void disableFeatureFile(File featureFile) throws MojoExecutionException {
        File disabledFeatureFile = new File(featureFile.getPath() + ".ignore");
        if (disabledFeatureFile.exists()) {
            throw new MojoExecutionException(
                    "Failed to disable " + featureFile.getPath() + ": ignored feature already exists, " +
                            "call the clean-cucumber-features goal to clean the feature files.");
        }

        if (!featureFile.renameTo(disabledFeatureFile)) {
            throw new MojoExecutionException("Failed to disable " + featureFile.getPath() + ": renaming failed.");
        }
    }

    /**
     * Returns a list of all feature files in a cucumber shard file.
     *
     * @param shard the shard file
     * @return the list of the feature files
     * @throws MojoExecutionException if there is a problem in the properties
     */
    public static List<String> getAllFeatureFileOf(File shard) throws MojoExecutionException {
        List<String> features = new ArrayList<>();
        BufferedReader reader = null;
        try {
            reader = Files.newBufferedReader(shard.toPath());

            String line;
            while ((line = reader.readLine()) != null) {
                features.add(line);
            }
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to read feature files.", e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignore) {
                }
            }
        }
        return features;
    }

    /**
     * Enables a disabled feature file by removing the .ignore file name ending
     *
     * @param file the feature file to enable
     * @throws MojoFailureException   if something wrong with the dependencies or sources of a the plugin
     * @throws MojoExecutionException if there is a problem in the properties
     */
    public static void enableFeatureFile(File file) throws MojoFailureException, MojoExecutionException {
        String fileName = file.getPath();
        fileName = fileName.substring(0, fileName.lastIndexOf('.'));

        if (!fileName.endsWith(".feature")) {
            throw new MojoFailureException("Invalid ignored feature file " + file.getPath());
        }

        File renamedFile = new File(fileName);
        if (renamedFile.exists()) {
            throw new MojoExecutionException(
                    "Failed to enable " + file.getPath() + ", non-ignored file already exists.");
        }

        if (!file.renameTo(renamedFile)) {
            throw new MojoExecutionException("Failed to enable " + file.getPath() + ": renaming failed.");
        }
    }
}
