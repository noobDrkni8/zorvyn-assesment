package app.finance.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import app.finance.api.FinanceApiService;
import app.finance.api.RetrofitClient;
import app.finance.models.ApiResponse;
import app.finance.models.Record;
import app.finance.models.Summary;
import app.finance.models.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FinanceRepository {
    private final FinanceApiService apiService;

    public FinanceRepository() {
        this.apiService = RetrofitClient.getApiService();
    }

    public LiveData<ApiResponse<User>> login(User user) {
        MutableLiveData<ApiResponse<User>> data = new MutableLiveData<>();
        apiService.login(user).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful()) {
                    data.setValue(response.body());
                } else {
                    data.setValue(new ApiResponse<>(false, "Auth failed: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                data.setValue(new ApiResponse<>(false, "Network error: " + t.getMessage(), null));
            }
        });
        return data;
    }

    public LiveData<ApiResponse<Summary>> getSummary(String userId, String targetId, String type) {
        MutableLiveData<ApiResponse<Summary>> data = new MutableLiveData<>();
        apiService.getSummary(userId, targetId, type).enqueue(new Callback<ApiResponse<Summary>>() {
            @Override
            public void onResponse(Call<ApiResponse<Summary>> call, Response<ApiResponse<Summary>> response) {
                if (response.isSuccessful()) {
                    data.setValue(response.body());
                } else {
                    data.setValue(new ApiResponse<>(false, "Error: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Summary>> call, Throwable t) {
                data.setValue(new ApiResponse<>(false, t.getMessage(), null));
            }
        });
        return data;
    }

    public LiveData<ApiResponse<List<User>>> getUsers(String userId) {
        MutableLiveData<ApiResponse<List<User>>> data = new MutableLiveData<>();
        apiService.getUsers(userId).enqueue(new Callback<ApiResponse<List<User>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<User>>> call, Response<ApiResponse<List<User>>> response) {
                if (response.isSuccessful()) {
                    data.setValue(response.body());
                } else {
                    data.setValue(new ApiResponse<>(false, "Error: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<User>>> call, Throwable t) {
                data.setValue(new ApiResponse<>(false, t.getMessage(), null));
            }
        });
        return data;
    }

    public LiveData<ApiResponse<Record>> addRecord(String userId, Record record) {
        MutableLiveData<ApiResponse<Record>> data = new MutableLiveData<>();
        apiService.addRecord(userId, record).enqueue(new Callback<ApiResponse<Record>>() {
            @Override
            public void onResponse(Call<ApiResponse<Record>> call, Response<ApiResponse<Record>> response) {
                if (response.isSuccessful()) {
                    data.setValue(response.body());
                } else {
                    data.setValue(new ApiResponse<>(false, "Error: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Record>> call, Throwable t) {
                data.setValue(new ApiResponse<>(false, t.getMessage(), null));
            }
        });
        return data;
    }

    public LiveData<ApiResponse<List<Record>>> getRecords(String userId, String targetId) {
        MutableLiveData<ApiResponse<List<Record>>> data = new MutableLiveData<>();
        apiService.getRecords(userId, targetId, null, null).enqueue(new Callback<ApiResponse<List<Record>>>() {
            @Override
            public void onResponse(Call<ApiResponse<List<Record>>> call, Response<ApiResponse<List<Record>>> response) {
                if (response.isSuccessful()) {
                    data.setValue(response.body());
                } else {
                    data.setValue(new ApiResponse<>(false, "Error: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<List<Record>>> call, Throwable t) {
                data.setValue(new ApiResponse<>(false, t.getMessage(), null));
            }
        });
        return data;
    }

    public LiveData<ApiResponse<User>> updateUser(String userId, int targetId, User user) {
        MutableLiveData<ApiResponse<User>> data = new MutableLiveData<>();
        apiService.updateUser(userId, targetId, user).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful()) {
                    data.setValue(response.body());
                } else {
                    data.setValue(new ApiResponse<>(false, "Error: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                data.setValue(new ApiResponse<>(false, t.getMessage(), null));
            }
        });
        return data;
    }

    public LiveData<ApiResponse<User>> searchUser(String userId, String name, String email) {
        MutableLiveData<ApiResponse<User>> data = new MutableLiveData<>();
        apiService.searchUser(userId, name, email).enqueue(new Callback<ApiResponse<User>>() {
            @Override
            public void onResponse(Call<ApiResponse<User>> call, Response<ApiResponse<User>> response) {
                if (response.isSuccessful()) {
                    data.setValue(response.body());
                } else {
                    data.setValue(new ApiResponse<>(false, "Error: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<User>> call, Throwable t) {
                data.setValue(new ApiResponse<>(false, t.getMessage(), null));
            }
        });
        return data;
    }

    public LiveData<ApiResponse<Void>> changePassword(String userId, String newPassword) {
        MutableLiveData<ApiResponse<Void>> data = new MutableLiveData<>();
        java.util.Map<String, String> body = new java.util.HashMap<>();
        body.put("newPassword", newPassword);
        
        apiService.changePassword(userId, body).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    data.setValue(response.body());
                } else {
                    data.setValue(new ApiResponse<>(false, "Security Update Failed: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                data.setValue(new ApiResponse<>(false, "Network failure: " + t.getMessage(), null));
            }
        });
        return data;
    }

    public LiveData<ApiResponse<Void>> deleteRecord(String userId, int recordId) {
        MutableLiveData<ApiResponse<Void>> data = new MutableLiveData<>();
        apiService.deleteRecord(userId, recordId).enqueue(new Callback<ApiResponse<Void>>() {
            @Override
            public void onResponse(Call<ApiResponse<Void>> call, Response<ApiResponse<Void>> response) {
                if (response.isSuccessful()) {
                    data.setValue(response.body());
                } else {
                    data.setValue(new ApiResponse<>(false, "Deletion failed: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Void>> call, Throwable t) {
                data.setValue(new ApiResponse<>(false, "Network error: " + t.getMessage(), null));
            }
        });
        return data;
    }

    public LiveData<ApiResponse<Record>> updateRecord(String userId, int recordId, Record record) {
        MutableLiveData<ApiResponse<Record>> data = new MutableLiveData<>();
        apiService.updateRecord(userId, recordId, record).enqueue(new Callback<ApiResponse<Record>>() {
            @Override
            public void onResponse(Call<ApiResponse<Record>> call, Response<ApiResponse<Record>> response) {
                if (response.isSuccessful()) {
                    data.setValue(response.body());
                } else {
                    data.setValue(new ApiResponse<>(false, "Update failed: " + response.code(), null));
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Record>> call, Throwable t) {
                data.setValue(new ApiResponse<>(false, "Network failure: " + t.getMessage(), null));
            }
        });
        return data;
    }
}