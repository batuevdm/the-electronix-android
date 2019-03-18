package ru.batuevdm.theelectronix;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class CartActivity extends AppCompatActivity {

    Api api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        setTitle("Корзина");
        api = new Api(getApplicationContext());
        try {
            loadCart();
        } catch (JSONException e) {

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void loadCart() throws JSONException {
        Shop shop = new Shop(getApplicationContext());
        JSONArray cart = shop.getCart();
        ProgressBar progressBar = findViewById(R.id.cartProgressBar);

        api.loading(true, progressBar);
        if (cart.length() > 0) {
            for (int i = 0; i < cart.length(); i++) {
                JSONObject product = cart.getJSONObject(i);
                addProduct(product, findViewById(R.id.cartScrollLayout));
            }
        } else {
            Toast.makeText(getApplicationContext(), "Пусто", Toast.LENGTH_LONG).show();
        }
        api.loading(false, progressBar);
        Button cartButton = findViewById(R.id.cartOrderButton);
        cartButton.setVisibility(Button.VISIBLE);
    }

    public void addProduct(JSONObject JSONproduct, LinearLayout layout) throws JSONException {
        int productID = JSONproduct.getInt("id");
        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.cart_item, null);
        layout.addView(rowView, layout.getChildCount());

        api.getProduct(productID, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() -> {
                    api.loadError(findViewById(android.R.id.content), v -> {
                        try {
                            addProduct(JSONproduct, layout);
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        }
                        api.dismissLoadError();
                    });
                });
            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                runOnUiThread(() -> {
                    try {
                        String res = response.body() != null ? response.body().string() : "{}";
                        try {
                            JSONObject result = new JSONObject(res);

                            String status = result.getString("status");
                            if (status.equals("error")) {
                                api.toast(result.getString("message"));
                                return;
                            }

                            JSONObject product = result.getJSONObject("product");

                            TextView productName =                  rowView.findViewById(R.id.cartProductName);
                            ImageView productImage =                rowView.findViewById(R.id.cartProductImage);
                            TextView productPrice =                 rowView.findViewById(R.id.cartPrice);
                            EditText productCol =                   rowView.findViewById(R.id.cartCol);
                            TextView maxProductCol =                rowView.findViewById(R.id.cartColMax);

                            int col = Integer.parseInt(product.getString("col"));
                            int cartCol = JSONproduct.getInt("col");
                            if (col < cartCol) {
                                cartCol = col;
                            }
                            productCol.setText(String.format("%s", cartCol));
                            productCol.setFilters(new InputFilter[]{ new InputFilterMinMax(1, col)});
                            maxProductCol.setText("Максимальное количество: " + col);

                            productName.setText(product.getString("name"));
                            productPrice.setText(product.getString("price") + " \u20BD");

                            if (product.getString("new_price").equals("null")) {
                                productPrice.setText(product.getString("price") + " \u20BD");
                            } else {
                                productPrice.setText(product.getString("new_price") + " \u20BD");
                            }

                            String photo = product.getString("main_photo").equals("null") ? "default.png" : product.getString("main_photo");
                            Picasso.get()
                                    .load(Api.site + "images/products/" + photo)
                                    .placeholder(R.drawable.ic_menu_gallery)
                                    .error(R.drawable.ic_menu_camera)
                                    .into(productImage);

                        } catch (JSONException e) {
                            api.loadError(findViewById(android.R.id.content), v -> {
                                try {
                                    addProduct(JSONproduct, layout);
                                } catch (JSONException e1) {
                                    e1.printStackTrace();
                                }
                                api.dismissLoadError();
                            });
                        }

                    } catch (IOException e) {
                        api.loadError(findViewById(android.R.id.content), v -> {
                            try {
                                addProduct(JSONproduct, layout);
                            } catch (JSONException e1) {
                                e1.printStackTrace();
                            }
                            api.dismissLoadError();
                        });
                    }
                });
            }
        });
    }
}
