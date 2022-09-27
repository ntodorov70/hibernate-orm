/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later.
 * See the lgpl.txt file in the root directory or <http://www.gnu.org/licenses/lgpl-2.1.html>.
 */
package org.hibernate.envers.test.integration.modifiedflags;

import static junit.framework.Assert.assertEquals;
import static org.hibernate.envers.test.tools.TestTools.extractRevisionNumbers;
import static org.hibernate.envers.test.tools.TestTools.makeList;

import org.hibernate.envers.test.Priority;
import org.hibernate.envers.test.entities.components.*;
import org.hibernate.envers.test.tools.TestTools;
import org.junit.Test;

import javax.persistence.EntityManager;
import java.util.List;

/**
 * @author Adam Warski (adam at warski dot org)
 * @author Michal Skowronek (mskowr at o2 dot pl)
 */
public class HasChangedComponentProperties extends AbstractModifiedFlagsEntityTest {
    private Integer id1;
    private Integer id8;

    public boolean forceModifiedFlags() {
        return false;
    }

    @Override
    protected Class<?>[] getAnnotatedClasses() {
        return new Class[]{ComponentTestEntity.class,ComponentChainTestEntity.class};
    }

    @Test
    @Priority(10)
    public void initData() {
        // Revision 1
        EntityManager em = getEntityManager();
        em.getTransaction().begin();

        ComponentTestEntity cte1 = new ComponentTestEntity(new Component1("a", "b"), new Component2("x", "y"));
        ComponentChainTestEntity cte8 = new ComponentChainTestEntity(new Component2("a", "b"), new Component5("c", "d", new Component1("x", "y")));
        em.persist(cte1);
        em.persist(cte8);

        em.getTransaction().commit();

        // Revision 2
        em = getEntityManager();
        em.getTransaction().begin();

        cte1 = em.find(ComponentTestEntity.class, cte1.getId());
        cte8 = em.find(ComponentChainTestEntity.class, cte8.getId());

        cte1.setComp1(new Component1("a'", "b'"));
        cte8.setComp5(new Component5("c", "d", new Component1("x'", "y'")));

        em.getTransaction().commit();

        // Revision 3
        em = getEntityManager();
        em.getTransaction().begin();

        cte1 = em.find(ComponentTestEntity.class, cte1.getId());
        cte8 = em.find(ComponentChainTestEntity.class, cte8.getId());

        cte1.setComp2(new Component2("x'", "y'"));
        cte8.setComp5(new Component5("c'", "d'", new Component1("x'", "y'")));

        em.getTransaction().commit();


        id1 = cte1.getId();
        id8 = cte8.getId();
    }

    @Test
    public void testModFlagProperties() {
        assertEquals(
            TestTools.makeSet("comp1_MOD", "comp1_str1_MOD"),
            TestTools.extractModProperties(
                metadata().getEntityBinding(
                    "org.hibernate.envers.test.entities.components.ComponentTestEntity_AUD"
                )
            )
        );
        assertEquals(
            TestTools.makeSet(
                "comp2_MOD", "comp5_MOD" ,"comp5_str7_MOD", "comp5_comp1_MOD", "comp5_comp1_str1_MOD"),
            TestTools.extractModProperties(
                metadata().getEntityBinding(
                    "org.hibernate.envers.test.entities.components.ComponentChainTestEntity_AUD"
                )
            )
        );
    }

    @Test(expected = IllegalArgumentException.class)
    public void testHasChangedNotAudited() throws Exception {
        queryForPropertyHasChanged(ComponentTestEntity.class, id1, "comp2");
    }

    @Test
    public void testHasChangedId1() throws Exception {
        List list = queryForPropertyHasChanged(ComponentTestEntity.class, id1, "comp1");
        assertEquals(2, list.size());
        assertEquals(makeList(1, 2), extractRevisionNumbers(list));

        list = queryForPropertyHasChanged(ComponentTestEntity.class, id1, "comp1_str1");
        assertEquals(2, list.size());

        list = queryForPropertyHasNotChanged(ComponentTestEntity.class, id1, "comp1_str1");
        assertEquals(0, list.size());

        list = queryForPropertyHasNotChanged(ComponentTestEntity.class, id1, "comp1");
        assertEquals(0, list.size());
    }

    @Test
    public void testHasChangedId8() throws Exception {
        List list = queryForPropertyHasChanged(ComponentChainTestEntity.class, id8, "comp5");
        assertEquals(3, list.size());
        assertEquals(makeList(1, 2, 3), extractRevisionNumbers(list));

        list = queryForPropertyHasNotChanged(ComponentChainTestEntity.class, id8, "comp5");
        assertEquals(0, list.size());

        list = queryForPropertyHasChanged(ComponentChainTestEntity.class, id8, "comp5_str7");
        assertEquals(2, list.size());
        assertEquals(makeList(1, 3), extractRevisionNumbers(list));

        list = queryForPropertyHasNotChanged(ComponentChainTestEntity.class, id8, "comp5_str7");
        assertEquals(1, list.size());
        assertEquals(makeList(2), extractRevisionNumbers(list));

        list = queryForPropertyHasChanged(ComponentChainTestEntity.class, id8, "comp5_comp1_str1");
        assertEquals(2, list.size());
        assertEquals(makeList(1, 2), extractRevisionNumbers(list));

        list = queryForPropertyHasNotChanged(ComponentChainTestEntity.class, id8, "comp5_comp1_str1");
        assertEquals(1, list.size());
        assertEquals(makeList(3), extractRevisionNumbers(list));
    }
}
