/**
 * ﻿Copyright 2012, Deutsche Telekom AG, DTAG GHS GIS. All rights reserved.
 */

package com.telekom.gis.psa.test.shard.maven.plugin.cucumber;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.io.File;

/**
 * A cleaner goal, which renames all ignores Cucumber feature files.
 *
 * During the execution of the cucumber tests, some feature file names will be appended with .ignore.
 * Those feature files will be ignored, when executing the cucumber tests. After all tests, this mojo
 * removes the appendix.
 *
 * @author Patrick Fischer patrick.fischer@qaware.de
 */
@Mojo(name = "clean-cucumber-features", defaultPhase = LifecyclePhase.POST_INTEGRATION_TEST)
public class CucumberShardPostExecutorMojo extends AbstractMojo {

    @Parameter(property = "shard.create.testFolders")
    protected String[] testFolders;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        for(String testFolder : testFolders){
            findDisabledCucumberFeatures(new File(testFolder));
        }
    }

    private void findDisabledCucumberFeatures(File file) throws MojoFailureException, MojoExecutionException {
        if(file.isDirectory()){
            for(File subFile : file.listFiles()){
                findDisabledCucumberFeatures(subFile);
            }
        }else if(file.getName().endsWith(".feature.ignore")){
            CucumberFeatureFileManager.enableFeatureFile(file);
        }
    }
}
