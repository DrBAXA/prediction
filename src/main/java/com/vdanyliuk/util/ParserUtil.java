package com.vdanyliuk.util;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalQueries;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class ParserUtil {
    public static Document getDocument(String url, int tryCount) {
        try {
            return Jsoup.connect(url).get();
        } catch (IOException e) {
            if (tryCount < 5) {
                log.warn("Can't get page " + url + ". Try to get one more time");
                return getDocument(url, tryCount + 1);
            } else {
                log.error("Reached try cont limit.");
                throw new RuntimeException("Can't load data from internet");
            }
        }
    }

    public static String getStringByPattern(String src, String patternString, int groupNumber) throws PatternMatchingException {
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(src);
        if (matcher.find()) {
            return matcher.group(groupNumber).trim();
        } else {
            throw new PatternMatchingException("Pattern " + patternString + " doesn't match any data in string " + src);
        }
    }

    public static LocalTime getTimeValueForCssAndRegex(Element document, String cssPath, String regex) {
        try {
            String textRepresentation = getStringByPattern(document.select(cssPath).text(), regex, 1);
            return DateTimeFormatter.ofPattern("kk:mm").parse(textRepresentation).query(TemporalQueries.localTime());
        } catch (PatternMatchingException patternMatchingException) {
            log.warn("Can't get value for page url " +
                    "\n\t\t\tCSSPath " + cssPath +
                    "\n\t\t\tRegex " + regex +
                    "\n\t\t\tZero will be returned.");
            return LocalTime.MIDNIGHT;
        }
    }

    public static double getValueForCssAndRegex(Element document, String cssPath, String regex) {
        try {
            String textRepresentation = getStringByPattern(document.select(cssPath).text(), regex, 1);
            return Double.parseDouble(textRepresentation);
        } catch (PatternMatchingException patternMatchingException) {
            log.warn("Can't get value for page url " +
                    "\n\t\t\tCSSPath " + cssPath +
                    "\n\t\t\tRegex " + regex +
                    "\n\t\t\tZero will be returned.");
            return 0;
        }
    }

    public static double getMinutesValueForCssAndRegex(Element document, String cssPath, String regexAll, String regexHours, String regexMinutes) {
        try {
            String textRepresentation = getStringByPattern(document.select(cssPath).text(), regexAll, 1);
            return 60 * Double.parseDouble(getStringByPattern(textRepresentation, regexHours, 1)) + Integer.parseInt(getStringByPattern(textRepresentation, regexMinutes, 1));
        } catch (PatternMatchingException patternMatchingException) {
            log.warn("Can't get value for page url " +
                    "\nCSSPath " + cssPath +
                    "\nRegex " + regexAll +
                    ".\nZero will be returned.");
            return 0;
        }
    }
}
