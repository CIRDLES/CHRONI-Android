package org.cirdles.chroni;

import com.loopj.android.http.*;

public class UserVerificationClient {

  private static final String BASE_URL = "http://www.geochronportal.org/credentials_service.php";

  private static AsyncHttpClient client = new AsyncHttpClient();

  public static void get(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
      client.get(url, params, responseHandler);
  }

  public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
      client.post(url, params, responseHandler);
  }

  private static String getAbsoluteUrl(String relativeUrl) {
      return BASE_URL + relativeUrl;
  }
}