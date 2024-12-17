package ru.lsv.librarian2.models;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class Library extends PanacheEntityBase {

	/**
	 * Primary key
	 */
	@Id
	@GeneratedValue
	@Column(name = "library_id")
	public Integer libraryId;

	/**
	 * Library name
	 */
	public String name;
	/**
	 * Library storage path
	 */
	public String storagePath;
	/**
	 * Library type Supported types: 0 - simple library, no outer links 1 -
	 * lib.rus.ec 2 - flibusta.net
	 */
	public Integer libraryKind;
	/**
	 * Link to inpx library file
	 */
	public String inpxPath;

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "<" + libraryId + ">";
	}

}
