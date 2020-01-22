package model;

import java.util.*;

public class HiringRecords {

    private String _roomID;
    private String _recordID;
    private DateTime _rentDate;
    private DateTime _estimatedReturnDate;
    private DateTime _actualReturnDate;
    private double _rentalFee;
    private double _lateFee;

    public HiringRecords(String customerID, String roomID, DateTime rentDate, int numberOfDaysToRent) {
        _recordID = roomID + "_" + customerID + "_" + rentDate.getEightDigitDate();
        _rentDate = rentDate;
        set_estimatedReturnDate(numberOfDaysToRent);
    }

    // for fetching data from DB
    public HiringRecords(String roomID, String recordID, DateTime rentDate, DateTime estimatedReturnDate,
                         DateTime actualReturnDate, double rentalFee, double lateFee) {
        _roomID = roomID;
        _recordID = recordID;
        _rentDate = rentDate;
        _estimatedReturnDate = estimatedReturnDate;
        _actualReturnDate = actualReturnDate;
        _rentalFee = rentalFee;
        _lateFee = lateFee;
    }

    // for fetching data from DB
    public HiringRecords(String roomID, String recordID, DateTime rentDate, DateTime estimatedReturnDate) {
        _roomID = roomID;
        _recordID = recordID;
        _rentDate = rentDate;
        _estimatedReturnDate = estimatedReturnDate;
    }

    public String get_roomID() {
        String[] recordDetails = _recordID.split("_");
        return recordDetails[0] + "_" + recordDetails[1];
    }

    public String get_recordID() {
        return _recordID;
    }

    public DateTime get_rentDate() {
        return _rentDate;
    }

    public void set_rentDate(int day, int month, int year) {
        DateTime rentDate = new DateTime(day, month, year);
        _rentDate = rentDate;
    }

    public DateTime get_estimatedReturnDate() {
        return _estimatedReturnDate;
    }

    public void set_estimatedReturnDate(int numberDaysToRent) {
        DateTime returnDate = new DateTime(_rentDate, numberDaysToRent);
        _estimatedReturnDate = returnDate;
    }

    public DateTime get_actualReturnDate() {
        return _actualReturnDate;
    }

    public void set_actualReturnDate(DateTime actualReturnDate) {
        _actualReturnDate = actualReturnDate;
    }

    public double get_rentalFee() {
        return _rentalFee;
    }

    public void set_rentalFee(double rentalFee) {
        _rentalFee = rentalFee;
    }

    public double get_lateFee() {
        return _lateFee;
    }

    public void set_lateFee(double lateFee) {
        _lateFee = lateFee;
    }

    @Override
    public String toString() {
        StringBuilder recordDetails = new StringBuilder();
        Formatter formatter = new Formatter(recordDetails);
        formatter.format("%10s:%10s:%10s", _recordID, _rentDate, _estimatedReturnDate);
        if (isRoomReturned()) {
            formatter.format(":%5s:%5.2f:%5.2f", _actualReturnDate, _rentalFee, _lateFee);
        } else {
            formatter.format(":%5s:%5s:%5s", "none", "none", "none");
        }
        return recordDetails.toString();
    }

    private boolean isRoomReturned() {
        if (!(_actualReturnDate == null)) {
            return true;
        }
        return false;
    }

    public String getDetails() {
        StringBuilder roomDetails = new StringBuilder();
        roomDetails.append("Record ID:\t\t\t\t" + _recordID + "\n" + "Rent Date:\t\t\t\t" + _rentDate + "\n" +
                "Estimated Return Date:\t" + _estimatedReturnDate + "\n");
        if (isRoomReturned()) {
            roomDetails.append("Actual Return Date:\t\t" + _actualReturnDate + "\n" + "Rental Fee:\t\t\t\t" + _rentalFee + "\n" +
                    "Late Fee:\t\t\t\t" + _lateFee + "\n");
        }
        return roomDetails.toString();
    }

    // calculate rental fee for standard rooms
    public double calculateRentalFee(DateTime returnDate, DateTime rentDate, int numOfBeds) {
        double rentalFee = StandardRoom.RENTAL_RATES.get(numOfBeds) * (DateTime.diffDays(returnDate, rentDate));
        set_rentalFee(rentalFee);
        return rentalFee;
    }

    // calculate rental fee for suites
    public double calculateRentalFee(DateTime returnDate, DateTime rentDate) {
        double rentalFee = Suite.RENTAL_RATE * (DateTime.diffDays(returnDate, rentDate));
        set_rentalFee(rentalFee);
        return rentalFee;
    }

    // calculate late fee for standard rooms
    public double calculateLateFee(DateTime actReturnDate, DateTime estReturnDate, int numOfBeds) {
        double lateFee = (StandardRoom.RENTAL_RATES.get(numOfBeds) * StandardRoom.LATE_FEE_PERCENTAGE) *
                (DateTime.diffDays(actReturnDate, estReturnDate));
        set_lateFee(lateFee);
        return lateFee;
    }

    // calculate late fee for suites
    public double calculateLateFee(DateTime actReturnDate, DateTime estReturnDate) {
        double lateFee = Suite.LATE_FEE * (DateTime.diffDays(actReturnDate, estReturnDate));
        set_lateFee(lateFee);
        return lateFee;
    }
}
