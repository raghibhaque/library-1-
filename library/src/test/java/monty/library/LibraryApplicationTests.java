package monty.library;

import monty.library.kyle.book;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class LibraryApplicationTests {

	@Test
	void testBookCreatedWithValidInput () {
		book book = new book("Clean Code", "Robert C. Martin", 2008, 9780132350884.0);

		assertEquals("Clean Code", book.getTitle());
		assertEquals("Robert C. Martin", book.getAuthor());
		assertEquals(2008, book.getYearofPublication());
		assertEquals(9780132350884.0, book.getIsbn(), 0.0);
	}

	@Test
	void testBookWithEmptyTitle(){

	}
}

