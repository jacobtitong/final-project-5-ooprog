public class BulkBookingRequest {
    private int id;
    private String requesterName;
    private String contactInfo;
    private int scheduleId;
    private int seatCount;
    private String status;
    private int reviewerId;

    public BulkBookingRequest(int id, String requesterName, String contactInfo, int scheduleId,
            int seatCount, String status, int reviewerId) {
        this.id = id;
        this.requesterName = requesterName;
        this.contactInfo = contactInfo;
        this.scheduleId = scheduleId;
        this.seatCount = seatCount;
        this.status = status;
        this.reviewerId = reviewerId;
    }

    public int getId() { return id; }
    public String getRequesterName() { return requesterName; }
    public String getContactInfo() { return contactInfo; }
    public int getScheduleId() { return scheduleId; }
    public int getSeatCount() { return seatCount; }
    public String getStatus() { return status; }
    public int getReviewerId() { return reviewerId; }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setReviewerId(int reviewerId) {
        this.reviewerId = reviewerId;
    }

    public String toFileLine() {
        return id + ";" + requesterName + ";" + contactInfo + ";" + scheduleId
                + ";" + seatCount + ";" + status + ";" + reviewerId;
    }
}
