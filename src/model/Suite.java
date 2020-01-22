package model;

import model.custom.exceptions.MaintenanceException;
import model.custom.exceptions.RentException;
import model.custom.exceptions.ReturnException;

import java.util.*;

public class Suite extends Room {

    public static final int NUMBER_OF_BEDROOMS = 6;
    public static final int RENTAL_RATE = 999;
    public static final int LATE_FEE = 1099;

    private List<Maintenance> _suiteMaintenance = new ArrayList<Maintenance>();

    public Suite(String ID, String summary, String roomType, int numberOfBedrooms, DateTime lastMaintenanceDate) {
        super(ID, summary, roomType, numberOfBedrooms);
        _suiteMaintenance.add(new Maintenance(lastMaintenanceDate));
    }

    // constructor for retrieving data from DB
    public Suite(String ID, String summary, String roomType, int numberOfBedrooms, String roomStatus, DateTime lastMaintenanceDate, String roomImage) {
        super(ID, summary, roomType, numberOfBedrooms, roomStatus, roomImage);
        _suiteMaintenance.add(new Maintenance(lastMaintenanceDate));
    }

    public List<Maintenance> get_suiteMaintenance() {
        return _suiteMaintenance;
    }

    public void set_suiteMaintenance(List<Maintenance> suiteMaintenance) {
        _suiteMaintenance = suiteMaintenance;
    }

    @Override
    public void rent(String customerID, DateTime rentDate, int numOfRentDay) throws RentException {
        super.rent(customerID, rentDate, numOfRentDay);
        if (suiteRentalCondition(rentDate, numOfRentDay)) {
            super.set_roomStatus(Room.RENTED_ROOM);
            HiringRecords newHiringRecord = new HiringRecords(customerID, super.get_roomID(), rentDate, numOfRentDay);
            super.get_hiringRecords().add(newHiringRecord);
            ConnectDatabase.getInstance().addPartialHiringRecord(newHiringRecord);
        }
    }

    /**
     * This method checks the difference between last maintenance date and the desired rental date.
     * If there's more than 10 days until the rental date, it checks the closest scheduled maintenance date
     * to the rental date. E.g.: if last maintenance = 10/08 and rentDate = 28/08 -> daysSinceInspection = 18 days,
     * but in those 18 days, another maintenance must been done. Therefore, the modulus operator gets
     * the number of days AFTER the closest scheduled inspection to the rental date. So another inspection will
     * be done on the 20/08, and 8 days will pass from then until the rental date, which means the maximum
     * period the suite can be rented for is 2 days (maintenance interval - days since inspection).
     * If there were less than 10 days since the last inspection, and the number of days the client wants to rent
     * the suite for is less than the number of days until next maintenance, then the suite can be rented.
     **/
    private boolean suiteRentalCondition(DateTime rentDate, int numOfRentDay) throws RentException {
        DateTime lastMaintenanceDate = (_suiteMaintenance.get(_suiteMaintenance.size() - 1)).get_maintenanceDate();
        int daysSinceInspection = DateTime.diffDays(rentDate, lastMaintenanceDate);
        if (daysSinceInspection > Maintenance.SUITE_MAINENANCE_INTERVAL) {
            daysSinceInspection = daysSinceInspection % Maintenance.SUITE_MAINENANCE_INTERVAL;
        }
        int maxRentalPeriod = Maintenance.SUITE_MAINENANCE_INTERVAL - daysSinceInspection;
        if ((daysSinceInspection < Maintenance.SUITE_MAINENANCE_INTERVAL) && (numOfRentDay <= maxRentalPeriod)) {
            return true;
        } else {
            throw new RentException("The number of days the client wishes to rent the suite " + get_roomID() + " exceeds the maintenance interval", get_roomID());
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
            latestRecord.calculateRentalFee(returnDate, latestRecord.get_rentDate());
        } else {
            latestRecord.calculateRentalFee(latestRecord.get_estimatedReturnDate(), latestRecord.get_rentDate());
            latestRecord.calculateLateFee(returnDate, latestRecord.get_estimatedReturnDate());
        }
        ConnectDatabase.getInstance().updateHiringRecord(latestRecord, returnDate);
    }

    @Override
    public void performMaintenance() throws MaintenanceException {
        super.performMaintenance();
    }

    /**
     * Store last maintenance date for suites.
     **/
    @Override
    public void completeMaintenance(DateTime completionDate) throws MaintenanceException {
        super.completeMaintenance(completionDate);
    }

    /**
     * This method takes the string of details for a suite, and inserts the last maintenance date in between the
     * room status and the summary. By doing this, the toString method in the Room class can return the entire
     * string for a standard room, therefore ensuring that the overriden method in the StandardRoom class does
     * not contain any duplicated code.
     */
    @Override
    public String toString() {
        StringBuilder details = new StringBuilder(super.toString());
        int indexBeforeSummary = details.indexOf(super.get_summary());
        String maintenanceDate = getLastMaintenanceDate().toString() + ":";
        details.insert(indexBeforeSummary, maintenanceDate, 0, getLastMaintenanceDate().toString().length() + 1);
        return details.toString();
    }

    @Override
    public String getDetails() {
        StringBuilder details = new StringBuilder(super.getDetails());
        int indexBeforeSummary = details.indexOf(super.get_roomStatus()) + get_roomStatus().length();
        String maintenanceDate = String.format("%nLast maintenance date:%12s", getLastMaintenanceDate().toString());
        details.insert(indexBeforeSummary, maintenanceDate, 0, maintenanceDate.length());
        return details.toString();
    }

    public DateTime getLastMaintenanceDate() {
        return _suiteMaintenance.get(_suiteMaintenance.size() - 1).get_maintenanceDate();
    }

}