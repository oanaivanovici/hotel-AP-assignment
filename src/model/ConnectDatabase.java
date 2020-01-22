package model;

import java.sql.*;
import java.util.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class ConnectDatabase {

    public static final int LENGTH_IF_NO_ROOM_ID = 6;
    public static ConnectDatabase instance;
    private final String TABLE_NAME_CITY_LODGE = "CITY_LODGE";
    private final String TABLE_NAME_HIRING_RECORDS = "HIRING_RECORDS";

    public ConnectDatabase() {
        instance = this;
    }

    public static ConnectDatabase getInstance() {
        if (instance == null) {
            instance = new ConnectDatabase();
        }

        return instance;
    }

    public static Connection con = null;

    public void connectToDatabase() {
        final String DB_NAME = "testdb";
        try {
            Class.forName("org.hsqldb.jdbc.JDBCDriver");
            con = DriverManager.getConnection("jdbc:hsqldb:file:database/" + DB_NAME, "SA", "");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createTables() {
        try {
            Statement statement = con.createStatement();
            DatabaseMetaData dbm = con.getMetaData();

            //statement.executeUpdate("DROP TABLE city_lodge");
            //statement.executeUpdate("DROP TABLE hiring_records");

            ResultSet tablesCityLodge = dbm.getTables(null, null, TABLE_NAME_CITY_LODGE.toUpperCase(), null);
            if (tablesCityLodge != null) {
                if (!tablesCityLodge.next()) {
                    String createCitylodgeQuery = "CREATE TABLE city_lodge (RoomId VARCHAR(5) PRIMARY KEY NOT NULL, Summary VARCHAR(300) NOT NULL, RoomType VARCHAR(50) NOT NULL, NumberOfBedrooms INT NOT NULL, RoomStatus VARCHAR(50) NOT NULL, Maintenance DATE, FileName VARCHAR(100))";
                    statement.execute(createCitylodgeQuery);
                    tablesCityLodge.close();
                }
            }
            ResultSet tablesHiringRecords = dbm.getTables(null, null, TABLE_NAME_HIRING_RECORDS.toUpperCase(), null);
            if (tablesHiringRecords != null) {
                if (!tablesHiringRecords.next()) {
                    String createHiringRecordsQuery = "CREATE TABLE hiring_records (RoomId VARCHAR(5) NOT NULL, RecordId VARCHAR(50) PRIMARY KEY NOT NULL, RentDate DATE NOT NULL, EstimatedReturnDate DATE NOT NULL, ActualReturnDate DATE, RentalFee DECIMAL, LateFee DECIMAL)";
                    statement.execute(createHiringRecordsQuery);
                    tablesHiringRecords.close();
                }
            }

        } catch (SQLException sqlE) {
            sqlE.printStackTrace();
        }
    }

    public List<Room> getAllRooms() {
        List<Room> hotelRooms = new ArrayList<Room>();
        String row;
        try {
            String selectQuery = "SELECT * FROM city_lodge;";
            PreparedStatement preparedStatement1 = con.prepareStatement(selectQuery);
            ResultSet result = preparedStatement1.executeQuery();
            while (result.next()) {
                row = result.getString(1) + " " + result.getString(2) + " " +
                        result.getString(3) + " " + result.getString(4) + " " +
                        result.getString(5) + " " + result.getString(6) + " " +
                        result.getString(7);
                hotelRooms.add(stringToRoom(row, CityLodge.SPACE_SEPARATOR));
            }
        } catch (SQLException sqlE) {
            sqlE.printStackTrace();
        }
        return hotelRooms;
    }


    public Room getRoomById(String roomID) {
        Room roomToReturn = null;
        try {
            String selectQuery = "SELECT * FROM city_lodge WHERE RoomId = ?";
            PreparedStatement preparedStatement = con.prepareStatement(selectQuery);
            preparedStatement.setString(1, roomID);
            ResultSet result = preparedStatement.executeQuery();
            while (result.next()) {
                String row = result.getString(1) + " " + result.getString(2) + " " +
                        result.getString(3) + " " + result.getString(4) + " " +
                        result.getString(5) + " " + result.getString(6) + " " +
                        result.getString(7);
                roomToReturn = stringToRoom(row, CityLodge.SPACE_SEPARATOR);
            }
        } catch (SQLException sqlE) {
            sqlE.printStackTrace();
        }
        return roomToReturn;
    }

    public void addRoomToDatabase(Room newRoom) {
        PreparedStatement preparedStatement;
        try {
            String insertQuery = "INSERT INTO city_lodge VALUES (?, ?, ?, ?, ?, ?, ?);";
            preparedStatement = con.prepareStatement(insertQuery);
            preparedStatement.setString(1, newRoom.get_roomID());
            preparedStatement.setString(2, newRoom.get_summary());
            preparedStatement.setString(3, newRoom.get_roomType());
            preparedStatement.setInt(4, newRoom.get_numberOfBedrooms());
            preparedStatement.setString(5, newRoom.get_roomStatus());
            if (newRoom.get_roomType().equals(Room.SUITE_ROOM_TYPE)) {
                String date = dateTimeToSqlDate(((Suite) newRoom).getLastMaintenanceDate().toString());
                preparedStatement.setString(6, date);
            } else {
                preparedStatement.setString(6, null);
            }
            preparedStatement.setString(7, newRoom.get_roomImage());
            preparedStatement.executeUpdate();

        } catch (SQLException sqlE) {
            sqlE.printStackTrace();
        }
    }

    public void changeRoomStatus(String roomId, String roomStatus) {
        try {
            String updateQuery = "UPDATE city_lodge SET RoomStatus = ? WHERE RoomId = ?";
            PreparedStatement preparedStatement = con.prepareStatement(updateQuery);
            preparedStatement.setString(1, roomStatus);
            preparedStatement.setString(2, roomId);
            preparedStatement.executeUpdate();
        } catch (SQLException sqlE) {
            sqlE.printStackTrace();
        }
    }

    public void changeRoomStatusWithDate(String roomId, String roomStatus, DateTime returnDate) {
        try {
            String updateQuery = "UPDATE city_lodge SET RoomStatus = ?, Maintenance = ? WHERE RoomId = ?";
            PreparedStatement preparedStatement = con.prepareStatement(updateQuery);
            preparedStatement.setString(1, roomStatus);
            preparedStatement.setString(2, dateTimeToSqlDate(returnDate.toString()));
            preparedStatement.setString(3, roomId);
            preparedStatement.executeUpdate();
        } catch (SQLException sqlE) {
            sqlE.printStackTrace();
        }
    }

    public List<HiringRecords> getHiringRecords(String roomID) {
        List<HiringRecords> hiringRecords = new ArrayList<>();
        try {
            String selectQuery = "SELECT * FROM hiring_records WHERE RoomId = ?";
            PreparedStatement preparedStatement = con.prepareStatement(selectQuery);
            preparedStatement.setString(1, roomID);
            ResultSet result = preparedStatement.executeQuery();
            while (result.next()) {
                String row = result.getString(1) + " " + result.getString(2) + " " +
                        result.getString(3) + " " + result.getString(4) + " " +
                        result.getString(5) + " " + result.getDouble(6) + " " +
                        result.getDouble(7);
                // if not returned yet
                if (result.getDouble(6) == 0) {
                    hiringRecords.add(stringToPartialHiringRecord(row, CityLodge.SPACE_SEPARATOR));
                } else {
                    hiringRecords.add(stringToFullHiringRecord(row, CityLodge.SPACE_SEPARATOR));
                }
                Collections.sort(hiringRecords, new Comparator<HiringRecords>() {
                    @Override
                    public int compare(HiringRecords o1, HiringRecords o2) {
                        return DateTime.diffDays(o2.get_rentDate(), o1.get_rentDate());
                    }
                });
            }
        } catch (SQLException sqlE) {
            sqlE.printStackTrace();
        }
        return hiringRecords;
    }

    public void addPartialHiringRecord(HiringRecords hiringRecord) {
        PreparedStatement preparedStatement;
        try {
            String insertQuery = "INSERT INTO hiring_records (RoomId, RecordId, RentDate, EstimatedReturnDate) VALUES (?, ?, ?, ?);";
            preparedStatement = con.prepareStatement(insertQuery);
            preparedStatement.setString(1, hiringRecord.get_roomID());
            preparedStatement.setString(2, hiringRecord.get_recordID());
            preparedStatement.setString(3, dateTimeToSqlDate(hiringRecord.get_rentDate().toString()));
            preparedStatement.setString(4, dateTimeToSqlDate(hiringRecord.get_estimatedReturnDate().toString()));
            preparedStatement.executeUpdate();
        } catch (SQLException sqlE) {
            sqlE.printStackTrace();
        }
    }

    public void updateHiringRecord(HiringRecords hiringRecords, DateTime actualReturnDate) {
        try {
            String updateQuery = "UPDATE hiring_records SET ActualReturnDate = ?, RentalFee = ?, LateFee = ? WHERE RecordId = ?";
            PreparedStatement preparedStatement1 = con.prepareStatement(updateQuery);
            preparedStatement1.setString(1, dateTimeToSqlDate(actualReturnDate.toString()));
            preparedStatement1.setDouble(2, hiringRecords.get_rentalFee());
            preparedStatement1.setDouble(3, hiringRecords.get_lateFee());
            preparedStatement1.setString(4, hiringRecords.get_recordID());
            preparedStatement1.executeUpdate();
        } catch (SQLException sqlE) {
            sqlE.printStackTrace();
        }
    }

    public void addFullHiringRecord(HiringRecords hiringRecord) {
        PreparedStatement preparedStatement;
        try {
            String insertQuery = "INSERT INTO hiring_records (RoomId, RecordId, RentDate, EstimatedReturnDate, ActualReturnDate, RentalFee, LateFee) VALUES (?, ?, ?, ?, ?, ?, ?);";
            preparedStatement = con.prepareStatement(insertQuery);
            preparedStatement.setString(1, hiringRecord.get_roomID());
            preparedStatement.setString(2, hiringRecord.get_recordID());
            preparedStatement.setString(3, dateTimeToSqlDate(hiringRecord.get_rentDate().toString()));
            preparedStatement.setString(4, dateTimeToSqlDate(hiringRecord.get_estimatedReturnDate().toString()));
            preparedStatement.setString(5, dateTimeToSqlDate(hiringRecord.get_actualReturnDate().toString()));
            preparedStatement.setString(6, String.valueOf(hiringRecord.get_rentalFee()));
            preparedStatement.setString(7, String.valueOf(hiringRecord.get_lateFee()));
            preparedStatement.executeUpdate();
        } catch (SQLException sqlE) {
            sqlE.printStackTrace();
        }
    }

    /* This method converts the string fetched from the DB to a Room object
     */
    public Room stringToRoom(String result, String split) {
        String[] roomDetails = result.split(split);
        Room roomToReturn = null;
        if (roomDetails[2].equals(Room.STANDARD_ROOM_TYPE)) {
            roomToReturn = new StandardRoom(roomDetails[0], roomDetails[1], roomDetails[2], Integer.parseInt(roomDetails[3]), roomDetails[4], roomDetails[6]);

        } else if (roomDetails[2].equals(Room.SUITE_ROOM_TYPE)) {
            DateTime formattedDate;
            if (roomDetails[5].contains("/")) {
                formattedDate = slashStringToDateTime(roomDetails[5]);
            } else {
                formattedDate = stringToDateTime(roomDetails[5]);
            }
            roomToReturn = new Suite(roomDetails[0], roomDetails[1], roomDetails[2], Integer.parseInt(roomDetails[3]), roomDetails[4], formattedDate, roomDetails[6]);
        }
        return roomToReturn;
    }

    /* This method returns a string fetched from DB to a Hiring Record object that is not finalised
     * (i.e. that does not have actual return date and fees set yet)
     */
    public HiringRecords stringToPartialHiringRecord(String result, String split) {
        List<String> hiringRecordDetails = splitResult(result, split);
        DateTime rentDate;
        DateTime estimatedReturnDate;
        if (hiringRecordDetails.get(2).contains("/")) {
            rentDate = slashStringToDateTime(hiringRecordDetails.get(2));
            estimatedReturnDate = slashStringToDateTime(hiringRecordDetails.get(3));
        } else {
            rentDate = stringToDateTime(hiringRecordDetails.get(2));
            estimatedReturnDate = stringToDateTime(hiringRecordDetails.get(3));
        }
        HiringRecords record = new HiringRecords(hiringRecordDetails.get(0), hiringRecordDetails.get(1),
                rentDate, estimatedReturnDate);
        return record;
    }

    /* This method returns a string fetched from DB to a Hiring Record object that IS  finalised
     * (i.e. that has actual return date and fees set)
     */
    public HiringRecords stringToFullHiringRecord(String result, String split) {
        List<String> hiringRecordDetails = splitResult(result, split);
        DateTime rentDate;
        DateTime estimatedReturnDate;
        DateTime actualReturnDate;
        if (hiringRecordDetails.get(2).contains("/")) {
            rentDate = slashStringToDateTime(hiringRecordDetails.get(2));
            estimatedReturnDate = slashStringToDateTime(hiringRecordDetails.get(3));
            actualReturnDate = slashStringToDateTime(hiringRecordDetails.get(4));
        } else {
            rentDate = stringToDateTime(hiringRecordDetails.get(2));
            estimatedReturnDate = stringToDateTime(hiringRecordDetails.get(3));
            actualReturnDate = stringToDateTime(hiringRecordDetails.get(4));
        }
        HiringRecords record = new HiringRecords(hiringRecordDetails.get(0), hiringRecordDetails.get(1),
                rentDate, estimatedReturnDate, actualReturnDate,
                Double.parseDouble(hiringRecordDetails.get(5)), Double.parseDouble(hiringRecordDetails.get(6)));
        return record;
    }

    // This method splits the result into a list of hiring record properties
    private List<String> splitResult(String result, String split) {
        List<String> hiringRecordDetails = new ArrayList<String>(Arrays.asList(result.split(split)));
        if (hiringRecordDetails.size() == LENGTH_IF_NO_ROOM_ID) {
            insertRoomId(hiringRecordDetails);
        }
        return hiringRecordDetails;
    }

    // This method fetches the room ID from record ID
    private void insertRoomId(List<String> hiringRecordDetails) {
        String roomId = hiringRecordDetails.get(0).substring(0, 5);
        hiringRecordDetails.add(0, roomId);
    }

    // This method converts a string '2019-08-12' to a DateTime 22/09/2019
    private DateTime stringToDateTime(String date) {
        String[] dayMonthYear = date.split("-");
        return new DateTime(Integer.parseInt(dayMonthYear[2]), Integer.parseInt(dayMonthYear[1]),
                Integer.parseInt(dayMonthYear[0]));
    }

    // This method converts string 22/09/2019 to string 2019-09-22
    private String dateTimeToSqlDate(String date) {
        String[] dayMonthYear = date.split("/");
        return dayMonthYear[2] + "-" + dayMonthYear[1] + "-" + dayMonthYear[0];
    }

    // This method converts string 22/09/2019 to DateTime
    private DateTime slashStringToDateTime(String date) {
        String[] dateDigits = date.split("/");
        return new DateTime(Integer.parseInt(dateDigits[0]), Integer.parseInt(dateDigits[1]),
                Integer.parseInt(dateDigits[2]));
    }
}