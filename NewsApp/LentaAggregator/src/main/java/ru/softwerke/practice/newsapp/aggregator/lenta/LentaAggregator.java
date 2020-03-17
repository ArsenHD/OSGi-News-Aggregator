package ru.softwerke.practice.newsapp.aggregator.lenta;

import com.google.gson.Gson;
import org.apache.felix.scr.annotations.*;
import ru.softwerke.practice.newsapp.aggregator.Aggregator;
import ru.softwerke.practice.newsapp.aggregator.lenta.util.ArticleInfo;
import ru.softwerke.practice.newsapp.aggregator.util.NewsAggregator;

import java.util.*;

@SuppressWarnings("deprecation")
@Component(name = "Lenta Aggregator")
@Service(value = Aggregator.class)
@Property(name = "news.source", value = "Lenta.ru")
public class LentaAggregator extends NewsAggregator {
    private final String API_URL = "https://api.lenta.ru/lists/latest";

    private Gson gson = new Gson();

    public LentaAggregator() {
        super();
        source = API_URL;
    }

    @Override
    protected synchronized void parseResponseToMap(String response, Map<String, Integer> wordCount) {
        response = response.substring(response.indexOf('['), response.length() - 1);

        ArticleInfo[] info = gson.fromJson(response, ArticleInfo[].class);

        for (ArticleInfo i: info) {
            String[] words = i.toString().split("[\\p{Blank}\\p{Punct}]");
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
