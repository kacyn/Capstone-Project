package example.kacyn.com.caltrainplus;

import com.squareup.okhttp.Response;

/**
 * Created by kacyn on 12/19/15.
 */
public interface HttpCallback {
    void onSuccess(Response response);
}
