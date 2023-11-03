package com.telekom.gis.psa.test.shard.maven.plugin.utils.distribution;

import org.apache.maven.plugin.logging.Log;

import java.util.*;

import static java.util.Comparator.*;
import static java.util.Comparator.comparing;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

public class EqualExecutionTimeDistributor implements Distributor {

    private final Log log;
    private final Map<String, Long> durationMap;
    private final Long averageDuration;

    public EqualExecutionTimeDistributor(Map<String, Long> durationMap, Log log) {
        this.durationMap = durationMap;
        this.log = log;
        averageDuration = (long) durationMap.values().stream()
                .mapToLong(duration -> duration)
                .average()
                .orElse(10000.0);
        log.debug("List of test execution times (in ms):");
        durationMap.forEach((key, value) -> log.debug(key + " ----> " + value));
    }

    @Override
    public List<List<String>> distribute(List<String> tests, int shardCount) {
        List<Shard> shards = new ArrayList<>();
        for (int i = 0; i < shardCount; i++) {
            shards.add(new Shard());
        }

        tests = sortTestsByDecreasingDuration(tests);

        for (String test : tests) {
            Shard shortestShard = findFastestShard(shards);
            shortestShard.getTests().add(new Test(test, getDuration(test)));
        }

        for (int i = 0; i < shards.size(); i++) {
            Shard s = shards.get(i);
            log.info("Shard " + i + " has duration " + s.duration() + " for " + s.tests.size() + " tests");
        }

        return shards.stream()
                .map(i -> i.getTests().stream()
                        .map(Test::getName)
                        .collect(toList()))
                .collect(toList());
    }

    private List<String> sortTestsByDecreasingDuration(List<String> tests) {
        return tests.stream()
                .sorted(comparing(this::getDuration).reversed())
                .collect(toList());
    }

    private long getDuration(String test) {
        Long duration = durationMap.get(test);
        if (duration == null) {
            log.info("No duration for test: " + test + "; using default " + averageDuration);
            return averageDuration;
        }
        return duration;
    }

    private Shard findFastestShard(List<Shard> shards) {
        return shards.stream()
                .min(comparing(Shard::duration))
                .orElseThrow(IllegalStateException::new);
    }

    private static class Shard {

        private final List<Test> tests = new ArrayList<>();

        private Shard() {
        }

        public List<Test> getTests() {
            return tests;
        }

        double duration() {
            return tests.stream()
                    .mapToDouble(Test::getDuration)
                    .sum();
        }

        @Override
        public String toString() {
            return "Shard{" +
                    "tests=" + tests +
                    '}';
        }
    }

    private static class Test {
        private final String name;
        private final double duration;

        public Test(String name, double duration) {
            this.name = name;
            this.duration = duration;
        }

        public String getName() {
            return name;
        }

        public double getDuration() {
            return duration;
        }

        @Override
        public String toString() {
            return "Test{" +
                    "name='" + name + '\'' +
                    ", duration=" + duration +
                    '}';
        }
    }
}
