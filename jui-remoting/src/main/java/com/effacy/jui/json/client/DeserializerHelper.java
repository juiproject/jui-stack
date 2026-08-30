/*******************************************************************************
 * Copyright 2024 Jeremy Buckley
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p>
 * <a href= "http://www.apache.org/licenses/LICENSE-2.0">Apache License v2</a>
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.effacy.jui.json.client;

import java.util.Date;

import org.gwtproject.json.client.JSONBoolean;
import org.gwtproject.json.client.JSONException;
import org.gwtproject.json.client.JSONNull;
import org.gwtproject.json.client.JSONNumber;
import org.gwtproject.json.client.JSONString;
import org.gwtproject.json.client.JSONValue;

import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.DateTimeFormat.PredefinedFormat;

/**
 * Support methods for deserialization.
 * 
 * @author Jeremy Buckley
 */
public final class DeserializerHelper {

	/**
	 * Parses dates of the form 2008-10-03T10:29:40.046-04:00.
	 */
	private static final DateTimeFormat DATE_PARSE = DateTimeFormat.getFormat(PredefinedFormat.ISO_8601);

	/**
	 * Converts a JSON value to a string.
	 * 
	 * @param value the value to convert.
	 * @return The value as a string.
	 * @throws JSONException On error.
	 */
	public static String getString(JSONValue value) throws JSONException {
		if ((value == null) || value instanceof JSONNull)
			return null;
		if (!(value instanceof JSONString))
			throw new JSONException();
		JSONString jsonString = (JSONString) value;
		return jsonString.stringValue();
	}

	/**
	 * Gets a character representation of the value (namely the first character in
	 * the associated json string).
	 * 
	 * @param value the value to convert.
	 * @return The first character in the string.
	 * @throws JSONException On error.
	 */
	public static Character getChar(JSONValue value) throws JSONException {
		if ((value == null) || value instanceof JSONNull)
			return 0;
		if (!(value instanceof JSONString))
			throw new JSONException();
		JSONString jsonString = (JSONString) value;
		try {
			return jsonString.stringValue().charAt(0);
		} catch (IndexOutOfBoundsException e) {
			throw new JSONException();
		}
	}

	/**
	 * Reads a number, absent or JSON-null yielding {@code null}.
	 * <p>
	 * The {@code getXxx} family below defaults an absent value to zero, which is
	 * what a <em>primitive</em> field needs — there is no other value it could
	 * take. A <em>boxed</em> field can hold {@code null}, and for those the
	 * defaulting is destructive: a genuinely unset value arrives indistinguishable
	 * from a deliberate zero, and no amount of care further up can recover the
	 * difference. So the boxed case reads through the {@code OrNull} family, and
	 * the defaulting variants delegate to it rather than parsing separately.
	 */
	public static Double getDoubleOrNull(JSONValue value) throws JSONException {
		if ((value == null) || value instanceof JSONNull)
			return null;
		if (!(value instanceof JSONNumber))
			throw new JSONException();
		JSONNumber jsonNumber = (JSONNumber) value;
		return jsonNumber.doubleValue();
	}

	public static Double getDouble(JSONValue value) throws JSONException {
		Double parsed = getDoubleOrNull(value);
		return (parsed == null) ? 0.0 : parsed;
	}

	/**
	 * See {@link #getDoubleOrNull(JSONValue)}.
	 */
	public static Float getFloatOrNull(JSONValue value) throws JSONException {
		Double parsed = getDoubleOrNull(value);
		return (parsed == null) ? null : parsed.floatValue();
	}

	public static Float getFloat(JSONValue value) throws JSONException {
		Float parsed = getFloatOrNull(value);
		return (parsed == null) ? 0.0F : parsed;
	}

	/**
	 * See {@link #getDoubleOrNull(JSONValue)}.
	 */
	public static Integer getIntOrNull(JSONValue value) throws JSONException {
		Double parsed = getDoubleOrNull(value);
		return (parsed == null) ? null : parsed.intValue();
	}

	public static Integer getInt(JSONValue value) throws JSONException {
		Integer parsed = getIntOrNull(value);
		return (parsed == null) ? 0 : parsed;
	}

	/**
	 * See {@link #getDoubleOrNull(JSONValue)}.
	 */
	public static Long getLongOrNull(JSONValue value) throws JSONException {
		Double parsed = getDoubleOrNull(value);
		return (parsed == null) ? null : parsed.longValue();
	}

	public static Long getLong(JSONValue value) throws JSONException {
		Long parsed = getLongOrNull(value);
		return (parsed == null) ? 0l : parsed;
	}

	/**
	 * See {@link #getDoubleOrNull(JSONValue)}.
	 */
	public static Short getShortOrNull(JSONValue value) throws JSONException {
		Double parsed = getDoubleOrNull(value);
		return (parsed == null) ? null : parsed.shortValue();
	}

	public static Short getShort(JSONValue value) throws JSONException {
		Short parsed = getShortOrNull(value);
		return (parsed == null) ? 0 : parsed;
	}

	/**
	 * See {@link #getDoubleOrNull(JSONValue)}.
	 */
	public static Byte getByteOrNull(JSONValue value) throws JSONException {
		Double parsed = getDoubleOrNull(value);
		return (parsed == null) ? null : parsed.byteValue();
	}

	public static Byte getByte(JSONValue value) throws JSONException {
		Byte parsed = getByteOrNull(value);
		return (parsed == null) ? 0 : parsed;
	}

	/**
	 * See {@link #getDoubleOrNull(JSONValue)}. A boxed {@code Boolean} carries
	 * three states, and collapsing the third onto {@code false} loses "not
	 * answered" — often the state that matters most.
	 */
	public static Boolean getBooleanOrNull(JSONValue value) throws JSONException {
		if ((value == null) || value instanceof JSONNull)
			return null;
		if (!(value instanceof JSONBoolean))
			throw new JSONException();
		JSONBoolean jsonBoolean = (JSONBoolean) value;
		return jsonBoolean.booleanValue();
	}

	public static Boolean getBoolean(JSONValue value) throws JSONException {
		Boolean parsed = getBooleanOrNull(value);
		return (parsed == null) ? false : parsed;
	}

	public static Date getDate(JSONValue value) throws JSONException {
		if ((value == null) || value instanceof JSONNull)
			return null;
		if (!(value instanceof JSONString || value instanceof JSONNumber))
			throw new JSONException();
		if (value instanceof JSONString) {
			String stringValue = ((JSONString) value).stringValue();
			if (stringValue.length() == 0)
				return null;
			try {

				long dateValue = Long.parseLong(stringValue);
				return new Date(dateValue);
			} catch (NumberFormatException e) {
				try {
					return DATE_PARSE.parse(stringValue);
				} catch (IllegalArgumentException e1) {
					throw new JSONException();
				}
			}
		}
		return new Date(Double.valueOf(((JSONNumber) value).doubleValue()).longValue());
	}

	public static <T extends Enum<T>> T getEnum(Class<T> klass, JSONValue value) {
		if ((value == null) || value instanceof JSONNull)
			return null;
		if (!(value instanceof JSONString))
			throw new JSONException();
		return (T) Enum.valueOf(klass, ((JSONString) value).stringValue());
	}

	/**
	 * Private constructor.
	 */
	private DeserializerHelper() {
		// Nothing.
	}
}
