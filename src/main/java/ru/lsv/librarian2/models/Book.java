package ru.lsv.librarian2.models;

import java.sql.Date;
import java.util.List;
import java.util.Set;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Sort;
import io.quarkus.runtime.annotations.RegisterForReflection;
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

	@RegisterForReflection
	public static class Serie {
		public String serieName;
	}

	/**
	 * Get series by partial name (LIKE syntax)
	 * 
	 * @param serieSearch Search criteria in LIKE format
	 * @return List of series
	 */
	public static List<String> searchBySerie(String serieSearch) {
		return find("select distinct serieName from Book where serieName like ?1 order by serieName", serieSearch)
				.project(String.class).list();
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "<" + bookId + ">";
	}

}
