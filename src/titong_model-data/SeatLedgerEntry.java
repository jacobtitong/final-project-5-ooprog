public class SeatLedgerEntry {
    private int scheduleId;
    private String seatCode;
    private int bookingId;

    public SeatLedgerEntry(int scheduleId, String seatCode, int bookingId) {
        this.scheduleId = scheduleId;
        this.seatCode = seatCode;
        this.bookingId = bookingId;
    }

    public int getScheduleId() { return scheduleId; }
    public String getSeatCode() { return seatCode; }
    public int getBookingId() { return bookingId; }

    public String toFileLine() {
        return scheduleId + ";" + seatCode + ";" + bookingId;
    }
}
