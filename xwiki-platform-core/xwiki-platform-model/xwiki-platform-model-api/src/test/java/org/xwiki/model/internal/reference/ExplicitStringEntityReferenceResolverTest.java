/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.model.internal.reference;

import org.junit.jupiter.api.Test;
import org.xwiki.model.EntityType;
import org.xwiki.model.reference.DocumentReference;
import org.xwiki.model.reference.EntityReference;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link org.xwiki.model.internal.reference.ExplicitStringEntityReferenceResolver}.
 *
 * @version $Id$
 * @since 2.2.3
 */
@ComponentTest
@ComponentList({
    DefaultSymbolScheme.class
})
public class ExplicitStringEntityReferenceResolverTest
{
    @InjectMockComponents
    private ExplicitStringEntityReferenceResolver resolver;

    @Test
    public void resolveWithExplicitDocumentReference()
    {
        EntityReference reference =
            this.resolver.resolve("", EntityType.DOCUMENT, new DocumentReference("wiki", "space", "page"));

        assertEquals("page", reference.getName());
        assertEquals(EntityType.DOCUMENT, reference.getType());
        assertEquals("space", reference.getParent().getName());
        assertEquals(EntityType.SPACE, reference.getParent().getType());
        assertEquals("wiki", reference.getParent().getParent().getName());
        assertEquals(EntityType.WIKI, reference.getParent().getParent().getType());
    }

    @Test
    public void resolveWithExplicitEntityReference()
    {
        EntityReference reference =
            this.resolver.resolve("space.page", EntityType.DOCUMENT, new EntityReference("wiki", EntityType.WIKI));

        assertEquals("page", reference.getName());
        assertEquals(EntityType.DOCUMENT, reference.getType());
        assertEquals("space", reference.getParent().getName());
        assertEquals(EntityType.SPACE, reference.getParent().getType());
        assertEquals("wiki", reference.getParent().getParent().getName());
        assertEquals(EntityType.WIKI, reference.getParent().getParent().getType());
    }

    @Test
    public void resolveWithAbsoluteReferenceAndNoExplicitReference()
    {
        EntityReference reference = this.resolver.resolve("wiki:space.page", EntityType.DOCUMENT);

        assertEquals("page", reference.getName());
        assertEquals(EntityType.DOCUMENT, reference.getType());
        assertEquals("space", reference.getParent().getName());
        assertEquals(EntityType.SPACE, reference.getParent().getType());
        assertEquals("wiki", reference.getParent().getParent().getName());
        assertEquals(EntityType.WIKI, reference.getParent().getParent().getType());
    }

    @Test
    public void resolveWithExplicitReferenceWithHoles()
    {
        EntityReference reference = this.resolver.resolve("space.page", EntityType.DOCUMENT,
            new EntityReference("page", EntityType.DOCUMENT, new EntityReference("wiki", EntityType.WIKI)));

        assertEquals("page", reference.getName());
        assertEquals(EntityType.DOCUMENT, reference.getType());
        assertEquals("space", reference.getParent().getName());
        assertEquals(EntityType.SPACE, reference.getParent().getType());
        assertEquals("wiki", reference.getParent().getParent().getName());
        assertEquals(EntityType.WIKI, reference.getParent().getParent().getType());
    }
    
    @Test
    public void resolveWithNoExplicitAndPartialReference()
    {
        IllegalArgumentException expected = assertThrows(IllegalArgumentException.class,
            () -> this.resolver.resolve("", EntityType.DOCUMENT));
        assertEquals("The resolver parameter doesn't contain an Entity Reference of type [DOCUMENT]",
            expected.getMessage());
    }

    @Test
    public void resolveWithInvalidParameterType()
    {
        IllegalArgumentException expected = assertThrows(IllegalArgumentException.class,
            () -> this.resolver.resolve("", EntityType.DOCUMENT, "wrong type"));
        assertEquals("The resolver parameter doesn't contain an Entity Reference of type [DOCUMENT]",
            expected.getMessage());
    }

    @Test
    public void resolveWithIncompleteExplicitReference()
    {
        IllegalArgumentException expected = assertThrows(IllegalArgumentException.class,
            () -> this.resolver.resolve("", EntityType.DOCUMENT, new EntityReference("wiki", EntityType.WIKI)));
        assertEquals("The resolver parameter doesn't contain an Entity Reference of type [DOCUMENT]",
            expected.getMessage());
    }
}
