public class Transaction {
    public Transaction(int id, int bookingId, String ownerType, int ownerId,
            int scheduleId, int ticketCount, double ticketAmount, double snackAmount,
            double totalAmount, String method, String status, double refundAmount,
            String timestamp) {
    }

    public int getId() { return 0; }
    public int getBookingId() { return 0; }
    public String getOwnerType() { return ""; }
    public int getOwnerId() { return 0; }
    public int getScheduleId() { return 0; }
    public int getTicketCount() { return 0; }
    public double getTicketAmount() { return 0.0; }
    public double getSnackAmount() { return 0.0; }
    public double getTotalAmount() { return 0.0; }
    public String getMethod() { return ""; }
    public String getStatus() { return ""; }
    public double getRefundAmount() { return 0.0; }
    public String getTimestamp() { return ""; }

    public void setStatus(String status) {
    }

    public void setRefundAmount(double refundAmount) {
    }

    public String toFileLine() { return ""; }
}
