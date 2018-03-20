/**
 * ﻿Copyright 2012, Deutsche Telekom AG, DTAG GHS GIS. All rights reserved.
 */

package com.telekom.gis.psa.test.shard.maven.plugin.junit;

import com.telekom.gis.psa.test.shard.maven.plugin.utils.TestFileReader;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * The reader for test files. It is used to get all test classes (.java files)
 *
 * @author Patrick Fischer patrick.fischer@qaware.de
 */
public class JUnitFileReader implements TestFileReader {

    private List<String> testFilePaths;
    private FilenameFilter filenameFilter;

    /**
     * Default constructor, initializes some not nullable fields
     */
    public JUnitFileReader() {
        this.testFilePaths = new ArrayList<>();
    }

    /**
     * Reads the files in the given folders and converts their name to [package].[file] without file ending. This is
     * necessary, so surefire can read the shards properly.
     *
     * @param log             the Logger to print warnings to the console
     * @param testFolderPaths the test folders
     */
    public void read(Log log, String... testFolderPaths) {
        for (String testFolderPath : testFolderPaths) {
            File fileFolder = new File(testFolderPath);
            if (!fileFolder.exists() || !fileFolder.isDirectory()) {
                log.warn("Invalid test folder: " + fileFolder.getAbsolutePath());
                continue;
            }
            addFilesIn(fileFolder, null);
        }
    }

    private void addFilesIn(final File dir, String currentPackage) {
        File[] subFolderArray = dir.listFiles(
                file -> file.isDirectory() || filenameFilter.accept(file.getParentFile(), file.getName()));

        if (subFolderArray == null) {
            return;
        }

        String previousPackage;
        if (currentPackage == null) {
            previousPackage = "";
        } else {
            previousPackage = currentPackage + ".";
        }

        for (File file : subFolderArray) {
            String rawFileName = file.getName();
            if (rawFileName.contains(".")) {
                //Remove the file type
                rawFileName = rawFileName.substring(0, rawFileName.lastIndexOf('.'));
            }

            String packageName = previousPackage + rawFileName;
            if (file.isDirectory()) {
                addFilesIn(file, packageName);
            } else {
                testFilePaths.add(packageName);
            }
        }
    }

    /**
     * Return all found test file paths. {@link JUnitFileReader#read(Log, String...)} must be invoked first, otherwise it
     * will always return an empty list
     *
     * @return a list with test file paths ([package].[file]) without file ending
     */
    public List<String> getTestFilePaths() {
        return testFilePaths;
    }

    /**
     * Setter for the file name filter
     *
     * @param filenameFilter a file name filter
     */
    public void setFilenameFilter(FilenameFilter filenameFilter) {
        this.filenameFilter = filenameFilter;
    }
}
