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
package org.apache.pivot.wtk.content;

import java.net.URL;

import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.wtk.media.Image;

/**
 * Default list item implementation.
 */
public class ListItem {
    private Image icon;
    private String text;
    private Object userData = null;

    public ListItem() {
        this(null, null);
    }

    public ListItem(Image icon) {
        this(icon, null);
    }

    public ListItem(String text) {
        this(null, text);
    }

    public ListItem(Image icon, String text) {
        this.icon = icon;
        this.text = text;
    }

    public Image getIcon() {
        return icon;
    }

    public void setIcon(Image icon) {
        this.icon = icon;
    }

    /**
     * Sets the list item's icon by URL.
     * <p>
     * If the icon already exists in the application context resource cache,
     * the cached value will be used. Otherwise, the icon will be loaded
     * synchronously and added to the cache.
     *
     * @param iconURL
     * The location of the icon to set.
     */
    public void setIcon(URL iconURL) {
        if (iconURL == null) {
            throw new IllegalArgumentException("iconURL is null.");
        }

        Image icon = (Image)ApplicationContext.getResourceCache().get(iconURL);

        if (icon == null) {
            try {
                icon = Image.load(iconURL);
            } catch (TaskExecutionException exception) {
                throw new IllegalArgumentException(exception);
            }

            ApplicationContext.getResourceCache().put(iconURL, icon);
        }

        setIcon(icon);
    }

    /**
     * Sets the list item's icon by {@linkplain ClassLoader#getResource(String)
     * resource name}.
     *
     * @param iconName
     * The resource name of the icon to set.
     *
     * @see #setIcon(URL)
     */
    public void setIcon(String iconName) {
        if (iconName == null) {
            throw new IllegalArgumentException("iconName is null.");
        }

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        setIcon(classLoader.getResource(iconName.substring(1)));
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Object getUserData() {
        return userData;
    }

    public void setUserData(Object userData) {
        this.userData = userData;
    }
}
