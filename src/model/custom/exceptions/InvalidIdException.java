package model.custom.exceptions;

public class InvalidIdException extends Exception {

    public InvalidIdException(String errMessage, String roomID) {
        super(errMessage);
    }

}
