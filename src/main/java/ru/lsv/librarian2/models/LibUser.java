package ru.lsv.librarian2.models;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

@Entity
public class LibUser extends PanacheEntityBase {

	@Id
	@GeneratedValue
	@Column(name = "user_id")
	public Integer userId;
	public String name;
	public String password;

}
