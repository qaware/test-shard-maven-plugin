/**
 * ﻿Copyright 2012, Deutsche Telekom AG, DTAG GHS GIS. All rights reserved.
 */

package com.telekom.gis.psa.test.shard.maven.plugin.cucumber;

import com.telekom.gis.psa.test.shard.maven.plugin.utils.TestFileReader;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The test file reader for cucumber files, reads all cucumber .feature files
 *
 * @author Patrick Fischer patrick.fischer@qaware.de
 */
public class CucumberFileReader implements TestFileReader {

    private FilenameFilter filenameFilter;
    private List<String> testFilePaths;

    public CucumberFileReader(){
        testFilePaths = new ArrayList<>();
    }

    @Override
    public void read(Log log, String... testFolderPaths) {
        for(String fileName : testFolderPaths){
            addFile(new File(fileName));
        }
    }

    private void addFile(File file) {
        if(file.isDirectory()){
            Arrays.stream(file.listFiles((dir, name) -> filenameFilter.accept(dir, name) || new File(dir, name).isDirectory()))
                    .forEach(this::addFile);
        }else if(filenameFilter.accept(file.getParentFile(), file.getName())){
            testFilePaths.add(file.getPath());
        }
    }

    @Override
    public List<String> getTestFilePaths() {
        return testFilePaths;
    }

    @Override
    public void setFilenameFilter(FilenameFilter filenameFilter) {
        this.filenameFilter = filenameFilter;
    }
}
