package com.arthur.downloaddialog.http.request;



import com.arthur.downloaddialog.http.tools.CheckTool;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * 拼装请求参数
 */
public class EasyURLEncoder {
    public static String appendUrl(String url, Map<String, String> map) {
        if (!CheckTool.isEmpty(map) && !CheckTool.isEmpty(url)) {
            StringBuffer buffer = new StringBuffer(url);
            if (!url.endsWith("?")) {
                buffer.append("?");
            } else {
                buffer.append("&");
            }

            int i = 0;
            for (String key : map.keySet()) {
                try {
                    String value = URLEncoder.encode(map.get(key), "UTF-8");
                    buffer.append(key);
                    buffer.append("=");
                    buffer.append(value);
                    if (i != map.size() - 1) {
                        buffer.append("&");
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            url = buffer.toString();
        }

        return url;
    }
}
