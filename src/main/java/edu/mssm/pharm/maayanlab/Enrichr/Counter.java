/**
 * Model for storing web app counts
 * 
 * @author		Edward Y. Chen
 * @since		04/25/2013 
 */

package edu.mssm.pharm.maayanlab.Enrichr;

import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Transient;

import static javax.persistence.GenerationType.IDENTITY;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.DynamicUpdate;

@Entity
@DynamicUpdate
@Table(name = "counters", catalog = "enrichr", uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class Counter implements Serializable {

	private static final long serialVersionUID = -5318689976045747460L;
	
	private int id;
	private String name;
	private AtomicInteger count;	// So that the count is thread-safe

	public Counter() {
		this.count = new AtomicInteger();
	}

	public Counter(String name, int count) {
		this.name = name;
		this.count = new AtomicInteger(count);
	}

	@Id
	@GeneratedValue(strategy = IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Column(name = "name", unique = true, nullable = false, length = 20)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "count", nullable = false)
	public int getCount() {
		return this.count.get();
	}

	public void setCount(int count) {
		this.count.set(count);
	}
	
	public int incrementAndGet() {
		return this.count.incrementAndGet();
	}

	@Transient
	public int getAndIncrement() {	// Transient so Hibernate doesn't look for the AndIncrement field
		return this.count.getAndIncrement();
	}
}
