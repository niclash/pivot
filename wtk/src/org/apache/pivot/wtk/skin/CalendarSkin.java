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
package org.apache.pivot.wtk.skin;

import java.util.Locale;

import org.apache.pivot.util.CalendarDate;
import org.apache.pivot.wtk.Calendar;
import org.apache.pivot.wtk.CalendarListener;
import org.apache.pivot.wtk.CalendarSelectionListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.skin.ContainerSkin;


/**
 * Abstract base class for calendar skins.
 *
 * @author gbrown
 */
public abstract class CalendarSkin extends ContainerSkin
    implements CalendarListener, CalendarSelectionListener {
    @Override
    public void install(Component component) {
        super.install(component);

        Calendar calendar = (Calendar)component;
        calendar.getCalendarListeners().add(this);
        calendar.getCalendarSelectionListeners().add(this);
    }

    @Override
    public void uninstall() {
        Calendar calendar = (Calendar)getComponent();
        calendar.getCalendarListeners().remove(this);
        calendar.getCalendarSelectionListeners().remove(this);

        super.uninstall();
    }

    // Calendar events

    public void yearChanged(Calendar calendar, int previousYear) {
        // No-op
    }

    public void monthChanged(Calendar calendar, int previousMonth) {
        // No-op
    }

    public void selectedDateKeyChanged(Calendar calendar,
        String previousSelectedDateKey) {
        // No-op
    }

    public void localeChanged(Calendar calendar, Locale previousLocale) {
        invalidateComponent();
    }

    // Calendar selection events

    public void selectedDateChanged(Calendar calendar, CalendarDate previousSelectedDate) {
        // No-op
    }
}