package ru.lsv.librarian2.models;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Cacheable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.transaction.Transactional;

@Entity
@Cacheable
public class FileEntity extends PanacheEntityBase {

	/**
	 * Id
	 */
	@Id
	@GeneratedValue
	@Column(name = "file_id")
	public Integer id;
	/**
	 * File name
	 */
	public String name;
	/**
	 * Size of file
	 */
	public Long size;
	/**
	 * Belongs to which library
	 */
	@ManyToOne
	@JoinColumn(name = "library_id")
	public Library library;

	@Transactional
	public static Stream<FileEntity> getEntitiesAsStream(Integer libId) {
		return stream("from FileEntity fe join fe.library l where l.libraryId = ?1", libId);
	}

	@Transactional
	public static List<FileEntity> getEntities(Integer libId) {
		return getEntitiesAsStream(libId).collect(Collectors.toList());
	}

	@Override
	public String toString() {
		return this.getClass().getSimpleName() + "<" + id + ">";
	}

}
