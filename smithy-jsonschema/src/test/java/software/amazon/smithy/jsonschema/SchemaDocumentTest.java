/*
 * Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package software.amazon.smithy.jsonschema;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import software.amazon.smithy.model.node.Node;

public class SchemaDocumentTest {
    @Test
    public void canSetIdKeyword() {
        var document = SchemaDocument.builder().idKeyword("foo").build();
        var node = document.toNode().expectObjectNode();

        assertThat(document.getIdKeyword().get(), equalTo("foo"));
        assertThat(node.size(), is(1));
        assertThat(node.getStringMember("$id").get().getValue(), equalTo("foo"));
        assertThat(document.toBuilder().build(), equalTo(document));
    }

    @Test
    public void canSetSchemaKeyword() {
        var document = SchemaDocument.builder().schemaKeyword("foo").build();
        var node = document.toNode().expectObjectNode();

        assertThat(document.getSchemaKeyword().get(), equalTo("foo"));
        assertThat(node.size(), is(1));
        assertThat(node.getStringMember("$schema").get().getValue(), equalTo("foo"));
        assertThat(document.toBuilder().build(), equalTo(document));
    }

    @Test
    public void canSetRootSchema() {
        var document = SchemaDocument.builder()
                .rootSchema(Schema.builder().type("string").build())
                .build();
        var node = document.toNode().expectObjectNode();

        assertThat(document.getRootSchema().getType().get(), equalTo("string"));
        assertThat(node.getStringMember("type").get().getValue(), equalTo("string"));
        assertThat(document.toBuilder().build(), equalTo(document));
    }

    @Test
    public void canAddExtensions() {
        var extensions = Node.objectNode().withMember("foo", Node.from("bar"));
        var document = SchemaDocument.builder().extensions(extensions).build();
        var node = document.toNode().expectObjectNode();

        assertThat(document.getExtensions(), equalTo(extensions));
        assertThat(document.getExtension("foo").get(), equalTo(Node.from("bar")));
        assertThat(node.getStringMember("foo").get().getValue(), equalTo("bar"));
        assertThat(document.toBuilder().build(), equalTo(document));
    }

    @Test
    public void canAddDefinitions() {
        var document = SchemaDocument.builder()
                .putDefinition("#/definitions/foo", Schema.builder().type("string").build())
                .putDefinition("#/definitions/bar", Schema.builder().type("string").build())
                .build();
        var node = document.toNode().expectObjectNode();

        assertThat(document.getDefinitions().values(), hasSize(2));
        assertTrue(document.getDefinition("#/definitions/foo").isPresent());
        assertTrue(document.getDefinition("#/definitions/bar").isPresent());
        assertThat(node.getObjectMember("definitions").get().getMembers().keySet(),
                   containsInAnyOrder(Node.from("bar"), Node.from("foo")));
        assertThat(document.toBuilder().build(), equalTo(document));
    }

    @Test
    public void skipsDefinitionsNotRelative() {
        var document = SchemaDocument.builder()
                .putDefinition("http://foo.com/bar", Schema.builder().type("string").build())
                .build();
        var node = document.toNode().expectObjectNode();

        assertThat(document.getDefinitions().values(), hasSize(1));
        assertFalse(node.getMember("definitions").isPresent());
        assertThat(document.toBuilder().build(), equalTo(document));
    }

    @Test
    public void requiresSegmentsWithMultipleSlashes() {
        Assertions.assertThrows(RuntimeException.class, () -> {
            var document = SchemaDocument.builder()
                    .putDefinition("#/definitions", Schema.builder().type("string").build())
                    .build();
            document.toNode().expectObjectNode();
        });

    }

    @Test
    public void detectsConflictingPointers() {
        Assertions.assertThrows(RuntimeException.class, () -> {
            var document = SchemaDocument.builder()
                    .putDefinition("#/definitions/foo", Schema.builder().type("string").build())
                    .putDefinition("#/definitions/foo/bar", Schema.builder().type("string").build())
                    .build();
            document.toNode().expectObjectNode();
        });
    }
}
