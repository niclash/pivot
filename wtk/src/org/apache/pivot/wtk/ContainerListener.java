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

import org.apache.pivot.collections.Sequence;

/**
 * Container listener interface.
 *
 * @author gbrown
 */
public interface ContainerListener  {
    /**
     * Called when a component has been inserted into a container's component
     * sequence.
     *
     * @param container
     * @param index
     */
    public void componentInserted(Container container, int index);

    /**
     * Called when components have been removed from a container's component
     * sequence.
     *
     * @param container
     * @param index
     * @param removed
     */
    public void componentsRemoved(Container container, int index, Sequence<Component> removed);

    /**
     * Called when a container's context key has changed.
     *
     * @param container
     * @param previousContextKey
     */
    public void contextKeyChanged(Container container, String previousContextKey);

    /**
     * Called when a container's focus traversal policy has changed.
     *
     * @param container
     * @param previousFocusTraversalPolicy
     */
    public void focusTraversalPolicyChanged(Container container,
        FocusTraversalPolicy previousFocusTraversalPolicy);
}