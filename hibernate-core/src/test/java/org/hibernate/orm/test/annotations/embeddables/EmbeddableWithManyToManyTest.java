/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.orm.test.annotations.embeddables;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.testing.TestForIssue;
import org.hibernate.testing.orm.junit.DomainModel;
import org.hibernate.testing.orm.junit.SessionFactory;
import org.hibernate.testing.orm.junit.SessionFactoryScope;
import org.junit.jupiter.api.Test;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

import static org.assertj.core.api.Assertions.assertThat;

@DomainModel(
		annotatedClasses = {
				EmbeddableWithManyToManyTest.Product.class,
				EmbeddableWithManyToManyTest.User.class
		}
)
@SessionFactory
@TestForIssue(jiraKey = "HHH-15453")
public class EmbeddableWithManyToManyTest {

	@Test
	public void testMerge(SessionFactoryScope scope) {
		scope.inTransaction(
				session -> {
					User user = new User( 1L, "Fab" );
					session.persist( user );

					Product product = new Product( 2L, "Sugar", new Users( user ) );
					Product mergedProduct = session.merge( product );
					assertThat( mergedProduct.getUsers().getUsers() ).isNotNull();
				}
		);

		scope.inTransaction(
				session -> {
					Product product = session.get( Product.class, 2L );
					assertThat( product ).isNotNull();
					assertThat( product.getUsers().getUsers() ).isNotNull();
				}
		);
	}

	@Entity(name = "Product")
	public static class Product {
		@Id
		private Long id;

		private String name;

		@Embedded
		private Users users;

		public Product() {
		}

		public Product(Long id, String name, Users users) {
			this.id = id;
			this.name = name;
			this.users = users;
		}

		public Long getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public Users getUsers() {
			return users;
		}
	}

	@Embeddable
	public static class Users {

		@ManyToMany(cascade = { CascadeType.PERSIST, CascadeType.MERGE })
		private Set<User> users;

		public Users() {
		}

		public Users(User... users) {
			this.users = Arrays.stream( users ).collect( Collectors.toSet() );
		}

		public Set<User> getUsers() {
			return users;
		}

		public void setUsers(Set<User> users) {
			this.users = users;
		}
	}

	@Entity(name = "User")
	@Table(name = "USER_TABLE")
	public static class User {
		@Id
		private Long id;

		private String name;

		public User() {
		}

		public User(Long id, String name) {
			this.id = id;
			this.name = name;
		}

		public Long getId() {
			return id;
		}

		public String getName() {
			return name;
		}
	}
}
