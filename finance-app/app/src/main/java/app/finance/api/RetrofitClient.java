package app.finance.api;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    /**
     * TO MAKE IT WORK FOR EVERYONE:
     * 1. Deploy your backend to Render.com or Railway.app
     * 2. Replace the URL below with your public "onrender.com" link
     */
    private static final String BASE_URL = "http://YOUR_CLOUD_URL_HERE.onrender.com/api/";

    private static Retrofit retrofit = null;

    public static FinanceApiService getApiService() {
        if (retrofit == null) {
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit.create(FinanceApiService.class);
    }
}
