package model;

import model.custom.exceptions.MaintenanceException;
import model.custom.exceptions.RentException;
import model.custom.exceptions.ReturnException;

import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Room {

    private String _roomID;
    private String _summary;
    private String _roomType;
    private int _numberOfBedrooms;
    private String _roomStatus;
    private String _roomImage;

    public static final int MIN_RENTAL_DAYS = 1;
    public static final int NUMBER_OF_HIRING_RECORDS = 10;
    public static final String STANDARD_ROOM_TYPE = "Standard";
    public static final String SUITE_ROOM_TYPE = "Suite";
    public static final String AVAILABLE_ROOM = "Available";
    public static final String RENTED_ROOM = "Rented";
    public static final String MAINTENANCE_ROOM = "Maintenance";
    public static final int PRINT_RECORDS_LINE = 1;
    public static final String SEPRATATING_LINE = "---------------------------------------------";
    private List<HiringRecords> _hiringRecords = new ArrayList<HiringRecords>(NUMBER_OF_HIRING_RECORDS);

    public Room(String roomID, String summary, String roomType, int numberOfBedrooms) {
        _roomID = roomID;
        _summary = summary;
        _roomStatus = AVAILABLE_ROOM;
        _roomType = roomType;
        _numberOfBedrooms = numberOfBedrooms;
        if (_roomType.equals(Room.SUITE_ROOM_TYPE)) {
            _roomImage = "Suite_" + randomNumber() + ".jpeg";
        } else {
            _roomImage = "Standard_room_" + randomNumber() + ".jpeg";
        }
    }

    // constructor for retrieving data from DB
    public Room(String roomID, String summary, String roomType, int numberOfBedrooms, String roomStatus, String roomImage) {
        _roomID = roomID;
        _summary = summary;
        _roomType = roomType;
        _numberOfBedrooms = numberOfBedrooms;
        _roomStatus = roomStatus;
        _roomImage = roomImage;
    }

    public String get_roomID() {
        return _roomID;
    }

    public void set_roomID(String roomID) {
        _roomID = roomID;
    }

    public String get_summary() {
        return _summary;
    }

    public void set_summary(String summary) {
        _summary = summary;
    }

    public String get_roomStatus() {
        return _roomStatus;
    }

    public void set_roomStatus(String roomStatus) {
        _roomStatus = roomStatus;
    }

    public String get_roomType() {
        return _roomType;
    }

    public void set_roomType(String roomType) {
        _roomType = roomType;
    }

    public int get_numberOfBedrooms() {
        return _numberOfBedrooms;
    }

    public void set_numberOfBedrooms(int numberOfBedrooms) {
        _numberOfBedrooms = numberOfBedrooms;
    }

    public List<HiringRecords> get_hiringRecords() {
        return _hiringRecords;
    }

    public void set_hiringRecords(List<HiringRecords> hiringRecords) {
        _hiringRecords = hiringRecords;
    }

    public String get_roomImage() {
        return _roomImage;
    }

    public void set_roomImage(String roomImage) {
        _roomImage = roomImage;
    }

    public void rent(String customerID, DateTime rentDate, int numOfRentDay) throws RentException {
        if (!(_roomStatus.equals(Room.AVAILABLE_ROOM))) {
            throw new RentException("Room " + _roomID + " is not available at the moment", _roomID);
        }
    }

    public void returnRoom(DateTime returnDate) throws ReturnException {
        HiringRecords latestRecord = ConnectDatabase.getInstance().getHiringRecords(get_roomID()).get(0);
        // if room is rented and the return date is after the rent date
        if (_roomStatus.equals(RENTED_ROOM) && (DateTime.diffDays(returnDate, latestRecord.get_rentDate()) > 0)) {
            set_roomStatus(AVAILABLE_ROOM);
            latestRecord.set_actualReturnDate(returnDate);
        } else if (!(_roomStatus.equals(RENTED_ROOM))) {
            throw new ReturnException(_roomType + _roomID + " cannot be returned because it has not been rented yet", _roomID);
        } else if (!(DateTime.diffDays(returnDate, latestRecord.get_rentDate()) > 0)) {
            throw new ReturnException(_roomType + _roomID + " cannot be returned because the return date is before the rent date", _roomID);
        }
    }

    public void performMaintenance() throws MaintenanceException {
        if (_roomStatus.equals(AVAILABLE_ROOM)) {
            set_roomStatus(MAINTENANCE_ROOM);
        } else {
            throw new MaintenanceException("Maintenance cannot be performed on " + _roomType + " " + _roomID + " because it is not available at the moment", _roomID);
        }
    }

    public void completeMaintenance(DateTime completionDate) throws MaintenanceException {
        if (_roomStatus.equals(MAINTENANCE_ROOM)) {
            set_roomStatus(AVAILABLE_ROOM);
        } else {
            throw new MaintenanceException("Maintenance cannot be completed on " + _roomType + " " + _roomID + " because the room was not under maintenance to begin with", _roomID);
        }
    }

    public String toString() {
        StringBuilder details = new StringBuilder();
        Formatter formatter = new Formatter(details);
        formatter.format("%1s:%1s:%1s:%1s:%1s:%1s", _roomID, _numberOfBedrooms, _roomType, _roomStatus, _summary, _roomImage);
        return details.toString();
    }

    public String getDetails() {
        StringBuilder roomDetails = new StringBuilder();
        roomDetails.append("Room ID:\t\t\t\t" + _roomID + "\n" + "Number of bedrooms:\t\t" + _numberOfBedrooms + "\n" +
                "Type:\t\t\t\t\t" + _roomType + "\n" + "Status:\t\t\t\t\t" + _roomStatus + "\n" + "Feature summary:\t\t" +
                _summary + "\n" + "RENTAL RECORD\t\t\n");
        if (get_hiringRecords().size() == 0) {
            roomDetails.append("Empty" + "\n");
        } else {
            // iterate through the hiring records
            for (int i = _hiringRecords.size() - 1; i >= 0; i--) {
                HiringRecords record = _hiringRecords.get(i);
                roomDetails.append(record.getDetails());
                if (i >= PRINT_RECORDS_LINE) {  // if more than one record, separate with line
                    roomDetails.append(SEPRATATING_LINE + "\n");
                }
            }
        }
        return roomDetails.toString();
    }

    private int randomNumber() {
        return ThreadLocalRandom.current().nextInt(1, 3);
    }
}
