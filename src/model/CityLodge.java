package model;

import controller.GeneralController;
import model.custom.exceptions.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class CityLodge {

    public static final Map<String, String> ROOM_ID_PREFIX = new HashMap<String, String>() {
        {
            put(Room.STANDARD_ROOM_TYPE, "R_");
            put(Room.SUITE_ROOM_TYPE, "S_");
        }
    };

    private final List<Integer> NUMBER_OF_BEDROOMS = new ArrayList<Integer>() {{
        add(1);
        add(2);
        add(4);
    }};

    private final List<String> SUMMARY = new ArrayList<String>() {{
        add("TV");
        add("Mini-fridge");
        add("Air-conditioning");
        add("TV,Kettle");
        add("TV,Mini-fridge");
        add("TV,Air-conditioning");
    }};

    private static final String STANDARD_ROOM_ID_PREFIX = "R";
    private static final int ROOM_ID_LENGTH = 5;
    private static final int NUMBER_OF_STANDARD_ROOMS = 3;
    private static final int NUMBER_OF_SUITES = 3;

    public static final String COLON_SEPARATOR = ":";
    public static final String SPACE_SEPARATOR = " ";

    private List<Room> _hotelRooms = new ArrayList<Room>();

    public static CityLodge instance;

    public CityLodge() {
        instance = this;
    }

    public static CityLodge getInstance() {
        if (instance == null)
            instance = new CityLodge();

        return instance;
    }

    public List<Room> getAllRooms() {
        return _hotelRooms;
    }

    public Room getRoomById(String roomID) {
        return _hotelRooms.stream().filter(r -> r.get_roomID().equals(roomID)).findFirst().orElse(null);
    }

    private boolean checkIfRoomExists(String roomID) {
        return _hotelRooms.stream().anyMatch(r -> r.get_roomID().equals(roomID));
    }

    private boolean checkIfRoomIsEmpty(String roomID) {
        return getRoomById(roomID).get_roomStatus().equals(Room.AVAILABLE_ROOM);
    }

    private boolean checkIfRoomIsRented(String roomID) {
        return getRoomById(roomID).get_roomStatus().equals(Room.RENTED_ROOM);
    }

    private boolean matchingRoomId(String input) throws InvalidIdException {
        if (input.matches("^[R,S]{1}_\\d{3}")) {
            return true;
        } else {
            throw new InvalidIdException("The Room ID you have entered is not valid because it does not match the patterns 'R_xxx' or 'S_xxx'.", input);
        }
    }

    // add Standard Room
    public void addRoom(String roomID, String summary, int numOfBeds) throws AddRoomException, InvalidIdException {
        if (roomID.isEmpty() || summary.isEmpty()) {
            throw new NullPointerException("Please fill in all the fields.");
        } else if (checkIfRoomExists(roomID)) {
            throw new AddRoomException("Room " + roomID + " already exists at City Lodge.", roomID);
        } else if (!checkIfRoomExists(roomID) && matchingRoomId(roomID)) {
            Room newRoom = new StandardRoom(roomID, summary, Room.STANDARD_ROOM_TYPE, numOfBeds);
            _hotelRooms.add(newRoom);
            ConnectDatabase.getInstance().addRoomToDatabase(newRoom);
        } else {
            throw new AddRoomException("Room " + roomID + " could not be created due to an unforeseen error.", roomID);
        }
    }

    // add Suite
    public void addRoom(String roomID, String summary, DateTime lastMaintenanceDate) throws AddRoomException, InvalidIdException {
        if (roomID.isEmpty() || summary.isEmpty() || lastMaintenanceDate.equals(null)) {
            throw new NullPointerException("Please fill in all the fields.");
        } else if (checkIfRoomExists(roomID)) {
            throw new AddRoomException("Suite " + roomID + " already exists at City Lodge.", roomID);
        } else if (!checkIfRoomExists(roomID) && matchingRoomId(roomID)) {
            Room newRoom = new Suite(roomID, summary, Room.SUITE_ROOM_TYPE, Suite.NUMBER_OF_BEDROOMS, lastMaintenanceDate);
            _hotelRooms.add(newRoom);
            ConnectDatabase.getInstance().addRoomToDatabase(newRoom);
        } else {
            throw new AddRoomException("Room " + roomID + " could not be created due to an unforeseen error.", roomID);
        }
    }


    public void rentRoom(String roomID, String customerID, DateTime rentDate, int numOfDays) throws RentException {
        Room roomToRent = getRoomById(roomID);
        if (checkIfRoomExists(roomID) && checkIfRoomIsEmpty(roomID)) {
            roomToRent.rent(customerID, rentDate, numOfDays);
            ConnectDatabase.getInstance().changeRoomStatus(roomID, Room.RENTED_ROOM);
        } else if (checkIfRoomExists(roomID) && checkIfRoomIsRented(roomID)) {
            throw new RentException("Room " + roomID + " is currently rented", roomID);
        } else if (checkIfRoomExists(roomID) && !checkIfRoomIsRented(roomID) && !checkIfRoomIsEmpty(roomID)) {
            throw new RentException(roomToRent.get_roomType() + " " + roomID + " could not be rented to customer "
                    + customerID + " due to maintenance.", roomID);
        } else if (!checkIfRoomExists(roomID)) {
            throw new RentException("Room " + roomID + " is not a room at City Lodge.", roomID);
        } else {
            throw new RentException("The rent could not be completed due to an unexpected error.", roomID);
        }
    }

    public void returnRoom(String roomID, DateTime returnDate) throws ReturnException {
        Room roomToReturn = getRoomById(roomID);
        if (checkIfRoomIsRented(roomID)) {
            roomToReturn.returnRoom(returnDate);
            ConnectDatabase.getInstance().changeRoomStatusWithDate(roomID, Room.AVAILABLE_ROOM, returnDate);
        } else if (!checkIfRoomIsRented(roomID)) {
            throw new ReturnException(roomToReturn.get_roomType() + " " + roomID +
                    " cannot be returned because it current status is " + roomToReturn.get_roomStatus(), roomID);
        } else {
            throw new ReturnException("The return could not be completed due to an unexpected error.", roomID);
        }
    }

    public void performMaintenance(String roomID) throws MaintenanceException {
        Room roomToPerformMaint = getRoomById(roomID);
        if (!(roomToPerformMaint == null)) {
            roomToPerformMaint.performMaintenance();
            ConnectDatabase.getInstance().changeRoomStatus(roomID, Room.MAINTENANCE_ROOM);
        } else if (roomToPerformMaint == null) {
            throw new MaintenanceException(roomID + " is not a room at City Lodge", roomID);
        } else {
            throw new MaintenanceException("Maintenance could not be performed due to an unexpected error.", roomID);
        }
    }

    public void completeMaintenance(String roomID, DateTime completionDate) throws MaintenanceException {
        Room roomToCompleteMaint = getRoomById(roomID);
        if (!(roomToCompleteMaint == null)) {
            roomToCompleteMaint.completeMaintenance(completionDate);
            ConnectDatabase.getInstance().changeRoomStatusWithDate(roomID, Room.AVAILABLE_ROOM, completionDate);
        } else if (roomToCompleteMaint == null) {
            throw new MaintenanceException(roomID + " is not a room at City Lodge", roomID);
        } else {
            throw new MaintenanceException("Maintenance could not be completed due to an unexpected error.", roomID);
        }
    }

    /*
     * This method deals with the import from text files. The first if statement it checks if line is room or
     * hiring record - if the length of the first String is equal to the room id length, it means the line of text
     * is a room, otherwise a hiring record. The second if statement check if record is full or partial - if
     * if record contains "none" instead of actual return date, it means the room hasn't been returned
     * */
    public void importFromText(File selectedFile) throws IOException {
        BufferedReader bufr = new BufferedReader(new FileReader(selectedFile));
        String inputLine = bufr.readLine();
        while (inputLine != null) {
            String firstItem = inputLine.split(COLON_SEPARATOR)[0];
            if (firstItem.length() <= ROOM_ID_LENGTH) {
                String input = rearrangeParameters(inputLine);
                Room roomToAdd = ConnectDatabase.getInstance().stringToRoom(input, COLON_SEPARATOR);
                ConnectDatabase.getInstance().addRoomToDatabase(roomToAdd);
            } else {
                HiringRecords record;
                String actualReturnDate = inputLine.split(":")[3];
                if (!actualReturnDate.contains("none")) {
                    record = ConnectDatabase.getInstance().stringToFullHiringRecord(inputLine, COLON_SEPARATOR);
                    ConnectDatabase.getInstance().addFullHiringRecord(record);
                } else {
                    record = ConnectDatabase.getInstance().stringToPartialHiringRecord(inputLine, COLON_SEPARATOR);
                    ConnectDatabase.getInstance().addPartialHiringRecord(record);
                }
            }
            inputLine = bufr.readLine();
        }
    }

    /*
     * This method rearranges the parameters as there is a difference between the order of room properties
     * outputted by the toString() method and the order in which room properties are inserted into the database
     * */
    private String rearrangeParameters(String inputLine) {
        List<String> inputDetails = new ArrayList<String>(Arrays.asList(inputLine.split(COLON_SEPARATOR)));
        if (inputDetails.get(0).contains(STANDARD_ROOM_ID_PREFIX)) {
            inputDetails.add(4, "null");
        }
        return inputDetails.get(0) + ":" + inputDetails.get(5) + ":" + inputDetails.get(2) + ":" +
                inputDetails.get(1) + ":" + inputDetails.get(3) + ":" + inputDetails.get(4) + ":" +
                inputDetails.get(6);
    }


    public void exportToText(String selectedFolderPath) throws IOException {
        _hotelRooms = ConnectDatabase.getInstance().getAllRooms();
        BufferedWriter bufw = new BufferedWriter(new FileWriter(selectedFolderPath + "/export_data.txt"));
        for (Room room : _hotelRooms) {
            bufw.write(room.toString());
            bufw.newLine();
            List<HiringRecords> records = ConnectDatabase.getInstance().getHiringRecords(room.get_roomID());
            for (HiringRecords record : records) {
                bufw.write(record.toString());
                bufw.newLine();
            }
        }
        bufw.close();
    }

    // The following methods generate data when the menu button is pushed
    public void generateData(String selectedFolderPath) throws IOException {
        BufferedWriter bufw = new BufferedWriter(new FileWriter(selectedFolderPath + "/generated_data.txt"));
        GeneralController controller = new GeneralController();
        generateStandardRoom(controller, bufw);
        generateSuite(controller, bufw);
        bufw.close();
    }

    private void generateStandardRoom(GeneralController controller, BufferedWriter bufw) throws IOException {
        for (int i = 0; i < NUMBER_OF_STANDARD_ROOMS; i++) {
            String standardRoomId = controller.generateIdAutomatically(ROOM_ID_PREFIX.get(Room.STANDARD_ROOM_TYPE));
            String summary = SUMMARY.get(new Random().nextInt(SUMMARY.size()));
            int numberOfBeds = NUMBER_OF_BEDROOMS.get(new Random().nextInt(NUMBER_OF_BEDROOMS.size()));
            bufw.write(new StandardRoom(standardRoomId, summary, Room.STANDARD_ROOM_TYPE, numberOfBeds).toString());
            bufw.newLine();
            if (i != NUMBER_OF_STANDARD_ROOMS) {
                generateHiringRecord(Room.STANDARD_ROOM_TYPE, standardRoomId, numberOfBeds, bufw);
            }
        }
    }

    private void generateSuite(GeneralController controller, BufferedWriter bufw) throws IOException {
        for (int i = 0; i < NUMBER_OF_SUITES; i++) {
            String suiteRoomId = controller.generateIdAutomatically(ROOM_ID_PREFIX.get(Room.SUITE_ROOM_TYPE));
            String summary = SUMMARY.get(new Random().nextInt(SUMMARY.size()));
            DateTime maintenanceDate = new DateTime(randomDay(), 10, 2019);
            bufw.write(new Suite(suiteRoomId, summary, Room.SUITE_ROOM_TYPE, Suite.NUMBER_OF_BEDROOMS, maintenanceDate).toString());
            bufw.newLine();
            if (i != NUMBER_OF_STANDARD_ROOMS) {
                generateHiringRecord(Room.SUITE_ROOM_TYPE, suiteRoomId, 0, bufw);
            }
        }
    }

    private void generateHiringRecord(String roomType, String roomId, int numberOfBeds, BufferedWriter bufw) throws IOException {
        for (int j = 0; j < NUMBER_OF_STANDARD_ROOMS - 1; j++) {
            DateTime rentDate = new DateTime(randomDay(), 10 - j, 2019);
            String recordId = roomId + "_" + "CUS" + randomCustomerNumber() + "_" + rentDate.getEightDigitDate();
            DateTime estReturnDate = new DateTime(rentDate, randomDaysForward());
            DateTime actualReturnDate = new DateTime(estReturnDate, j);
            double rentFee = 0;
            double lateFee = 0;
            if (roomType.equals(Room.STANDARD_ROOM_TYPE)) {
                rentFee = StandardRoom.RENTAL_RATES.get(numberOfBeds) * DateTime.diffDays(estReturnDate, rentDate);
                lateFee = StandardRoom.RENTAL_RATES.get(numberOfBeds) * StandardRoom.LATE_FEE_PERCENTAGE * DateTime.diffDays(actualReturnDate, estReturnDate);
            } else if (roomType.equals(Room.SUITE_ROOM_TYPE)) {
                rentFee = Suite.RENTAL_RATE * DateTime.diffDays(estReturnDate, rentDate);
                lateFee = Suite.LATE_FEE * DateTime.diffDays(actualReturnDate, estReturnDate);
            }
            bufw.write(new HiringRecords(roomId, recordId, rentDate, estReturnDate, actualReturnDate, rentFee, lateFee).toString());
            bufw.newLine();
        }
    }

    private int randomDay() {
        return ThreadLocalRandom.current().nextInt(1, 31);
    }

    private int randomDaysForward() {
        return ThreadLocalRandom.current().nextInt(3, 6);
    }

    private int randomCustomerNumber() {
        return ThreadLocalRandom.current().nextInt(100, 999);

    }
}
