package edu.mssm.pharm.maayanlab.Enrichr;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "lists", catalog = "enrichr")
public class List implements Serializable {

	private static final long serialVersionUID = -1387864947273228907L;
	
	private int listid;
	private User user;
	private String description;
	private String passkey;

	public List() {
	}

	public List(int listid) {
		this.listid = listid;
	}

	public List(int listid, User user, String description) {
		this(listid, user, description, null);
	}
	
	public List(int listid, User user, String description, String passkey) {
		this.listid = listid;
		this.user = user;
		this.description = description;
		this.passkey = passkey;
	}

	@Id
	@Column(name = "listid", unique = true, nullable = false)
	public int getListid() {
		return this.listid;
	}

	public void setListid(int listid) {
		this.listid = listid;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ownerid")
	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Column(name = "description", length = 200)
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name = "passkey", length = 16)
	public String getPasskey() {
		return this.passkey;
	}

	public void setPasskey(String passkey) {
		this.passkey = passkey;
	}

}
