package org.cirdles.chroni;

import com.loopj.android.http.*;

public class UserVerificationClient {

  private static AsyncHttpClient client = new AsyncHttpClient();

  public static void post(String url, RequestParams params, AsyncHttpResponseHandler responseHandler) {
      client.post(url, params, responseHandler);
  }

}