package com.car.carsharing.mycarsharing;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.car.carsharing.mycarsharing.model.Client;
import com.car.carsharing.mycarsharing.model.stat.ClientStatic;
import com.car.carsharing.mycarsharing.model.LogPass;
import com.car.carsharing.mycarsharing.model.Token;
import com.car.carsharing.mycarsharing.server.ClientService;
import com.car.carsharing.mycarsharing.server.Server;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    public static final String APP_PREFERENCES = "mysettings";
    public static final String APP_PREFERENCES_TOKEN = "token";
    public static final String APP_PREFERENCES_ID = "id";
    private static final String TAG = "asda";
    //Создаём переменную, представляющую экземпляр класса SharedPreferences, который отвечает за работу с настройками:
    private SharedPreferences mSettings;

    private ClientService clientService;

    TextInputEditText login;
    TextInputEditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        //Если авторизован, открываем карту или кабинет
        testAuth();
        //Иначе показываем стр входа
        setContentView(R.layout.activity_login);
        setTitle("Авторизация");


        Button buttonLogin = findViewById(R.id.buttonLogin);
        Button buttonReg = findViewById(R.id.buttonReg);



        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login = findViewById(R.id.loginPhone);
                password = findViewById(R.id.loginPass);



                if (login.getText().toString().equals("") || password.getText().toString().equals("")){
                    if (login.getText().toString().equals("")) {
                        login.setError("Введите номер телефона");
                    }
                    if (password.getText().toString().equals("")) {
                        password.setError("Введите пароль");
                    }
                }else {

                    LogPass logPass = new LogPass(login.getText().toString(), password.getText().toString());
                    clientService = new Server("").clientService;
                    Call<Client> call = clientService.login(logPass);
                    call.enqueue(new Callback<Client>() {
                        @Override
                        public void onResponse(Call<Client> call, Response<Client> response) {

                            Client client = response.body();

                            if (client != null) {

                                if (!client.isEnabled()){

                                    AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
                                    alertDialog.setTitle("Ошибка");
                                    alertDialog.setMessage("Ваш аккаунт заблокирован");
                                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            });
                                    alertDialog.show();

                                }else {

                                    mSettings.edit().putString(APP_PREFERENCES_TOKEN, client.getToken()).apply();
                                    mSettings.edit().putInt(APP_PREFERENCES_ID, client.getId()).apply();

                                    Intent intent = new Intent(getApplicationContext(), NavActivity.class);
                                    intent.putExtra("client", client);
                                    ClientStatic.client = client;
                                    startActivity(intent);
                                    finish();

                                }
                            } else {

                                AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
                                alertDialog.setTitle("Ошибка");
                                alertDialog.setMessage("Неверный логин или пароль");
                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                dialog.dismiss();
                                            }
                                        });
                                alertDialog.show();
                            }
                        }

                        @Override
                        public void onFailure(Call<Client> call, Throwable t) {
                            Log.d(TAG, "onFailure: " + t.getMessage());
                            AlertDialog alertDialog = new AlertDialog.Builder(LoginActivity.this).create();
                            alertDialog.setTitle("Ошибка");
                            alertDialog.setMessage("Не удалось подключиться к серверу");
                            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            alertDialog.show();
                        }
                    });
                }
            }
        });


        buttonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(),RegActivity.class);
                startActivity(intent);
            }
        });

    }

    private void testAuth(){
        final Token token;
        //Если есть токен
        if (mSettings.contains(APP_PREFERENCES_TOKEN) && mSettings.contains(APP_PREFERENCES_ID)) {
            token = new Token(mSettings.getInt(APP_PREFERENCES_ID, 0), mSettings.getString(APP_PREFERENCES_TOKEN, ""));
            //Отсылаем id и токен на сервер
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Client client = new Server(token.getToken()).testClient(token);
                    if (client!=null && client.isEnabled()){
                        Intent intent = new Intent(getApplicationContext(), NavActivity.class);
                        intent.putExtra("id", token.getId());
                        intent.putExtra("token",token.getToken());
                        intent.putExtra("client",client);
                        ClientStatic.client = client;
                        startActivity(intent);
                        finish();
                    }
                }
            }).start();

        }
    }
}
