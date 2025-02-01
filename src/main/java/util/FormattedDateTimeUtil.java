package util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * <b> Formatted LocalDateTime Message Utility Class </b>
 *
 * @author Aki Chou
 * @date 2025/02/01
 */
public class FormattedDateTimeUtil {

    private static final DateTimeFormatter FORMATTER_NO_DATE = DateTimeFormatter.ofPattern("HH:mm:ss.SSS") ;
    private static final DateTimeFormatter FORMATTER_WITH_DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS") ;

    public static String getWithBrackets() {

        return String.format("[%s]", LocalDateTime.now().format(FORMATTER_NO_DATE)) ;
    }

    public static String getWithParentheses() {

        return String.format("(%s)", LocalDateTime.now().format(FORMATTER_NO_DATE)) ;
    }

    public static String getWithBracketsAndDate() {

        return String.format("[%s]", LocalDateTime.now().format(FORMATTER_WITH_DATE)) ;
    }

    public static String getWithParenthesesAndDate() {

        return String.format("(%s)", LocalDateTime.now().format(FORMATTER_WITH_DATE)) ;
    }
}
