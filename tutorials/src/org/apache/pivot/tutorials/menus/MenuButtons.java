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
package org.apache.pivot.tutorials.menus;

import java.net.URL;

import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.Map;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Checkbox;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.RadioButton;
import org.apache.pivot.wtk.Window;

public class MenuButtons extends Window implements Bindable {
    private BoxPane componentBoxPane = null;

    public MenuButtons() {
        Action.getNamedActions().put("addPushButton", new Action() {
            @Override
            public void perform(Component source) {
                componentBoxPane.add(new PushButton("Push button"));

            }
        });

        Action.getNamedActions().put("addCheckbox", new Action() {
            @Override
            public void perform(Component source) {
                componentBoxPane.add(new Checkbox("Checkbox"));
            }
        });

        Action.getNamedActions().put("addRadioButton", new Action() {
            @Override
            public void perform(Component source) {
                componentBoxPane.add(new RadioButton("Radio button"));
            }
        });
    }

    @Override
    public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
        componentBoxPane = (BoxPane)namespace.get("componentBoxPane");
    }
}
