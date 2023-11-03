package com.telekom.gis.psa.test.shard.maven.plugin.utils.distribution;

import com.google.common.collect.Lists;
import org.apache.maven.plugin.logging.Log;

import java.util.List;

public class SimpleDistributor implements Distributor {

    private final Log log;

    public SimpleDistributor(Log log) {
        this.log = log;
    }

    @Override
    public List<List<String>> distribute(List<String> items, int binCount) {

        //Calculate average test shard size (count of test files per shard). Added 1 or 0 to make sure,
        //that testShardSize * shardCount >= testClassList.size and all test can be added
        int elementCount = (items.size() / binCount) + Math.min(items.size() % binCount, 1);

        List<List<String>> partitions = Lists.partition(items, elementCount);

        for (int i = 0; i < partitions.size(); i++) {
            List<String> s = partitions.get(i);
            log.info("Shard " + i + " contains " + s.size() + " tests");
        }

        return partitions;
    }
}
