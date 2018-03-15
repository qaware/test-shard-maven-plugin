/**
 * ﻿Copyright 2012, Deutsche Telekom AG, DTAG GHS GIS. All rights reserved.
 */

package com.telekom.gis.psa.test.shard.maven.plugin;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The reader for test files. It is used to get all test classes (.java files)
 *
 * @author Patrick Fischer patrick.fischer@qaware.de
 */
public class TestFileReader {
    
    private List<String> testFilePaths;
    private FilenameFilter filenameFilter;

    /**
     * Default constructor
     */
    public TestFileReader(){
        this.testFilePaths = new ArrayList<>();
    }

    /**
     * Reads the files in the given folders and converts their name to [package].[file] without file ending. This is
     * necessary, so surefire can read the shards properly.
     *
     * @param pathToPackage the directory to the package begin, e.g "src/test/java" (afterwards the package name starts.
     * @param testFolderPaths the test folders
     */
    public void read(String pathToPackage, String... testFolderPaths){
        Map<String, File> testFolders = new HashMap<>();
        for(String testFolderPath : testFolderPaths){
            File fileFolder = new File(testFolderPath);
            if(!fileFolder.exists() || !fileFolder.isDirectory()){
                continue;
            }
            addSubFoldersOf(fileFolder, testFolders);
        }

        testFolders.forEach((path, folder) -> addTestFilePathsIn(pathToPackage, path, folder, testFilePaths));
    }

    private void addSubFoldersOf(final File dir, Map<String, File> subFolders){
        File[] subFolderArray = dir.listFiles(new FileFilter() {

            private boolean added = false;

            @Override
            public boolean accept(File file) {
                //Just add the directory, if it contains any test files
                if(!added && filenameFilter.accept(dir, file.getName())){
                    added = true;
                    subFolders.put(dir.getAbsolutePath(), dir);
                }
                return file.isDirectory();
            }
        });

        for(File file : subFolderArray){
            addSubFoldersOf(file, subFolders);
        }
    }

    private void addTestFilePathsIn(String pathToPackage, String path, File dir, List<String> testFilePaths){
        String[] filePathArray = dir.list(filenameFilter);
        for (String filePath : filePathArray) {
            int i = filePath.lastIndexOf('.');
            if(i > 0) {
                filePath = filePath.substring(0, i);
            }
            testFilePaths.add(getPackageName(pathToPackage, path) + "." + filePath);
        }
    }

    private String getPackageName(String pathToPackage, String path) {
        int index = path.indexOf(pathToPackage) + pathToPackage.length() + 1;
        if(index <= pathToPackage.length()){
            return path;
        }
        return path.substring(index).replaceAll("[\\\\/]", ".");
    }

    /**
     * Return all found test file paths. {@link TestFileReader#read(String, String...)} must be invoked first, otherwise it
     * will always return an empty list
     *
     * @return a list with test file paths ([package].[file]) without file ending
     */
    public List<String> getTestFilePaths(){
        return testFilePaths;
    }

    /**
     * Setter for the file name filter
     * @param filenameFilter a file name filter
     */
    public void setFilenameFilter(FilenameFilter filenameFilter) {
        this.filenameFilter = filenameFilter;
    }
}
