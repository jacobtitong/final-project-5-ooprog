public class Transaction {
    private int id;
    private int bookingId;
    private String ownerType;
    private int ownerId;
    private int scheduleId;
    private int ticketCount;
    private double ticketAmount;
    private double snackAmount;
    private double totalAmount;
    private String method;
    private String status;
    private double refundAmount;
    private String timestamp;

    public Transaction(int id, int bookingId, String ownerType, int ownerId,
            int scheduleId, int ticketCount, double ticketAmount, double snackAmount,
            double totalAmount, String method, String status, double refundAmount,
            String timestamp) {
        this.id = id;
        this.bookingId = bookingId;
        this.ownerType = ownerType;
        this.ownerId = ownerId;
        this.scheduleId = scheduleId;
        this.ticketCount = ticketCount;
        this.ticketAmount = ticketAmount;
        this.snackAmount = snackAmount;
        this.totalAmount = totalAmount;
        this.method = method;
        this.status = status;
        this.refundAmount = refundAmount;
        this.timestamp = timestamp;
    }

    public int getId() { return id; }
    public int getBookingId() { return bookingId; }
    public String getOwnerType() { return ownerType; }
    public int getOwnerId() { return ownerId; }
    public int getScheduleId() { return scheduleId; }
    public int getTicketCount() { return ticketCount; }
    public double getTicketAmount() { return ticketAmount; }
    public double getSnackAmount() { return snackAmount; }
    public double getTotalAmount() { return totalAmount; }
    public String getMethod() { return method; }
    public String getStatus() { return status; }
    public double getRefundAmount() { return refundAmount; }
    public String getTimestamp() { return timestamp; }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setRefundAmount(double refundAmount) {
        this.refundAmount = refundAmount;
    }

    public String toFileLine() {
        return id + ";" + bookingId + ";" + ownerType + ";" + ownerId
                + ";" + scheduleId + ";" + ticketCount + ";" + ticketAmount + ";" + snackAmount
                + ";" + totalAmount + ";" + method + ";" + status + ";" + refundAmount
                + ";" + timestamp;
    }
}
