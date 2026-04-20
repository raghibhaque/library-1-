package monty.library;

import monty.library.kyle.Book;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.Scanner;

@SpringBootApplication
public class LibraryApplication {

	public static void main(String[] args) {
		SpringApplication.run(LibraryApplication.class, args);
		Book book;
		try (Scanner sc = new Scanner(System.in)) {
			System.out.print("Enter book title: ");
			String title = sc.nextLine();
			System.out.print("Enter book author: ");
			String author = sc.nextLine();
			System.out.print("Enter book year of publication: ");
			int yearofPublication = sc.nextInt();
			System.out.print("Enter book ISBN: ");
			double isbn = sc.nextDouble();
			book = new Book(title, author, yearofPublication, isbn);
		}
		System.out.println("Book Details:");
		System.out.println("Title: " + book.getTitle());
		System.out.println("Author: " + book.getAuthor());
		System.out.println("Year of Publication: " + book.getYearofPublication());
		System.out.println("ISBN: " + book.getIsbn());

	}

}
