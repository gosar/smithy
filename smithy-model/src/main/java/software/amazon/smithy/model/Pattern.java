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

package software.amazon.smithy.model;

import static java.lang.String.format;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Represents a contained pattern.
 *
 * <p>A pattern is a series of segments, some of which may be labels.
 *
 * <p>Labels may appear in the pattern in the form of "{label}". Labels must
 * not be repeated, must not contain other labels (e.g., "{fo{bar}oo}"),
 * and the label name must match the regex "^[a-zA-Z0-9_]+$". No labels can
 * appear after the query string.
 *
 * <p>Greedy labels, a specialized type of label, may be specified using
 * "{label+}". Only a single greedy label may appear in a pattern, and it
 * must be the last label in a pattern. Greedy labels may be disabled for a
 * pattern as part of the builder construction.
 */
public class Pattern {

    private final String pattern;
    private final List<Segment> segments;

    protected Pattern(Builder builder) {
        pattern = Objects.requireNonNull(builder.pattern);
        segments = Objects.requireNonNull(builder.segments);

        checkForDuplicateLabels();
        if (builder.allowsGreedyLabels) {
            checkForLabelsAfterGreedyLabels();
        } else if (segments.stream().anyMatch(Segment::isGreedyLabel)) {
            throw new InvalidPatternException("Pattern must not contain a greedy label. Found " + pattern);
        }
    }

    /**
     * Gets all segments, in order.
     *
     * @return All segments, in order, in an unmodifiable list.
     */
    public List<Segment> getSegments() {
        return Collections.unmodifiableList(segments);
    }

    /**
     * Get a list of all segments that are labels.
     *
     * @return Label segments in an unmodifiable list.
     */
    public List<Segment> getLabels() {
        return Collections.unmodifiableList(
                segments.stream().filter(Segment::isLabel).collect(Collectors.toList()));
    }

    /**
     * Get a label by case-insensitive name.
     *
     * @param name Name of the label to retrieve.
     * @return An optionally found label.
     */
    public Optional<Segment> getLabel(String name) {
        String searchKey = name.toLowerCase(Locale.US);
        return segments.stream()
                       .filter(Segment::isLabel)
                       .filter(label -> label.getContent().toLowerCase(Locale.US).equals(searchKey))
                       .findFirst();
    }

    /**
     * Gets the greedy label of the pattern, if present.
     *
     * @return Returns the optionally found segment that is a greedy label.
     */
    public Optional<Segment> getGreedyLabel() {
        return segments.stream().filter(Segment::isGreedyLabel).findFirst();
    }

    @Override
    public String toString() {
        return pattern;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Pattern && pattern.equals(((Pattern) other).pattern);
    }

    @Override
    public int hashCode() {
        return pattern.hashCode();
    }

    private void checkForDuplicateLabels() {
        Set<String> labels = new HashSet<>();
        segments.forEach(segment -> {
            if (segment.isLabel() && !labels.add(segment.getContent().toLowerCase(Locale.US))) {
                throw new InvalidPatternException(format("Label `%s` is defined more than once in pattern: %s",
                        segment.getContent(), pattern));
            }
        });
    }

    private void checkForLabelsAfterGreedyLabels() {
        // Make sure at most one greedy label exists, and that it is the
        // last label segment.
        for (int i = 0; i < segments.size(); i++) {
            Segment s = segments.get(i);
            if (s.isGreedyLabel()) {
                for (int j = i + 1; j < segments.size(); j++) {
                    if (segments.get(j).isGreedyLabel()) {
                        throw new InvalidPatternException(
                                "At most one greedy label segment may exist in a pattern: " + pattern);
                    } else if (segments.get(j).isLabel()) {
                        throw new InvalidPatternException(
                                "A greedy label must be the last label in its pattern: " + pattern);
                    }
                }
            }
        }
    }

    /**
     * @return Returns a builder used to create a Pattern.
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder used to create a Pattern.
     */
    public static final class Builder {
        private boolean allowsGreedyLabels = true;
        private String pattern;
        private List<Segment> segments;

        private Builder() {}

        public Builder allowsGreedyLabels(boolean allowsGreedyLabels) {
            this.allowsGreedyLabels = allowsGreedyLabels;
            return this;
        }

        public Builder pattern(String pattern) {
            this.pattern = pattern;
            return this;
        }

        public Builder segments(List<Segment> segments) {
            this.segments = segments;
            return this;
        }

        public Pattern build() {
            return new Pattern(this);
        }
    }

    /**
     * Segment within a Pattern.
     */
    public static final class Segment {

        public enum Type { LITERAL, LABEL, GREEDY_LABEL }

        private static final java.util.regex.Pattern LABEL_PATTERN = java.util.regex.Pattern.compile("^[a-zA-Z0-9_]+$");

        private final String asString;
        private final String content;
        private final Type segmentType;

        public Segment(String content, Type segmentType) {
            this.content = Objects.requireNonNull(content);
            this.segmentType = segmentType;

            checkForInvalidContents();

            if (segmentType == Type.GREEDY_LABEL) {
                asString = "{" + content + "+}";
            } else if (segmentType == Type.LABEL) {
                asString = "{" + content + "}";
            } else {
                asString = content;
            }
        }

        private void checkForInvalidContents() {
            if (segmentType == Type.LITERAL) {
                if (content.isEmpty()) {
                    throw new InvalidPatternException("Segments must not be empty");
                } else if (content.contains("{") || content.contains("}")) {
                    throw new InvalidPatternException(
                            "Literal segments must not contain `{` or `}` characters. Found segment `" + content + "`");
                }
            } else if (content.isEmpty()) {
                throw new InvalidPatternException("Empty label declaration in pattern.");
            } else if (!LABEL_PATTERN.matcher(content).matches()) {
                throw new InvalidPatternException(
                        "Invalid label name in pattern: '" + content + "'. Labels must satisfy the "
                                + "following regular expression: " + LABEL_PATTERN.pattern());
            }
        }

        /**
         * Parse a segment from the given offset.
         *
         * @param content Content of the segment.
         * @param offset Character offset where the segment starts.
         * @return Returns the created segment.
         * @throws InvalidPatternException if the segment is invalid.
         */
        public static Segment parse(String content, int offset) {
            if (content.length() >= 2 && content.charAt(0) == '{' && content.charAt(content.length() - 1) == '}') {
                Type labelType = content.charAt(content.length() - 2) == '+' ? Type.GREEDY_LABEL : Type.LABEL;
                content = labelType == Type.GREEDY_LABEL
                          ? content.substring(1, content.length() - 2)
                          : content.substring(1, content.length() - 1);
                return new Segment(content, labelType);
            } else {
                return new Segment(content, Type.LITERAL);
            }
        }

        /**
         * Get the content of the segment.
         *
         * <p>The return value contains the segment in its entirety for
         * non-labels, and the label name for both labels and greedy labels.
         * For example, given a segment of "{label+}", the return value of
         * getContent would be "label".
         *
         * @return Content of the segment.
         */
        public String getContent() {
            return content;
        }

        /**
         * @return True if the segment is a label.
         */
        public boolean isLabel() {
            return segmentType != Type.LITERAL;
        }

        /**
         * @return True if the segment is a greedy label.
         */
        public boolean isGreedyLabel() {
            return segmentType == Type.GREEDY_LABEL;
        }

        /**
         * Get the segment as a literal value to be used in a pattern.
         *
         * <p>Unlike the result of {@link #getContent}, the return value
         * of {@code toString} includes braces for labels and "+" for
         * greedy labels.
         *
         * @return The literal segment.
         */
        @Override
        public String toString() {
            return asString;
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof Segment && asString.equals(((Segment) other).asString);
        }

        @Override
        public int hashCode() {
            return asString.hashCode();
        }
    }
}
