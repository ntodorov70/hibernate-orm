/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.type.descriptor.java;

import java.io.Reader;
import java.io.StringReader;
import java.sql.Clob;
import java.sql.Types;

import org.hibernate.cache.internal.CacheKeyValueDescriptor;
import org.hibernate.cache.internal.DefaultCacheKeyValueDescriptor;
import org.hibernate.engine.jdbc.CharacterStream;
import org.hibernate.engine.jdbc.internal.CharacterStreamImpl;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.type.descriptor.WrapperOptions;
import org.hibernate.type.descriptor.jdbc.JdbcType;
import org.hibernate.type.descriptor.jdbc.JdbcTypeIndicators;
import org.hibernate.type.descriptor.jdbc.spi.JdbcTypeRegistry;
import org.hibernate.type.spi.TypeConfiguration;

/**
 * Descriptor for {@link String} handling.
 *
 * @author Steve Ebersole
 */
public class StringJavaType extends AbstractClassJavaType<String> {
	public static final StringJavaType INSTANCE = new StringJavaType();

	public StringJavaType() {
		super( String.class );
	}

	public String toString(String value) {
		return value;
	}

	public String fromString(CharSequence string) {
		return string.toString();
	}

	@Override
	public JdbcType getRecommendedJdbcType(JdbcTypeIndicators stdIndicators) {
		final TypeConfiguration typeConfiguration = stdIndicators.getTypeConfiguration();
		final JdbcTypeRegistry stdRegistry = typeConfiguration.getJdbcTypeRegistry();

		if ( stdIndicators.isLob() ) {
			return stdIndicators.isNationalized()
					? stdRegistry.getDescriptor( Types.NCLOB )
					: stdRegistry.getDescriptor( Types.CLOB );
		}
		else if ( stdIndicators.isNationalized() ) {
			return stdRegistry.getDescriptor( Types.NVARCHAR );
		}

		return super.getRecommendedJdbcType( stdIndicators );
	}

	@SuppressWarnings("unchecked")
	public <X> X unwrap(String value, Class<X> type, WrapperOptions options) {
		if ( value == null ) {
			return null;
		}
		if ( String.class.isAssignableFrom( type ) ) {
			return (X) value;
		}
		if ( Reader.class.isAssignableFrom( type ) ) {
			return (X) new StringReader( value );
		}
		if ( CharacterStream.class.isAssignableFrom( type ) ) {
			return (X) new CharacterStreamImpl( value );
		}
		// Since NClob extends Clob, we need to check if type is an NClob
		// before checking if type is a Clob. That will ensure that
		// the correct type is returned.
		if ( DataHelper.isNClob( type ) ) {
			return (X) options.getLobCreator().createNClob( value );
		}
		if ( Clob.class.isAssignableFrom( type ) ) {
			return (X) options.getLobCreator().createClob( value );
		}

		throw unknownUnwrap( type );
	}

	public <X> String wrap(X value, WrapperOptions options) {
		if ( value == null ) {
			return null;
		}
		if (value instanceof String) {
			return (String) value;
		}
		if (value instanceof Reader) {
			return DataHelper.extractString( (Reader) value );
		}
		if (value instanceof Clob) {
			return DataHelper.extractString( (Clob) value );
		}

		throw unknownWrap( value.getClass() );
	}

	@Override
	public boolean isWider(JavaType<?> javaType) {
		switch ( javaType.getJavaType().getTypeName() ) {
			case "char":
			case "char[]":
			case "java.lang.Character":
			case "java.lang.Character[]":
				return true;
			default:
				return false;
		}
	}

	@Override
	public CacheKeyValueDescriptor toCacheKeyDescriptor(SessionFactoryImplementor sessionFactory) {
		return DefaultCacheKeyValueDescriptor.INSTANCE;
	}
}
