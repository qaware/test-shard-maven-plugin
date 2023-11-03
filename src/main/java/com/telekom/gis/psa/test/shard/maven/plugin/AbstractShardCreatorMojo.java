/**
 * ﻿Copyright 2012, Deutsche Telekom AG, DTAG GHS GIS. All rights reserved.
 */

package com.telekom.gis.psa.test.shard.maven.plugin;

import com.telekom.gis.psa.test.shard.maven.plugin.junit.JUnitFileReader;
import com.telekom.gis.psa.test.shard.maven.plugin.utils.ShardFileWriter;
import com.telekom.gis.psa.test.shard.maven.plugin.utils.TestClassFileFilter;
import com.telekom.gis.psa.test.shard.maven.plugin.utils.TestFileReader;
import com.telekom.gis.psa.test.shard.maven.plugin.utils.distribution.EqualExecutionTimeDistributor;
import com.telekom.gis.psa.test.shard.maven.plugin.utils.distribution.SimpleDistributor;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.Collections.emptyMap;

/**
 * The creator mojo, reads the test files and created the shard files
 *
 * @author Patrick Fischer patrick.fischer@qaware.de
 */
public abstract class AbstractShardCreatorMojo extends AbstractShardMojo {

    private final ShardFileWriter writer;

    @Parameter(property = "shard.create.shardCount", required = true)
    protected int shardCount;

    @Parameter(property = "shard.create.includes")
    protected String[] includes;

    @Parameter(property = "shard.create.excludes")
    protected String[] excludes;

    @Parameter(property = "shard.create.testFolders", defaultValue = "${project.build.testSourceDirectory}")
    protected String[] testFolders;

    @Parameter(property = "shard.create.testReportDirectory", required = false)
    protected String testReportDirectory;

    /**
     * Default constructor, initializes necessary fields
     */
    protected AbstractShardCreatorMojo() {
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
            throw new MojoExecutionException("Failed to load test classes, no test folder is assigned.");
        } else {
            reader.read(getLog(), testFolders);
        }

        List<String> testClassList = reader.getTestFilePaths();
        if (testClassList.isEmpty()) {
            throw new MojoExecutionException("Failed to load test classes, or no tests classes found.");
        }
        getLog().info("Test files loaded, found " + testClassList.size() + " test(s) to split into " +
                shardCount + " test shard(s)");

        List<List<String>> testClassesPerShard = getTestDistribution(testClassList);

        writer.createOutputFolder(outputFolder);

        for (int testShardNumber = 0; testShardNumber < testClassesPerShard.size(); testShardNumber++) {
            List<String> testClassArray = testClassesPerShard.get(testShardNumber);
            try {
                String shardName = shardNamePattern + testShardNumber + ".txt";

                writer.openShardFile(shardName);
                writer.addTestClass(testClassArray.toArray(new String[0]));
                writer.saveAndClose();
            } catch (IOException e) {
                throw new MojoFailureException("Failed to write " + shardNamePattern + ".", e);
            }
        }
    }

    private List<List<String>> getTestDistribution(List<String> testClassList) throws MojoExecutionException {
        Map<String, Long> durationMap = processFailsafeReportFiles(testClassList);
        getLog().info("Found " + durationMap.size() + " tests in test report files");
        if(durationMap.isEmpty()) {
            getLog().info("Using simple test distribution");
            return new SimpleDistributor(getLog()).distribute(testClassList, shardCount);
        } else {
            getLog().info("Using optimized execution time test distribution");
            return new EqualExecutionTimeDistributor(durationMap, getLog()).distribute(testClassList, shardCount);
        }
    }

    /**
     * Getter for the test file reader
     *
     * @return the test file reader
     */
    public abstract TestFileReader getReader();

    public abstract Map<String, String> getMapping(List<String> testClassList);

    private Map<String, Long> processFailsafeReportFiles(List<String> testClassList) throws MojoExecutionException {
        if (testReportDirectory == null) {
            getLog().info("No test report directory configured.");
            return emptyMap();
        }
        try {
            XPath xPath = XPathFactory.newInstance().newXPath();
            DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = builderFactory.newDocumentBuilder();

            XPathExpression testExpr = xPath.compile("/testsuite/testcase");

            List<String> reportFiles = getFailsafeReportFiles(testReportDirectory);
            getLog().info("Processing report files in " + testReportDirectory);

            Map<String, String> mapping = getMapping(testClassList);

            Map<String, Long> result = new HashMap<>();
            for (String reportFile : reportFiles) {
                getLog().info("Processing junit report file '" + reportFile + ".xml");
                String fullReportFile = testReportDirectory + "/" + reportFile + ".xml";
                try (FileInputStream fileIS = new FileInputStream(fullReportFile)) {
                    Document xmlDocument = builder.parse(fileIS);
                    NodeList tests = (NodeList) testExpr.evaluate(xmlDocument, XPathConstants.NODESET);
                    if (tests.getLength() == 0) {
                        getLog().info("Ignored entry " + fullReportFile);
                        continue;
                    }
                    Map<String, Long> fileMap = getFileMap(tests, mapping);
                    result.putAll(fileMap);
                }
            }
            return result;
        } catch (XPathExpressionException | IOException | SAXException | ParserConfigurationException e) {
            getLog().error(e);
            throw new MojoExecutionException("Could not process report file", e);
        }
    }

    private Map<String, Long> getFileMap(NodeList tests, Map<String, String> mapping) {
        Map<String, Long> result = new HashMap<>();
        getLog().info("Found " + tests.getLength() + " tests");
        for (int i = 0; i < tests.getLength(); i++) {
            NamedNodeMap attributes = tests.item(i).getAttributes();
            String className =  mapping.get(attributes.getNamedItem("classname").getTextContent());
            if (className != null) {
                Long duration = (long)(1000 * Double.parseDouble(attributes.getNamedItem("time").getTextContent()));
                result.compute(className, (k, v) -> v != null ? v + duration : duration);
            }
        }
        getLog().info("xxx:" + result);
        return result;
    }

    private final Pattern reportFilePattern = Pattern.compile("^TEST-.+\\.xml$");

    private List<String> getFailsafeReportFiles(String directory) {
        JUnitFileReader reader = new JUnitFileReader();
        reader.setFilenameFilter((dir, name) -> reportFilePattern.matcher(name).matches());
        reader.read(getLog(), directory);
        List<String> testFilePaths = reader.getTestFilePaths();
        if(testFilePaths.isEmpty()) {
            getLog().warn("Could not find any failsafe report files in directory '" + directory + "'");
        } else {
            getLog().info("Found the following failsafe report files in directory '" + directory + "'");
            getLog().info("" + testFilePaths);
        }
        return testFilePaths;
    }
}
