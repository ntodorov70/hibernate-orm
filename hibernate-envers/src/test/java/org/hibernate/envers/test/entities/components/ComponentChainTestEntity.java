/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.envers.test.entities.components;

import org.hibernate.envers.Audited;

import javax.persistence.*;

/**
 * @author Adam Warski (adam at warski dot org)
 */
@Entity
@Table(name = "CompChainTest")
public class ComponentChainTestEntity {
	@Id
	@GeneratedValue
	private Integer id;

	@Embedded
	@Audited(withModifiedFlag = true)
	private Component2 comp2;

	@Embedded
	@Audited(withModifiedFlag = true)
	private Component5 comp5;

	public ComponentChainTestEntity() {
	}

	public ComponentChainTestEntity(Integer id, Component2 comp1, Component5 comp2) {
		this.id = id;
		this.comp2 = comp1;
		this.comp5 = comp2;
	}

	public ComponentChainTestEntity(Component2 comp1, Component5 comp5) {
		this.comp2 = comp1;
		this.comp5 = comp5;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Component2 getComp2() {
		return comp2;
	}

	public void setComp2(Component2 comp2) {
		this.comp2 = comp2;
	}

	public Component5 getComp5() {
		return comp5;
	}

	public void setComp5(Component5 comp5) {
		this.comp5 = comp5;
	}

	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( !(o instanceof ComponentChainTestEntity) ) {
			return false;
		}

		ComponentChainTestEntity that = (ComponentChainTestEntity) o;

		if (comp2 != null ? !comp2.equals( that.comp2) : that.comp2 != null ) {
			return false;
		}
		if (comp5 != null ? !comp5.equals( that.comp5) : that.comp5 != null ) {
			return false;
		}
		if ( id != null ? !id.equals( that.id ) : that.id != null ) {
			return false;
		}

		return true;
	}

	public int hashCode() {
		int result;
		result = (id != null ? id.hashCode() : 0);
		result = 31 * result + (comp2 != null ? comp2.hashCode() : 0);
		result = 31 * result + (comp5 != null ? comp5.hashCode() : 0);
		return result;
	}

	public String toString() {
		return "CTE(id = " + id + ", comp1 = " + comp2 + ", comp2 = " + comp5 + ")";
	}
}
