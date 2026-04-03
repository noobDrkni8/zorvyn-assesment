package app.finance.api;

import android.util.Log;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String TAG = "RetrofitClient";
    private static final String PROD_URL = "https://zorvyn-assesment.onrender.com/api/";
    private static final String LOCAL_URL = "http://10.0.2.2:3000/api/"; 
    
    private static Retrofit retrofit = null;
    private static final AtomicBoolean isLocal = new AtomicBoolean(false);

    public static FinanceApiService getApiService() {
        if (retrofit == null) {
            checkServerAvailability();

            String finalUrl = isLocal.get() ? LOCAL_URL : PROD_URL;
            
            // This Log will show up in your Android Studio 'Logcat' tab
            Log.d(TAG, "Connecting to Environment: " + (isLocal.get() ? "LOCAL (Fast)" : "CLOUD (Global)"));
            Log.d(TAG, "Base URL: " + finalUrl);

            OkHttpClient okHttpClient = new OkHttpClient.Builder()
                    .retryOnConnectionFailure(true)
                    .build();

            Gson gson = new GsonBuilder().setLenient().create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(finalUrl)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
        }
        return retrofit.create(FinanceApiService.class);
    }

    private static void checkServerAvailability() {
        Thread thread = new Thread(() -> {
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress("10.0.2.2", 3000), 400);
                isLocal.set(true);
            } catch (IOException e) {
                isLocal.set(false);
            }
        });
        thread.start();
        try {
            thread.join(500);
        } catch (InterruptedException ignored) {}
    }
}
