package ru.lsv.librarian2.models;

import java.sql.Date;
import java.util.List;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;

@Entity
public class Book extends PanacheEntity {

	/**
	 * Primary key
	 */
	@Id @GeneratedValue
	public Integer bookId;
	/**
	 * Book file name in zipFileName
	 */
	public String id;
	/**
	 * Book authors list
	 */
	@Transient
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
	 * Book annonation
	 */
	public String annotation;

	/**
	 * Read mark
	 */
	private List<LibUser> readed;

	/**
	 * Mark about "want to read"
	 */
	private List<LibUser> mustRead;

	/**
	 * Mark what book was deleted in library
	 */
	private Boolean deletedInLibrary;
	/**
	 * Library
	 */
	private Library library;
	
}
