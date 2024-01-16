package com.smart.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "CONTACT")
public class Contact {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int cid;
	@NotBlank(message = "Name field is required !!")
	@Size(min = 2,max = 20,message ="min 2 and max 10 characters are allowed !!" )
	private String name;
	
	@NotBlank(message = "Second Name field is required !!")
	@Size(min = 2,max = 20,message ="min 2 and max 10 characters are allowed !!" )
	private String secondName;
	
	private String work;
	
	@NotBlank(message = "Email is Required !!")
	@Column(unique = true)
	@Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}",message = "Enter Valid form of mail")
	private String email;
	
	@Column(unique = true)
	@NotBlank(message = "Number is Required !!")
	@Size(min = 10,max = 10,message = "Enter the valid number !!")
	private String phone;
	
	private String image;
	
	
	@Column(length = 500)
	private String description;
	
	@ManyToOne
	private User user;
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public int getCid() {
		return cid;
	}
	public void setCid(int cid) {
		this.cid = cid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSecondName() {
		return secondName;
	}
	public void setSecondName(String secondName) {
		this.secondName = secondName;
	}
	public String getWork() {
		return work;
	}
	public void setWork(String work) {
		this.work = work;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPhone() {
		return phone;
	}
	public void setPhone(String phone) {
		this.phone = phone;
	}
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
//	@Override
//	public String toString() {
//		return "Contact [cid=" + cid + ", name=" + name + ", secondName=" + secondName + ", work=" + work + ", email="
//				+ email + ", phone=" + phone + ", image=" + image + ", description=" + description + ", user=" + user
//				+ "]";
//	}
	
	
	
}


