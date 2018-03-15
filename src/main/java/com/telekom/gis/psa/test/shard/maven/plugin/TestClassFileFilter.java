/**
 * ﻿Copyright 2012, Deutsche Telekom AG, DTAG GHS GIS. All rights reserved.
 */

package com.telekom.gis.psa.test.shard.maven.plugin;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;

/**
 * The test class file filter
 *
 * @author Patrick Fischer patrick.fischer@qaware.de
 */
public class TestClassFileFilter implements FileFilter, FilenameFilter {

    private String[] includes;
    private String[] excludes;

    /**
     * Default constructor
     * @param includes list from the pom
     * @param excludes list from the pom
     */
    public TestClassFileFilter(String[] includes, String[] excludes) {
        if (includes == null || includes.length > 0) {
            this.includes = includes;
        }else{
            this.includes = new String[]{ "**/*Test.java" };
        }
        if (excludes == null || excludes.length > 0) {
            this.excludes = excludes;
        }
    }
    
    @Override
    public boolean accept(File file) {
        return file.exists() && !file.isDirectory() && accept(file.getAbsoluteFile().getParentFile(), file.getName());
    }

    @Override
    public boolean accept(File dir, String name) {
        String dirPath = dir.getAbsolutePath().toLowerCase();
        name = name.toLowerCase();
        return !isExcluded(dirPath, name) && isIncluded(dirPath, name);
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
            if (!matchDirectoryPath(dirPath, pattern.substring(0, pattern.lastIndexOf("/")))) {
                return false;
            }

            if (pattern.endsWith("/")) {
                return true;
            }

            pattern = pattern.substring(pattern.lastIndexOf("/") + 1);
        }
        return matchString(name, pattern);
    }

    private boolean matchDirectoryPath(String dirPath, String pattern) {
        if (pattern.equals("**")) {
            return true;
        }
        return matchString(dirPath, pattern);
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
