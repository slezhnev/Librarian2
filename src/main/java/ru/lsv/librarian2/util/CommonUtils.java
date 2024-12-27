package ru.lsv.librarian2.util;

import java.util.HashSet;

import org.jboss.logging.Logger;

import jakarta.transaction.Transactional;
import ru.lsv.librarian2.models.Book;
import ru.lsv.librarian2.models.LibUser;

public class CommonUtils {

	private static final Logger LOG = Logger.getLogger(CommonUtils.class);

	public static String updateSearch(String searchString) {
		if (searchString == null || searchString.isBlank()) {
			return "%";
		} else {
			String replaced = searchString.replace('*', '%');
			if (!replaced.endsWith("%")) {
				return replaced + "%";
			} else {
				return replaced;
			}
		}
	}

    @Transactional
    public static void updateDownloadedBook(Integer bookId, Integer userId) {
        LOG.infof("[bookId: %d] Removing mustRead mark and set readed", bookId);
		Book book = Book.findById(bookId);
		LibUser user = LibUser.findById(userId);
        book.mustRead = new HashSet<>(book.mustRead);
        book.readed = new HashSet<>(book.readed);
        book.mustRead.remove(user);
        book.readed.add(user);
        book.persist();
    }	
} 
