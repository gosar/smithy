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

package software.amazon.smithy.mqtt.traits;

import software.amazon.smithy.model.SourceLocation;
import software.amazon.smithy.model.traits.StringTrait;
import software.amazon.smithy.model.traits.TraitService;

/**
 * mqttPublish trait.
 */
public final class PublishTrait extends StringTrait {
    private static final String TRAIT = "smithy.api#mqttPublish";
    private final Topic topic;

    public PublishTrait(String topic, SourceLocation sourceLocation) {
        super(TRAIT, topic, sourceLocation);
        this.topic = Topic.parse(topic);
    }

    public PublishTrait(String topic) {
        this(topic, SourceLocation.NONE);
    }

    public static TraitService provider() {
        return TraitService.createStringProvider(TRAIT, PublishTrait::new);
    }

    /**
     * Gets the parsed topic of the trait.
     *
     * @return Returns the topic.
     */
    public Topic getTopic() {
        return topic;
    }
}
