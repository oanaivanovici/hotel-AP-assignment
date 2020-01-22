package model;

import model.custom.exceptions.MaintenanceException;
import model.custom.exceptions.RentException;
import model.custom.exceptions.ReturnException;

import java.util.*;

public class StandardRoom extends Room {

    public static final double LATE_FEE_PERCENTAGE = 135 / 100;
    private static final int ST_ROOM_MIN_WEEKDAY_RENTAL = 2;
    private static final int ST_ROOM_MIN_WEEKEND_RENTAL = 3;
    private static final int ST_ROOM_MAX_RENTAL = 10;

    public static Map<Integer, Integer> RENTAL_RATES = new HashMap<Integer, Integer>() {
        {
            put(1, 59);
            put(2, 99);
            put(4, 199);
        }
    };
    private static final List<String> MONDAY_TO_FRIDAY = new ArrayList<String>() {
        {
            add("Monday");
            add("Tuesday");
            add("Wednesday");
            add("Thursday");
            add("Friday");
        }
    };

    public StandardRoom(String roomID, String summary, String roomType, int numberOfBedrooms) {
        super(roomID, summary, roomType, numberOfBedrooms);
    }

    // constructor for retrieving data from DB
    public StandardRoom(String roomID, String summary, String roomType, int numberOfBedrooms, String roomStatus, String roomImage) {
        super(roomID, summary, roomType, numberOfBedrooms, roomStatus, roomImage);
    }

    @Override
    public void rent(String customerID, DateTime rentDate, int numOfRentDay) throws RentException {
        super.rent(customerID, rentDate, numOfRentDay);
        if (rentalConditions(rentDate, numOfRentDay)) {
            super.set_roomStatus(Room.RENTED_ROOM);
            HiringRecords newHiringRecord = new HiringRecords(customerID, super.get_roomID(), rentDate, numOfRentDay);
            super.get_hiringRecords().add(newHiringRecord);
            ConnectDatabase.getInstance().addPartialHiringRecord(newHiringRecord);
        } else {
            throw new RentException("Room " + super.get_roomID() + " did not fulfil the rental conditions", super.get_roomID());
        }
    }

    /**
     * This method checks the rental conditions for a standard room on page 3 of the assignment
     * I.e. the rental period should be less than 10 days and if the rental date is on a weekday,
     * the rental period should be at least 2 days, otherwise if the rental date is on a weekend,
     * the rental period should be at least 3 days.
     **/
    private boolean rentalConditions(DateTime rentDate, int numOfRentDay) throws RentException {
        if (numOfRentDay <= ST_ROOM_MAX_RENTAL && (
                (MONDAY_TO_FRIDAY.contains(rentDate.getNameOfDay()) && (numOfRentDay >= ST_ROOM_MIN_WEEKDAY_RENTAL)) ||
                        (!(MONDAY_TO_FRIDAY.contains(rentDate.getNameOfDay())) && (numOfRentDay >= ST_ROOM_MIN_WEEKEND_RENTAL)))) {
            return true;
        } else if (!(numOfRentDay <= ST_ROOM_MAX_RENTAL)) {
            throw new RentException("The desired number of rent days exceeds the 10-days maximum rent period.", get_roomID());
        } else {
            throw new RentException("The rental period for room " + get_roomID() + " does not fulfil the required conditions", get_roomID());
        }
    }

    /**
     * If the return date is before or on the estimated date, calculate the rental fee based on
     * the return date and rent date.
     * Otherwise, calculate the rental fee based on the time between the estimated date and the rent rate,
     * and the late fee based on the time between the return date and estimated date
     **/
    @Override
    public void returnRoom(DateTime returnDate) throws ReturnException {
        super.returnRoom(returnDate);
        HiringRecords latestRecord = ConnectDatabase.getInstance().getHiringRecords(super.get_roomID()).get(0);
        if (DateTime.diffDays(returnDate, latestRecord.get_estimatedReturnDate()) <= 0) {
            latestRecord.calculateRentalFee(returnDate, latestRecord.get_rentDate(), super.get_numberOfBedrooms());
        } else {
            latestRecord.calculateRentalFee(latestRecord.get_estimatedReturnDate(), latestRecord.get_rentDate(), super.get_numberOfBedrooms());
            latestRecord.calculateLateFee(returnDate, latestRecord.get_estimatedReturnDate(), super.get_numberOfBedrooms());
        }
        ConnectDatabase.getInstance().updateHiringRecord(latestRecord, returnDate);
    }

    @Override
    public void performMaintenance() throws MaintenanceException {
        super.performMaintenance();
    }

    @Override
    public void completeMaintenance(DateTime completionDate) throws MaintenanceException {
        super.completeMaintenance(completionDate);
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public String getDetails() {
        return super.getDetails();
    }
}