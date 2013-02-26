package edu.mssm.pharm.maayanlab.Enrichr;

// Generated Dec 13, 2012 12:26:26 PM by Hibernate Tools 4.0.0

import static javax.persistence.GenerationType.IDENTITY;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

@Entity
@Table(name = "users", catalog = "enrichr", uniqueConstraints = @UniqueConstraint(columnNames = "email"))
public class User implements Serializable {

	private static final long serialVersionUID = 1893085998342363733L;
	
	private Integer userid;
	private String email;
	private String salt;
	private String password;
	private String first;
	private String last;
	private String institute;
	private Date accessed;
	private Set<List> lists;
	
	public User() {
	}

	public User(String email, String password) {
		this(email, password, null, null, null, new HashSet<List>(0));
	}
	
	public User(String email, String password, String first, String last, String institute) {
		this(email, password, first, last, institute, new HashSet<List>(0));
	}

	public User(String email, String password, String first, String last, String institute, Set<List> lists) {
		this.setEmail(email);
		this.setSalt(generateSalt());
		this.setPassword(hash(this.salt + password));
		this.setFirst(first);
		this.setLast(last);
		this.setInstitute(institute);
		this.lists = lists;
	}
	
	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "userid", unique = true, nullable = false)
	public Integer getUserid() {
		return this.userid;
	}

	// Shouldn't be used because auto-incremented by db
	public void setUserid(Integer userid) {
		this.userid = userid;
	}	
	
	@Column(name = "email", unique = true, nullable = false, length = 100)
	public String getEmail() {		
		return this.email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	@Column(name = "salt", nullable = false, length = 16)
	public String getSalt() {
		return this.salt;
	}

	public void setSalt(String salt) {
		this.salt = salt;
	}
	
	private String generateSalt() {
		Random r = new SecureRandom();
		byte[] saltBytes = new byte[8];
		r.nextBytes(saltBytes);
		return new BigInteger(1, saltBytes).toString(16);
	}

	@Column(name = "password", nullable = false, length = 32)
	public String getPassword() {
		return this.password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public boolean checkPassword(String password) {
		return this.password.equals(hash(this.salt + password)); 
	}
	
	private String hash(String saltedPassword) {
		String hash = null;
		
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5");
			digest.update(saltedPassword.getBytes());
			hash = new BigInteger(1, digest.digest()).toString(16);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		
		return hash;
	}

	@Column(name = "first", length = 50)
	public String getFirst() {
		return this.first;
	}

	public void setFirst(String first) {		
		if (first !=null && !first.isEmpty())
			this.first = first;
	}

	@Column(name = "last", length = 200)
	public String getLast() {
		return this.last;
	}

	public void setLast(String last) {
		if (last != null && !last.isEmpty())
			this.last = last;
	}

	@Column(name = "institute", length = 200)
	public String getInstitute() {
		return this.institute;
	}

	public void setInstitute(String institute) {
		if (institute != null && !institute.isEmpty())
			this.institute = institute;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "accessed", nullable = false, length = 19)
	public Date getAccessed() {
		return this.accessed;
	}

	// Shouldn't be used because it uses default timestamp by db
	public void setAccessed(Date accessed) {
		this.accessed = accessed;
	}
	
	// Use this to update timestamp
	public void updateAccessed() {
		this.accessed = null;
	}
	
	@OneToMany(fetch = FetchType.LAZY)
	@Cascade({CascadeType.ALL})
	@JoinColumn(name = "ownerid")
	public Set<List> getLists() {
		return this.lists;
	}

	public void setLists(Set<List> lists) {
		this.lists = lists;
	}
	
	@Override
	public String toString() {
		StringBuilder output = new StringBuilder();
		output.append("userid: ").append(userid).append(", ")
			  .append("email: ").append(email).append(", ")
			  .append("salt: ").append(salt).append(", ")
			  .append("password: ").append(password);
			  
		if (first != null)
			output.append(",").append("first: ").append(first);
		if (last != null)
			output.append(", ").append("last: ").append(last);
		if (institute != null)
			output.append(", ").append("institute: ").append(institute);
		
		output.append(", ").append("accessed: ").append(accessed);
		
		return output.toString();
	}
}
