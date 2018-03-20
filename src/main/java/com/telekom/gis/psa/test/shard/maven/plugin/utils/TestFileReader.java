/**
 * ﻿Copyright 2012, Deutsche Telekom AG, DTAG GHS GIS. All rights reserved.
 */

package com.telekom.gis.psa.test.shard.maven.plugin.utils;

import org.apache.maven.plugin.logging.Log;

import java.io.FilenameFilter;
import java.util.List;

/**
 * The reader for test files. It is used to get all test classes (.java files)
 *
 * @author Patrick Fischer patrick.fischer@qaware.de
 */
public interface TestFileReader {
    
    /**
     * Reads the files in the given folders and converts their name to [package].[file] without file ending. This is
     * necessary, so surefire can read the shards properly.
     *
     * @param log             the Logger to print warnings to the console
     * @param testFolderPaths the test folders
     */
    void read(Log log, String... testFolderPaths);

    /**
     * Return all found test file paths. {@link TestFileReader#read(Log, String...)} must be invoked first, otherwise it
     * will always return an empty list
     *
     * @return a list with test file paths ([package].[file]) without file ending
     */
    List<String> getTestFilePaths();

    /**
     * Setter for the file name filter
     *
     * @param filenameFilter a file name filter
     */
    void setFilenameFilter(FilenameFilter filenameFilter);
}
