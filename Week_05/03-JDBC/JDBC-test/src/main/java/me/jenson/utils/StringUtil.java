package me.jenson.utils;

import java.util.Iterator;

public class StringUtil {

    public static String convertIDsToIDStr(
            Iterable<? extends Number> idList, String separator) {
        if (idList == null) {
            return null;
        }
        String str = "";
        Iterator<? extends Number> it = idList.iterator();
        while (it.hasNext()) {
            Number id = it.next();
            if (it.hasNext()) {
                str = str + id + separator;
            } else {
                str = str + id;
            }
        }
        return str;
    }
}
