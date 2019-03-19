package ru.batuevdm.theelectronix;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class OrderActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

        setTitle("Заказ");

        Button orderButton = findViewById(R.id.orderButtonSend);

        orderButton.setOnClickListener(v -> {
            Api api = new Api(getApplicationContext());
            Shop shop = new Shop(getApplicationContext());
            EditText nameEdit = findViewById(R.id.orderPersonName);
            EditText phoneEdit = findViewById(R.id.orderPersonPhone);
            String name = nameEdit.getText()
                    .toString()
                    .trim();
            String phone = phoneEdit.getText()
                    .toString()
                    .trim();


            boolean isEmpty = false;
            if (name.isEmpty()) {
                nameEdit.setError("Введите имя");
                isEmpty = true;
            }
            if (phone.isEmpty()) {
                phoneEdit.setError("Введите номер телефона");
                isEmpty = true;
            }
            if (isEmpty) return;

            api.loading(true, findViewById(R.id.orderProgressBar));
            Log.d("messages", "sss");

            api.order(name, phone, shop.getCart(), new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        api.loading(false, findViewById(R.id.orderProgressBar));
                        AlertDialog.Builder builder = new AlertDialog.Builder(OrderActivity.this);
                        builder.setTitle("Ошибка")
                                .setMessage("Ошибка оформления заказа, попробуйте позже")
                                .setCancelable(false)
                                .setNegativeButton("OK",
                                        (dialog, id) -> dialog.cancel());
                        AlertDialog alert = builder.create();
                        alert.show();
                        Log.d("messages", "111");
                    });
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    runOnUiThread(() -> {
                        api.loading(false, findViewById(R.id.orderProgressBar));
                        try {
                            String resp = response.body().string();
                            Log.d("messages", "Response: '" + resp + "'");
                            if (resp.equals("ok")) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(OrderActivity.this);
                                builder.setTitle("Заказ оформлен")
                                        .setMessage("Ваш заказ оформлен, скоро с Вами свяжется менеджер")
                                        .setCancelable(false)
                                        .setNegativeButton("OK",
                                                (dialog, id) -> {
                                                    dialog.cancel();
                                                    Intent i = new Intent(OrderActivity.this, MainActivity.class);
                                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                                    startActivity(i);
                                                    shop.clearCart();
                                                });
                                AlertDialog alert = builder.create();
                                alert.show();
                                Log.d("messages", "222");
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(OrderActivity.this);
                                builder.setTitle("Ошибка")
                                        .setMessage("Ошибка оформления заказа, попробуйте позже")
                                        .setCancelable(false)
                                        .setNegativeButton("OK",
                                                (dialog, id) -> dialog.cancel());
                                AlertDialog alert = builder.create();
                                alert.show();
                            }
                        } catch (Exception e) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(OrderActivity.this);
                            builder.setTitle("Ошибка")
                                    .setMessage("Ошибка оформления заказа, попробуйте позже")
                                    .setCancelable(false)
                                    .setNegativeButton("OK",
                                            (dialog, id) -> dialog.cancel());
                            AlertDialog alert = builder.create();
                            alert.show();
                            Log.d("messages", "333");
                        }
                    });
                }
            });

        });
    }
}
