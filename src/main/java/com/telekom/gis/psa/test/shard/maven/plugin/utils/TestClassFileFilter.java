/**
 * ﻿Copyright 2012, Deutsche Telekom AG, DTAG GHS GIS. All rights reserved.
 */

package com.telekom.gis.psa.test.shard.maven.plugin.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

/**
 * The test class file filter, used to match file names against given maven styled include/exclude paths
 *
 * @author Patrick Fischer patrick.fischer@qaware.de
 */
public class TestClassFileFilter implements FileFilter, FilenameFilter {

    private String[] includes;
    private String[] excludes;

    /**
     * Default constructor
     * 
     * @param includes list from the pom
     * @param excludes list from the pom
     */
    public TestClassFileFilter(String[] includes, String[] excludes) {
        this.includes = includes;
        
        if (excludes != null && excludes.length > 0) {
            this.excludes = excludes;
        }else{
            this.excludes = new String[0];
        }
    }

    /**
     * Checks if a given file matches the include file pattern and does not match the exclude file pattern
     *
     * @param file the file to check
     * @return true if the file should be accepted
     */
    @Override
    public boolean accept(File file) {
        return file.exists() && !file.isDirectory() && accept(file.getAbsoluteFile().getParentFile(), file.getName());
    }

    /**
     * Checks if a given file matches the include file pattern and does not match the exclude file pattern
     *
     * @param dir the directory of the file
     * @param name the file name
     * @return true if the file should be accepted
     */
    @Override
    public boolean accept(File dir, String name) {
        String dirPath = dir.getAbsolutePath().toLowerCase();
        String lowerCaseName = name.toLowerCase();
        return !isExcluded(dirPath, lowerCaseName) && isIncluded(dirPath, lowerCaseName);
    }

    private boolean isIncluded(String dirPath, String name) {
        if (includes == null) {
            return true;
        }

        for (String includePattern : includes) {
            if (matchFilePath(dirPath, name, includePattern.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private boolean isExcluded(String dirPath, String name) {
        if (excludes == null) {
            return false;
        }

        for (String excludePattern : excludes) {
            if (matchFilePath(dirPath, name, excludePattern.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private boolean matchFilePath(String dirPath, String name, String pattern) {
        dirPath = dirPath.replaceAll("\\\\", "/");
        pattern = pattern.replaceAll("\\\\", "/");

        if (pattern.contains("/")) {
            if (!matchDirectoryPath(dirPath, pattern.substring(0, pattern.lastIndexOf('/')))) {
                return false;
            }

            if (pattern.endsWith("/")) {
                return true;
            }

            pattern = pattern.substring(pattern.lastIndexOf('/') + 1);
        }
        return matchString(name, pattern);
    }

    private boolean matchDirectoryPath(String dirPath, String pattern) {
        return pattern.equals("**") || matchString(dirPath, pattern);
    }

    private boolean matchString(String str, String pattern) {
        String[] patternParts = pattern.split("\\*");
        pattern = "";
        StringBuilder patternBuilder = new StringBuilder(pattern);
        for (String part : patternParts) {
            if (!part.isEmpty()) {
                part = "(" + part.replaceAll("\\.", "\\.") + ")";
            }
            patternBuilder.append(part).append(".*");
        }
        pattern = patternBuilder.substring(0, patternBuilder.length() - 2) + "+";

        return str.matches(pattern);
    }
}
