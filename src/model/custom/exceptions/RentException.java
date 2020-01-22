package model.custom.exceptions;

public class RentException extends Exception {

    public RentException(String errMessage, String roomID) {
        super(errMessage);
    }
}
