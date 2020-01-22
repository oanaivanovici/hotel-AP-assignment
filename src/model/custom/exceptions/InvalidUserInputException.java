package model.custom.exceptions;

public class InvalidUserInputException extends Exception {

    public InvalidUserInputException(String errMessage) {
        super(errMessage);
    }
}
