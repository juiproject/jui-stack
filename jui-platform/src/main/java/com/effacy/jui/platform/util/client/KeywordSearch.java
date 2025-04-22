package com.effacy.jui.platform.util.client;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Tool for performing simple keywrod searches.
 * <p>
 * This parses a search expression into components that can be used to generate
 * search conditions.
 */
public class KeywordSearch {

    /**
     * Parses a keyword search expression.
     * 
     * @param keywords
     *                 the expression to parse.
     * @return the parsed expression.
     */
    public static KeywordSearch parse(String keywords) {
        return new KeywordSearch(keywords);
    }

    /**
     * The terms of the expression.
     */
    private List<Block> terms = new ArrayList<>();

    /**
     * Construct and parse an expression.
     * 
     * @param keywords
     *                 the expression to parse.
     */
    protected KeywordSearch(String keywords) {
        if (!StringSupport.empty(keywords)) {
            for (String term : keywords.split(" ")) {
                term = term.toLowerCase().trim();
                boolean requred = false;
                if (term.startsWith("+")) {
                    requred = true;
                    term.substring(1);
                }
                if (!StringSupport.empty(term))
                    terms.add(new Block(term, requred));
            }
        }
    }

    /**
     * Matches some text against the expression.
     * 
     * @param text
     *             the text to test against.
     * @return {@code true} if there is a match.
     */
    public boolean match(String text) {
        if (StringSupport.empty(text))
            return false;
        text = text.toLowerCase();
        boolean found = false;
        for (Block term : terms) {
            if (found && !term.requred())
                continue;
            if (!term.match(text)) {
                if (term.requred())
                    return false;
            } else
                found = true;
        }
        return found;
    }

    public <T> List<T> filter(List<T> source, Function<T,String> keywords) {
        List<T> results = new ArrayList<>();
        filter(source, results, keywords);
        return results;
    }

    public <T> void filter(List<T> source, List<T> destination,Function<T,String> keywords) {
        if ((source == null) || (destination == null))
            return;
        for (T item : source) {
            if (match(keywords.apply(item)))
                destination.add(item);
        }
    }

    public <T> void filterMultiple(List<T> source, List<T> destination,Function<T,String[]> keywords) {
        if ((source == null) || (destination == null))
            return;
        LOOP: for (T item : source) {
            for (String text : keywords.apply(item)) {
                if (match(text)) {
                    destination.add(item);
                    continue LOOP;
                }
            }
            
        }
    }

    /**
     * Represents a search block.
     */
    record Block(String content, boolean requred) {
        public boolean match(String text) {
            return text.contains(content);
        }
    }
}
