package com.cloud.sdk.auth.signer;

import com.cloud.apigateway.sdk.utils.Request;
import com.cloud.sdk.util.BinaryUtils;
import com.cloud.sdk.util.HttpUtils;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedMap;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.StringUtils;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.openeuler.BGMJCEProvider;

public class Signer {
   public static final String LINE_SEPARATOR = "\n";
   public static final String SDK_SIGNING_ALGORITHM = "SDK-HMAC-SHA256";
   public static final String X_SDK_CONTENT_SHA256 = "x-sdk-content-sha256";
   public static final String X_SDK_DATE = "X-Sdk-Date";
   public static final String AUTHORIZATION = "Authorization";
   private static final Pattern AUTHORIZATION_PATTERN_SHA256 = Pattern.compile("SDK-HMAC-SHA256\\s+Access=([^,]+),\\s?SignedHeaders=([^,]+),\\s?Signature=(\\w+)");
   private static final Pattern AUTHORIZATION_PATTERN_SM3 = Pattern.compile("SDK-HMAC-SM3\\s+Access=([^,]+),\\s?SignedHeaders=([^,]+),\\s?Signature=(\\w+)");
   private static final String LINUX_NEW_LINE = "\n";
   public static final String HOST = "Host";
   public String messageDigestAlgorithm = "SDK-HMAC-SHA256";

   public Signer(String messageDigestAlgorithm) {
      this.messageDigestAlgorithm = messageDigestAlgorithm;
   }

   public Signer() {
   }

   public void sign(Request request) throws UnsupportedEncodingException {
      String singerDate = this.getHeader(request, "X-Sdk-Date");
      SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'", Locale.ENGLISH);
      sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
      if (singerDate == null) {
         singerDate = sdf.format(new Date());
         request.addHeader("X-Sdk-Date", singerDate);
      }

      this.addHostHeader(request);
      String messageDigestContent = this.calculateContentHash(request);
      String[] signedHeaders = this.getSignedHeaders(request);
      String canonicalRequest = this.createCanonicalRequest(request, signedHeaders, messageDigestContent);
      byte[] signingKey = this.deriveSigningKey(request.getSecrect());
      String stringToSign = this.createStringToSign(canonicalRequest, singerDate);
      byte[] signature = this.computeSignature(stringToSign, signingKey);
      String signatureResult = this.buildAuthorizationHeader(signedHeaders, signature, request.getKey());
      request.addHeader("Authorization", signatureResult);
   }

   protected String getCanonicalizedResourcePath(String resourcePath) throws UnsupportedEncodingException {
      if (resourcePath != null && !resourcePath.isEmpty()) {
         try {
            resourcePath = (new URI(resourcePath)).getPath();
         } catch (URISyntaxException var3) {
            return resourcePath;
         }

         String value = HttpUtils.urlEncode(resourcePath, true);
         if (!value.startsWith("/")) {
            value = "/".concat(value);
         }

         if (!value.endsWith("/")) {
            value = value.concat("/");
         }

         return value;
      } else {
         return "/";
      }
   }

   protected String getCanonicalizedQueryString(Map<String, List<String>> parameters) throws UnsupportedEncodingException {
      SortedMap<String, List<String>> sorted = new TreeMap();

      for(Map.Entry<String, List<String>> entry : parameters.entrySet()) {
         String encodedParamName = HttpUtils.urlEncode((String)entry.getKey(), false);
         List<String> paramValues = (List)entry.getValue();
         List<String> encodedValues = new ArrayList(paramValues.size());

         for(String value : paramValues) {
            encodedValues.add(HttpUtils.urlEncode(value, false));
         }

         Collections.sort(encodedValues);
         sorted.put(encodedParamName, encodedValues);
      }

      StringBuilder result = new StringBuilder();

      for(Map.Entry<String, List<String>> entry : sorted.entrySet()) {
         for(String value : (List)entry.getValue()) {
            if (result.length() > 0) {
               result.append("&");
            }

            result.append((String)entry.getKey()).append("=").append(value);
         }
      }

      return result.toString();
   }

   protected String createCanonicalRequest(Request request, String[] signedHeaders, String messageDigestContent) throws UnsupportedEncodingException {
      return request.getMethod().toString() + "\n" + this.getCanonicalizedResourcePath(request.getPath()) + "\n" + this.getCanonicalizedQueryString(request.getQueryStringParams()) + "\n" + this.getCanonicalizedHeaderString(request, signedHeaders) + "\n" + this.getSignedHeadersString(signedHeaders) + "\n" + messageDigestContent;
   }

   protected String createStringToSign(String canonicalRequest, String singerDate) {
      return StringUtils.equals(this.messageDigestAlgorithm, "SDK-HMAC-SHA256") ? this.messageDigestAlgorithm + "\n" + singerDate + "\n" + BinaryUtils.toHex(this.hash(canonicalRequest)) : this.messageDigestAlgorithm + "\n" + singerDate + "\n" + BinaryUtils.toHex(this.hashSm3(canonicalRequest));
   }

   private byte[] deriveSigningKey(String secret) {
      return secret.getBytes(StandardCharsets.UTF_8);
   }

   protected byte[] sign(byte[] data, byte[] key, SigningAlgorithm algorithm) {
      try {
         if (SigningAlgorithm.HmacSM3.equals(algorithm)) {
            Security.insertProviderAt(new BGMJCEProvider(), 1);
         }

         Mac mac = Mac.getInstance(algorithm.toString());
         mac.init(new SecretKeySpec(key, algorithm.toString()));
         return mac.doFinal(data);
      } catch (InvalidKeyException | NoSuchAlgorithmException var5) {
         return new byte[0];
      }
   }

   protected final byte[] computeSignature(String stringToSign, byte[] signingKey) {
      return StringUtils.equals(this.messageDigestAlgorithm, "SDK-HMAC-SHA256") ? this.sign(stringToSign.getBytes(StandardCharsets.UTF_8), signingKey, SigningAlgorithm.HmacSHA256) : this.sign(stringToSign.getBytes(StandardCharsets.UTF_8), signingKey, SigningAlgorithm.HmacSM3);
   }

   private String buildAuthorizationHeader(String[] signedHeaders, byte[] signature, String accessKey) {
      String credential = "Access=" + accessKey;
      String signerHeaders = "SignedHeaders=" + this.getSignedHeadersString(signedHeaders);
      String signatureHeader = "Signature=" + BinaryUtils.toHex(signature);
      return this.messageDigestAlgorithm + " " + credential + ", " + signerHeaders + ", " + signatureHeader;
   }

   protected String[] getSignedHeaders(Request request) {
      String[] signedHeaders = (String[])request.getHeaders().keySet().toArray(new String[0]);
      Arrays.sort(signedHeaders, String.CASE_INSENSITIVE_ORDER);
      return signedHeaders;
   }

   protected String getCanonicalizedHeaderString(Request request, String[] signedHeaders) {
      Map<String, String> requestHeaders = request.getHeaders();
      StringBuilder buffer = new StringBuilder();

      for(String header : signedHeaders) {
         String key = header.toLowerCase(Locale.getDefault());
         String value = (String)requestHeaders.get(header);
         buffer.append(key).append(":");
         if (value != null) {
            buffer.append(value.trim());
         }

         buffer.append("\n");
      }

      return buffer.toString();
   }

   protected String getSignedHeadersString(String[] signedHeaders) {
      StringBuilder buffer = new StringBuilder();

      for(String header : signedHeaders) {
         if (buffer.length() > 0) {
            buffer.append(";");
         }

         buffer.append(header.toLowerCase(Locale.getDefault()));
      }

      return buffer.toString();
   }

   protected void addHostHeader(Request request) {
      boolean haveHostHeader = false;

      for(String key : request.getHeaders().keySet()) {
         if ("Host".equalsIgnoreCase(key)) {
            haveHostHeader = true;
            break;
         }
      }

      if (!haveHostHeader) {
         request.addHeader("Host", request.getHost());
      }

   }

   protected String getHeader(Request request, String header) {
      if (header == null) {
         return null;
      } else {
         Map<String, String> headers = request.getHeaders();

         for(Map.Entry<String, String> entry : headers.entrySet()) {
            if (header.equalsIgnoreCase((String)entry.getKey())) {
               return (String)entry.getValue();
            }
         }

         return null;
      }
   }

   public boolean verify(Request request) throws UnsupportedEncodingException {
      String singerDate = this.getHeader(request, "X-Sdk-Date");
      String authorization = this.getHeader(request, "Authorization");
      Matcher match = AUTHORIZATION_PATTERN_SM3.matcher(authorization);
      if (StringUtils.equals(this.messageDigestAlgorithm, "SDK-HMAC-SHA256")) {
         match = AUTHORIZATION_PATTERN_SHA256.matcher(authorization);
      }

      if (!match.find()) {
         return false;
      } else {
         String[] signedHeaders = match.group(2).split(";");
         byte[] signingKey = this.deriveSigningKey(request.getSecrect());
         String messageDigestContent = this.calculateContentHash(request);
         String canonicalRequest = this.createCanonicalRequest(request, signedHeaders, messageDigestContent);
         String stringToSign = this.createStringToSign(canonicalRequest, singerDate);
         byte[] signature = this.computeSignature(stringToSign, signingKey);
         String signatureResult = this.buildAuthorizationHeader(signedHeaders, signature, request.getKey());
         return signatureResult.equals(authorization);
      }
   }

   protected String calculateContentHash(Request request) {
      String content_sha256 = this.getHeader(request, "x-sdk-content-sha256");
      if (content_sha256 != null) {
         return content_sha256;
      } else {
         return StringUtils.equals(this.messageDigestAlgorithm, "SDK-HMAC-SHA256") ? BinaryUtils.toHex(this.hash(request.getBody())) : BinaryUtils.toHex(this.hashSm3(request.getBody()));
      }
   }

   public byte[] hash(String text) {
      try {
         MessageDigest md = MessageDigest.getInstance("SHA-256");
         md.update(text.getBytes(StandardCharsets.UTF_8));
         return md.digest();
      } catch (NoSuchAlgorithmException var3) {
         return new byte[0];
      }
   }

   public byte[] hashSm3(String text) {
      byte[] srcData = text.getBytes(StandardCharsets.UTF_8);
      SM3Digest digest = new SM3Digest();
      digest.update(srcData, 0, srcData.length);
      byte[] hash = new byte[digest.getDigestSize()];
      digest.doFinal(hash, 0);
      return hash;
   }
}
