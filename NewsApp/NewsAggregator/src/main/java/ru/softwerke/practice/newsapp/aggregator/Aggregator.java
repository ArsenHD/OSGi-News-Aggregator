package ru.softwerke.practice.newsapp.aggregator;

import java.util.Map;

public interface Aggregator {
    Map<String, Integer> aggregate();
}
