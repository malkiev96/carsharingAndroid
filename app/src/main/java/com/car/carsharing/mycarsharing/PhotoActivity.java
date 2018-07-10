package com.car.carsharing.mycarsharing;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.car.carsharing.mycarsharing.model.Client;
import com.car.carsharing.mycarsharing.model.ClientReg;
import com.car.carsharing.mycarsharing.server.ClientService;
import com.car.carsharing.mycarsharing.server.Server;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PhotoActivity extends AppCompatActivity {

    private static final String TAG = "Photo";
    private Button imageButton1;
    private Button imageButton2;
    private Button imageButton3;
    private Button imageButton4;
    private Button imageButton5;
    private Bitmap image1;
    private Bitmap image2;
    private Bitmap image3;
    private Bitmap image4;
    private Bitmap image5;
    ClientService clientService;
    private String phone;
    private String mail;
    private String password;
    private String firstname;
    private String secondname;
    private String middlename;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo);
        setTitle("Загрузка фотографий");
        phone = getIntent().getStringExtra("phone");
        mail = getIntent().getStringExtra("mail");
        password = getIntent().getStringExtra("password");
        firstname = getIntent().getStringExtra("firstname");
        secondname = getIntent().getStringExtra("secondname");
        middlename = getIntent().getStringExtra("middlename");


        imageButton1 = findViewById(R.id.imageButton1);
        imageButton2 = findViewById(R.id.imageButton2);
        imageButton3 = findViewById(R.id.imageButton3);
        imageButton4 = findViewById(R.id.imageButton4);
        imageButton5 = findViewById(R.id.imageButton5);

        Button buttonSentPhoto = findViewById(R.id.buttonSentPhoto);

        clientService = new Server("").clientService;

        imageButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isStoragePermissionGranted()){
                    openImageWindow(1);
                }
            }
        });

        imageButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isStoragePermissionGranted()){
                    openImageWindow(2);
                }
            }
        });

        imageButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isStoragePermissionGranted()){
                    openImageWindow(3);
                }
            }
        });

        imageButton4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isStoragePermissionGranted()){
                    openImageWindow(4);
                }
            }
        });

        imageButton5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isStoragePermissionGranted()){
                    openImageWindow(5);
                }
            }
        });

        buttonSentPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                if (image1!=null && image2!=null && image3!=null && image4!=null && image5!=null){
                    ClientReg clientReg = new ClientReg();
                    clientReg.setPassword(password);
                    clientReg.setMail(mail);
                    clientReg.setTelephone(phone);
                    clientReg.setSecondname(secondname);
                    clientReg.setFirstname(firstname);
                    clientReg.setMiddlename(middlename);
                    clientReg.setImageByte1(bitmapToByteArray(image1));
                    clientReg.setImageByte2(bitmapToByteArray(image2));
                    clientReg.setImageByte3(bitmapToByteArray(image3));
                    clientReg.setImageByte4(bitmapToByteArray(image4));
                    clientReg.setImageByte5(bitmapToByteArray(image5));
                    System.out.println("1"+clientReg.toString());

                    Call<Client> call = clientService.regClient(clientReg);

                    call.enqueue(new Callback<Client>() {
                        @Override
                        public void onResponse(Call<Client> call, Response<Client> response) {
                            Log.d(TAG, "onResponse: "+response.code());
                            if (response.code()==200){

                                final Client client = response.body();

                                if (client!=null) {
                                    AlertDialog alertDialog = new AlertDialog.Builder(PhotoActivity.this).create();
                                    alertDialog.setMessage("Регистрация прошла успешно, необходимо дождаться проверки аккаунта");
                                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int which) {
                                                    Intent intent = new Intent(view.getContext(), LoginActivity.class);
                                                   /* intent.putExtra("id", client.getId());
                                                    intent.putExtra("hash", client.getToken());
                                                    intent.putExtra("client",client);
                                                    SharedPreferences mSettings = getSharedPreferences(LoginActivity.APP_PREFERENCES, Context.MODE_PRIVATE);
                                                    mSettings.edit().putString(LoginActivity.APP_PREFERENCES_TOKEN, client.getToken()).apply();
                                                    mSettings.edit().putInt(LoginActivity.APP_PREFERENCES_ID, client.getId()).apply();*/
                                                    startActivity(intent);
                                                    finish();
                                                }
                                            });
                                    alertDialog.show();



                                }

                            }else {
                                AlertDialog alertDialog = new AlertDialog.Builder(PhotoActivity.this).create();
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
                        }

                        @Override
                        public void onFailure(Call<Client> call, Throwable t) {
                            AlertDialog alertDialog = new AlertDialog.Builder(PhotoActivity.this).create();
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
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.v(TAG,"Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
            //openImageWindow(5);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 1:
                if (resultCode == RESULT_OK){
                    try {
                        image1 = getImage(imageButton1,data);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case 2:
                if (resultCode == RESULT_OK){
                    try {
                        image2 = getImage(imageButton2,data);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case 3:
                if (resultCode == RESULT_OK){
                    try {
                        image3 = getImage(imageButton3,data);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case 4:
                if (resultCode == RESULT_OK){
                    try {
                        image4 = getImage(imageButton4,data);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case 5:
                if (resultCode == RESULT_OK){
                    try {
                        image5 = getImage(imageButton5,data);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private Bitmap getImage(Button button, Intent data) throws FileNotFoundException{
        //Получаем URI изображения, преобразуем его в Bitmap
        final Uri imageUri = data.getData();
        final InputStream imageStream = getContentResolver().openInputStream(imageUri);
        Bitmap selectedImage = BitmapFactory.decodeStream(imageStream);
        button.setBackgroundTintList(getApplicationContext().getColorStateList(R.color.wallet_bright_foreground_disabled_holo_light));
        return selectedImage;
    }

    private String bitmapToByteArray(Bitmap bitmap){
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,stream);
        byte[] byteArray = stream.toByteArray();

        return android.util.Base64.encodeToString(byteArray, android.util.Base64.DEFAULT);
    }

    private void openImageWindow(int requestCode){
        Intent photoPickerIntent = new Intent(Intent.ACTION_PICK);
        photoPickerIntent.setType("image/*");
        startActivityForResult(photoPickerIntent,requestCode);
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {
                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }
}
