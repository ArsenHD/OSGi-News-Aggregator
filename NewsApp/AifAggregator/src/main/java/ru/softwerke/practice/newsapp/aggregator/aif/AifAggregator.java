package ru.softwerke.practice.newsapp.aggregator.aif;

import org.apache.felix.scr.annotations.*;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import ru.softwerke.practice.newsapp.aggregator.Aggregator;
import ru.softwerke.practice.newsapp.aggregator.util.NewsAggregator;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("deprecation")
@Component(name = "AIF Aggregator")
@Service(value = Aggregator.class)
@Property(name = "news.source", value = "aif.ru")
public class AifAggregator extends NewsAggregator {
    private final String RSS_URL = "https://aif.ru/rss/news.php";

    private final int SKIP_AT_BEGINNING = 16;
    private final int SKIP_AT_END = 11;

    public AifAggregator() {
        super();
        source = RSS_URL;
    }

    @Override
    protected synchronized void parseResponseToMap(String response, Map<String, Integer> wordCount) {
        Document document = Jsoup.parse(response, "", Parser.xmlParser());

        List<Element> titles = new ArrayList<>(document.select("title"));
        titles.remove(0);

        List<String> strTitles = titles.stream()
                .map(Objects::toString)
                .map(title -> title.substring(SKIP_AT_BEGINNING))
                .map(title -> title.substring(0, title.length() - SKIP_AT_END))
                .collect(Collectors.toList());

        for (String title: strTitles) {
            String[] words = title.split("[\\p{Blank}\\p{Punct}]");
            for (String word: words) {
                if (validate(word)) {
                    addWordToMap(wordCount, word);
                }
            }
        }
    }

    @Activate
    public synchronized void activate(Map<String, Object> properties) {
        this.properties = properties;
    }

    @Modified
    public synchronized void modified(Map<String, Object> properties) {
        this.properties = properties;
    }
}
