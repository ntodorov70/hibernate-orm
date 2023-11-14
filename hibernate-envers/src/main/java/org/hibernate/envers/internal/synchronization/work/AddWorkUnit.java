/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.envers.internal.synchronization.work;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.boot.internal.EnversService;
import org.hibernate.envers.internal.entities.PropertyData;
import org.hibernate.envers.internal.entities.mapper.ComponentPropertyMapper;
import org.hibernate.envers.internal.entities.mapper.ExtendedPropertyMapper;
import org.hibernate.envers.internal.entities.mapper.PropertyMapper;
import org.hibernate.envers.internal.tools.ArraysTools;
import org.hibernate.persister.entity.EntityPersister;

/**
 * @author Adam Warski (adam at warski dot org)
 * @author Lukasz Antoniak (lukasz dot antoniak at gmail dot com)
 */
public class AddWorkUnit extends AbstractAuditWorkUnit implements AuditWorkUnit {
	private final Object[] state;
	private final Map<String, Object> data;

	public AddWorkUnit(
			SessionImplementor sessionImplementor,
			String entityName,
			EnversService enversService,
			Serializable id, EntityPersister entityPersister, Object[] state) {
		super( sessionImplementor, entityName, enversService, id, RevisionType.ADD );

		this.data = new HashMap<>();
		this.state = state;
		this.enversService.getEntitiesConfigurations().get( getEntityName() ).getPropertyMapper().map(
				sessionImplementor,
				data,
				entityPersister.getPropertyNames(),
				state,
				null
		);
	}

	public AddWorkUnit(
			SessionImplementor sessionImplementor,
			String entityName,
			EnversService enversService,
			Serializable id,
			Map<String, Object> data) {
		super( sessionImplementor, entityName, enversService, id, RevisionType.ADD );

		this.data = data;
		final String[] propertyNames = sessionImplementor.getFactory().getMetamodel()
				.entityPersister( getEntityName() )
				.getPropertyNames();
		this.state = ArraysTools.mapToArray( data, propertyNames );
	}

	@Override
	public boolean containsWork() {
		return true;
	}

	@Override
	public Map<String, Object> generateData(Object revisionData) {
		fillDataWithId( data, revisionData );
		return data;
	}

	public Object[] getState() {
		return state;
	}

	@Override
	public AuditWorkUnit merge(AddWorkUnit second) {
		return second;
	}

	@Override
	public AuditWorkUnit merge(ModWorkUnit second) {
		return new AddWorkUnit(
				sessionImplementor,
				entityName,
				enversService,
				id,
				mergeModifiedFlags( data, second.getData() )
		);
	}

	@Override
	public AuditWorkUnit merge(DelWorkUnit second) {
		return null;
	}

	@Override
	public AuditWorkUnit merge(CollectionChangeWorkUnit second) {
		second.mergeCollectionModifiedData( data );
		return this;
	}

	@Override
	public AuditWorkUnit merge(FakeBidirectionalRelationWorkUnit second) {
		return FakeBidirectionalRelationWorkUnit.merge( second, this, second.getNestedWorkUnit() );
	}

	@Override
	public AuditWorkUnit dispatch(WorkUnitMergeVisitor first) {
		return first.merge( this );
	}

	private Map<String, Object> mergeModifiedFlags(Map<String, Object> lhs, Map<String, Object> rhs) {
		final ExtendedPropertyMapper mapper = enversService.getEntitiesConfigurations().get( getEntityName() ).getPropertyMapper();
		// Designed to take any lhs modified flag values of true and merge those into the data set for the rhs
		// This makes sure that when merging ModAuditWork with AddWorkUnit within the same transaction for the
		// same entity that the modified flags are tracked correctly.
		for ( PropertyData propertyData : mapper.getProperties().keySet() ) {
			mergeModifiedFlags(lhs, rhs, propertyData);

			// in case the property is a component go trough his properties as well
			PropertyMapper propertyMapper = mapper.getProperties().get(propertyData);
			if (propertyMapper instanceof ComponentPropertyMapper) {
				for (PropertyData componentPropertyData : ((ComponentPropertyMapper) propertyMapper).getProperties().keySet()) {
					mergeModifiedFlags(lhs, rhs, componentPropertyData);
				}
			}
		}

		return rhs;
	}

	private void mergeModifiedFlags(Map<String, Object> lhs, Map<String, Object> rhs, PropertyData propertyData) {
		if ( propertyData.isUsingModifiedFlag() && !propertyData.isSynthetic() ) {
			Boolean lhsValue = (Boolean) lhs.get( propertyData.getModifiedFlagPropertyName() );
			if ( lhsValue != null && lhsValue ) {
				Boolean rhsValue = (Boolean) rhs.get( propertyData.getModifiedFlagPropertyName() );
				if ( rhsValue == null || !rhsValue ) {
					rhs.put( propertyData.getModifiedFlagPropertyName(), true );
				}
			}
		}
	}
}
