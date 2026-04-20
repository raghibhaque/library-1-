package monty.library.kyle;

public class Book {
        private String title;
        private String author;
        private int yearofPublication;
        private double isbn;

        public Book(String title, String author, int yearofPublication, double isbn) {
            this.title = title;
            this.author = author;
            this.yearofPublication = yearofPublication;
            this.isbn = isbn;
        }

        public String getTitle() {
            return title;
        }

        public String getAuthor() {
            return author;
        }

        public int getYearofPublication() {
            return yearofPublication;
        }

        public double getIsbn() {
            return isbn;
        }
    }
