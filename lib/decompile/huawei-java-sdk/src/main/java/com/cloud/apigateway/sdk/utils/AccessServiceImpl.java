package com.cloud.apigateway.sdk.utils;

import com.cloud.sdk.auth.signer.Signer;
import com.cloud.sdk.http.HttpMethodName;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.apache.http.Header;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpOptions;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;

public class AccessServiceImpl extends AccessService {
   private static final String UTF8 = "UTF-8";
   private static final String CHAR_SET_NAME_ISO = "ISO-8859-1";

   public AccessServiceImpl(String ak, String sk) {
      super(ak, sk);
   }

   public AccessServiceImpl(String ak, String sk, String messageDigestAlgorithm) {
      super(ak, sk, messageDigestAlgorithm);
   }

   public HttpRequestBase access(String url, Map<String, String> headers, String content, HttpMethodName httpMethod) throws Exception {
      Request request = new Request();
      request.setAppKey(this.ak);
      request.setAppSecrect(this.sk);
      request.setMethod(httpMethod.name());
      request.setUrl(url);

      for(Map.Entry<String, String> map : headers.entrySet()) {
         request.addHeader((String)map.getKey(), (String)map.getValue());
      }

      request.setBody(content);
      Signer signer = new Signer(this.messageDigestAlgorithm);
      signer.sign(request);
      HttpRequestBase httpRequestBase = createRequest(url, (Header)null, content, httpMethod);

      for(Map.Entry<String, String> map : request.getHeaders().entrySet()) {
         if (map.getKey() != null && !((String)map.getKey()).equalsIgnoreCase("Content-Length") && map.getValue() != null) {
            httpRequestBase.addHeader((String)map.getKey(), new String(((String)map.getValue()).getBytes("UTF-8"), "ISO-8859-1"));
         }
      }

      return httpRequestBase;
   }

   public HttpRequestBase access(String url, Map<String, String> headers, InputStream content, Long contentLength, HttpMethodName httpMethod) throws Exception {
      String body = "";
      if (content != null) {
         ByteArrayOutputStream result = new ByteArrayOutputStream();
         byte[] buffer = new byte[1024];

         int length;
         while((length = content.read(buffer)) != -1) {
            result.write(buffer, 0, length);
         }

         body = result.toString("UTF-8");
      }

      return this.access(url, headers, body, httpMethod);
   }

   private static HttpRequestBase createRequest(String url, Header header, String content, HttpMethodName httpMethod) {
      HttpRequestBase httpRequest;
      if (httpMethod == HttpMethodName.POST) {
         HttpPost postMethod = new HttpPost(url);
         if (content != null) {
            StringEntity entity = new StringEntity(content, StandardCharsets.UTF_8);
            postMethod.setEntity(entity);
         }

         httpRequest = postMethod;
      } else if (httpMethod == HttpMethodName.PUT) {
         HttpPut putMethod = new HttpPut(url);
         httpRequest = putMethod;
         if (content != null) {
            StringEntity entity = new StringEntity(content, StandardCharsets.UTF_8);
            putMethod.setEntity(entity);
         }
      } else if (httpMethod == HttpMethodName.PATCH) {
         HttpPatch patchMethod = new HttpPatch(url);
         httpRequest = patchMethod;
         if (content != null) {
            StringEntity entity = new StringEntity(content, StandardCharsets.UTF_8);
            patchMethod.setEntity(entity);
         }
      } else if (httpMethod == HttpMethodName.GET) {
         httpRequest = new HttpGet(url);
      } else if (httpMethod == HttpMethodName.DELETE) {
         httpRequest = new HttpDelete(url);
      } else if (httpMethod == HttpMethodName.OPTIONS) {
         httpRequest = new HttpOptions(url);
      } else {
         if (httpMethod != HttpMethodName.HEAD) {
            throw new UnknownHttpMethodException("Unknown HTTP method name: " + httpMethod);
         }

         httpRequest = new HttpHead(url);
      }

      httpRequest.addHeader(header);
      return httpRequest;
   }
}
