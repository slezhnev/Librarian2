package ru.lsv.librarian2.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Sort;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.transaction.Transactional;
import ru.lsv.librarian2.util.CommonUtils;

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
	@ManyToMany(mappedBy = "authors")
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
				"from Author where firstName = ?1 and middleName = ?2 and lastName = ?3 and library.libraryId=?4",
				author.firstName, author.middleName, author.lastName, library.libraryId).firstResultOptional();
		if (existedAuthor.isPresent()) {
			return existedAuthor;
		} else {
			author.library = library;
			author.persist();
			return Optional.of(author);
		}
	}

	public static List<Author> search(String lastNameSearch) {
		return list("from Author where lastName like ?1", Sort.by("lastName").and("firstName").and("middleName"),
				CommonUtils.updateSearch(lastNameSearch));
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

	public static List<Author> searchWithNewBooks(String userName, String lastNameSearch) {
		return list("from Author where authorId in (" +
				"select distinct a.authorId from Author a join a.books b " +
				"where a.lastName like ?1 and ?2 not in elements(b.readed) " +
				"and a.authorId in (select distinct a.authorId from Author a left join a.books b where ?2 in elements(b.readed)))",
				Sort.by("lastName").and("firstName").and("middleName"),
				CommonUtils.updateSearch(lastNameSearch), userName);
	}

	public static List<Author> searchReaded(String userName, String lastNameSearch) {
		List<Integer> authorIds = find(
				"select distinct a.authorId from Author a left join a.books b "
						+ "where a.lastName like ?1 and ?2 in elements(b.readed)",
				CommonUtils.updateSearch(lastNameSearch), userName).project(Integer.class).list();
		return list("from Author where authorId in ?1", Sort.by("lastName").and("firstName").and("middleName"),
				authorIds);
	}

}
