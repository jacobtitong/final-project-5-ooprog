public class Schedule {
    private int id;
    private int movieId;
    private int hallId;
    private String date;
    private String startTime;
    private String format;
    private double ticketPrice;

    public Schedule(int id, int movieId, int hallId, String date,
            String startTime, String format, double ticketPrice) {
        this.id = id;
        this.movieId = movieId;
        this.hallId = hallId;
        this.date = date;
        this.startTime = startTime;
        this.format = format;
        this.ticketPrice = ticketPrice;
    }

    public int getId() { return id; }
    public int getMovieId() { return movieId; }
    public int getHallId() { return hallId; }
    public String getDate() { return date; }
    public String getStartTime() { return startTime; }
    public String getFormat() { return format; }
    public double getTicketPrice() { return ticketPrice; }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public void setHallId(int hallId) {
        this.hallId = hallId;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void setTicketPrice(double ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    public String toFileLine() {
        return id + ";" + movieId + ";" + hallId + ";" + date
                + ";" + startTime + ";" + format + ";" + ticketPrice;
    }
}
