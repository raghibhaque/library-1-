package monty.library.kyle;

public class Book {
    private String title;
    private String author;
    private int yearofPublication;
    private double isbn;
    private String description;
    private String genre;
    private boolean borrowed;

    public Book() {}

    public Book(String title, String author, int yearofPublication, double isbn,
                String description, String genre) {
        this.title = title;
        this.author = author;
        this.yearofPublication = yearofPublication;
        this.isbn = isbn;
        this.description = description;
        this.genre = genre;
    }

    public Book(String title, String author, int yearofPublication, double isbn, String description) {
        this(title, author, yearofPublication, isbn, description, "");
    }

    public Book(String title, String author, int yearofPublication, double isbn) {
        this(title, author, yearofPublication, isbn, "", "");
    }

    public String  getTitle()             { return title; }
    public String  getAuthor()            { return author; }
    public int     getYearofPublication() { return yearofPublication; }
    public double  getIsbn()              { return isbn; }
    public String  getDescription()       { return description; }
    public String  getGenre()             { return genre != null ? genre : ""; }
    public boolean isBorrowed()           { return borrowed; }

    public void setTitle(String title)                       { this.title = title; }
    public void setAuthor(String author)                     { this.author = author; }
    public void setYearofPublication(int yearofPublication)  { this.yearofPublication = yearofPublication; }
    public void setIsbn(double isbn)                         { this.isbn = isbn; }
    public void setDescription(String description)           { this.description = description; }
    public void setGenre(String genre)                       { this.genre = genre; }
    public void setBorrowed(boolean borrowed)                { this.borrowed = borrowed; }
}
