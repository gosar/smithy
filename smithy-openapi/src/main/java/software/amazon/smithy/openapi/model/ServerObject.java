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

package software.amazon.smithy.openapi.model;

import java.util.Map;
import java.util.Optional;
import software.amazon.smithy.model.SmithyBuilder;
import software.amazon.smithy.model.ToSmithyBuilder;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.ObjectNode;

public final class ServerObject extends Component implements ToSmithyBuilder<ServerObject> {
    private final String url;
    private final String description;
    private final Map<String, ObjectNode> variables;

    private ServerObject(Builder builder) {
        super(builder);
        url = SmithyBuilder.requiredState("url", builder.url);
        description = builder.description;
        variables = Map.copyOf(builder.variables);
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isEmpty() {
        return false;
    }

    public String getUrl() {
        return url;
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    public Map<String, ObjectNode> getVariables() {
        return variables;
    }

    @Override
    public Builder toBuilder() {
        return builder()
                .extensions(getExtensions())
                .url(url)
                .description(description)
                .variables(variables);
    }

    @Override
    protected ObjectNode.Builder createNodeBuilder() {
        var builder = Node.objectNodeBuilder()
                .withMember("url", getUrl())
                .withOptionalMember("description", getDescription().map(Node::from));

        if (!variables.isEmpty()) {
            builder.withMember("variables", getVariables().entrySet().stream()
                    .collect(ObjectNode.collectStringKeys(Map.Entry::getKey, Map.Entry::getValue)));
        }

        return builder;
    }

    public static final class Builder extends Component.Builder<Builder, ServerObject> {
        private String url;
        private String description;
        private Map<String, ObjectNode> variables;

        @Override
        public ServerObject build() {
            return new ServerObject(this);
        }

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder variables(Map<String, ObjectNode> variables) {
            this.variables = variables;
            return this;
        }
    }
}
