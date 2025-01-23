package com.example.locationmonitoring.model;

import android.os.Bundle;

public class addDriverHelper {

    String name,plate,body,QrCode,Operator,Address,Contact;

    // Default constructor for Firebase deserialization
    public addDriverHelper() {
    }
    public addDriverHelper(String name, String plate, String body, String qrCode, String operator, String address, String contact) {
        this.name = name;
        this.plate = plate;
        this.body = body;
        this.QrCode = qrCode;
        this.Operator = operator;
        this.Address = address;
        this.Contact = contact;
    }

    public String getOperator() {
        return Operator;
    }

    public void setOperator(String operator) {
        Operator = operator;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getContact() {
        return Contact;
    }

    public void setContact(String contact) {
        Contact = contact;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getQrCode() {
        return QrCode;
    }

    public void setQrCode(String qrCode) {
        QrCode = qrCode;
    }

    public void setValuesFromBundle(Bundle bundle) {
        if (bundle != null) {
            this.name = bundle.getString("name");
            this.plate = bundle.getString("plate");
            this.body = bundle.getString("body");
            this.QrCode = bundle.getString("qrCode");
            this.Operator = bundle.getString("operator");
            this.Address = bundle.getString("address");
            this.Contact = bundle.getString("contact");
        }
    }

}
