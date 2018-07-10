package com.car.carsharing.mycarsharing;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.akexorcist.googledirection.DirectionCallback;
import com.akexorcist.googledirection.GoogleDirection;
import com.akexorcist.googledirection.constant.TransportMode;
import com.akexorcist.googledirection.model.Direction;
import com.akexorcist.googledirection.model.Route;
import com.akexorcist.googledirection.util.DirectionConverter;
import com.car.carsharing.mycarsharing.model.Action;
import com.car.carsharing.mycarsharing.model.AndroidCar;
import com.car.carsharing.mycarsharing.model.Zone;
import com.car.carsharing.mycarsharing.model.stat.CarStatic;
import com.car.carsharing.mycarsharing.model.Client;
import com.car.carsharing.mycarsharing.model.stat.ClientStatic;
import com.car.carsharing.mycarsharing.model.AndroidOrder;
import com.car.carsharing.mycarsharing.model.stat.MyLocation;
import com.car.carsharing.mycarsharing.model.Tariff;
import com.car.carsharing.mycarsharing.model.stat.TariffStatic;
import com.car.carsharing.mycarsharing.server.ClientService;
import com.car.carsharing.mycarsharing.server.Server;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.car.carsharing.mycarsharing.model.stat.MyLocation.location;

public class RentActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener,DirectionCallback{

    public static final String TAG = "RENT";

    private TextView bViewAction;
    private TextView bViewTariff;
    private TextView bViewTime;
    private TextView bViewPrice;
    private TextView bViewNumber;
    private TextView bViewRent;
    private TextView bViewWait;
    private TextView bViewCar;
    private Polyline polyline;

    private LatLng latLng;
    private GoogleMap mMap;
    UiSettings uiSettings;
    private BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_marker);
    private Button buttonFinish;
    private Button buttonNext;

    private Client client;
    private AndroidCar car;
    private AndroidOrder androidOrder;
    private Tariff tariff;
    private List<Zone> zones;
    private int currentAct = 1;
    private boolean work = true;
    private ClientService clientService;
    private LatLng carPosition;
    private Marker carMarker;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rent);
        carPosition = getIntent().getParcelableExtra("carPosition");

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000,10,this);
        }

        bViewNumber = findViewById(R.id.bViewNumber);
        bViewRent = findViewById(R.id.bViewRent);
        bViewWait = findViewById(R.id.bViewWait);
        bViewTime = findViewById(R.id.bViewTime);
        bViewPrice = findViewById(R.id.bViewPrice);
        bViewCar = findViewById(R.id.bViewCar);
        bViewAction = findViewById(R.id.bViewAction);
        bViewTariff = findViewById(R.id.bViewTariff);

        buttonFinish = findViewById(R.id.buttonFinish);
        buttonNext = findViewById(R.id.buttonNext);

        car = CarStatic.car;
        tariff = TariffStatic.tariff;
        client = ClientStatic.client;

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.rentMap);
        mapFragment.getMapAsync(this);

        clientService = new Server(client.getToken()).clientService;
        updateText(currentAct);

        final Thread thread;
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (work){
                        new GetActual().doInBackground();
                        Thread.sleep(1000);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();

        nextButtonClick();
        finishButtonClick();

    }

    private void nextButtonClick(){
        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentAct == Action.BOOKING){

                    AlertDialog alertDialog = new AlertDialog.Builder(RentActivity.this).create();
                    alertDialog.setMessage("Начать аренду?");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Ок",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    clientService.rentCar(client.getId()).enqueue(new Callback<AndroidOrder>() {
                                        @SuppressLint("MissingPermission")
                                        @Override
                                        public void onResponse(Call<AndroidOrder> call, Response<AndroidOrder> response) {
                                            if (response.isSuccessful()){
                                                if (polyline!=null){
                                                    polyline.remove();
                                                }
                                                currentAct = Action.RENT;
                                                updateText(currentAct);
                                                mMap.animateCamera(CameraUpdateFactory.newLatLng(carPosition));
                                                uiSettings.setMyLocationButtonEnabled(false);
                                                mMap.setMyLocationEnabled(false);

                                                AlertDialog alertDialog = new AlertDialog.Builder(RentActivity.this).create();
                                                alertDialog.setMessage("Аренда начата");
                                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                                        new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                dialog.dismiss();
                                                            }
                                                        });
                                                alertDialog.show();

                                            }else {
                                                AlertDialog alertDialog = new AlertDialog.Builder(RentActivity.this).create();
                                                alertDialog.setMessage("Не удалось начать аренду");
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
                                        public void onFailure(Call<AndroidOrder> call, Throwable t) {
                                        }
                                    });

                                }
                            });
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Отмена",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();

                }else if (currentAct == Action.RENT){

                    AlertDialog alertDialog = new AlertDialog.Builder(RentActivity.this).create();
                    alertDialog.setMessage("Перейти в режим ожидания?");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ОК",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    clientService.waitCar(client.getId()).enqueue(new Callback<AndroidOrder>() {
                                        @Override
                                        public void onResponse(Call<AndroidOrder> call, Response<AndroidOrder> response) {
                                            if (response.isSuccessful()){

                                                currentAct = Action.WAITING;
                                                updateText(currentAct);

                                                AlertDialog alertDialog = new AlertDialog.Builder(RentActivity.this).create();
                                                alertDialog.setMessage("Включен режим ожидания");
                                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                                        new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                dialog.dismiss();
                                                            }
                                                        });
                                                alertDialog.show();
                                            }else {
                                                AlertDialog alertDialog = new AlertDialog.Builder(RentActivity.this).create();
                                                alertDialog.setMessage("Ошибка при включении режима ожидания");
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
                                        public void onFailure(Call<AndroidOrder> call, Throwable t) {

                                        }
                                    });
                                }
                            });
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Отмена",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();

                }else if (currentAct == Action.WAITING){
                    AlertDialog alertDialog = new AlertDialog.Builder(RentActivity.this).create();
                    alertDialog.setMessage("Перейти в режим аренды?");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ОК",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {

                                    clientService.rentCar(client.getId()).enqueue(new Callback<AndroidOrder>() {
                                        @Override
                                        public void onResponse(Call<AndroidOrder> call, Response<AndroidOrder> response) {
                                            if (response.isSuccessful()){

                                                currentAct = Action.RENT;
                                                updateText(currentAct);

                                                AlertDialog alertDialog = new AlertDialog.Builder(RentActivity.this).create();
                                                alertDialog.setMessage("Включен режим Аренды");
                                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                                        new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                dialog.dismiss();
                                                            }
                                                        });
                                                alertDialog.show();
                                            }else {
                                                AlertDialog alertDialog = new AlertDialog.Builder(RentActivity.this).create();
                                                alertDialog.setMessage("Ошибка при включении режима аренды");
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
                                        public void onFailure(Call<AndroidOrder> call, Throwable t) {

                                        }
                                    });
                                }
                            });
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Отмена",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
            }
        });

    }

    private void updateText(int action){
        if (action == Action.BOOKING){
            bViewCar.setText(car.getName());
            bViewNumber.setText(car.getNumber());
            final String rent = tariff.getPayRent()+" \u20BD/мин";
            bViewRent.setText(rent);
            String wait = tariff.getPayWaiting()+" \u20BD/мин";
            bViewWait.setText(wait);
            bViewAction.setText("Забронирован");
            bViewTariff.setText(tariff.getFreeBookingMin()+" мин бесплатно");
            bViewPrice.setText("");
            bViewTime.setText("");
        }else if (action == Action.RENT){
            bViewAction.setText("Режим аренды");
            bViewTariff.setText(tariff.getPayRent()+" \u20BD/мин");
            buttonFinish.setText("Завершить аренду");
            buttonNext.setText("Режим ожидания");
        }else if (action == Action.WAITING){
            bViewAction.setText("Режим ожидания");
            bViewTariff.setText(tariff.getPayWaiting()+" \u20BD/мин");
            buttonFinish.setText("Завершить аренду");
            buttonNext.setText("Режим аренды");
        }
    }

    private void finishButtonClick(){
        buttonFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (currentAct== Action.BOOKING){

                    AlertDialog alertDialog = new AlertDialog.Builder(RentActivity.this).create();
                    alertDialog.setMessage("Отменить бронирование?");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();

                                    clientService.stopBooking(client.getId()).enqueue(new Callback<AndroidOrder>() {

                                        @Override
                                        public void onResponse(Call<AndroidOrder> call, Response<AndroidOrder> response) {

                                            if (response.isSuccessful()){
                                                AndroidOrder order = response.body();
                                                work = false;

                                                if (order.getPrice()==0){
                                                    AlertDialog alertDialog = new AlertDialog.Builder(RentActivity.this).create();
                                                    alertDialog.setMessage("Бронирование отменено");
                                                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                                            new DialogInterface.OnClickListener() {
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    dialog.dismiss();
                                                                    Intent intent = new Intent(getApplicationContext(), NavActivity.class);
                                                                    intent.putExtra("client", client);
                                                                    ClientStatic.client = client;
                                                                    startActivity(intent);
                                                                    finish();
                                                                }
                                                            });
                                                    alertDialog.show();
                                                }else {
                                                    AlertDialog alertDialog = new AlertDialog.Builder(RentActivity.this).create();
                                                    alertDialog.setMessage("Бронирование отменено\n" +
                                                            "Превышено время бесплатного ожидания\n" +
                                                            "К оплате "+order.getPrice()+" \u20BD.");
                                                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                                            new DialogInterface.OnClickListener() {
                                                                public void onClick(DialogInterface dialog, int which) {
                                                                    dialog.dismiss();
                                                                    Intent intent = new Intent(getApplicationContext(), NavActivity.class);
                                                                    intent.putExtra("client", client);
                                                                    ClientStatic.client = client;
                                                                    startActivity(intent);
                                                                    finish();
                                                                }
                                                            });
                                                    alertDialog.show();
                                                }
                                            }
                                        }

                                        @Override
                                        public void onFailure(Call<AndroidOrder> call, Throwable t) {
                                            Log.d(TAG, "onFailure: " + t.getMessage());
                                            AlertDialog alertDialog = new AlertDialog.Builder(RentActivity.this).create();
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
                            });
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Отмена",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                    alertDialog.show();

                } else {

                    AlertDialog alertDialog = new AlertDialog.Builder(RentActivity.this).create();
                    alertDialog.setMessage("Завершить аренду?");
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    work = false;
                                    clientService.finishCar(client.getId()).enqueue(new Callback<AndroidOrder>() {
                                        @Override
                                        public void onResponse(Call<AndroidOrder> call, Response<AndroidOrder> response) {
                                            if (response.isSuccessful()){
                                                AndroidOrder order = response.body();
                                                AlertDialog alertDialog = new AlertDialog.Builder(RentActivity.this).create();
                                                alertDialog.setMessage("Аренда завершена\n"+"Время аренды "+order.getTime()+"\n" +
                                                        "К оплате "+order.getPrice()+" \u20BD.");
                                                alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                                        new DialogInterface.OnClickListener() {
                                                            public void onClick(DialogInterface dialog, int which) {
                                                                dialog.dismiss();
                                                                Intent intent = new Intent(getApplicationContext(), NavActivity.class);
                                                                intent.putExtra("client", client);
                                                                ClientStatic.client = client;
                                                                startActivity(intent);
                                                                finish();
                                                            }
                                                        });
                                                alertDialog.show();
                                            }else {
                                                AlertDialog alertDialog = new AlertDialog.Builder(RentActivity.this).create();
                                                alertDialog.setMessage("Не удалось завершить аренду");
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
                                        public void onFailure(Call<AndroidOrder> call, Throwable t) {

                                        }
                                    });
                                }
                            });
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Отмена",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                    alertDialog.show();
                }
            }
        });
    }

    private void setCameraWithCoordinationBounds(Route route) {
        LatLng southwest = route.getBound().getSouthwestCoordination().getCoordination();
        LatLng northeast = route.getBound().getNortheastCoordination().getCoordination();
        LatLngBounds bounds = new LatLngBounds(southwest, northeast);
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100));
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        drawZones();
        uiSettings = googleMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);
        uiSettings.setMapToolbarEnabled(false);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.title(car.getName());
        markerOptions.position(carPosition);
        markerOptions.icon(icon);

        carMarker = googleMap.addMarker(markerOptions);

        latLng = new LatLng(location.getLatitude(),location.getLongitude());
        googleMap.setMyLocationEnabled(true);
        uiSettings.setMyLocationButtonEnabled(true);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,14));
        requestDirection();
    }

    public void requestDirection() {
        GoogleDirection.withServerKey("AIzaSyBwtmbDM12G1kwlNetQU9fHw3_h7GbLIRQ")
                .from(new LatLng(MyLocation.location.getLatitude(),MyLocation.location.getLongitude()))
                .to(carPosition)
                .optimizeWaypoints(true)
                .language("ru")
                .transportMode(TransportMode.WALKING)
                .execute(new DirectionCallback() {
                    @Override
                    public void onDirectionSuccess(Direction direction, String rawBody) {
                        Log.d(TAG, "onDirectionSuccess: "+direction);
                        if(direction.isOK()) {
                            Route route = direction.getRouteList().get(0);
                            if (polyline!=null){
                                polyline.remove();
                            }

                            ArrayList<LatLng> directionPositionList = route.getLegList().get(0).getDirectionPoint();
                            polyline = mMap.addPolyline(DirectionConverter.createPolyline(getApplicationContext(), directionPositionList, 3, Color.RED));
                            setCameraWithCoordinationBounds(route);

                        } else {
                            // Do something
                        }
                    }

                    @Override
                    public void onDirectionFailure(Throwable t) {
                        // Do something
                    }
                });
    }

    @Override
    public void onLocationChanged(Location location) {
        MyLocation.location = location;
        if (currentAct==Action.BOOKING) {
            requestDirection();
        }
    }

    private void drawZones(){
        clientService.getZones().enqueue(new Callback<List<Zone>>() {
            @Override
            public void onResponse(Call<List<Zone>> call, Response<List<Zone>> response) {
                if (response.isSuccessful()){
                    zones = response.body();

                    if (zones!=null && mMap!=null) {
                        for (Zone z : zones) {
                            try {
                                JSONArray array = new JSONArray(z.getPolygon());
                                PolygonOptions polygonOptions = new PolygonOptions();
                                if (z.getType()==2) {
                                    polygonOptions.fillColor(Color.argb(25, 0, 0, 255));
                                    polygonOptions.strokeWidth(0);
                                }else if (z.getType()==1){
                                    polygonOptions.fillColor(Color.argb(25, 255, 0, 0));
                                    polygonOptions.strokeWidth(0);
                                }

                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject object = array.getJSONObject(i);
                                    LatLng latLng = new LatLng(object.getDouble("lat"), object.getDouble("lng"));
                                    polygonOptions.add(latLng);
                                }


                                mMap.addPolygon(polygonOptions);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Zone>> call, Throwable t) {

            }
        });
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }
    @Override
    public void onProviderEnabled(String s) {
    }
    @Override
    public void onProviderDisabled(String s) {
    }
    @Override
    public void onDirectionSuccess(Direction direction, String rawBody) {
    }
    @Override
    public void onDirectionFailure(Throwable t) {
    }

    @SuppressLint("StaticFieldLeak")
    class GetActual extends AsyncTask<Void, Void, Void>{

        @Override
        protected Void doInBackground(Void... voids) {
            clientService.getActual(client.getId()).enqueue(new Callback<AndroidOrder>() {
                @Override
                public void onResponse(Call<AndroidOrder> call, Response<AndroidOrder> response) {
                    if (response.isSuccessful()){
                        androidOrder = response.body();
                        currentAct = androidOrder.getCurrentAction();
                        bViewTime.setText(androidOrder.getTime());
                        bViewPrice.setText(Float.toString(androidOrder.getPrice())+" \u20BD");
                        carPosition = new LatLng(androidOrder.getAndroidCar().getLat(),androidOrder.getAndroidCar().getLon());
                        carMarker.setPosition(carPosition);
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(carPosition));
                    }
                }

                @Override
                public void onFailure(Call<AndroidOrder> call, Throwable t) {

                }
            });
            return null;
        }
    }
}
