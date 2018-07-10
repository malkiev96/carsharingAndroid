package com.car.carsharing.mycarsharing;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.car.carsharing.mycarsharing.fragments.AboutFragment;
import com.car.carsharing.mycarsharing.fragments.HelpFragment;
import com.car.carsharing.mycarsharing.fragments.MapFragment;
import com.car.carsharing.mycarsharing.fragments.OrderFragment;
import com.car.carsharing.mycarsharing.fragments.ProfileFragment;
import com.car.carsharing.mycarsharing.listener.MyLocationListener;
import com.car.carsharing.mycarsharing.model.Client;
import com.car.carsharing.mycarsharing.model.stat.ClientStatic;
import com.car.carsharing.mycarsharing.model.stat.MyLocation;
import com.car.carsharing.mycarsharing.model.stat.OrderPayStatic;
import com.car.carsharing.mycarsharing.model.PayInfo;
import com.car.carsharing.mycarsharing.server.Server;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.wallet.AutoResolveHelper;
import com.google.android.gms.wallet.PaymentData;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NavActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LocationListener {

    MapFragment mapFragment;
    ProfileFragment profileFragment;
    HelpFragment helpFragment;
    AboutFragment aboutFragment;
    OrderFragment orderFragment;
    private Client client;
    private TextView navHeaderText;
    private static final int INITIAL_REQUEST = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav);
        setTitle("Карта");


        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new MyLocationListener();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(LOCATION_PERMS, INITIAL_REQUEST);
        }else {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    5000,
                    10,
                    locationListener); // здесь можно указать другие более подходящие вам параметры

            MyLocation.location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        }


        client = getIntent().getParcelableExtra("client");
        ClientStatic.client = client;

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mapFragment = new MapFragment();
        profileFragment = new ProfileFragment();
        helpFragment = new HelpFragment();
        aboutFragment = new AboutFragment();
        orderFragment = new OrderFragment();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        View headerView = navigationView.getHeaderView(0);
        navigationView.setNavigationItemSelectedListener(this);

        navHeaderText = headerView.findViewById(R.id.navHeaderText);

        if (client.isEnabled() && client.isActivated()) {
            String fio = client.getSecondname() + " " + client.getFirstname() + " " + client.getMiddlename();
            navHeaderText.setText(fio);
        } else if (client.isEnabled() && !client.isActivated()) {
            navHeaderText.setText("Аккаунт ожидает подтверждения");
        }

        FragmentTransaction ftrans = getFragmentManager().beginTransaction();
        MapFragment.newInstance(client.getId(),client.getToken());
        ftrans.replace(R.id.contentLayout,mapFragment);
        ftrans.commit();

    }

    private boolean canAccessLocation() {
        return(hasPermission(Manifest.permission.ACCESS_FINE_LOCATION));
    }

    private boolean hasPermission(String perm) {
        return(PackageManager.PERMISSION_GRANTED==checkSelfPermission(perm));
    }

    private static final String[] LOCATION_PERMS={
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch(requestCode) {
            case INITIAL_REQUEST:
                if (canAccessLocation()) {

                }
                else {
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        PaymentData paymentData = PaymentData.getFromIntent(data);
                        String token = paymentData.getPaymentMethodToken().getToken();

                        PayInfo payInfo = new PayInfo();
                        payInfo.setOrderId(OrderPayStatic.orderPay.getOrderId());
                        payInfo.setToken(token);
                        payInfo.setPrice(OrderPayStatic.orderPay.getPrice());



                        new Server(client.getToken()).clientService.makePay(payInfo).enqueue(new Callback<ResponseBody>() {
                            @Override
                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                System.out.println(response.code());
                                if (response.isSuccessful()){

                                    AlertDialog alertDialog = new AlertDialog.Builder(NavActivity.this).create();
                                    alertDialog.setMessage("Оплата совершена успешно");
                                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    dialog.dismiss();
                                                }
                                            });
                                    alertDialog.show();

                                    FragmentTransaction ftrans = getFragmentManager().beginTransaction();
                                    ftrans.replace(R.id.contentLayout,new MapFragment());
                                    ftrans.commit();
                                }
                            }

                            @Override
                            public void onFailure(Call<ResponseBody> call, Throwable t) {

                            }
                        });


                        break;
                    case Activity.RESULT_CANCELED:
                        System.out.println("CANC");
                        break;
                    case AutoResolveHelper.RESULT_ERROR:
                        Status status = AutoResolveHelper.getStatusFromIntent(data);
                        System.out.println(status+"ERR");
                        // Log the status for debugging.
                        // Generally, there is no need to show an error to
                        // the user as the Google Pay API will do that.
                        break;
                    default:
                        // Do nothing.
                }
                break;
            default:
                // Do nothing.
        }
    }


    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        FragmentTransaction ftrans = getFragmentManager().beginTransaction();

        if (id == R.id.nav_map) {
            setTitle("Карта");
            MapFragment.newInstance(client.getId(),client.getToken());
            ftrans.replace(R.id.contentLayout,mapFragment);
        }else if (id == R.id.nav_profile){
            setTitle("Профиль");
            ftrans.replace(R.id.contentLayout,profileFragment);
        }else if (id == R.id.nav_help){
            setTitle("Помощь");
            ftrans.replace(R.id.contentLayout,helpFragment);
        }else if (id == R.id.nav_about){
            setTitle("О программе");
            ftrans.replace(R.id.contentLayout,aboutFragment);
        }else if (id == R.id.nav_exit){
            SharedPreferences mSettings = getSharedPreferences(LoginActivity.APP_PREFERENCES,Context.MODE_PRIVATE);
            mSettings.edit().clear().apply();
            Intent intent = new Intent(getApplicationContext(),LoginActivity.class);
            startActivity(intent);
            finish();
        }else if (id == R.id.nav_order){
            setTitle("Аренды");
            ftrans.replace(R.id.contentLayout,orderFragment);
        }
        ftrans.commit();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        MyLocation.location = location;
    }
    @Override
    public void onProviderDisabled(String provider) {}
    @Override
    public void onProviderEnabled(String provider) {}
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}



}
