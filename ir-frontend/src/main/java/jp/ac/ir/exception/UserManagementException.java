package jp.ac.ir.exception;

public class UserManagementException extends RuntimeException{
    public UserManagementException() {}
    public UserManagementException(String message) {
        super(message);
    }
}
