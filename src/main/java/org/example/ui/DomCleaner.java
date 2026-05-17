package org.example.ui;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class DomCleaner {

    public static String clean(String html) {

        Document doc = Jsoup.parse(html);

        doc.select("script").remove();
        doc.select("style").remove();
        doc.select("svg").remove();

        Elements elements = doc.select(
                "button, input, a, textarea, select"
        );

        return elements.html();
    }
}