package com.car.carsharing.mycarsharing.server;


import android.util.Log;

import com.car.carsharing.mycarsharing.model.Client;
import com.car.carsharing.mycarsharing.model.ClientReg;
import com.car.carsharing.mycarsharing.model.LogPass;
import com.car.carsharing.mycarsharing.model.Token;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.ContentValues.TAG;

public class Server {
    private static final String BASE_URL = "http://10.0.3.2/";
    public ClientService clientService;
    private String token;

    public Server(String s) {
        token = s;
       /* OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(Chain chain) throws IOException {
                        Request original = chain.request();

                        if (token==null){
                            token = "";
                        }
                        Request request = original.newBuilder()
                                .header("Authorization", token)
                                .method(original.method(), original.body())
                                .build();
                        return chain.proceed(request);
                    }
                }).build();
*/
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
//                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        clientService = retrofit.create(ClientService.class);
    }



    public Client testClient(Token token){
        Call<Client> call = clientService.testClient(token);
        Client client = null;
        try {
            Response<Client> response = call.execute();
            if (response.isSuccessful()){
                client = response.body();
                if (client != null) {
                    if (client.getId() == token.getId()) {
                        return client;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
