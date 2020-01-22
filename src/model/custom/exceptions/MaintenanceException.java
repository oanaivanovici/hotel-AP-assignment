package model.custom.exceptions;

public class MaintenanceException extends Exception {

    public MaintenanceException(String errMessage, String roomID) {
        super(errMessage);
    }
}
