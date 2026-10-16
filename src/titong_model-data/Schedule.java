public class Schedule {
    public Schedule(int id, int movieId, int hallId, String date,
            String startTime, String format, double ticketPrice) {
    }

    public int getId() { return 0; }
    public int getMovieId() { return 0; }
    public int getHallId() { return 0; }
    public String getDate() { return ""; }
    public String getStartTime() { return ""; }
    public String getFormat() { return ""; }
    public double getTicketPrice() { return 0.0; }

    public void setMovieId(int movieId) {
    }

    public void setHallId(int hallId) {
    }

    public void setDate(String date) {
    }

    public void setStartTime(String startTime) {
    }

    public void setFormat(String format) {
    }

    public void setTicketPrice(double ticketPrice) {
    }

    public String toFileLine() { return ""; }
}
