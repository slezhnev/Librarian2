package ru.lsv.librarian2.models;

import java.sql.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Sort;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.persistence.Cacheable;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import ru.lsv.librarian2.util.CommonUtils;

@Entity
@Cacheable
public class Book extends PanacheEntityBase {

	/**
	 * Primary key
	 */
	@Id
	@GeneratedValue
	@Column(name = "book_id")
	public Integer bookId;

	/**
	 * Book file name in zipFileName
	 */
	public String id;
	/**
	 * Book authors list
	 */
	@ManyToMany(cascade = CascadeType.ALL)
	@JoinTable(name = "book_authors", joinColumns = @JoinColumn(name = "book_id"), inverseJoinColumns = @JoinColumn(name = "author_id"))
	public List<Author> authors;
	/**
	 * Book title
	 */
	public String title;
	/**
	 * Genre
	 */
	public String genre;
	/**
	 * Language
	 */
	public String language;
	/**
	 * Source language (for translated books)
	 */
	public String sourceLanguage;
	/**
	 * Serie name (if book in serie)
	 */
	public String serieName;
	/**
	 * Number in serie (if book in serie)
	 */
	public Integer numInSerie;
	/**
	 * Zip file in which book is stored
	 */
	public String zipFileName;
	/**
	 * CRC32 of the book. Needs to remove duplicates in library
	 */
	public Long crc32;
	/**
	 * Book add time to library
	 */
	public Date addTime;

	/**
	 * Book annotation
	 */
	@Column(columnDefinition = "TEXT")
	public String annotation;

	/**
	 * Mark what book was deleted in library
	 */
	public Boolean deletedInLibrary;
	/**
	 * Library
	 */
	@ManyToOne
	@JoinColumn(name = "library_id")
	public Library library;

	/**
	 * Set of user names, who read this book
	 */
	@ElementCollection
	@Column(columnDefinition = "TEXT")
	public Set<String> readed;

	/**
	 * Set of user names, which marks this book as "must read"
	 */
	@ElementCollection
	public Set<String> mustRead;

	/**
	 * Returns boolean isReaded, based on supplied userId
	 * 
	 * @param userName userName
	 * @return is book was readed by supplied user
	 */
	public boolean isReaded(String userName) {
		if (readed != null && readed.size() > 0) {
			return readed.contains(userName);
		} else {
			return false;
		}
	}

	/**
	 * Returns boolean isMustRead, based on supplied userId
	 * 
	 * @param userName userName
	 * @return is book should be read by supplied user
	 */
	public boolean isMustRead(String userName) {
		if (mustRead != null && mustRead.size() > 0) {
			return mustRead.contains(userName);
		} else {
			return false;
		}
	}

	/**
	 * Get all books by serie
	 * 
	 * @param serie Serie name
	 * @return Books in serie
	 */
	public static List<Book> listBySerie(String serie) {
		return list("serieName", Sort.by("numInSerie"), serie);
	}

	/**
	 * Get series by partial name (LIKE syntax)
	 * 
	 * @param serieSearch Search criteria in LIKE format
	 * @return List of series
	 */
	public static List<String> searchBySerie(String serieSearch) {
		return find("select distinct serieName from Book where serieName like ?1 order by serieName",
				CommonUtils.updateSearch(serieSearch)).project(String.class).list();
	}

	/**
	 * Get read series by partial name (LIKE syntax)
	 * 
	 * @param serieSearch Search criteria in LIKE format
	 * @param userId      userId which read the serie
	 * @return set of series
	 */
	public static List<String> searchForReadedSeries(String serieSearch, String userName) {
		return find(
				"select distinct b.serieName from Book b where b.serieName like ?1 and ?2 in elements(b.readed) order by b.serieName",
				CommonUtils.updateSearch(serieSearch), userName).project(String.class).stream()
				.filter(el -> !el.isBlank()).collect(Collectors.toList());
	}

	@RegisterForReflection
	private static class ReadedSeries {
		public String serieName;
		@SuppressWarnings("unused")
		public Integer user_id;
		@SuppressWarnings("unused")
		public Long totalInSerie;

		@SuppressWarnings("unused")
		public ReadedSeries(String serieName, Integer user_id, Long totalInSerie) {
			this.serieName = serieName;
			this.user_id = user_id;
			this.totalInSerie = totalInSerie;
		}

	}

	/**
	 * Find serie which has new books
	 * 
	 * @param serieSearch Search criteria in LIKE format
	 * @param userId      userId which read the serie
	 * @return Set of series which has new books
	 */
	public static List<String> searchSeriesWithNewBooks(String serieSearch, String userName) {
		List<String> seriesWithNew = find(
				"select distinct b.serieName from Book b " +
						"where b.serieName like ?1 and ?2 not in elements(b.readed) " +
						"and b.serieName in (select distinct b.serieName from Book b where ?2 in elements(b.readed)) " +
						"order by b.serieName",
				CommonUtils.updateSearch(serieSearch), userName).project(String.class).list();
		return seriesWithNew;
	}

	/**
	 * Get a full list of book from specified author
	 * 
	 * @param authorId Author id
	 * @return List of books
	 */
	public static List<Book> listByAuthor(Integer authorId) {
		return find(
				"from Book b join b.authors a where a.authorId = ?1 order by b.serieName, b.numInSerie, b.title",
				authorId).list();
	}

	/**
	 * Search list of books by title
	 * 
	 * @param searchTitle Title to search
	 * @return List of books
	 */
	public static List<Book> searchByTitle(String searchTitle) {
		return list("from Book where title like ?1", Sort.by("title"), CommonUtils.updateSearch(searchTitle));
	}

	public static long countToDownload(String userName) {
		return count("from Book b where ?1 in elements(b.mustRead)", userName);
	}

	public static List<Integer> searchToDownload(String userName) {
		return find("select b.bookId from Book b where ?1 in elements(b.mustRead) order by b.title, b.crc32",
				userName).project(Integer.class).list();
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "<" + bookId + ">";

	}

	public String titleWithSerie() {
		if (serieName != null && !serieName.isBlank()) {
			return String.format("%s (%s - %d)", this.title, this.serieName, this.numInSerie);
		} else {
			return this.title;
		}
	}

	public String titleWithSerieAndAuthor() {
		String author = null;
		if (authors != null && !authors.isEmpty()) {
			author = String.format("%s %s", authors.getFirst().lastName, authors.getFirst().firstName);
		}
		if (serieName != null && !serieName.isBlank() && author != null) {
			return String.format("%s %s (%s - %d)", author, this.title, this.serieName, this.numInSerie);
		} else if ((serieName == null || serieName.isBlank()) && author != null) {
			return String.format("%s %s", author, this.title);
		} else {
			return this.title;
		}
	}

}
