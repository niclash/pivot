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
package pivot.tutorials;

import java.awt.Color;
import java.awt.Font;

import pivot.collections.Dictionary;
import pivot.wtk.Application;
import pivot.wtk.Display;
import pivot.wtk.HorizontalAlignment;
import pivot.wtk.Label;
import pivot.wtk.VerticalAlignment;
import pivot.wtk.Window;

public class HelloJava implements Application {
    private Window window = null;

    public void startup(Display display, Dictionary<String, String> properties) {
        Label label = new Label();
        label.setText("Hello World!");
        label.getStyles().put("font", new Font("Arial", Font.BOLD, 24));
        label.getStyles().put("color", Color.RED);
        label.getStyles().put("horizontalAlignment",
            HorizontalAlignment.CENTER);
        label.getStyles().put("verticalAlignment",
            VerticalAlignment.CENTER);

        window = new Window();
        window.setContent(label);
        window.setMaximized(true);
        window.open(display);
    }

    public boolean shutdown(boolean optional) {
        window.close();
        return true;
    }

    public void suspend() {
    }

    public void resume() {
    }
}
