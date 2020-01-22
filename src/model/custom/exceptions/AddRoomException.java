package model.custom.exceptions;

public class AddRoomException extends Exception {

    public AddRoomException(String errMessage, String roomID) {
        super(errMessage);
    }
}
