package com.car.carsharing.mycarsharing.model;


import com.google.gson.annotations.SerializedName;

public class ClientReg {

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
    @SerializedName("password")
    private String password;
    @SerializedName("imageByte1")
    private String imageByte1;
    @SerializedName("imageByte2")
    private String imageByte2;
    @SerializedName("imageByte3")
    private String imageByte3;
    @SerializedName("imageByte4")
    private String imageByte4;
    @SerializedName("imageByte5")
    private String imageByte5;

    public String getImageByte1() {
        return imageByte1;
    }

    public void setImageByte1(String imageByte1) {
        this.imageByte1 = imageByte1;
    }

    public String getImageByte2() {
        return imageByte2;
    }

    public void setImageByte2(String imageByte2) {
        this.imageByte2 = imageByte2;
    }

    public String getImageByte3() {
        return imageByte3;
    }

    public void setImageByte3(String imageByte3) {
        this.imageByte3 = imageByte3;
    }

    public String getImageByte4() {
        return imageByte4;
    }

    public void setImageByte4(String imageByte4) {
        this.imageByte4 = imageByte4;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getImageByte5() {
        return imageByte5;
    }

    public void setImageByte5(String imageByte5) {
        this.imageByte5 = imageByte5;
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

}
