public class Movie {
    private int id;
    private String title;
    private String genre;
    private String language;
    private String ageRating;
    private String synopsis;
    private int durationMinutes;

    public Movie(int id, String title, String genre, String language,
            String ageRating, String synopsis, int durationMinutes) {
        this.id = id;
        this.title = title;
        this.genre = genre;
        this.language = language;
        this.ageRating = ageRating;
        this.synopsis = synopsis;
        this.durationMinutes = durationMinutes;
    }

    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getGenre() { return genre; }
    public String getLanguage() { return language; }
    public String getAgeRating() { return ageRating; }
    public String getSynopsis() { return synopsis; }
    public int getDurationMinutes() { return durationMinutes; }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public void setAgeRating(String ageRating) {
        this.ageRating = ageRating;
    }

    public void setSynopsis(String synopsis) {
        this.synopsis = synopsis;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public String toFileLine() {
        return id + ";" + title + ";" + genre + ";" + language
                + ";" + ageRating + ";" + synopsis + ";" + durationMinutes;
    }
}
