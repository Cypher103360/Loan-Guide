package com.instantloanguide.loanguideadmin.services;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiWebServices {

    private static final String base_url = "https://gedgetsworld.in/Loan_App/";
    public static Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(base_url)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    public static ApiInterface getApiInterface() {
        return retrofit.create(ApiInterface.class);
    }
}
