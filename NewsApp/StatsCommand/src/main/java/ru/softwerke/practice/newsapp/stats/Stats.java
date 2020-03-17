package ru.softwerke.practice.newsapp.stats;

import org.apache.felix.scr.annotations.*;
import org.apache.felix.scr.annotations.Properties;
import ru.softwerke.practice.newsapp.aggregator.Aggregator;
import ru.softwerke.practice.newsapp.aggregator.util.NewsAggregator;

import java.util.*;

import static java.util.Map.Entry.comparingByValue;
import static java.util.stream.Collectors.toMap;

@SuppressWarnings("deprecation")
@Component(name = "stats Gogo command")
@Service(value = Object.class)
@Properties({
        @Property(name = "osgi.command.scope", value = "news"),
        @Property(name = "osgi.command.function", value = "stats")
})
public class Stats {
    @Reference(
            cardinality = ReferenceCardinality.OPTIONAL_MULTIPLE,
            policy = ReferencePolicy.DYNAMIC,
            referenceInterface = Aggregator.class,
            bind = "setAggregators",
            unbind = "unsetAggregators"
    )
    private List<Aggregator> aggregators = new ArrayList<>();

    private void mergeMaps(Map<String, Integer> map1, Map<String, Integer> map2) {
        map2.forEach(
                (key, value) -> map1.merge(key, value, Integer::sum)
        );
    }

    public synchronized void stats() {
        if (aggregators.isEmpty()) {
            System.out.println("No aggregators available");
            return;
        }

        Map<Integer, Aggregator> optionAggregator = new HashMap<>();
        System.out.println("Choose the sources to aggregate the news from:");
        int i = 1;
        for (Aggregator a: aggregators) {
            optionAggregator.put(i, a);
            System.out.println(String.format("%d. %s", i++, getSource(a)));
        }

        if (aggregators.size() != 1) {
            System.out.println(String.format("%d. %s", i++, "All of the above"));
        }

        Scanner scanner = new Scanner(System.in);

        int option;
        option = scanner.nextInt();

        Map<String, Integer> wordCount = new HashMap<>();

        if (option > 2 && option == i - 1) {
            for (Aggregator a: aggregators) {
                if (wordCount.isEmpty()) {
                    wordCount.putAll(a.aggregate());
                } else {
                    mergeMaps(wordCount, a.aggregate());
                }
            }
        } else if (option >= 1 && option < i) {
            stats(getSource(optionAggregator.get(option)));
        }

        Map<String, Integer> sortedWordCount =
                wordCount.entrySet().stream()
                        .sorted(comparingByValue(Comparator.reverseOrder()))
                        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));

        int k = 0;
        for (Map.Entry<String, Integer> entry: sortedWordCount.entrySet()) {
            if (k++ == 10) {
                break;
            }
            System.out.println(String.format("\"%s\": %d occurrences", entry.getKey(), entry.getValue()));
        }
    }

    private String getSource(Aggregator aggregator) {
        return ((NewsAggregator) aggregator).getProperties().get("news.source").toString();
    }

    public synchronized void stats(String source) {
        if (aggregators.isEmpty()) {
            System.out.println("No aggregators available");
            return;
        }

        Aggregator aggregator = null;
        for (Aggregator a: aggregators) {
            if (source.equals(getSource(a))) {
                aggregator = a;
                break;
            }
        }

        if (aggregator == null) {
            System.err.println(String.format("\"%s\" aggregator is not available", source));
            return;
        }

        Map<String, Integer> wordCount = aggregator.aggregate();

        Map<String, Integer> sortedWordCount =
                wordCount.entrySet().stream()
                        .sorted(comparingByValue(Comparator.reverseOrder()))
                        .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2, LinkedHashMap::new));

        int k = 0;
        for (Map.Entry<String, Integer> entry: sortedWordCount.entrySet()) {
            if (k++ == 10) {
                break;
            }
            System.out.println(String.format("\"%s\": %d occurrences", entry.getKey(), entry.getValue()));
        }
    }

    protected synchronized void setAggregators(Aggregator aggregator) {
        if (aggregators == null) {
            aggregators = new ArrayList<>();
        }
        aggregators.add(aggregator);
    }

    protected synchronized void unsetAggregators(Aggregator aggregator) {
        aggregators.remove(aggregator);
    }
}
