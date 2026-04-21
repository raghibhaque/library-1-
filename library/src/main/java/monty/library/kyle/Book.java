package monty.library.kyle;

public class Book {
    private String title;
    private String author;
    private int yearofPublication;
    private double isbn;
    private String description;

    public Book(String title, String author, int yearofPublication, double isbn, String description) {
        this.title = title;
        this.author = author;
        this.yearofPublication = yearofPublication;
        this.isbn = isbn;
        this.description = description;
    }

    public Book(String title, String author, int yearofPublication, double isbn) {
        this(title, author, yearofPublication, isbn, "");
    }

    public String getTitle()            { return title; }
    public String getAuthor()           { return author; }
    public int    getYearofPublication(){ return yearofPublication; }
    public double getIsbn()             { return isbn; }
    public String getDescription()      { return description; }
}
