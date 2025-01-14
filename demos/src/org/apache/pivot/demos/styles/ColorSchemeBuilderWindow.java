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
package org.apache.pivot.demos.styles;

import org.apache.pivot.ui.awt.JavaAwtLocalManifest;
import org.apache.pivot.wtk.graphics.ColorFactory;
import java.io.IOException;
import java.net.URL;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.beans.Bindable;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Map;
import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Resources;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Clipboard;
import org.apache.pivot.wtk.ColorChooserButton;
import org.apache.pivot.wtk.ColorChooserButtonSelectionListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.Prompt;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Spinner;
import org.apache.pivot.wtk.SpinnerSelectionListener;
import org.apache.pivot.wtk.TablePane;
import org.apache.pivot.wtk.Theme;
import org.apache.pivot.wtk.VerticalAlignment;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.content.NumericSpinnerData;
import org.apache.pivot.wtk.content.SpinnerItemRenderer;
import org.apache.pivot.wtk.skin.terra.TerraTheme;

public class ColorSchemeBuilderWindow extends Window implements Bindable {
    @BXML private TablePane colorChooserTablePane = null;
    @BXML private TablePane colorPaletteTablePane = null;
    @BXML private PushButton copyToClipboardButton = null;
    @BXML private PushButton resetPaletteButton = null;
    @BXML private Border sampleContentBorder = null;

    private ArrayList<ColorChooserButton> colorChooserButtons = new ArrayList<ColorChooserButton>();
    private ArrayList<Color> themeOriginalColors = new ArrayList<Color>(8);


    @Override
    public void initialize(Map<String, Object> namespace, URL location, Resources resources) {
        createColorPalette();

        for (int i = 0; i < 8; i++) {
            final ColorChooserButton colorChooserButton = new ColorChooserButton();
            colorChooserButtons.add(colorChooserButton);
            colorChooserButton.setSelectedColor( ColorFactory.BLACK);

            NumericSpinnerData colorSpinnerData = new NumericSpinnerData(0, 255);
            SpinnerItemRenderer colorSpinnerItemRenderer = new SpinnerItemRenderer();
            colorSpinnerItemRenderer.getStyles().put("horizontalAlignment", HorizontalAlignment.RIGHT);

            final Spinner redSpinner = new Spinner();
            redSpinner.setSpinnerData(colorSpinnerData);
            redSpinner.setItemRenderer(colorSpinnerItemRenderer);
            redSpinner.setPreferredWidth(40);
            redSpinner.setSelectedIndex(0);

            final Spinner greenSpinner = new Spinner();
            greenSpinner.setSpinnerData(colorSpinnerData);
            greenSpinner.setItemRenderer(colorSpinnerItemRenderer);
            greenSpinner.setPreferredWidth(40);
            greenSpinner.setSelectedIndex(0);

            final Spinner blueSpinner = new Spinner();
            blueSpinner.setSpinnerData(colorSpinnerData);
            blueSpinner.setItemRenderer(colorSpinnerItemRenderer);
            blueSpinner.setPreferredWidth(40);
            blueSpinner.setSelectedIndex(0);

            BoxPane colorBoxPane = new BoxPane();
            colorBoxPane.getStyles().put("fill", true);
            colorBoxPane.getStyles().put("padding", "{left:4}");
            colorBoxPane.add(redSpinner);
            colorBoxPane.add(greenSpinner);
            colorBoxPane.add(blueSpinner);

            TablePane.Row row = new TablePane.Row();
            row.add(colorChooserButton);
            row.add(colorBoxPane);

            colorChooserTablePane.getRows().add(row);

            // Add listeners
            ColorChooserButtonSelectionListener colorChooserButtonSelectionListener =
                new ColorChooserButtonSelectionListener() {
                @Override
                public void selectedColorChanged(ColorChooserButton colorChooserButton,
                    Color previousSelectedColor) {
                    Color selectedColor = colorChooserButton.getSelectedColor();
                    redSpinner.setSelectedItem(selectedColor.getRed());
                    greenSpinner.setSelectedItem(selectedColor.getGreen());
                    blueSpinner.setSelectedItem(selectedColor.getBlue());

                    // Update the theme
                    TerraTheme terraTheme = (TerraTheme)Theme.getTheme();
                    int i = colorChooserButtons.indexOf(colorChooserButton);
                    terraTheme.setBaseColor(i, colorChooserButtons.get(i).getSelectedColor());

                    // Update the palette
                    int offset = i * 3;
                    for (int j = 0; j < 3; j++) {
                        Component colorPaletteCell = colorPaletteTablePane.getRows().get(i).get(j);
                        colorPaletteCell.getStyles().put("backgroundColor", offset + j);
                    }

                    reloadContent();
                }
            };

            colorChooserButton.getColorChooserButtonSelectionListeners().add(colorChooserButtonSelectionListener);

            SpinnerSelectionListener spinnerSelectionListener = new SpinnerSelectionListener.Adapter() {
                @Override
                public void selectedItemChanged(Spinner spinner, Object previousSelectedItem) {
                    int red = (Integer)redSpinner.getSelectedItem();
                    int green = (Integer)greenSpinner.getSelectedItem();
                    int blue = (Integer)blueSpinner.getSelectedItem();

                    colorChooserButton.setSelectedColor(new Color(red, green, blue));
                }
            };

            redSpinner.getSpinnerSelectionListeners().add(spinnerSelectionListener);
            greenSpinner.getSpinnerSelectionListeners().add(spinnerSelectionListener);
            blueSpinner.getSpinnerSelectionListeners().add(spinnerSelectionListener);

            // Initialize the button color with the theme default
            TerraTheme terraTheme = (TerraTheme)Theme.getTheme();
            themeOriginalColors.add(terraTheme.getBaseColor(i));
            colorChooserButton.setSelectedColor(terraTheme.getBaseColor(i));
        }

        copyToClipboardButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                copyToClipboard();
            }
        });

        resetPaletteButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                resetPalette();
            }
        });

        // Initialize content
        reloadContent();
    }

    private void createColorPalette() {
        colorPaletteTablePane.getColumns().add(new TablePane.Column(1, true));
        colorPaletteTablePane.getColumns().add(new TablePane.Column(1, true));
        colorPaletteTablePane.getColumns().add(new TablePane.Column(1, true));

        for (int i = 0; i < 8; i++) {
            TablePane.Row row = new TablePane.Row(1, true);

            int offset = i * 3;
            row.add(createColorPaletteCell(offset));
            row.add(createColorPaletteCell(offset + 1));
            row.add(createColorPaletteCell(offset + 2));

            colorPaletteTablePane.getRows().add(row);
        }

        colorPaletteTablePane.getStyles().put("horizontalSpacing", 4);
        colorPaletteTablePane.getStyles().put("verticalSpacing", 4);
    }

    private Component createColorPaletteCell(int index) {
        Border border = new Border();
        border.getStyles().put("backgroundColor", index);

        TerraTheme terraTheme = (TerraTheme)Theme.getTheme();

        Label label = new Label();
        label.setText(Integer.toString(index));
        label.getStyles().put("font", "{size:'80%'}");
        label.getStyles().put("backgroundColor", terraTheme.getColor(4));
        label.getStyles().put("padding", 1);

        BoxPane boxPane = new BoxPane();
        boxPane.getStyles().put("padding", 2);
        boxPane.getStyles().put("horizontalAlignment", HorizontalAlignment.CENTER);
        boxPane.getStyles().put("verticalAlignment", VerticalAlignment.CENTER);

        boxPane.add(new Border(label));
        border.setContent(boxPane);

        return border;
    }

    private void reloadContent() {
        BXMLSerializer bxmlSerializer = new BXMLSerializer();

        try {
            Component sampleContent = (Component)bxmlSerializer.readObject(ColorSchemeBuilderWindow.class,
                "sample_content.bxml");
            sampleContentBorder.setContent(sampleContent);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        } catch (SerializationException exception) {
            throw new RuntimeException(exception);
        }
    }

    private void copyToClipboard() {
        ArrayList<String> colors = new ArrayList<String>(8);
        for (int i = 0; i < 8; i++) {
            ColorChooserButton colorChooserButton = colorChooserButtons.get(i);
            Color color = colorChooserButton.getSelectedColor();
            colors.add(String.format("#%02X%02X%02X",
                color.getRed(),
                color.getGreen(),
                color.getBlue()));
        }

        JavaAwtLocalManifest clipboardContent = new JavaAwtLocalManifest();

        try {
            clipboardContent.putText(JSONSerializer.toString(colors));
        } catch (SerializationException exception) {
            Prompt.prompt(exception.getMessage(), this);
        }

        Clipboard.setContent(clipboardContent);
    }

    private void resetPalette() {
        ArrayList<String> colors = new ArrayList<String>(8);
        for (int i = 0; i < 8; i++) {
            ColorChooserButton colorChooserButton = colorChooserButtons.get(i);

            colorChooserButton.setSelectedColor(themeOriginalColors.get(i));

            Color color = colorChooserButton.getSelectedColor();
            colors.add(String.format("#%02X%02X%02X",
                color.getRed(),
                color.getGreen(),
                color.getBlue()));
        }

        reloadContent();
    }

}
