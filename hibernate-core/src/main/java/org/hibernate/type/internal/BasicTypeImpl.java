/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.type.internal;

import java.util.Locale;

import org.hibernate.cache.internal.CacheKeyValueDescriptor;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.internal.util.collections.ArrayHelper;
import org.hibernate.type.AbstractSingleColumnStandardBasicType;
import org.hibernate.type.AdjustableBasicType;
import org.hibernate.type.descriptor.java.JavaType;
import org.hibernate.type.descriptor.jdbc.JdbcType;

/**
 * @author Steve Ebersole
 */
public class BasicTypeImpl<J> extends AbstractSingleColumnStandardBasicType<J> implements AdjustableBasicType<J> {
	public static final String EXTERNALIZED_PREFIX = "basicType";
	public static final String[] NO_REG_KEYS = ArrayHelper.EMPTY_STRING_ARRAY;

	private static int count;

	private final String name;

	private transient CacheKeyValueDescriptor cacheKeyValueDescriptor;

	public BasicTypeImpl(JavaType<J> jtd, JdbcType std) {
		super( std, jtd );
		name = String.format(
				Locale.ROOT,
				"%s@%s(%s,%s)",
				EXTERNALIZED_PREFIX,
				++count,
				jtd.getJavaTypeClass().getName(),
				std.getDefaultSqlTypeCode()
		);
	}

	@Override
	public String[] getRegistrationKeys() {
		// irrelevant - these are created on-the-fly
		return NO_REG_KEYS;
	}

	/**
	 * BasicTypeImpl produces a name whose sole purpose is to
	 * be used as part of interpreting Envers-produced mappings.
	 * We want to use the same exact BasicTypeImpl *instance* in
	 * the audit mapping (Envers) as is used in the audited (ORM)
	 * mapping.
	 *
	 * The name is in the form {@code `basicType@${u}(${o},${r})`}, where<ol>
	 *     <li>${u} is a unique number</li>
	 *     <li>${o} is the mapped Java type</li>
	 *     <li>${r} is the mapped SQL type (JDBC type code)</li>
	 * </ol>
	 *
	 * {@code `basicType@${u}`} is enough to uniquely identify this type instance;
	 * the Java Type and JDBC type code are informational
	 *
	 * E.g. {@code `basicType@321(java.lang.String,12)`}
	 */
	@Override
	public String getName() {
		return name;
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public CacheKeyValueDescriptor toCacheKeyDescriptor(SessionFactoryImplementor sessionFactory) {
		CacheKeyValueDescriptor cacheKeyValueDescriptor = this.cacheKeyValueDescriptor;
		if ( cacheKeyValueDescriptor == null ) {
			this.cacheKeyValueDescriptor = cacheKeyValueDescriptor = getMappedJavaType().toCacheKeyDescriptor( sessionFactory );
		}
		return cacheKeyValueDescriptor;
	}
}
