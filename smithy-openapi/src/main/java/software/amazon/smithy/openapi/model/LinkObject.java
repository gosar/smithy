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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import software.amazon.smithy.model.ToSmithyBuilder;
import software.amazon.smithy.model.node.Node;
import software.amazon.smithy.model.node.ObjectNode;

public final class LinkObject extends Component implements ToSmithyBuilder<LinkObject> {
    private final Map<String, Node> parameters = new LinkedHashMap<>();
    private final String operationRef;
    private final String operationId;
    private final Node requestBody;
    private final String description;
    private final ServerObject server;

    private LinkObject(Builder builder) {
        super(builder);
        parameters.putAll(builder.parameters);
        operationId = builder.operationId;
        operationRef = builder.operationRef;
        requestBody = builder.requestBody;
        description = builder.description;
        server = builder.server;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Map<String, Node> getParameters() {
        return parameters;
    }

    public Optional<String> getOperationRef() {
        return Optional.ofNullable(operationRef);
    }

    public Optional<String> getOperationId() {
        return Optional.ofNullable(operationId);
    }

    public Optional<Node> getRequestBody() {
        return Optional.ofNullable(requestBody);
    }

    public Optional<String> getDescription() {
        return Optional.ofNullable(description);
    }

    public Optional<ServerObject> getServer() {
        return Optional.ofNullable(server);
    }

    @Override
    protected ObjectNode.Builder createNodeBuilder() {
        var builder = Node.objectNodeBuilder()
                .withOptionalMember("operationRef", getOperationRef().map(Node::from))
                .withOptionalMember("operationId", getOperationId().map(Node::from))
                .withOptionalMember("requestBody", getRequestBody())
                .withOptionalMember("description", getDescription().map(Node::from))
                .withOptionalMember("server", getServer());

        if (!parameters.isEmpty()) {
            builder.withMember("parameters", parameters.entrySet().stream()
                    .collect(ObjectNode.collectStringKeys(Map.Entry::getKey, Map.Entry::getValue)));
        }

        return builder;
    }

    @Override
    public Builder toBuilder() {
        return builder()
                .extensions(getExtensions())
                .parameters(parameters)
                .operationId(operationId)
                .operationRef(operationRef)
                .requestBody(requestBody)
                .description(description)
                .server(server);
    }

    public static final class Builder extends Component.Builder<Builder, LinkObject> {
        private final Map<String, Node> parameters = new LinkedHashMap<>();
        private String operationRef;
        private String operationId;
        private Node requestBody;
        private String description;
        private ServerObject server;

        private Builder() {}

        @Override
        public LinkObject build() {
            return new LinkObject(this);
        }

        public Builder operationRef(String operationRef) {
            this.operationRef = operationRef;
            return this;
        }

        public Builder operationId(String operationId) {
            this.operationId = operationId;
            return this;
        }

        public Builder parameters(Map<String, Node> parameters) {
            this.parameters.clear();
            this.parameters.putAll(parameters);
            return this;
        }

        public Builder requestBody(Node requestBody) {
            this.requestBody = requestBody;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder server(ServerObject server) {
            this.server = server;
            return this;
        }
    }
}
