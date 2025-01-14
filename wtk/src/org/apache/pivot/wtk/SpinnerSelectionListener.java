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
package org.apache.pivot.wtk;

/**
 * Spinner selection listener interface.
 */
public interface SpinnerSelectionListener {
    /**
     * Spinner selection listener adapter.
     */
    public static class Adapter implements SpinnerSelectionListener {
        @Override
        public void selectedIndexChanged(Spinner spinner, int previousSelectedIndex) {
        }

        @Override
        public void selectedItemChanged(Spinner spinner, Object previousSelectedItem) {
        }
    }

    /**
     * Called when a spinner's selected index has changed.
     *
     * @param spinner
     * The source of the event.
     *
     * @param previousSelectedIndex
     * If the selection changed directly, contains the index that was previously
     * selected. Otherwise, contains the current selection.
     */
    public void selectedIndexChanged(Spinner spinner, int previousSelectedIndex);

    /**
     * Called when a spinners's selected item has changed.
     *
     * @param spinner
     * The source of the event.
     *
     * @param previousSelectedItem
     * The item that was previously selected, or <tt>null</tt> if the previous selection
     * cannot be determined.
     */
    public void selectedItemChanged(Spinner spinner, Object previousSelectedItem);
}
