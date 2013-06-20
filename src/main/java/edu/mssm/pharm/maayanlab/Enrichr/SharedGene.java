/**
 * Model for storing genes from crowdsourced lists
 * 
 * @author		Edward Y. Chen
 * @since		04/17/2013 
 */

package edu.mssm.pharm.maayanlab.Enrichr;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "shared_genes", catalog = "enrichr")
public class SharedGene implements Serializable {

	private static final long serialVersionUID = -738468100827509312L;
	
	private Integer geneid;
	private SharedList sharedList;
	private String genename;

	public SharedGene() {
	}

	public SharedGene(SharedList sharedList, String genename) {
		this.sharedList = sharedList;
		this.genename = genename;
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "geneid", unique = true, nullable = false)
	public Integer getGeneid() {
		return this.geneid;
	}

	public void setGeneid(Integer geneid) {
		this.geneid = geneid;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "listid", nullable = false)
	public SharedList getSharedList() {
		return this.sharedList;
	}

	public void setSharedList(SharedList sharedList) {
		this.sharedList = sharedList;
	}

	@Column(name = "genename", nullable = false, length = 20)
	public String getGenename() {
		return this.genename;
	}

	public void setGenename(String genename) {
		this.genename = genename;
	}
}
