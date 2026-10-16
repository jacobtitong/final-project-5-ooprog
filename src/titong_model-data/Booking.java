public class Booking {
    public Booking(int id, String ownerType, int ownerId, String customerName,
            int scheduleId, String seats, String status, double totalPaid,
            String timestamp) {
    }

    public int getId() { return 0; }
    public String getOwnerType() { return ""; }
    public int getOwnerId() { return 0; }
    public String getCustomerName() { return ""; }
    public int getScheduleId() { return 0; }
    public String getSeats() { return ""; }
    public String getStatus() { return ""; }
    public double getTotalPaid() { return 0.0; }
    public String getTimestamp() { return ""; }

    public void setStatus(String status) {
    }

    public String toFileLine() { return ""; }
}
