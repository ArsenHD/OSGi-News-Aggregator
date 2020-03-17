package ru.softwerke.practice.newsapp.aggregator.util;

import ru.softwerke.practice.newsapp.aggregator.Aggregator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.*;

public abstract class NewsAggregator implements Aggregator {
    private List<String> prepositions;

    protected String source;
    protected Map<String, Object> properties;

    public NewsAggregator() {
        setupPrepositionsList();
    }

    private void setupPrepositionsList() {
        String[] p = {"в", "В", "на", "На", "от", "От",
                "не", "Не", "под", "Под", "за", "За", "и", "И",
                "из", "Из", "о", "О", "с", "С", "об", "Об", "над",
                "Над", "у", "У", "к", "К", "по", "По", "для", "Для" };
        prepositions = Arrays.asList(p);
    }

    @Override
    public synchronized Map<String, Integer> aggregate() {
        Map<String, Integer> wordCount = new HashMap<>();

        try {
            URL url = new URL(source);

            URLConnection connection = url.openConnection();
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                String inputLine;
                StringBuilder content = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();

                String response = content.toString();

                parseResponseToMap(response, wordCount);
            } catch (IOException e) {
                System.err.println("Failed to read response");
            }
        } catch (MalformedURLException e) {
            System.err.println(String.format("Malformed URL: %s", source));
        } catch (IOException e) {
            System.err.println(String.format("Failed to connect to \"%s\"", source));
        }

        return wordCount;
    }

    protected abstract void parseResponseToMap(String response, Map<String, Integer> wordCount);

    public synchronized Map<String, Object> getProperties() {
        return properties;
    }

    protected synchronized boolean validate(String word) {
        return !word.isEmpty() && !isPreposition(word);
    }

    protected synchronized boolean isPreposition(String word) {
        return prepositions.contains(word);
    }

    protected synchronized void addWordToMap(Map<String, Integer> map, String word) {
        if (!map.containsKey(word)) {
            map.put(word, 1);
        } else {
            int prevCount = map.get(word);
            map.put(word, prevCount + 1);
        }
    }
}
