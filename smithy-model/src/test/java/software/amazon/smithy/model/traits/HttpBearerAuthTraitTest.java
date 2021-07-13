/*
 * Copyright Amazon.com, Inc. or its affiliates. All Rights Reserved.
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

package software.amazon.smithy.model.traits;

import java.util.Optional;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.ObjectNode;
import software.amazon.smithy.model.shapes.ShapeId;

public class HttpBearerAuthTraitTest {
    @Test
    public void loadsTraitWithoutBearerFormat() {
        TraitFactory provider = TraitFactory.createServiceFactory();
        ObjectNode node = Node.objectNode()
                .withMember("bearerFormat", "JWT");
        Optional<Trait> trait = provider.createTrait(
                HttpBearerAuthTrait.ID, ShapeId.from("ns.qux#foo"), node);
        assertTrue(trait.isPresent());
        assertThat(trait.get(), instanceOf(HttpBearerAuthTrait.class));
        HttpBearerAuthTrait auth = (HttpBearerAuthTrait) trait.get();

        assertThat(auth.getBearerFormat(), equalTo(Optional.of("JWT")));
        assertThat(auth.toNode(), equalTo(node));
        assertThat(auth.toBuilder().build(), equalTo(auth));
    }

    @Test
    public void loadsTraitWithBearerFormat() {
        TraitFactory provider = TraitFactory.createServiceFactory();
        ObjectNode node = Node.objectNode();
        Optional<Trait> trait = provider.createTrait(
                HttpBearerAuthTrait.ID, ShapeId.from("ns.qux#foo"), node);
        assertTrue(trait.isPresent());
        assertThat(trait.get(), instanceOf(HttpBearerAuthTrait.class));
        HttpBearerAuthTrait auth = (HttpBearerAuthTrait) trait.get();

        assertThat(auth.getBearerFormat(), equalTo(Optional.empty()));
        assertThat(auth.toNode(), equalTo(node));
        assertThat(auth.toBuilder().build(), equalTo(auth));
    }

    @Test
    public void defaultConstructor() {
        ObjectNode node = Node.objectNode();
        HttpBearerAuthTrait auth = new HttpBearerAuthTrait();
        assertThat(auth.getBearerFormat(), equalTo(Optional.empty()));
        assertThat(auth.toNode(), equalTo(node));
        assertThat(auth.toBuilder().build(), equalTo(auth));
    }

    @Test
    public void constructorWithObjectNode() {
        ObjectNode node = Node.objectNode()
                .withMember("bearerFormat", "JWT");
        HttpBearerAuthTrait auth = new HttpBearerAuthTrait(node);
        assertThat(auth.getBearerFormat(), equalTo(Optional.of("JWT")));
        assertThat(auth.toNode(), equalTo(node));
        assertThat(auth.toBuilder().build(), equalTo(auth));
    }
}
