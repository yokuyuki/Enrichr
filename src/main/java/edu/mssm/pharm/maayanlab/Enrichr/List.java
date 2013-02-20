package edu.mssm.pharm.maayanlab.Enrichr;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "lists", catalog = "enrichr")
public class List implements java.io.Serializable {

	private static final long serialVersionUID = -1387864947273228907L;
	
	private int listid;
	private User user;
	private String description;
	private String key;

	public List() {
	}

	public List(int listid) {
		this.listid = listid;
	}

	public List(int listid, User user, String description) {
		this(listid, user, description, null);
	}
	
	public List(int listid, User user, String description, String key) {
		this.listid = listid;
		this.user = user;
		this.description = description;
		this.key = key;
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

	@Column(name = "key", length = 16)
	public String getKey() {
		return this.key;
	}

	public void setKey(String key) {
		this.key = key;
	}

}
