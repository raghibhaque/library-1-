package monty.library;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import monty.library.kyle.Book;


@SpringBootTest
class LibraryApplicationTests {

	@Test
	public void testBookCreatedWithValidInput() {
		Book book = new Book("Clean Code", "Robert C. Martin", 2008, 9780132350884.0);

		assertEquals("Clean Code", book.getTitle());
		assertEquals("Robert C. Martin", book.getAuthor());
		assertEquals(2008, book.getYearofPublication());
		assertEquals(9780132350884.0, book.getIsbn(), 0.0);
	}
	@Test
	public void testBookWithBlankTitle(){
		Book book = new Book("","Kyle Joyce" , 2001 , 5454367567523.0);

		assertEquals("", book.getTitle());

	}
	@Test
	public void testBookWithBlankAuthor(){
		Book book = new Book("The Great Gatsby", "" , 1925 , 9780743273565.0);
		assertEquals("", book.getAuthor());
	}
	@Test
	public void testBookWithNegativeYearOfPublication(){
		Book book = new Book("The Great Gatsby", "F. Scott Fitzgerald" , -1925 , 9780743273565.0);
		assertEquals(-1925, book.getYearofPublication());
	}
	@Test
	public void testBookWithNegativeIsbn(){
		Book book = new Book("The Great Gatsby", "F. Scott Fitzgerald" , 1925 , -9780743273565.0);
		assertEquals(-9780743273565.0, book.getIsbn(), 0.0);
	}
	
}