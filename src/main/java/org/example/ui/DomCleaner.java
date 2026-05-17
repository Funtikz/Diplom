package org.example.ui;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class DomCleaner {

    public static String clean(String html) {

        Document doc = Jsoup.parse(html);

        // Удаляем мусор
        doc.select(
                "script, style, svg, noscript, iframe"
        ).remove();

        // Берем только полезные элементы
        Elements elements = doc.select(
                "input, button, a, textarea, select, option"
        );

        StringBuilder sb = new StringBuilder();

        for (Element el : elements) {

            Element clean = new Element(el.tagName());

            copyAttr(el, clean, "id");
            copyAttr(el, clean, "name");
            copyAttr(el, clean, "type");
            copyAttr(el, clean, "value");
            copyAttr(el, clean, "placeholder");
            copyAttr(el, clean, "href");
            copyAttr(el, clean, "role");
            copyAttr(el, clean, "title");
            copyAttr(el, clean, "aria-label");
            copyAttr(el, clean, "data-testid");
            copyAttr(el, clean, "data-test");
            copyAttr(el, clean, "data-qa");

            String text = el.ownText().trim();

            if (!text.isEmpty()) {
                clean.text(text);
            }

            sb.append(clean.outerHtml())
                    .append("\n");
        }

        return sb.toString();
    }

    private static void copyAttr(
            Element source,
            Element target,
            String attr
    ) {

        if (source.hasAttr(attr)) {
            target.attr(attr, source.attr(attr));
        }
    }
}