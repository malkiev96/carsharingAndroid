package com.car.carsharing.mycarsharing.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class Client implements Parcelable{

    public Client(){

    }

    @SerializedName("id")
    private int id;
    @SerializedName("firstname")
    private String firstname;
    @SerializedName("secondname")
    private String secondname;
    @SerializedName("middlename")
    private String middlename;
    @SerializedName("mail")
    private String mail;
    @SerializedName("telephone")
    private String telephone;
    @SerializedName("activated")
    private boolean activated;
    @SerializedName("enabled")
    private boolean enabled;
    @SerializedName("token")
    private String token;


    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getSecondname() {
        return secondname;
    }

    public void setSecondname(String secondname) {
        this.secondname = secondname;
    }

    public String getMiddlename() {
        return middlename;
    }

    public void setMiddlename(String middlename) {
        this.middlename = middlename;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public boolean isActivated() {
        return activated;
    }

    public void setActivated(boolean activated) {
        this.activated = activated;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return "Client{" +
                "id=" + id +
                ", firstname='" + firstname + '\'' +
                ", secondname='" + secondname + '\'' +
                ", middlename='" + middlename + '\'' +
                ", mail='" + mail + '\'' +
                ", telephone='" + telephone + '\'' +
                ", activated=" + activated +
                ", enabled=" + enabled +
                ", token=" + token +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(secondname);
        parcel.writeString(firstname);
        parcel.writeString(middlename);
        parcel.writeString(mail);
        parcel.writeString(telephone);
        parcel.writeString(token);
        parcel.writeByte((byte) (activated ? 1 : 0));
        parcel.writeByte((byte) (enabled ? 1 : 0));
    }

    public static final Parcelable.Creator<Client> CREATOR = new Parcelable.Creator<Client>() {
        // распаковываем объект из Parcel
        public Client createFromParcel(Parcel in) {
            return new Client(in);
        }

        public Client[] newArray(int size) {
            return new Client[size];
        }
    };

    // конструктор, считывающий данные из Parcel
    private Client(Parcel parcel) {
        id = parcel.readInt();
        secondname = parcel.readString();
        firstname = parcel.readString();
        middlename = parcel.readString();
        mail = parcel.readString();
        telephone = parcel.readString();
        token = parcel.readString();
        activated = parcel.readByte() != 0;
        enabled = parcel.readByte() != 0;
    }
}
