package app.finance.models;

public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    // Added constructor for manual creation (Error handling)
    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
}
