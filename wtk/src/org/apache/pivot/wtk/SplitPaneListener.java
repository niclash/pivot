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
 * Split pane listener interface.
 *
 * @author tvolkert
 */
public interface SplitPaneListener {
    /**
     * Called when a split pane's top left component has changed.
     *
     * @param splitPane
     * @param previousTopLeftComponent
     */
    public void topLeftComponentChanged(SplitPane splitPane, Component previousTopLeftComponent);

    /**
     * Called when a split pane's bottom right component has changed.
     *
     * @param splitPane
     * @param previousBottomRightComponent
     */
    public void bottomRightComponentChanged(SplitPane splitPane, Component previousBottomRightComponent);

    /**
     * Called when a split pane's orientation has changed.
     *
     * @param splitPane
     */
    public void orientationChanged(SplitPane splitPane);

    /**
     * Called when a split pane's primary region has changed.
     *
     * @param splitPane
     */
    public void primaryRegionChanged(SplitPane splitPane);

    /**
     * Called when a split pane's split location has changed.
     *
     * @param splitPane
     * @param previousSplitLocation
     */
    public void splitLocationChanged(SplitPane splitPane, int previousSplitLocation);

    /**
     * Called when a split pane's split bounds have changed.
     *
     * @param splitPane
     * @param previousSplitBounds
     */
    public void splitBoundsChanged(SplitPane splitPane, Span previousSplitBounds);

    /**
     * Called when a split pane's locked flag has changed.
     *
     * @param splitPane
     */
    public void lockedChanged(SplitPane splitPane);
}