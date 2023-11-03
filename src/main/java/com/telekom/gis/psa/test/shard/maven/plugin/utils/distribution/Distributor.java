package com.telekom.gis.psa.test.shard.maven.plugin.utils.distribution;

import java.util.List;

public interface Distributor {

    List<List<String>> distribute(List<String> items, int binCount);
}
