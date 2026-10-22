package titong_model_data;

public class Review {
    private int id;
    private int userId;
    private int movieId;
    private int rating;
    private String comment;
    private String timestamp;

    public Review(int id, int userId, int movieId, int rating, String comment, String timestamp) {
        this.id = id;
        this.userId = userId;
        this.movieId = movieId;
        this.rating = rating;
        this.comment = comment;
        this.timestamp = timestamp;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public int getMovieId() { return movieId; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public String getTimestamp() { return timestamp; }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String toFileLine() {
        return id + ";" + userId + ";" + movieId + ";" + rating
                + ";" + comment + ";" + timestamp;
    }
}
