package ru.lsv.librarian2.models;

import java.util.List;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import io.quarkus.panache.common.Sort;
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
	@OneToMany
	@JoinTable(name = "book_authors", joinColumns = @JoinColumn(name = "author_id"), inverseJoinColumns = @JoinColumn(name = "book_id"))
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

	public static String updateSearch(String searchString) {
		if (searchString == null || searchString.isBlank()) {
			return "%";
		} else {
			return searchString + "%";
		}
	}

	public static List<Author> search(String lastNameSearch) {
		return list("from Author where lastName like ?1", Sort.by("lastName"), updateSearch(lastNameSearch));
	}

}
