package com.car.carsharing.mycarsharing.fragments;

import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.car.carsharing.mycarsharing.R;
import com.car.carsharing.mycarsharing.model.stat.ClientStatic;
import com.car.carsharing.mycarsharing.model.OrderPay;
import com.car.carsharing.mycarsharing.model.stat.OrderPayStatic;
import com.car.carsharing.mycarsharing.server.Server;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.CardRequirements;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentMethodTokenizationParameters;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.gms.wallet.TransactionInfo;
import com.google.android.gms.wallet.Wallet;
import com.google.android.gms.wallet.WalletConstants;

import java.util.Arrays;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderFragment extends Fragment {

    private OrderPay orderPay;

    private TextView orderMain;
    private TextView orderCar;
    private TextView orderNumber;
    private TextView orderTime;
    private TextView orderPrice;
    private TextView orderTimeInfo;
    private TextView orderNumberInfo;
    private TextView orderPriceInfo;

    private Button orderPayButton;

    private PaymentsClient mPaymentsClient;

    private static final String CLIENT_ID = "370190E9AC2656043498E48F7A8CCEBAD03D15E4CC4CC988A757825A560631EC";
    private static final String HOST = "https://demomoney.yandex.ru";
    private static final int REQUEST_CODE = 1;
    private static final int LOAD_PAYMENT_DATA_REQUEST_CODE = 0;


    public OrderFragment() {
        // Required empty public constructor
    }

    private void isReadyToPay() {
        IsReadyToPayRequest request =
                IsReadyToPayRequest.newBuilder()
                        .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_CARD)
                        .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD)
                        .build();
        Task<Boolean> task = mPaymentsClient.isReadyToPay(request);
        task.addOnCompleteListener(
                new OnCompleteListener<Boolean>() {
                    public void onComplete(Task<Boolean> task) {
                        try {
                            boolean result = task.getResult(ApiException.class);
                            if (result) {
                                // Show Google as payment option.
                            } else {
                                // Hide Google as payment option.
                            }
                        } catch (ApiException exception) {
                        }
                    }
                });
    }

    private PaymentDataRequest createPaymentDataRequest(String price) {
        PaymentDataRequest.Builder request =
                PaymentDataRequest.newBuilder()
                        .setTransactionInfo(
                                TransactionInfo.newBuilder()
                                        .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
                                        .setTotalPrice(price)
                                        .setCurrencyCode("RUB")
                                        .build())
                        .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_CARD)
                        .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD)
                        .setCardRequirements(
                                CardRequirements.newBuilder()
                                        .addAllowedCardNetworks(
                                                Arrays.asList(
                                                        WalletConstants.CARD_NETWORK_AMEX,
                                                        WalletConstants.CARD_NETWORK_DISCOVER,
                                                        WalletConstants.CARD_NETWORK_VISA,
                                                        WalletConstants.CARD_NETWORK_MASTERCARD))
                                        .build());

        PaymentMethodTokenizationParameters params =
                PaymentMethodTokenizationParameters.newBuilder()
                        .setPaymentMethodTokenizationType(
                                WalletConstants.PAYMENT_METHOD_TOKENIZATION_TYPE_PAYMENT_GATEWAY)
                        .addParameter("gateway", "example")
                        .addParameter("gatewayMerchantId", "exampleGatewayMerchantId")
                        .build();

        request.setPaymentMethodTokenizationParameters(params);
        return request.build();
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_order, container, false);

        orderCar = rootView.findViewById(R.id.orderCar);
        orderMain = rootView.findViewById(R.id.orderMain);
        orderNumber = rootView.findViewById(R.id.orderNumber);
        orderTime = rootView.findViewById(R.id.orderTime);
        orderPrice = rootView.findViewById(R.id.orderPrice);
        orderPayButton = rootView.findViewById(R.id.orderPay);
        orderTimeInfo = rootView.findViewById(R.id.orderTimeInfo);
        orderNumberInfo = rootView.findViewById(R.id.orderNumberInfo);
        orderPriceInfo = rootView.findViewById(R.id.orderPriceInfo);

        new Server(ClientStatic.client.getToken()).clientService.getPay(ClientStatic.client.getId()).enqueue(new Callback<OrderPay>() {
            @Override
            public void onResponse(Call<OrderPay> call, Response<OrderPay> response) {
                if (response.isSuccessful()){
                    orderPay = response.body();
                    OrderPayStatic.orderPay = orderPay;
                    orderMain.setText("У вас одна неоплаченная поездка");
                    orderCar.setText(orderPay.getCarName());
                    orderNumber.setText(orderPay.getCarNumber());
                    orderTime.setText(orderPay.getTime());
                    orderPrice.setText(String.valueOf(orderPay.getPrice())+" руб");

                    orderCar.setVisibility(View.VISIBLE);
                    orderNumber.setVisibility(View.VISIBLE);
                    orderTime.setVisibility(View.VISIBLE);
                    orderPrice.setVisibility(View.VISIBLE);
                    orderPayButton.setVisibility(View.VISIBLE);
                    orderTimeInfo.setVisibility(View.VISIBLE);
                    orderNumberInfo.setVisibility(View.VISIBLE);
                    orderPriceInfo.setVisibility(View.VISIBLE);

                    mPaymentsClient = Wallet.getPaymentsClient(
                            getActivity(),
                            new Wallet.WalletOptions.Builder()
                                    .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
                                    .build());

                    orderPayButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            System.out.println(orderPay.getPrice());
                            PaymentDataRequest request = createPaymentDataRequest(String.valueOf(orderPay.getPrice()));
                            if (request != null) {
                                AutoResolveHelper.resolveTask(
                                        mPaymentsClient.loadPaymentData(request),
                                        getActivity(),
                                        // LOAD_PAYMENT_DATA_REQUEST_CODE is a constant value
                                        // you define.
                                        LOAD_PAYMENT_DATA_REQUEST_CODE);
                            }

                        /*    PaymentParams paymentParams = PhoneParams.newInstance("79527414994",new BigDecimal(orderPay.getPrice()));
                            Intent intent = PaymentActivity.getBuilder(getActivity())
                                    .setPaymentParams(paymentParams)
                                    .setClientId(CLIENT_ID)
                                    .setHost(HOST)
                                    .build();
                            startActivityForResult(intent,REQUEST_CODE);*/



                        }
                    });

                }else {
                    orderMain.setText("Все поездки оплачены");
                    orderCar.setVisibility(View.INVISIBLE);
                    orderNumber.setVisibility(View.INVISIBLE);
                    orderTime.setVisibility(View.INVISIBLE);
                    orderPrice.setVisibility(View.INVISIBLE);
                    orderPayButton.setVisibility(View.INVISIBLE);
                    orderTimeInfo.setVisibility(View.INVISIBLE);
                    orderNumberInfo.setVisibility(View.INVISIBLE);
                    orderPriceInfo.setVisibility(View.INVISIBLE);
                }
            }

            @Override
            public void onFailure(Call<OrderPay> call, Throwable t) {

            }
        });

        return rootView;
    }


    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
