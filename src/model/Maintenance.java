package model;

public class Maintenance {

    private DateTime _maintenanceDate;
    public static final int SUITE_MAINENANCE_INTERVAL = 10;

    public Maintenance(DateTime maintenanceDate) {
        _maintenanceDate = maintenanceDate;
    }

    public DateTime get_maintenanceDate() {
        return _maintenanceDate;
    }

    public void set_maintenanceDate(DateTime maintenanceDate) {
        _maintenanceDate = maintenanceDate;
    }

}
