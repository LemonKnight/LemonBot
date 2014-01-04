package de.lemonknight.util;

/**
 *
 * @author Lemon Knight
 */
public class Utils {
    
    
    public static String unwrapCDATA(String message) {
        return message.substring(9, message.length()-3);
    }
}
