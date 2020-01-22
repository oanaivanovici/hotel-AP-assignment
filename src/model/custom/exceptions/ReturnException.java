package model.custom.exceptions;

public class ReturnException extends Exception {

    public ReturnException(String errMessage, String roomID) {
        super(errMessage);
    }

}
