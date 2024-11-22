package ru.lsv.librarian2.models;

import io.quarkus.hibernate.orm.panache.PanacheEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class LibUser extends PanacheEntity {

	@Id @GeneratedValue
	public Integer id;
	public String name;
	public String password;
	
}
