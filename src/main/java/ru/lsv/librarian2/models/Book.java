package ru.lsv.librarian2.models;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jboss.logging.Logger;

import io.quarkiverse.renarde.util.StringUtils;
import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.logging.Log;
import io.quarkus.panache.common.Sort;
import io.quarkus.runtime.annotations.RegisterForReflection;
import jakarta.inject.Inject;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

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
	@OneToMany
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
	 * Read mark
	 */
	@OneToMany
	@JoinTable(name = "book_readed", joinColumns = @JoinColumn(name = "book_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
	public Set<LibUser> readed;

	/**
	 * Mark about "want to read"
	 */
	@OneToMany
	@JoinTable(name = "book_must_readed", joinColumns = @JoinColumn(name = "book_id"), inverseJoinColumns = @JoinColumn(name = "user_id"))
	public Set<LibUser> mustRead;

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
	 * Get all books by serie
	 * 
	 * @param serie Serie name
	 * @return Books in serie
	 */
	public static List<Book> listBySerie(String serie) {
		return list("serieName", Sort.by("numInSerie"), serie);
	}

	private static String updateSearch(String search) {
		if (search == null || search.isBlank()) {
			return "%";
		} else {
			return search + "%";
		}
	}

	/**
	 * Get series by partial name (LIKE syntax)
	 * 
	 * @param serieSearch Search criteria in LIKE format
	 * @return List of series
	 */
	public static List<String> searchBySerie(String serieSearch) {
		return find("select distinct serieName from Book where serieName like ?1 order by serieName",
				updateSearch(serieSearch)).project(String.class).list();
	}

	public static List<String> searchForReadedSeries(String serieSearch, Integer userId) {
		return find(
				"select distinct b.serieName from Book b join b.readed r where b.serieName like ?1 and r.userId = ?2 order by b.serieName",
				updateSearch(serieSearch), userId).project(String.class).list();
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

	public static List<String> searchSeriesWithNewBooks(String serieSearch, Integer userId) {
		ReadedSeries prev = null;
		List<String> seriesWithNew = new ArrayList<>();
		List<ReadedSeries> series = find(
				"select b.serieName, u.userId, count(b.bookId) as totalInSerie from Book b " + "left join b.readed u "
						+ "where b.serieName like ?1 and (u.userId = ?2 or u.userId is null) "
						+ "group by b.serieName, u.userId order by b.serieName, u.userId",
				updateSearch(serieSearch), userId).project(ReadedSeries.class).list();
		for (ReadedSeries rs : series) {
			if (prev != null && prev.serieName != null && prev.serieName.equals(rs.serieName)) {
				seriesWithNew.add(rs.serieName);
			}
			prev = rs;
		}
		return seriesWithNew;
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "<" + bookId + ">";
	}

}
