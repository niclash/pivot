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
package pivot.wtk;

/**
 * Spinner item listener interface.
 *
 * @author tvolkert
 */
public interface SpinnerItemListener {
    /**
     * Called when an item is inserted into the spinner data.
     *
     * @param spinner
     * @param index
     */
    public void itemInserted(Spinner spinner, int index);

    /**
     * Called when items are removed from the spinner data.
     *
     * @param spinner
     * @param index
     * @param count
     */
    public void itemsRemoved(Spinner spinner, int index, int count);

    /**
     * Called when an item is updated within the spinner data.
     *
     * @param spinner
     * @param index
     */
    public void itemUpdated(Spinner spinner, int index);

    /**
     * Called when the spinner data is sorted
     *
     * @param spinner
     */
    public void itemsSorted(Spinner spinner);
}
