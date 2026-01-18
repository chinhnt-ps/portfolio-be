package com.portfolio.wallet.model;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.data.convert.WritingConverter;

import java.time.YearMonth;

/**
 * Converters for YearMonth to/from String for MongoDB
 */
public class YearMonthConverter {
    
    @WritingConverter
    public static class YearMonthToStringConverter implements Converter<YearMonth, String> {
        @Override
        public String convert(YearMonth source) {
            return source.toString(); // Format: "2025-01"
        }
    }
    
    @ReadingConverter
    public static class StringToYearMonthConverter implements Converter<String, YearMonth> {
        @Override
        public YearMonth convert(String source) {
            return YearMonth.parse(source); // Parse: "2025-01"
        }
    }
}
