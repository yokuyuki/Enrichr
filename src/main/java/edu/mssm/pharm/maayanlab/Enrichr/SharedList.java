/**
 * Model for storing crowdsourced lists
 * 
 * @author		Edward Y. Chen
 * @since		04/17/2013 
 */

package edu.mssm.pharm.maayanlab.Enrichr;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "shared_lists", catalog = "enrichr")
public class SharedList implements Serializable {

	private static final long serialVersionUID = -2740238356775143002L;
	
	private int shareid;
	private User user;
	private String description;
	private boolean privacy;
	private Date shared;
	private Set<SharedGene> sharedGenes = new HashSet<SharedGene>(0);

	public SharedList() {
	}

	public SharedList(int shareid, User user, boolean privacy) {
		this.shareid = shareid;
		this.user = user;
		this.privacy = privacy;
	}

	public SharedList(int shareid, User user, String description, boolean privacy) {
		this.shareid = shareid;
		this.user = user;
		if (!description.trim().isEmpty()) {
			this.description = description;
		}
		this.privacy = privacy;
	}

	@Id
	@Column(name = "shareid", unique = true, nullable = false)
	public int getShareid() {
		return this.shareid;
	}

	public void setShareid(int shareid) {
		this.shareid = shareid;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "sharerid", nullable = false)
	public User getUser() {
		return this.user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	@Column(name = "description", length = 500)
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name = "privacy", nullable = false)
	public boolean isPrivacy() {
		return this.privacy;
	}

	public void setPrivacy(boolean privacy) {
		this.privacy = privacy;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "shared", nullable = false, length = 19)
	public Date getShared() {
		return this.shared;
	}

	public void setShared(Date shared) {
		this.shared = shared;
	}

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "sharedList")
	@Cascade({CascadeType.ALL})
	public Set<SharedGene> getSharedGenes() {
		return this.sharedGenes;
	}

	public void setSharedGenes(Set<SharedGene> sharedGenes) {
		this.sharedGenes = sharedGenes;
	}

}
