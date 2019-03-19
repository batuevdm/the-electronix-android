package ru.batuevdm.theelectronix;

import android.content.Context;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONArray;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

class Api {
    static String site = "https://the-electronix.store/";
    private static final String QUERY_ERROR = "-q-err";
    private static final String RESPONSE_ERROR = "-q-res";
    private Context context;
    private Snackbar bar;

    Api(Context context) {
        this.context = context;
    }

    private void get(String address, Callback callback) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url(site + address)
                .build();

        Call call = client.newCall(request);
        call.enqueue(callback);
    }

    private void post(String address, Map<String, String> params, Callback callback)
    {
        OkHttpClient client = new OkHttpClient();
        MultipartBody.Builder requestBuilder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        for (Map.Entry<String, String> entry : params.entrySet()) {
            requestBuilder.addFormDataPart(entry.getKey(), entry.getValue());
        }

        RequestBody requestBody = requestBuilder.build();

        Request request = new Request.Builder()
                .post(requestBody)
                .url(site + address)
                .build();

        Call call = client.newCall(request);
        call.enqueue(callback);
    }

    void toast(String text)
    {
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        toast.show();
    }

    void loading(boolean status, ProgressBar progressBar)
    {
        if (status) {
            progressBar.setVisibility(ProgressBar.VISIBLE);
        } else {
            progressBar.setVisibility(ProgressBar.INVISIBLE);
        }
    }

    void loadError(View view, View.OnClickListener onClickListener)
    {
        bar = Snackbar.make(view, "Ошибка загрузки. Проверьте подключение к интернету.", Snackbar.LENGTH_INDEFINITE);
        bar.setAction("Повторить", onClickListener);
        bar.show();
    }

    void dismissLoadError()
    {
        if (bar != null && bar.isShown())
            bar.dismiss();
    }

    void getNewProducts(Callback callback)
    {
        get("api/products/get_new", callback);
    }

    void getProduct(int productID, Callback callback)
    {
        get("api/products/get/" + productID, callback);
    }

    void getCategories(int parentCategory, Callback callback)
    {
        if (parentCategory == -1) {
            get("api/categories/get", callback);
        } else {
            get("api/categories/one/" + parentCategory, callback);
        }
    }

    void getCategoryProducts(int categoryID, Callback callback)
    {
        get("api/products/get_category/" + categoryID, callback);
    }

    void search(String query, Callback callback)
    {
        get("api/products/search?q=" + query, callback);
    }

    void order(String name, String phone, JSONArray cart, Callback callback)
    {
        Map<String, String> params = new HashMap<>();
        params.put("name", name);
        params.put("phone", phone);
        params.put("cart", cart.toString());

        post("api/orders/new", params, callback);
    }
}