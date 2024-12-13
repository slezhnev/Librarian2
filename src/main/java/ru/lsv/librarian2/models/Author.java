package ru.lsv.librarian2.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jboss.resteasy.reactive.RestPath;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Sort;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.transaction.Transactional;
import ru.lsv.librarian2.util.AccessUtils;

@Entity
@Cacheable
public class Author extends PanacheEntityBase {

	/**
	 * Unique key
	 */
	@Id
	@GeneratedValue
	@Column(name = "author_id")
	public Integer authorId;
	/**
	 * Author first name
	 */
	public String firstName;
	/**
	 * Author middle name
	 */
	public String middleName;
	/**
	 * Author last name
	 */
	public String lastName;
	/**
	 * Author books
	 */
	@ManyToMany(mappedBy="authors")
	public List<Book> books;
	/**
	 * Library
	 */
	@ManyToOne
	@JoinColumn(name = "library_id")
	public Library library;

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "<" + authorId + ">";
	}

	@Transactional
	public static Optional<Author> addIfNotExists(Author author, Library library) {
		Optional<Author> existedAuthor = find(
				"from Author where firstName = ?1 and middleName = ?2 and lastName = ?3 and library.library_id=?4",
				author.firstName, author.middleName, author.lastName, library.libraryId).singleResultOptional();
		if (existedAuthor.isPresent()) {
			return existedAuthor;
		} else {
			author.library = library;
			author.persist();
			return Optional.of(author);
		}
	}

	public static List<Author> search(String lastNameSearch, Integer libraryId) {
		return list("from Author where lastName like ?1 and library.library_id=?2", Sort.by("lastName"),
				AccessUtils.updateSearch(lastNameSearch), libraryId);
	}

	@SuppressWarnings("unused")
	private static class SearchAuthor {
		private final Integer authorId;
		private final Integer userId;
		private final Long totalInSerie;

		public SearchAuthor(Integer authorId, Integer userId, Long totalInSerie) {
			super();
			this.authorId = authorId;
			this.userId = userId;
			this.totalInSerie = totalInSerie;
		}
	}

	public static List<Author> searchWithNewBooks(@RestPath Integer userId, String lastNameSearch) {
		List<SearchAuthor> authorIds = find(
				"select a.authorId, u.userId, count(b.bookId) as totalInSerie from Author a left join a.books b left join b.readed u "
						+ "where b.serieName like ?1 and (u.userId = ?2 or u.userId is null) "
						+ "group by a.authorId, u.userId order by a.authorId, u.userId",
				AccessUtils.updateSearch(lastNameSearch), userId).project(SearchAuthor.class).list();
		SearchAuthor prev = null;
		List<Integer> foundAuthors = new ArrayList<>();
		for (SearchAuthor curr : authorIds) {
			if (prev != null && curr != null && prev.authorId != null && prev.authorId.equals(curr.authorId)) {
				foundAuthors.add(curr.authorId);
			}
			prev = curr;
		}
		return list("from Author where authorId in ?1", foundAuthors);
	}

}
