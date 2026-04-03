package app.finance.api;

import app.finance.models.ApiResponse;
import app.finance.models.Record;
import app.finance.models.Summary;
import app.finance.models.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface FinanceApiService {
    @POST("auth/login")
    Call<ApiResponse<User>> login(@Body User loginRequest);

    @PUT("auth/change-password")
    Call<ApiResponse<Void>> changePassword(
            @Header("X-User-Id") String userId,
            @Body java.util.Map<String, String> body
    );

    @GET("records/summary")
    Call<ApiResponse<Summary>> getSummary(
            @Header("X-User-Id") String userId,
            @Query("targetUserId") String targetId,
            @Query("type") String type
    );

    @DELETE("records/{id}")
    Call<ApiResponse<Void>> deleteRecord(
            @Header("X-User-Id") String userId,
            @Path("id") int id
    );

    @GET("records")
    Call<ApiResponse<List<Record>>> getRecords(
            @Header("X-User-Id") String userId,
            @Query("targetUserId") String targetId,
            @Query("category") String category,
            @Query("type") String type
    );

    @POST("records")
    Call<ApiResponse<Record>> addRecord(
            @Header("X-User-Id") String userId,
            @Body Record record
    );

    @PUT("records/{id}")
    Call<ApiResponse<Record>> updateRecord(
            @Header("X-User-Id") String userId,
            @Path("id") int id,
            @Body Record record
    );

    @GET("users")
    Call<ApiResponse<List<User>>> getUsers(@Header("X-User-Id") String userId);

    @POST("users")
    Call<ApiResponse<User>> createUser(
            @Header("X-User-Id") String userId,
            @Body User user
    );

    @PUT("users/{id}")
    Call<ApiResponse<User>> updateUser(
            @Header("X-User-Id") String userId,
            @Path("id") int targetId,
            @Body User user
    );

    @GET("search")
    Call<ApiResponse<User>> searchUser(
            @Header("X-User-Id") String userId,
            @Query("name") String name,
            @Query("email") String email
    );
}