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
package pivot.tutorials.progress;

import pivot.collections.Dictionary;
import pivot.wtk.ActivityIndicator;
import pivot.wtk.Application;
import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.DesktopApplicationContext;
import pivot.wtk.Display;
import pivot.wtk.PushButton;
import pivot.wtk.Window;
import pivot.wtkx.Bindable;

public class ActivityIndicators extends Bindable implements Application {
    @Load(resourceName="activity_indicators.wtkx") private Window window;
    @Bind(fieldName="window") private ActivityIndicator activityIndicator1;
    @Bind(fieldName="window") private ActivityIndicator activityIndicator2;
    @Bind(fieldName="window") private ActivityIndicator activityIndicator3;
    @Bind(fieldName="window") private PushButton activityButton;

    public void startup(Display display, Dictionary<String, String> properties)
        throws Exception {
        bind();

        activityButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                activityIndicator1.setActive(!activityIndicator1.isActive());
                activityIndicator2.setActive(!activityIndicator2.isActive());
                activityIndicator3.setActive(!activityIndicator3.isActive());
                updateButtonData();
            }
        });

        updateButtonData();

        window.open(display);
    }

    public boolean shutdown(boolean optional) {
        if (window != null) {
            window.close();
        }

        return true;
    }

    public void suspend() {
    }

    public void resume() {
    }

    private void updateButtonData() {
        activityButton.setButtonData(activityIndicator1.isActive() ? "Stop" : "Start");
    }

    public static void main(String[] args) {
        DesktopApplicationContext.main(ActivityIndicators.class, args);
    }
}