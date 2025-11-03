package com.cloud.sdk.util;

import java.util.Locale;

public class BinaryUtils {
   public static String toHex(byte[] data) {
      StringBuffer sbuff = new StringBuffer(data.length * 2);

      for(byte bye : data) {
         String hexStr = Integer.toHexString(bye);
         if (hexStr.length() == 1) {
            sbuff.append("0");
         } else if (hexStr.length() == 8) {
            hexStr = hexStr.substring(6);
         }

         sbuff.append(hexStr);
      }

      return sbuff.toString().toLowerCase(Locale.getDefault());
   }
}
