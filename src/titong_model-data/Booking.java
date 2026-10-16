public class Booking {
    private int id;
    private String ownerType;
    private int ownerId;
    private String customerName;
    private int scheduleId;
    private String seats;
    private String status;
    private double totalPaid;
    private String timestamp;

    public Booking(int id, String ownerType, int ownerId, String customerName,
            int scheduleId, String seats, String status, double totalPaid,
            String timestamp) {
        this.id = id;
        this.ownerType = ownerType;
        this.ownerId = ownerId;
        this.customerName = customerName;
        this.scheduleId = scheduleId;
        this.seats = seats;
        this.status = status;
        this.totalPaid = totalPaid;
        this.timestamp = timestamp;
    }

    public int getId() { return id; }
    public String getOwnerType() { return ownerType; }
    public int getOwnerId() { return ownerId; }
    public String getCustomerName() { return customerName; }
    public int getScheduleId() { return scheduleId; }
    public String getSeats() { return seats; }
    public String getStatus() { return status; }
    public double getTotalPaid() { return totalPaid; }
    public String getTimestamp() { return timestamp; }

    public void setStatus(String status) {
        this.status = status;
    }

    public String toFileLine() {
        return id + ";" + ownerType + ";" + ownerId + ";" + customerName
                + ";" + scheduleId + ";" + seats + ";" + status + ";" + totalPaid
                + ";" + timestamp;
    }
}
