package com.cloud.sdk.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpUtils {
   private static final String DEFAULT_ENCODING = "UTF-8";
   private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtils.class);
   private static final Pattern ENCODED_CHARACTERS_PATTERN;

   public static String urlEncode(String rawValue, boolean path) throws UnsupportedEncodingException {
      if (rawValue == null) {
         return "";
      } else {
         try {
            String encoded = URLEncoder.encode(rawValue, "UTF-8");
            Matcher match = ENCODED_CHARACTERS_PATTERN.matcher(encoded);

            StringBuffer buffer;
            String replacementTemp;
            for(buffer = new StringBuffer(encoded.length()); match.find(); match.appendReplacement(buffer, replacementTemp)) {
               replacementTemp = match.group(0);
               if ("+".equals(replacementTemp)) {
                  replacementTemp = "%20";
               } else if ("*".equals(replacementTemp)) {
                  replacementTemp = "%2A";
               } else if ("%7E".equals(replacementTemp)) {
                  replacementTemp = "~";
               } else if (path && "%2F".equals(replacementTemp)) {
                  replacementTemp = "/";
               }
            }

            match.appendTail(buffer);
            return buffer.toString();
         } catch (UnsupportedEncodingException ex) {
            LOGGER.info("fail to encode url: ", ex.getMessage());
            throw ex;
         }
      }
   }

   static {
      StringBuilder pattern = new StringBuilder();
      pattern.append(Pattern.quote("+")).append("|").append(Pattern.quote("*")).append("|").append(Pattern.quote("%7E")).append("|").append(Pattern.quote("%2F"));
      ENCODED_CHARACTERS_PATTERN = Pattern.compile(pattern.toString());
   }
}
