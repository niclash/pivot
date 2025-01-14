/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pivot.demos.roweditor;

import java.text.DecimalFormat;
import java.text.ParseException;

import org.apache.pivot.wtk.TextInput;

public class AmountBindMapping implements TextInput.TextBindMapping {
    protected static final DecimalFormat FORMAT = new DecimalFormat("0.00");

    @Override
    public String toString(Object value) {
        return FORMAT.format(value);
    }

    @Override
    public Object valueOf(String text) {
        // in case of an empty string to validate, return a default value, to avoid an NPE
        if (text.length() < 1)
            return "";

        try {
            return FORMAT.parse(text);
        } catch (ParseException ex) {
            throw new NumberFormatException(ex.getMessage());
        }
    }
}
