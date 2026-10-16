public class Hall {
    private int id;
    private String cinemaLocation;
    private String hallName;
    private int rows;
    private int columns;

    public Hall(int id, String cinemaLocation, String hallName, int rows, int columns) {
        this.id = id;
        this.cinemaLocation = cinemaLocation;
        this.hallName = hallName;
        this.rows = rows;
        this.columns = columns;
    }

    public int getId() { return id; }
    public String getCinemaLocation() { return cinemaLocation; }
    public String getHallName() { return hallName; }
    public int getRows() { return rows; }
    public int getColumns() { return columns; }

    public void setCinemaLocation(String cinemaLocation) {
        this.cinemaLocation = cinemaLocation;
    }

    public void setHallName(String hallName) {
        this.hallName = hallName;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public String toFileLine() {
        return id + ";" + cinemaLocation + ";" + hallName + ";" + rows
                + ";" + columns;
    }
}
