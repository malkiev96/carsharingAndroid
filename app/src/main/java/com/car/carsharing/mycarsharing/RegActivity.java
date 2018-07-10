package com.car.carsharing.mycarsharing;

import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;

import com.car.carsharing.mycarsharing.model.Client;
import com.car.carsharing.mycarsharing.model.ClientReg;
import com.car.carsharing.mycarsharing.server.ClientService;
import com.car.carsharing.mycarsharing.server.Server;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegActivity extends AppCompatActivity {

    private static final String TAG = "Reg";
    private ClientService clientService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg);
        setTitle("Регистрация");
        clientService = new Server("").clientService;
        Button buttonReg = findViewById(R.id.buttonRegSubmit);

        buttonReg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                final TextInputEditText firstname = findViewById(R.id.inputName);
                final TextInputEditText secondname = findViewById(R.id.inputSurname);
                final TextInputEditText middlename = findViewById(R.id.inputMiddlename);
                final TextInputEditText phone = findViewById(R.id.inputPhone);
                final TextInputEditText mail = findViewById(R.id.inputMail);
                final TextInputEditText pass = findViewById(R.id.inputPass);
                TextInputEditText pass2 = findViewById(R.id.inputPass2);
                CheckBox checkAge = findViewById(R.id.checkAge);
                CheckBox checkDrive = findViewById(R.id.checkDrive);

                if (!checkTelNumber(phone.getText().toString())){
                    phone.setError("Неверный номер телефона");
                } else if (!checkMail(mail.getText().toString())){
                    mail.setError("Неверный формат E-Mail");
                } else if (!checkPassword(pass.getText().toString())){
                    pass.setError("Неверный формат пароля");
                } else if (!pass.getText().toString().equals(pass2.getText().toString())){
                    pass2.setError("Пароль не совпадает");
                } else if (!checkAge.isChecked()){
                    checkAge.setError("Ошибка");
                } else if (!checkDrive.isChecked()){
                    checkDrive.setError("Ошибка");
                } else {

                    final ClientReg clientReg = new ClientReg();
                    clientReg.setTelephone(phone.getText().toString());
                    clientReg.setMail(mail.getText().toString());
                    clientReg.setPassword(pass.getText().toString());
                    clientReg.setMiddlename(middlename.getText().toString());
                    clientReg.setFirstname(firstname.getText().toString());
                    clientReg.setSecondname(secondname.getText().toString());


                    Call<ResponseBody> call = clientService.validClient(clientReg);
                    call.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.code()==200){
                                AlertDialog alertDialog = new AlertDialog.Builder(RegActivity.this).create();
                                alertDialog.setTitle("Регистрация");
                                alertDialog.setMessage("Для продолжения регистрации необходимо отправить фотографии документов");
                                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                Intent intent = new Intent(getApplicationContext(),PhotoActivity.class);
                                                intent.putExtra("phone",phone.getText().toString());
                                                intent.putExtra("mail",mail.getText().toString());
                                                intent.putExtra("password",pass.getText().toString());
                                                intent.putExtra("middlename",middlename.getText().toString());
                                                intent.putExtra("firstname",firstname.getText().toString());
                                                intent.putExtra("secondname",secondname.getText().toString());
                                                System.out.println(clientReg);
                                                startActivity(intent);
                                                finish();
                                            }
                                        });
                                alertDialog.show();

                            }else {
                                AlertDialog alertDialog = new AlertDialog.Builder(RegActivity.this).create();
                                alertDialog.setTitle("Ошибка");
                                alertDialog.setMessage("Введенный телефон или E-Mail уже используются");
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
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            AlertDialog alertDialog = new AlertDialog.Builder(RegActivity.this).create();
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


    //Минимум 6 символов
    private boolean checkPassword(String testString){
        Pattern p = Pattern.compile("^(?=.*[a-z])[a-zA-Z\\d]{6,25}$");
        Matcher m = p.matcher(testString);
        return m.matches();
    }

    private boolean checkTelNumber(String testString) {
        Pattern p = Pattern.compile("^((8|\\+7)[\\- ]?)?(\\(?\\d{3}\\)?[\\- ]?)?[\\d\\- ]{7,10}$");
        Matcher m = p.matcher(testString);
        return m.matches();
    }

    private boolean checkMail(String testString) {
        Pattern p = Pattern.compile("^[-\\w.]+@([A-z0-9][-A-z0-9]+\\.)+[A-z]{2,4}$");
        Matcher m = p.matcher(testString);
        return m.matches();
    }

    
}
