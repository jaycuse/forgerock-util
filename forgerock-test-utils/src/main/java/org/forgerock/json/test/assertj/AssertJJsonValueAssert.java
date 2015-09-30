/*
 * The contents of this file are subject to the terms of the Common Development and
 * Distribution License (the License). You may not use this file except in compliance with the
 * License.
 *
 * You can obtain a copy of the License at legal/CDDLv1.0.txt. See the License for the
 * specific language governing permission and limitations under the License.
 *
 * When distributing Covered Software, include this CDDL Header Notice in each file and include
 * the License file at legal/CDDLv1.0.txt. If applicable, add the following below the CDDL
 * Header, with the fields enclosed by brackets [] replaced by your own identifying
 * information: "Portions copyright [year] [name of copyright owner]".
 *
 * Copyright 2015 ForgeRock AS.
 */

package org.forgerock.json.test.assertj;

import java.util.List;
import java.util.Map;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.AbstractBooleanAssert;
import org.assertj.core.api.AbstractCharSequenceAssert;
import org.assertj.core.api.AbstractDoubleAssert;
import org.assertj.core.api.AbstractIntegerAssert;
import org.assertj.core.api.AbstractListAssert;
import org.assertj.core.api.AbstractLongAssert;
import org.assertj.core.api.AbstractMapAssert;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;
import org.assertj.core.data.MapEntry;
import org.forgerock.json.JsonPointer;
import org.forgerock.json.JsonValue;
import org.forgerock.util.promise.Promise;
import org.forgerock.util.test.assertj.AbstractAssertJPromiseAssert;

public class AssertJJsonValueAssert {

    /**
     * Creates the relevant {@code AbstractJsonValueAssert} instance for the provided {@link JsonValue}.
     * @param value The actual value.
     * @return Either a {@code ArrayJsonValueAssert} if the JsonValue is underpinned by a {@link java.util.Set}
     * or {@link java.util.List}, or a {@code ObjectJsonValueAssert} if underpinned by a {@link java.util.Map}.
     * @throws java.lang.IllegalArgumentException If the JsonValue is not a JSON array or object.
     */
    public static AbstractJsonValueAssert assertThat(JsonValue value) {
        if (value.isList() || value.isSet()) {
            return new ArrayJsonValueAssert(value);
        } else if (value.isMap()) {
            return new ObjectJsonValueAssert(value);
        } else {
            throw new IllegalArgumentException("Should be a root JsonValue - either an Object or an Array");
        }
    }

    /**
     * Creates a promise assert class for {@link JsonValue} instances.
     * <p>
     * On calling the succeeded method, the {@link AbstractJsonValueAssert#isObject()} and
     * {@link AbstractJsonValueAssert#isArray()} must be used to access array/object specific assert methods.
     * @param promise The {@link JsonValue} promise.
     * @return The assertion object.
     */
    public static AssertJJsonValuePromiseAssert assertThat(Promise<JsonValue, ?> promise) {
        return new AssertJJsonValuePromiseAssert(promise);
    }

    /**
     * An alias for {@link #assertThat(Promise)} for the case where different Promise assertThat methods
     * are statically imported and would clash.
     */
    public static AssertJJsonValuePromiseAssert assertThatJsonValue(Promise<JsonValue, ?> promise) {
        return assertThat(promise);
    }

    /**
     * An assertion class for promises that return {@code JsonValue}s.
     */
    public static class AssertJJsonValuePromiseAssert
            extends AbstractAssertJPromiseAssert<JsonValue, AssertJJsonValuePromiseAssert, PromisedJsonValueAssert> {

        private AssertJJsonValuePromiseAssert(Promise<JsonValue, ?> promise) {
            super(promise, AssertJJsonValuePromiseAssert.class);
        }

        @Override
        protected PromisedJsonValueAssert createSucceededAssert(JsonValue jsonValue) {
            return new PromisedJsonValueAssert(jsonValue);
        }
    }

    public static abstract class AbstractJsonValueAssert<T extends AbstractAssert<T, JsonValue>> extends AbstractAssert<T, JsonValue> {

        private AbstractJsonValueAssert(Class<T> type, JsonValue value) {
            super(value, type);
        }

        /**
         * Check that the {@link JsonValue} is an object.
         * @return The {@code ObjectJsonValueAssert} representation of this Assert instance.
         */
        public ObjectJsonValueAssert isObject() {
            Assertions.assertThat(actual.isMap()).isTrue();
            return new ObjectJsonValueAssert(actual);
        }

        /**
         * Check that the {@link JsonValue} is an array.
         * @return The {@code ArrayJsonValueAssert} representation of this Assert instance.
         */
        public ArrayJsonValueAssert isArray() {
            Assertions.assertThat(actual.isSet() || actual.isList()).isTrue();
            return new ArrayJsonValueAssert(actual);
        }

        /**
         * Check that the JSON is either an array or an object and is empty.
         * @return This assertion object.
         */
        public T isEmpty() {
            if (actual.isMap()) {
                isObject().isEmpty();
            } else {
                isArray().isEmpty();
            }
            return myself;
        }

        /**
         * Check that the referenced {@link JsonValue} is an object.
         * @param path The {@link JsonPointer} path to the expected value.
         * @return The {@code ObjectJsonValueAssert} for that node.
         */
        public ObjectJsonValueAssert hasObject(String path) {
            JsonValue child = actual.get(new JsonPointer(path));
            Assertions.assertThat(child.isMap()).isTrue();
            return new ObjectJsonValueAssert(child);
        }

        /**
         * Check that the referenced {@link JsonValue} is an array.
         * @param path The {@link JsonPointer} path to the expected value.
         * @return The {@code ArrayJsonValueAssert} for that node.
         */
        public ArrayJsonValueAssert hasArray(String path) {
            JsonValue child = actual.get(new JsonPointer(path));
            Assertions.assertThat(child.isList() || child.isSet()).isTrue();
            return new ArrayJsonValueAssert(child);
        }

        /**
         * Check that the referenced {@link JsonValue} is null.
         * @param path The {@link JsonPointer} path to the expected null.
         * @return This assert object, for further processing.
         */
        public T hasNull(String path) {
            JsonValue child = actual.get(new JsonPointer(path));
            Assertions.assertThat(child.isNull()).isTrue();
            return myself;
        }

        /**
         * Check that the referenced {@link JsonValue} doesn't exist in this object.
         * @param path The {@link JsonPointer} path.
         * @return This assert object, for further processing.
         */
        public T doesNotContain(String path) {
            JsonValue child = actual.get(new JsonPointer(path));
            Assertions.assertThat(child).isNull();
            return myself;
        }

        /**
         * Check that the referenced {@link JsonValue} is a boolean.
         * @param path The {@link JsonPointer} path to the expected value.
         * @param condition What condition you expect the value to match.
         * @return This assert object, for further processing.
         */
        public T booleanIs(String path, Condition<Boolean> condition) {
            JsonValue child = actual.get(new JsonPointer(path));
            Assertions.assertThat(child.isBoolean()).isTrue();
            Assertions.assertThat(child.asBoolean()).is(condition);
            return myself;
        }

        /**
         * Check that the referenced {@link JsonValue} is a boolean, irrespective of its value.
         * @param path The {@link JsonPointer} path to the expected value.
         * @return This assert object, for further processing.
         */
        public T hasBoolean(String path) {
            JsonValue child = actual.get(new JsonPointer(path));
            Assertions.assertThat(child.isBoolean()).isTrue();
            return myself;
        }

        /**
         * Get a {@link AbstractBooleanAssert} for the referenced {@link JsonValue} is a boolean, to check its value.
         * @param path The {@link JsonPointer} path to the expected value.
         * @return This {@link AbstractBooleanAssert} instance.
         */
        public AbstractBooleanAssert<?> booleanAt(String path) {
            JsonValue child = actual.get(new JsonPointer(path));
            Assertions.assertThat(child.isBoolean()).isTrue();
            return Assertions.assertThat(child.asBoolean());
        }

        /**
         * Check that the referenced {@link JsonValue} is a string, irrespective of its value.
         * @param path The {@link JsonPointer} path to the expected value.
         * @return This assert object, for further processing.
         */
        public T hasString(String path) {
            JsonValue child = actual.get(new JsonPointer(path));
            Assertions.assertThat(child.isString()).isTrue();
            return myself;
        }

        /**
         * Check the value of the referenced {@link JsonValue} string.
         * @param path The {@link JsonPointer} path to the expected value.
         * @param condition What condition you expect the value to match.
         * @return This assert object, for further processing.
         */
        public T stringIs(String path, Condition<String> condition) {
            JsonValue child = actual.get(new JsonPointer(path));
            Assertions.assertThat(child.isString()).isTrue();
            Assertions.assertThat(child.asString()).is(condition);
            return myself;
        }

        /**
         * Get a {@link AbstractCharSequenceAssert} for the referenced {@link JsonValue} is a string, to check its value.
         * @param path The {@link JsonPointer} path to the expected value.
         * @return This {@link AbstractCharSequenceAssert} instance.
         */
        public AbstractCharSequenceAssert<?, String> stringAt(String path) {
            JsonValue child = actual.get(new JsonPointer(path));
            Assertions.assertThat(child.isString()).isTrue();
            return Assertions.assertThat(child.asString());
        }

        /**
         * Check that the referenced {@link JsonValue} is a number, irrespective of its value.
         * @param path The {@link JsonPointer} path to the expected value.
         * @return This assert object, for further processing.
         */
        public T hasNumber(String path) {
            JsonValue child = actual.get(new JsonPointer(path));
            Assertions.assertThat(child.isNumber()).isTrue();
            return myself;
        }

        /**
         * Check the integer value of the referenced {@link JsonValue}.
         * @param path The {@link JsonPointer} path to the expected value.
         * @param condition What condition you expect the value to match.
         * @return This assert object, for further processing.
         */
        public T integerIs(String path, Condition<Integer> condition) {
            JsonValue child = actual.get(new JsonPointer(path));
            Assertions.assertThat(child.isNumber()).isTrue();
            Assertions.assertThat(child.asInteger()).is(condition);
            return myself;
        }

        /**
         * Get a {@link AbstractIntegerAssert} for the referenced {@link JsonValue} is an integer, to check its value.
         * @param path The {@link JsonPointer} path to the expected value.
         * @return This {@link AbstractIntegerAssert} instance.
         */
        public AbstractIntegerAssert<?> integerAt(String path) {
            JsonValue child = actual.get(new JsonPointer(path));
            Assertions.assertThat(child.isNumber()).isTrue();
            return Assertions.assertThat(child.asInteger());
        }

        /**
         * Check the long value of the referenced {@link JsonValue}.
         * @param path The {@link JsonPointer} path to the expected value.
         * @param condition What condition you expect the value to match.
         * @return This assert object, for further processing.
         */
        public T longIs(String path, Condition<Long> condition) {
            JsonValue child = actual.get(new JsonPointer(path));
            Assertions.assertThat(child.isNumber()).isTrue();
            Assertions.assertThat(child.asLong()).is(condition);
            return myself;
        }

        /**
         * Get a {@link AbstractLongAssert} for the referenced {@link JsonValue} is a long, to check its value.
         * @param path The {@link JsonPointer} path to the expected value.
         * @return This {@link AbstractLongAssert} instance.
         */
        public AbstractLongAssert<?> longAt(String path) {
            JsonValue child = actual.get(new JsonPointer(path));
            Assertions.assertThat(child.isNumber()).isTrue();
            return Assertions.assertThat(child.asLong());
        }

        /**
         * Check the double value of the referenced {@link JsonValue}.
         * @param path The {@link JsonPointer} path to the expected value.
         * @param condition What condition you expect the value to match.
         * @return This assert object, for further processing.
         */
        public T doubleIs(String path, Condition<Double> condition) {
            JsonValue child = actual.get(new JsonPointer(path));
            Assertions.assertThat(child.isNumber()).isTrue();
            Assertions.assertThat(child.asDouble()).is(condition);
            return myself;
        }

        /**
         * Get a {@link AbstractDoubleAssert} for the referenced {@link JsonValue} is a double, to check its value.
         * @param path The {@link JsonPointer} path to the expected value.
         * @return This {@link AbstractDoubleAssert} instance.
         */
        public AbstractDoubleAssert<?> doubleAt(String path) {
            JsonValue child = actual.get(new JsonPointer(path));
            Assertions.assertThat(child.isNumber()).isTrue();
            return Assertions.assertThat(child.asDouble());
        }

    }

    public static class PromisedJsonValueAssert extends AbstractJsonValueAssert<PromisedJsonValueAssert> {
        public PromisedJsonValueAssert(JsonValue value) {
            super(PromisedJsonValueAssert.class, value);
        }
    }

    public static class ObjectJsonValueAssert extends AbstractJsonValueAssert<ObjectJsonValueAssert> {

        private AbstractMapAssert<?, ? extends Map<String, Object>, String, Object> mapAssert;

        private ObjectJsonValueAssert(JsonValue value) {
            super(ObjectJsonValueAssert.class, value);
            this.mapAssert = Assertions.assertThat(value.asMap());
        }

        @Override
        public ObjectJsonValueAssert isEmpty() {
            mapAssert.isEmpty();
            return myself;
        }

        /**
         * Check that this object contains a property with the given name, and value.
         * @param key The name of the object property.
         * @param value The expected value.
         * @return This assert instance for further processing (if required).
         * @see AbstractMapAssert#containsEntry
         */
        public ObjectJsonValueAssert contains(String key, Object value) {
            mapAssert.containsEntry(key, value);
            return this;
        }

        /**
         * Check that this object contains the specified properties.
         * @param entries The expected values.
         * @return This assert instance for further processing (if required).
         * @see AbstractMapAssert#contains
         */
        public ObjectJsonValueAssert contains(MapEntry... entries) {
            mapAssert.contains(entries);
            return this;
        }

        /**
         * Check that this object only contains the specified properties.
         * @param entries The expected values.
         * @return This assert instance for further processing (if required).
         * @see AbstractMapAssert#containsOnly
         */
        public ObjectJsonValueAssert containsOnly(MapEntry... entries) {
            mapAssert.containsOnly(entries);
            return this;
        }

        /**
         * Check that this object contains exactly the specified properties.
         * @param entries The expected values.
         * @return This assert instance for further processing (if required).
         * @see AbstractMapAssert#containsExactly
         */
        public ObjectJsonValueAssert containsExactly(MapEntry... entries) {
            mapAssert.containsExactly(entries);
            return this;
        }

        /**
         * Check that this object contains a field with the specified name.
         * @param key The expected key.
         * @return This assert instance for further processing (if required).
         * @see AbstractMapAssert#containsKey
         */
        public ObjectJsonValueAssert containsField(String key) {
            mapAssert.containsKey(key);
            return this;
        }

        /**
         * Check that this object contains fields with the specified names.
         * @param keys The expected keys.
         * @return This assert instance for further processing (if required).
         * @see AbstractMapAssert#containsKeys
         */
        public ObjectJsonValueAssert containsFields(String... keys) {
            mapAssert.containsKeys(keys);
            return this;
        }

        /**
         * Check that this object does not contain a property with the given name, and value.
         * @param key The name of the object property.
         * @param value The expected value it should not equal if it exists.
         * @return This assert instance for further processing (if required).
         * @see AbstractMapAssert#doesNotContainEntry
         */
        public ObjectJsonValueAssert doesNotContain(String key, Object value) {
            mapAssert.doesNotContainEntry(key, value);
            return this;
        }

        /**
         * Check that this object does not contain a property with the given name, and value.
         * @param entries The expected entries that should not exist.
         * @return This assert instance for further processing (if required).
         * @see AbstractMapAssert#doesNotContain
         */
        public ObjectJsonValueAssert doesNotContain(MapEntry... entries) {
            mapAssert.doesNotContain(entries);
            return this;
        }

    }

    public static class ArrayJsonValueAssert extends AbstractJsonValueAssert<ArrayJsonValueAssert> {
        private AbstractListAssert<?, ? extends List<?>, Object> listAssert;

        private ArrayJsonValueAssert(JsonValue value) {
            super(ArrayJsonValueAssert.class, value);
            this.listAssert = Assertions.assertThat(value.asList());
        }

        @Override
        public ArrayJsonValueAssert isEmpty() {
            listAssert.isEmpty();
            return myself;
        }

        /**
         * Check that this array contains the given values.
         * @param values The expected values.
         * @return This assert instance for further processing (if required).
         * @see AbstractListAssert#contains
         */
        public ArrayJsonValueAssert contains(Object... values) {
            listAssert.contains(values);
            return this;
        }

        /**
         * Check that this array contains exactly the given values.
         * @param values The expected values.
         * @return This assert instance for further processing (if required).
         * @see AbstractListAssert#containsExactly
         */
        public ArrayJsonValueAssert containsExactly(Object... values) {
            listAssert.containsExactly(values);
            return this;
        }

        /**
         * Check that this array contains the given values as a sequence.
         * @param values The expected values.
         * @return This assert instance for further processing (if required).
         * @see AbstractListAssert#containsSequence
         */
        public ArrayJsonValueAssert containsSequence(Object... values) {
            listAssert.containsSequence(values);
            return this;
        }

        /**
         * Check that this array contains only the given values.
         * @param values The expected values.
         * @return This assert instance for further processing (if required).
         * @see AbstractListAssert#containsOnly
         */
        public ArrayJsonValueAssert containsOnly(Object... values) {
            listAssert.containsOnly(values);
            return this;
        }

        /**
         * Check that this array does not contain the given values.
         * @param values The values expected to not be contained.
         * @return This assert instance for further processing (if required).
         * @see AbstractListAssert#doesNotContain
         */
        public ArrayJsonValueAssert doesNotContain(Object... values) {
            listAssert.doesNotContain(values);
            return this;
        }

        /**
         * Check that this array starts with the given values.
         * @param values The expected values.
         * @return This assert instance for further processing (if required).
         * @see AbstractListAssert#startsWith
         */
        public ArrayJsonValueAssert startsWith(Object... values) {
            listAssert.startsWith(values);
            return this;
        }

        /**
         * Check that this array ends with the given values.
         * @param values The expected values.
         * @return This assert instance for further processing (if required).
         * @see AbstractListAssert#endsWith
         */
        public ArrayJsonValueAssert endsWith(Object... values) {
            listAssert.endsWith(values);
            return this;
        }

        /**
         * Check that this array does not contain duplicates.
         * @return This assert instance for further processing (if required).
         * @see AbstractListAssert#doesNotHaveDuplicates
         */
        public ArrayJsonValueAssert doesNotHaveDuplicates() {
            listAssert.doesNotHaveDuplicates();
            return this;
        }

        /**
         * Check that this array contains the given size.
         * @param size The expected size.
         * @return This assert instance for further processing (if required).
         * @see AbstractListAssert#hasSize
         */
        public ArrayJsonValueAssert hasSize(int size) {
            listAssert.hasSize(size);
            return this;
        }

    }
}
