package app.finance.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import app.finance.models.ApiResponse;
import app.finance.models.Record;
import app.finance.models.Summary;
import app.finance.models.User;
import app.finance.repository.FinanceRepository;

import java.util.List;

public class FinanceViewModel extends ViewModel {
    private final FinanceRepository repository;

    public FinanceViewModel() {
        this.repository = new FinanceRepository();
    }

    public LiveData<ApiResponse<User>> login(User user) {
        return repository.login(user);
    }

    public LiveData<ApiResponse<Summary>> getSummary(String userId, String targetId) {
        return repository.getSummary(userId, targetId);
    }

    public LiveData<ApiResponse<List<User>>> getUsers(String userId) {
        return repository.getUsers(userId);
    }

    public LiveData<ApiResponse<Record>> addRecord(String userId, Record record) {
        return repository.addRecord(userId, record);
    }

    public LiveData<ApiResponse<List<Record>>> getRecords(String userId, String targetId) {
        return repository.getRecords(userId, targetId);
    }

    public LiveData<ApiResponse<User>> updateUser(String userId, int targetId, User user) {
        return repository.updateUser(userId, targetId, user);
    }

    public LiveData<ApiResponse<User>> searchUser(String userId, String name, String email) {
        return repository.searchUser(userId, name, email);
    }

    public LiveData<ApiResponse<Void>> changePassword(String userId, String newPassword) {
        return repository.changePassword(userId, newPassword);
    }

    public LiveData<ApiResponse<Void>> deleteRecord(String userId, int recordId) {
        return repository.deleteRecord(userId, recordId);
    }
}