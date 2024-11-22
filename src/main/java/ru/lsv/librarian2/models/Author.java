package ru.lsv.librarian2.models;

import java.util.List;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Author extends PanacheEntity {

	/**
	 * Unique key
	 */
	@Id @GeneratedValue
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
	public List<Book> books;
	/**
	 * Library
	 */
	public Library library;
	
	
}
