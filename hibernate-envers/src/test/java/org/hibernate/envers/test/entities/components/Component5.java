/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.envers.test.entities.components;

import org.hibernate.envers.Audited;

import javax.persistence.Embeddable;
import javax.persistence.Embedded;

/**
 * @author Adam Warski (adam at warski dot org)
 */
@Embeddable
public class Component5 {
	@Audited(withModifiedFlag = true)
	private String str7;

	private String str8;

	@Embedded
	@Audited(withModifiedFlag = true)
	private Component1 comp1;

	public Component5(String str7, String str8, Component1 comp2) {
		this.str7 = str7;
		this.str8 = str8;
		this.comp1 = comp2;
	}

	public Component5() {
	}

	public String getStr8() {
		return str8;
	}

	public void setStr8(String str8) {
		this.str8 = str8;
	}

	public String getStr7() {
		return str7;
	}

	public void setStr7(String str7) {
		this.str7 = str7;
	}

	public Component1 getComp1( ){
		return this.comp1;
	}

	public void setComp1(Component1 comp1){
		this.comp1 = comp1;
	}

	public boolean equals(Object o) {
		if ( this == o ) {
			return true;
		}
		if ( !(o instanceof Component5) ) {
			return false;
		}

		Component5 that = (Component5) o;

		if (str7 != null ? !str7.equals( that.str7) : that.str7 != null ) {
			return false;
		}
		if (str8 != null ? !str8.equals( that.str8) : that.str8 != null ) {
			return false;
		}

		return true;
	}

	public int hashCode() {
		int result;
		result = (str7 != null ? str7.hashCode() : 0);
		result = 31 * result + (str8 != null ? str8.hashCode() : 0);
		return result;
	}

	public String toString() {
		return "Comp1(str1 = " + str7 + ", " + str8 + ")";
	}
}
