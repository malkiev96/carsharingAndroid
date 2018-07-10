package com.car.carsharing.mycarsharing.server;


import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

import com.car.carsharing.mycarsharing.model.AndroidCar;
import com.car.carsharing.mycarsharing.model.Client;
import com.car.carsharing.mycarsharing.model.ClientReg;
import com.car.carsharing.mycarsharing.model.LogPass;
import com.car.carsharing.mycarsharing.model.AndroidOrder;
import com.car.carsharing.mycarsharing.model.OrderPay;
import com.car.carsharing.mycarsharing.model.PayInfo;
import com.car.carsharing.mycarsharing.model.Tariff;
import com.car.carsharing.mycarsharing.model.Token;
import com.car.carsharing.mycarsharing.model.Zone;

import java.util.List;

public interface ClientService {
    @POST("api/client/valid")
    Call<ResponseBody> validClient(@Body ClientReg clientReg);

    @GET("api/client/{id}")
    Call<Client> getClient(@Path("id") int id);

    @POST("api/client/token")
    Call<Client> testClient(@Body Token token);

    @POST("api/client/login")
    Call<Client> login(@Body LogPass logPass);

    @POST("api/client/registration")
    Call<Client> regClient(@Body ClientReg clientReg);

    @POST("api/carList")
    Call<List<AndroidCar>> getCars();

    @GET("api/zones")
    Call<List<Zone>> getZones();

    @POST("api/order/test")
    @FormUrlEncoded
    Call<AndroidCar> testBooking(@Field("client_id") int clientId, @Field("car_number") String number);

    @POST("api/order/actual")
    @FormUrlEncoded
    Call<AndroidOrder> getActual(@Field("client_id") int clientId);

    @GET("api/tariff/getAll")
    Call<List<Tariff>> getTariffs();

    @POST("api/order/booking")
    @FormUrlEncoded
    Call<AndroidOrder> bookCar(@Field("client_id") int clientId, @Field("car_id") int carId);

    @POST("api/order/rent")
    @FormUrlEncoded
    Call<AndroidOrder> rentCar(@Field("client_id") int clientId);

    @POST("api/order/wait")
    @FormUrlEncoded
    Call<AndroidOrder> waitCar(@Field("client_id") int clientId);

    @POST("api/order/finish")
    @FormUrlEncoded
    Call<AndroidOrder> finishCar(@Field("client_id") int clientId);

    @POST("api/order/stopBooking")
    @FormUrlEncoded
    Call<AndroidOrder> stopBooking(@Field("client_id") int clientId);

    @POST("api/order/pay")
    @FormUrlEncoded
    Call<OrderPay> getPay(@Field("client_id") int clientId);

    @POST("api/order/makePay")
    Call<ResponseBody> makePay(@Body PayInfo payInfo);


}
