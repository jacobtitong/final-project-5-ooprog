public class Review {
    public Review(int id, int userId, int movieId, int rating, String comment, String timestamp) {
    }

    public int getId() { return 0; }
    public int getUserId() { return 0; }
    public int getMovieId() { return 0; }
    public int getRating() { return 0; }
    public String getComment() { return ""; }
    public String getTimestamp() { return ""; }

    public void setRating(int rating) {
    }

    public void setComment(String comment) {
    }

    public void setTimestamp(String timestamp) {
    }

    public String toFileLine() { return ""; }
}
