package com.car.carsharing.mycarsharing.fragments;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.car.carsharing.mycarsharing.R;
import com.car.carsharing.mycarsharing.RentActivity;
import com.car.carsharing.mycarsharing.model.AndroidCar;
import com.car.carsharing.mycarsharing.model.AndroidOrder;
import com.car.carsharing.mycarsharing.model.stat.CarStatic;
import com.car.carsharing.mycarsharing.model.Client;
import com.car.carsharing.mycarsharing.model.stat.ClientStatic;
import com.car.carsharing.mycarsharing.model.OrderPay;
import com.car.carsharing.mycarsharing.model.Tariff;
import com.car.carsharing.mycarsharing.model.stat.TariffStatic;
import com.car.carsharing.mycarsharing.model.Zone;
import com.car.carsharing.mycarsharing.server.Server;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.car.carsharing.mycarsharing.model.stat.MyLocation.location;

public class MapFragment extends Fragment implements OnMapReadyCallback{

    private static final String TAG = "MapFragment";
    private Client client;

    private OnFragmentInteractionListener mListener;
    private MapView mMapView;
    private List<AndroidCar> carList;
    private List<Zone> zones;
    private Marker activeMarker;

    private static final String ARG_PARAM1 = "id";
    private static final String ARG_PARAM2 = "hash";

    private String hash;

    private LinearLayout carInfoLayout;
    TextView carInfoModel;
    TextView carInfoFuel;
    TextView carInfoNumber;
    TextView carInfoTariff;
    Button carInfoBooking;
    TextView tariffInfoRent;
    TextView tariffInfoWait;


    public static MapFragment newInstance(int param1, String param2) {
        MapFragment fragment = new MapFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public MapFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            int id = getArguments().getInt(ARG_PARAM1);
            hash = getArguments().getString(ARG_PARAM2);
        }

        client = ClientStatic.client;
        new Server(hash).clientService.getCars().enqueue(new Callback<List<AndroidCar>>() {
            @Override
            public void onResponse(Call<List<AndroidCar>> call, Response<List<AndroidCar>> response) {
                Log.d(TAG, "onResponse: "+response.code());
                carList = response.body();
            }

            @Override
            public void onFailure(Call<List<AndroidCar>> call, Throwable t) {
                Log.d(TAG, "onFailure: ");
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                zones = getZones();
                System.out.println(zones.size()+"ZONE");

            }
        }).start();

    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_map, container, false);
        // Inflate the layout for this fragment
        mMapView = rootView.findViewById(R.id.map);

        carInfoModel = rootView.findViewById(R.id.carInfoModel);
        carInfoNumber = rootView.findViewById(R.id.carInfoNumber);
        carInfoFuel = rootView.findViewById(R.id.carInfoFuel);
        carInfoBooking = rootView.findViewById(R.id.carInfoBooking);
        carInfoTariff = rootView.findViewById(R.id.carInfoTariff);
        tariffInfoRent = rootView.findViewById(R.id.tariffInfoRent);
        tariffInfoWait = rootView.findViewById(R.id.tariffInfoWait);

        carInfoLayout = rootView.findViewById(R.id.carInfoLayout);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately


        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(this);

        carInfoBooking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                new Server(hash).clientService.getPay(client.getId()).enqueue(new Callback<OrderPay>() {
                    @Override
                    public void onResponse(Call<OrderPay> call, Response<OrderPay> response) {
                        if (response.code()==200){
                            //Нужна оплата
                            AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                            alertDialog.setMessage("У вас 1 неоплаченная поездка, перейти к оплате?");
                            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ДА",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            OrderFragment orderFragment = new OrderFragment();
                                            getActivity().getFragmentManager().beginTransaction().replace(R.id.contentLayout,orderFragment).commit();
                                            dialog.dismiss();
                                        }
                                    });
                            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "НЕТ",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            dialog.dismiss();
                                        }
                                    });
                            alertDialog.show();
                        }else {
                            String number = carInfoNumber.getText().toString();
                            new Server(hash).clientService.testBooking(client.getId(),number).enqueue(new Callback<AndroidCar>() {
                                @Override
                                public void onResponse(final Call<AndroidCar> call, final Response<AndroidCar> response) {
                                    Log.d(TAG, "onResponse: "+response.code());
                                    if (response.code()==200){
                                        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                                        final Tariff tariff = TariffStatic.tariff;
                                        alertDialog.setMessage(tariff.getFreeBookingMin()+
                                                " минут бесплатно, далее "+tariff.getPayBooking()+
                                                " руб/мин. \nВыполнить бронирование?");

                                        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "ДА",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        ClientStatic.client = client;
                                                        CarStatic.car = response.body();
                                                        new Server(hash).clientService.bookCar(client.getId(), CarStatic.car.getId()).enqueue(new Callback<AndroidOrder>() {
                                                            @Override
                                                            public void onResponse(Call<AndroidOrder> call, Response<AndroidOrder> response) {
                                                                if (response.isSuccessful()){
                                                                    Intent intent = new Intent(getContext(),RentActivity.class);
                                                                    intent.putExtra("client",client);
                                                                    intent.putExtra("carPosition",activeMarker.getPosition());
                                                                    getActivity().finish();
                                                                    startActivity(intent);
                                                                }
                                                            }

                                                            @Override
                                                            public void onFailure(Call<AndroidOrder> call, Throwable t) {

                                                            }
                                                        });
                                                        dialog.dismiss();
                                                    }
                                                });
                                        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "НЕТ",
                                                new DialogInterface.OnClickListener() {
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        dialog.dismiss();
                                                    }
                                                });
                                        alertDialog.show();
                                    }else {
                                        AlertDialog alertDialog = new AlertDialog.Builder(getContext()).create();
                                        alertDialog.setTitle("Ошибка");
                                        alertDialog.setMessage("Автомобиль не может быть забронирован");
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
                                public void onFailure(Call<AndroidCar> call, Throwable t) {
                                    Log.d(TAG, "onFailure: "+t.getMessage());
                                }
                            });
                        }
                    }

                    @Override
                    public void onFailure(Call<OrderPay> call, Throwable t) {

                    }
                });

            }
        });

        return rootView;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }


    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(final GoogleMap googleMap) {

        UiSettings uiSettings = googleMap.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);

        final BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.ic_marker);

        if (carList !=null) {

            for (AndroidCar car : carList) {
                googleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(car.getLat(), car.getLon()))
                        .title(car.getName())
                        .icon(icon)
                        .snippet(car.getNumber()));
            }

            if (zones!=null) {
                for (Zone z : zones) {
                    if (z.getType() == 2) {
                        try {
                            JSONArray array = new JSONArray(z.getPolygon());
                            PolygonOptions polygonOptions = new PolygonOptions();
                            polygonOptions.fillColor(Color.argb(155,167,175,188 ));
                            polygonOptions.strokeWidth(0);

                            for (int i = 0; i < array.length(); i++) {
                                JSONObject object = array.getJSONObject(i);
                                LatLng latLng = new LatLng(object.getDouble("lat"), object.getDouble("lng"));
                                polygonOptions.add(latLng);
                            }


                            googleMap.addPolygon(polygonOptions);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        LatLng latLng;

        if(location!=null){
            latLng = new LatLng(location.getLatitude(),location.getLongitude());
            googleMap.setMyLocationEnabled(true);
            uiSettings.setMyLocationButtonEnabled(true);
        }else {
            latLng = new LatLng(56.827221,60.602596);
        }
        uiSettings.setMapToolbarEnabled(false);
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,14));


        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                AndroidCar car = getByMarker(marker);
                if (car!=null) {
                    Tariff tariff = car.getTariff();
                    if (tariff!=null) {
                        TariffStatic.tariff = tariff;
                        carInfoModel.setText(car.getName());
                        carInfoNumber.setText(car.getNumber());
                        carInfoTariff.setText(tariff.getName());
                        String f = car.getFuelLevel() + " %";
                        carInfoFuel.setText(f);
                        f = tariff.getPayRent()+" руб/мин";
                        tariffInfoRent.setText(f);
                        f = tariff.getPayWaiting()+" руб/мин";
                        tariffInfoWait.setText(f);
                        carInfoLayout.setVisibility(View.VISIBLE);
                        activeMarker = marker;
                    }
                }
                return false;
            }
        });
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                carInfoLayout.setVisibility(View.INVISIBLE);
            }
        });


    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    private List<AndroidCar> getCarList(){
        try {
            return new Server(hash).clientService.getCars().execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private AndroidCar getByMarker(Marker marker){
        for (AndroidCar car: carList){
            if (Objects.equals(car.getNumber(), marker.getSnippet())){
                return car;
            }
        }
        return null;
    }


    private List<Zone> getZones(){
        try {
            return new Server(hash).clientService.getZones().execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

}
