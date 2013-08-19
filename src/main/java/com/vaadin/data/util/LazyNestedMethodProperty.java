/*
 * Copyright 2000-2013 Vaadin Ltd.
 * Copyright 2013 Tommi Laukkanen
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.vaadin.data.util;

import com.vaadin.data.Property;
import com.vaadin.data.util.MethodProperty.MethodException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Nested accessor based property for a bean.
 * <p/>
 * The property is specified in the dotted notation, e.g. "address.street", and
 * can contain multiple levels of nesting.
 * <p/>
 * When accessing the property value, all intermediate getters must return
 * non-null values.
 *
 * @param <T> property type
 * @see MethodProperty
 * @since 6.6
 */
public final class LazyNestedMethodProperty<T> extends AbstractProperty<T> {

    private static final long serialVersionUID = -371825070417750385L;

    /**
     * The property name.
     */
    private String propertyName;

    /**
     * The getter methods.
     */
    private transient List<Method> getMethods;
    /**
     * The setter method.
     */
    private transient Method setMethod;

    /**
     * Bean instance used as a starting point for accessing the property value.
     */
    private final Object instance;

    /**
     * The property class.
     */
    private Class<? extends T> type;

    /**
     * Special serialization to handle method references
     *
     * @param out the output stream
     * @throws IOException if exception occurs in serialization
     */
    private void writeObject(final java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();

    }

    /**
     * Special serialization to handle method references.
     *
     * @param in the input stream
     * @throws IOException            if IO exception occurs in read
     * @throws ClassNotFoundException if class not found exception occur in read
     */
    private void readObject(final java.io.ObjectInputStream in) throws IOException,
            ClassNotFoundException {
        in.defaultReadObject();

        initialize(instance.getClass(), propertyName);
    }

    /**
     * Constructs a nested method property for a given object instance. The
     * property name is a dot separated string pointing to a nested property,
     * e.g. "manager.address.street".
     *
     * @param instance     top-level bean to which the property applies
     * @param propertyName dot separated nested property name
     */
    public LazyNestedMethodProperty(final Object instance, final String propertyName) {
        this.instance = instance;
        initialize(instance.getClass(), propertyName);
    }

    /**
     * For internal use to deduce property type etc. without a bean instance.
     * Calling {@link #setValue(Object)} or {@link #getValue()} on properties
     * constructed this way is not supported.
     *
     * @param instanceClass class of the top-level bean
     * @param propertyName  the property name
     */
    LazyNestedMethodProperty(final Class<?> instanceClass, final String propertyName) {
        instance = null;
        initialize(instanceClass, propertyName);
    }

    /**
     * Initializes most of the internal fields based on the top-level bean
     * instance and property name (dot-separated string).
     *
     * @param beanClass    class of the top-level bean to which the property applies
     * @param propertyName dot separated nested property name
     */
    @SuppressWarnings("unchecked")
    private void initialize(final Class<?> beanClass, final String propertyName) {

        final List<Method> getMethods = new ArrayList<>();

        String lastSimplePropertyName = propertyName;
        Class<?> lastClass = beanClass;

        // first top-level property, then go deeper in a loop
        Class<?> propertyClass = beanClass;
        final String[] simplePropertyNames = propertyName.split("\\.");
        if (propertyName.endsWith(".") || 0 == simplePropertyNames.length) {
            throw new IllegalArgumentException(
                    "Invalid property name '"
                            + propertyName + "'");
        }
        for (final String simplePropertyName1 : simplePropertyNames) {
            final String simplePropertyName = simplePropertyName1.trim();
            if (simplePropertyName.length() > 0) {
                lastSimplePropertyName = simplePropertyName;
                lastClass = propertyClass;
                try {
                    final Method getter = MethodProperty.initGetterMethod(
                            simplePropertyName, propertyClass);
                    propertyClass = getter.getReturnType();
                    getMethods.add(getter);
                } catch (final NoSuchMethodException e) {
                    throw new IllegalArgumentException("Bean property '"
                            + simplePropertyName + "' not found", e);
                }
            } else {
                throw new IllegalArgumentException(
                        "Empty or invalid bean property identifier in '"
                                + propertyName + "'");
            }
        }

        // In case the get method is found, resolve the type
        final Method lastGetMethod = getMethods.get(getMethods.size() - 1);
        final Class<?> type = lastGetMethod.getReturnType();

        // Finds the set method
        Method setMethod = null;
        try {
            // Assure that the first letter is upper cased (it is a common
            // mistake to write firstName, not FirstName).
            if (Character.isLowerCase(lastSimplePropertyName.charAt(0))) {
                final char[] buf = lastSimplePropertyName.toCharArray();
                buf[0] = Character.toUpperCase(buf[0]);
                lastSimplePropertyName = new String(buf);
            }

            setMethod = lastClass.getMethod("set" + lastSimplePropertyName,
                    new Class[]{type});
        } catch (final NoSuchMethodException ignored) {
        }

        this.type = (Class<? extends T>) MethodProperty
                .convertPrimitiveType(type);
        this.propertyName = propertyName;
        this.getMethods = getMethods;
        this.setMethod = setMethod;
    }

    @Override
    public Class<? extends T> getType() {
        return type;
    }

    @Override
    public boolean isReadOnly() {
        return super.isReadOnly() || (null == setMethod);
    }

    /**
     * Gets the value stored in the Property. The value is resolved by calling
     * the specified getter method with the argument specified at instantiation.
     *
     * @return the value of the Property
     */
    @SuppressWarnings("unchecked")
    @Override
    public T getValue() {
        try {
            Object object = instance;
            for (final Method m : getMethods) {
                object = m.invoke(object);
                if (object == null) {
                    return null;
                }
            }
            return (T) object;
        } catch (final Throwable e) {
            throw new MethodException(this, e);
        }
    }

    /**
     * Sets the value of the property. The new value must be assignable to the
     * type of this property.
     *
     * @param newValue the New value of the property.
     * @throws ReadOnlyException if the object is in
     *                           read-only mode.
     * @see #invokeSetMethod(Object)
     */
    @Override
    public void setValue(final T newValue) throws ReadOnlyException {
        // Checks the mode
        if (isReadOnly()) {
            throw new Property.ReadOnlyException();
        }

        invokeSetMethod(newValue);
        fireValueChange();
    }

    /**
     * Internal method to actually call the setter method of the wrapped
     * property.
     *
     * @param value
     */
    void invokeSetMethod(final T value) {
        try {
            Object object = instance;
            for (int i = 0; i < getMethods.size() - 1; i++) {
                object = getMethods.get(i).invoke(object);
                if (object == null) {
                    return;
                }
            }
            setMethod.invoke(object, value);
        } catch (final InvocationTargetException e) {
            throw new MethodException(this, e.getTargetException());
        } catch (final Exception e) {
            throw new MethodException(this, e);
        }
    }

    /**
     * Returns an unmodifiable list of getter methods to call in sequence to get
     * the property value.
     * <p/>
     * This API may change in future versions.
     *
     * @return unmodifiable list of getter methods corresponding to each segment
     *         of the property name
     */
    protected List<Method> getGetMethods() {
        return Collections.unmodifiableList(getMethods);
    }

}
