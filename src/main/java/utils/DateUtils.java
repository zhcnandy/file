package utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

public class DateUtils {

    private DateUtils() {
    }

    /**
     * 毫秒级时间戳转 LocalDateTime
     *
     * @param epochMilli 毫秒级时间戳
     * @return LocalDateTime
     */
    public static LocalDateTime ofEpochMilli(long epochMilli) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneOffset.of("+8"));
    }

    /**
     * LocalDateTime转时间格式字符串
     * @param localDateTime 时间
     * @return string
     */
    public static String formatToDate(LocalDateTime localDateTime){
        String format  = "yyyy-MM-dd";
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(format);
        return localDateTime.format(dateTimeFormatter);
    }

    /**
     * 毫秒级时间戳转 转时间格式字符串
     * @return string
     */
    public static String ofEpochMilliToDate(long epochMilli){
        return formatToDate(ofEpochMilli(epochMilli));
    }

    public static String ofEpochMilliToDateTime(long epochMilli){
        LocalDateTime localDateTime = ofEpochMilli(epochMilli);
        String format  = "yyyy-MM-dd HH:mm:ss";
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(format);
        return localDateTime.format(dateTimeFormatter);
    }

}
