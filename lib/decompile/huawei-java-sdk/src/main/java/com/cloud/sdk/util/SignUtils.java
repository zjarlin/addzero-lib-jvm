package com.cloud.sdk.util;

import com.cloud.apigateway.sdk.utils.Client;
import com.cloud.apigateway.sdk.utils.Request;
import com.cloud.sdk.auth.vo.SignResult;
import java.util.HashMap;
import java.util.Map;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpRequestBase;

public class SignUtils {
   private static final String SDKSIGNINGSHA256 = "SDK-HMAC-SHA256";

   public static SignResult sign(Request request, String algorithm) throws Exception {
      SignResult result = new SignResult();
      HttpRequestBase signedRequest = Client.sign(request, algorithm);
      Header[] headers = signedRequest.getAllHeaders();
      Map<String, String> headerMap = new HashMap();

      for(Header header : headers) {
         headerMap.put(header.getName(), header.getValue());
      }

      result.setUrl(signedRequest.getURI().toURL());
      result.setHeaders(headerMap);
      return result;
   }

   public static SignResult sign(Request request) throws Exception {
      return sign(request, "SDK-HMAC-SHA256");
   }
}
