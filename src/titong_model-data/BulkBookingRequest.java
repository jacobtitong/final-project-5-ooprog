public class BulkBookingRequest {
    public BulkBookingRequest(int id, String requesterName, String contactInfo, int scheduleId,
            int seatCount, String status, int reviewerId) {
    }

    public int getId() { return 0; }
    public String getRequesterName() { return ""; }
    public String getContactInfo() { return ""; }
    public int getScheduleId() { return 0; }
    public int getSeatCount() { return 0; }
    public String getStatus() { return ""; }
    public int getReviewerId() { return 0; }

    public void setStatus(String status) {
    }

    public void setReviewerId(int reviewerId) {
    }

    public String toFileLine() { return ""; }
}
