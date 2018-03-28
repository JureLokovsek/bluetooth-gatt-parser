package org.sputnikdev.bluetooth.gattparser;

/*-
 * #%L
 * org.sputnikdev:bluetooth-gatt-parser
 * %%
 * Copyright (C) 2017 Sputnik Dev
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;

import org.apache.commons.beanutils.converters.BigDecimalConverter;
import org.apache.commons.beanutils.converters.BooleanConverter;
import org.apache.commons.beanutils.converters.FloatConverter;
import org.apache.commons.beanutils.converters.IntegerConverter;
import org.apache.commons.beanutils.converters.LongConverter;
import org.apache.commons.beanutils.converters.StringConverter;
import org.sputnikdev.bluetooth.gattparser.spec.Field;
import org.sputnikdev.bluetooth.gattparser.spec.FlagUtils;

/**
 * Bluetooth GATT field holder. Field holder encapsulates notion about field type and field value as well as some
 * helper methods to access field values in a user-friendly manner.
 *
 * @author Vlad Kolotov
 */
public class PrimitiveFieldHolder implements FieldHolder {

    private final Field field;
    private Object value;

    /**
     * Creates a new field holder for a given GATT field and its raw value.
     * @param field GATT field specification
     * @param value field value
     */
    public PrimitiveFieldHolder(Field field, Object value) {
        this.field = field;
        this.value = value;
    }

    /**
     * Create a new field holder for a given GATT field.
     * @param field GATT field specification
     */
    public PrimitiveFieldHolder(Field field) {
        this.field = field;
    }

    /**
     * Returns the GATT field specification.
     * @return GATT field specification
     */
    @Override
    public Field getField() {
        return field;
    }

    @Override
    public int size() {
        return field.getFormat().getSize();
    }

    /**
     * Checks whether the field is a number.
     * @return true if a given field is a number, false otherwise
     */
    public boolean isNumber() {
        return field.getFormat().isNumber();
    }

    /**
     * Checks whether the field is of boolean type.
     * @return true if a given field is of type boolean, false otherwise
     */
    public boolean isBoolean() {
        return field.getFormat().isBoolean();
    }

    /**
     * Checks whether the field is of string type.
     * @return true if a given field is of type string, false otherwise
     */
    public boolean isString() {
        return field.getFormat().isString();
    }

    /**
     * Checks whether the field is of struct type.
     * @return true if a given field is of type struct, false otherwise
     */
    public boolean isStruct() {
        return field.getFormat().isStruct();
    }

    /**
     * Returns an Integer representation of the field or a default value in case if the field cannot
     * be converted to an Integer.
     * @param def the default value to be returned if an error occurs converting the field
     * @return an Integer representation of the field
     */
    public Integer getInteger(Integer def) {
        Integer result = new IntegerConverter(null).convert(Integer.class, value);
        if (result != null) {
            return (int) Math.round(result * getMultiplier() + getOffset());
        } else {
            return def;
        }
    }

    /**
     * Returns a Long representation of the field or a default value in case if the field cannot
     * be converted to a Long.
     * @param def the default value to be returned if an error occurs converting the field
     * @return a Long representation of the field
     */
    public Long getLong(Long def) {
        Long result = new LongConverter(null).convert(Long.class, value);
        if (result != null) {
            return Math.round(result * getMultiplier() + getOffset());
        } else {
            return def;
        }
    }

    /**
     * Returns a BigInteger representation of the field or a default value in case if the field cannot
     * be converted to a BigInteger.
     * @param def the default value to be returned if an error occurs converting the field
     * @return a BigInteger representation of the field
     */
    public BigInteger getBigInteger(BigInteger def) {
        BigDecimal result = new BigDecimalConverter(null).convert(BigDecimal.class, value);
        return result != null
                ? result.multiply(BigDecimal.valueOf(getMultiplier())).add(BigDecimal.valueOf(getOffset())).setScale(0, RoundingMode.HALF_UP).toBigInteger()
                : def;
    }

    /**
     * Returns a BigDecimal representation of the field or a default value in case if the field cannot
     * be converted to a BigDecimal.
     * @param def the default value to be returned if an error occurs converting the field
     * @return a BigDecimal representation of the field
     */
    public BigDecimal getBigDecimal(BigDecimal def) {
        BigDecimal result = new BigDecimalConverter(null).convert(BigDecimal.class, value);
        return result != null
                ? result.multiply(BigDecimal.valueOf(getMultiplier()))
                : def;
    }

    /**
     * Returns a Float representation of the field or a default value in case if the field cannot
     * be converted to a Float.
     * @param def the default value to be returned if an error occurs converting the field
     * @return a Float representation of the field
     */
    public Float getFloat(Float def) {
        Float result = new FloatConverter(null).convert(Float.class, value);
        if (result != null) {
            return (float) (result * getMultiplier() + getOffset());
        } else {
            return def;
        }
    }

    /**
     * Returns a Double representation of the field or a default value in case if the field cannot
     * be converted to a Double.
     * @param def the default value to be returned if an error occurs converting the field
     * @return a Double representation of the field
     */
    public Double getDouble(Double def) {
        Double result = new FloatConverter(null).convert(Double.class, value);
        if (result != null) {
            return result * getMultiplier() + getOffset();
        } else {
            return def;
        }
    }

    /**
     * Returns a Boolean representation of the field or a default value in case if the field cannot
     * be converted to a Boolean.
     * @param def the default value to be returned if an error occurs converting the field
     * @return a Boolean representation of the field
     */
    public Boolean getBoolean(Boolean def) {
        return new BooleanConverter(def).convert(Boolean.class, value);
    }

    /**
     * Returns a String representation of the field or a default value in case if the field cannot
     * be converted to a String.
     * @param def the default value to be returned if an error occurs converting the field
     * @return a String representation of the field
     */
    public String getString(String def) {
        if (field.getFormat().isReal()
                && (field.getDecimalExponent() != null || field.getBinaryExponent() != null)) {
            return String.valueOf(getDouble());
        }
        //TODO any other smart conversions?
        return new StringConverter(def).convert(String.class, value);
    }

    /**
     * Returns an Integer representation of the field or null in case if the field cannot
     * be converted to an Integer.
     * @return an Integer representation of the field
     */
    public Integer getInteger() {
        return getInteger(null);
    }

    /**
     * Returns a Long representation of the field or null in case if the field cannot
     * be converted to a Long.
     * @return a Long representation of the field
     */
    public Long getLong() {
        return getLong(null);
    }

    /**
     * Returns a BigInteger representation of the field or null in case if the field cannot
     * be converted to a BigInteger.
     * @return a BigInteger representation of the field
     */
    public BigInteger getBigInteger() {
        return getBigInteger(null);
    }

    /**
     * Returns a BigDecimal representation of the field or null in case if the field cannot
     * be converted to a BigDecimal.
     * @return a BigInteger representation of the field
     */
    public BigDecimal getBigDecimal() {
        return getBigDecimal(null);
    }

    /**
     * Returns a Float representation of the field or null in case if the field cannot
     * be converted to a Float.
     * @return a Float representation of the field
     */
    public Float getFloat() {
        return getFloat(null);
    }

    /**
     * Returns a Double representation of the field or null in case if the field cannot
     * be converted to a Double.
     * @return a Double representation of the field
     */
    public Double getDouble() {
        return getDouble(null);
    }

    /**
     * Returns a Boolean representation of the field or null in case if the field cannot
     * be converted to a Boolean.
     * @return a Boolean representation of the field
     */
    public Boolean getBoolean() {
        return getBoolean(null);
    }

    /**
     * Returns a String representation of the field or null in case if the field cannot
     * be converted to a String.
     * @return a String representation of the field
     */
    public String getString() {
        return getString(null);
    }

    /**
     * Returns field raw value.
     * @return field raw value
     */
    public Object getRawValue() {
        return value;
    }

    /**
     * Returns field enumeration value according to its value.
     * @return fields enumeration value (or a its flag) according to its value
     */
    public String getEnumerationValue() {
        return FlagUtils.getWriteFlag(field, getInteger());
    }

    /**
     * Sets the field value into a new boolean value.
     * @param value a new field value
     */
    public void setBoolean(Boolean value) {
        this.value = value;
    }

    /**
     * Sets the field value into a new Integer value.
     * @param value a new field value
     */
    public void setInteger(Integer value) {
        this.value = value;
    }

    /**
     * Sets the field value into a new Long value.
     * @param value a new field value
     */
    public void setLong(Long value) {
        this.value = value;
    }

    /**
     * Sets the field value into a new BigInteger value.
     * @param value a new field value
     */
    public void setBigInteger(BigInteger value) {
        this.value = value;
    }

    /**
     * Sets the field value into a new Float value.
     * @param value a new field value
     */
    public void setFloat(Float value) {
        this.value = value;
    }

    /**
     * Sets the field value into a new Double value.
     * @param value a new field value
     */
    public void setDouble(Double value) {
        this.value = value;
    }

    /**
     * Sets the field value into a new String value.
     * @param value a new field value
     */
    public void setString(String value) {
        this.value = value;
    }

    /**
     * Checks whether field value is set.
     * @return true if field value is set, false otherwise
     */
    public boolean isValueSet() {
        return value != null;
    }

    @Override
    public String toString() {
        return getString();
    }

    private double getMultiplier() {
        double multiplier = 1;
        if (field.getDecimalExponent() != null) {
            multiplier = Math.pow(10, field.getDecimalExponent());
        }
        if (field.getBinaryExponent() != null) {
            multiplier *= Math.pow(2, field.getBinaryExponent());
        }
        if (field.getMultiplier() != null) {
            multiplier *= (double) field.getMultiplier();
        }
        return multiplier;
    }

    /**
     * Reads offset-to-be-added to field value received from request.
     * This is an extension to official GATT characteristic field specification, 
     * allowing to implement subset of proprietary devices that almost follow standard
     * GATT specifications.
     * @return offset as double if set, 0 if not present
     */
    private double getOffset() {
        return (field.getOffset() != null) ? field.getOffset() : 0;
    }

}
