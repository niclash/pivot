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
package org.apache.pivot.tutorials;

import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.ui.awt.JavaAwtLocalManifest;
import java.io.IOException;
import java.net.URL;
import java.util.Comparator;

import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Map;
import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.CalendarDate;
import org.apache.pivot.util.Filter;
import org.apache.pivot.util.Vote;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.wtk.Action;
import org.apache.pivot.wtk.ActivityIndicator;
import org.apache.pivot.wtk.Alert;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.Border;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonGroup;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.ComponentMouseButtonListener;
import org.apache.pivot.wtk.ListButton;
import org.apache.pivot.wtk.MenuHandler;
import org.apache.pivot.wtk.DesktopApplicationContext;
import org.apache.pivot.wtk.DragSource;
import org.apache.pivot.wtk.DropAction;
import org.apache.pivot.wtk.DropTarget;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.Manifest;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.MessageType;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Point;
import org.apache.pivot.wtk.Prompt;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.Rollup;
import org.apache.pivot.wtk.RollupStateListener;
import org.apache.pivot.wtk.Slider;
import org.apache.pivot.wtk.SliderValueListener;
import org.apache.pivot.wtk.SortDirection;
import org.apache.pivot.wtk.Spinner;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TableViewSortListener;
import org.apache.pivot.wtk.TreeView;
import org.apache.pivot.wtk.Visual;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.content.CalendarDateSpinnerData;
import org.apache.pivot.wtk.content.ListItem;
import org.apache.pivot.wtk.content.NumericSpinnerData;
import org.apache.pivot.wtk.content.TableViewHeaderData;
import org.apache.pivot.wtk.content.TableViewRowComparator;
import org.apache.pivot.wtk.content.TreeBranch;
import org.apache.pivot.wtk.content.TreeNode;
import org.apache.pivot.wtk.effects.ReflectionDecorator;
import org.apache.pivot.wtk.media.Image;

public class KitchenSink implements Application, Application.AboutHandler {
    private abstract class RollupStateHandler
        implements RollupStateListener {
        @Override
        public void expandedChangeVetoed(Rollup rollup, Vote reason) {
            // No-op
        }

        @Override
        public void expandedChanged(Rollup rollup) {
            // No-op
        }
    }

    private class ButtonsRollupStateHandler extends RollupStateHandler {
        private Component component = null;

        @Override
        public Vote previewExpandedChange(Rollup rollup) {
            if (component == null) {
                BXMLSerializer bxmlSerializer = new BXMLSerializer();
                try {
                    component = (Component)bxmlSerializer.readObject(KitchenSink.class, "buttons.bxml");
                } catch(IOException exception) {
                    throw new RuntimeException(exception);
                } catch(SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                rollup.setContent(component);
            }

            return Vote.APPROVE;
        }
    }

    private class ListsRollupStateHandler extends RollupStateHandler {
        private Component component = null;
        private ListView editableListView = null;
        private ListView iconListView = null;
        private ListView checkedListView = null;
        private ListButton iconListButton = null;

        @SuppressWarnings("unchecked")
        @Override
        public Vote previewExpandedChange(Rollup rollup) {
            if (component == null) {
                BXMLSerializer bxmlSerializer = new BXMLSerializer();
                try {
                    component = (Component)bxmlSerializer.readObject(KitchenSink.class, "lists.bxml");
                } catch(IOException exception) {
                    throw new RuntimeException(exception);
                } catch(SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                editableListView = (ListView)bxmlSerializer.getNamespace().get("editableListView");
                iconListView = (ListView)bxmlSerializer.getNamespace().get("iconListView");
                checkedListView = (ListView)bxmlSerializer.getNamespace().get("checkedListView");
                iconListButton = (ListButton)bxmlSerializer.getNamespace().get("iconListButton");

                rollup.setContent(component);

                List<ListItem> listData = (List<ListItem>)editableListView.getListData();
                listData.setComparator(new Comparator<ListItem>() {
                    @Override
                    public int compare(ListItem listItem1, ListItem listItem2) {
                        String text1 = listItem1.getText();
                        String text2 = listItem2.getText();
                        return text1.compareToIgnoreCase(text2);
                    }
                });


                Filter<ListItem> disabledItemFilter = new Filter<ListItem>() {
                    @Override
                    public boolean include(ListItem listItem) {
                        return Character.toLowerCase(listItem.getText().charAt(0)) == 'c';
                    }
                };

                iconListView.setDisabledItemFilter(disabledItemFilter);
                iconListButton.setDisabledItemFilter(disabledItemFilter);

                checkedListView.setItemChecked(0, true);
                checkedListView.setItemChecked(2, true);
                checkedListView.setItemChecked(3, true);
            }

            return Vote.APPROVE;
        }
    }

    private class TextRollupStateHandler extends RollupStateHandler {
        private Component component = null;

        @Override
        public Vote previewExpandedChange(Rollup rollup) {
            if (component == null) {
                BXMLSerializer bxmlSerializer = new BXMLSerializer();
                try {
                    component = (Component)bxmlSerializer.readObject(KitchenSink.class, "text.bxml");
                } catch(IOException exception) {
                    throw new RuntimeException(exception);
                } catch(SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                rollup.setContent(component);
            }

            return Vote.APPROVE;
        }
    }

    private class CalendarsRollupStateHandler extends RollupStateHandler {
        private Component component = null;

        @Override
        public Vote previewExpandedChange(Rollup rollup) {
            if (component == null) {
                BXMLSerializer bxmlSerializer = new BXMLSerializer();
                try {
                    component = (Component)bxmlSerializer.readObject(KitchenSink.class, "calendars.bxml");
                } catch(IOException exception) {
                    throw new RuntimeException(exception);
                } catch(SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                rollup.setContent(component);
            }

            return Vote.APPROVE;
        }
    }

    private class ColorChoosersRollupStateHandler extends RollupStateHandler {
        private Component component = null;

        @Override
        public Vote previewExpandedChange(Rollup rollup) {
            if (component == null) {
                BXMLSerializer bxmlSerializer = new BXMLSerializer();
                try {
                    component = (Component)bxmlSerializer.readObject(KitchenSink.class, "color_choosers.bxml");
                } catch(IOException exception) {
                    throw new RuntimeException(exception);
                } catch(SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                rollup.setContent(component);
            }

            return Vote.APPROVE;
        }
    }

    private class NavigationRollupStateHandler extends RollupStateHandler {
        private Component component = null;

        @Override
        public Vote previewExpandedChange(Rollup rollup) {
            if (component == null) {
                BXMLSerializer bxmlSerializer = new BXMLSerializer();
                try {
                    component = (Component)bxmlSerializer.readObject(KitchenSink.class, "navigation.bxml");
                } catch(IOException exception) {
                    throw new RuntimeException(exception);
                } catch(SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                rollup.setContent(component);
            }

            return Vote.APPROVE;
        }
    }

    private class SplittersRollupStateHandler extends RollupStateHandler {
        private Component component = null;

        @Override
        public Vote previewExpandedChange(Rollup rollup) {
            if (component == null) {
                BXMLSerializer bxmlSerializer = new BXMLSerializer();
                try {
                    component = (Component)bxmlSerializer.readObject(KitchenSink.class, "splitters.bxml");
                } catch(IOException exception) {
                    throw new RuntimeException(exception);
                } catch(SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                rollup.setContent(component);
            }

            return Vote.APPROVE;
        }
    }

    private class MenusRollupStateHandler extends RollupStateHandler {
        private Component component = null;
        private ImageView menuImageView = null;
        private Menu.Item helpAboutMenuItem = null;

        private Menu.Section menuSection = null;
        private ButtonGroup imageMenuGroup = null;

        @Override
        public Vote previewExpandedChange(Rollup rollup) {
            if (component == null) {
                Action.getNamedActions().put("selectImageAction", new Action() {
                    @Override
                    public String getDescription() {
                        return "Select Image Action";
                    }

                    @Override
                    public void perform(Component source) {
                        Button selectedItem = imageMenuGroup.getSelection();

                        String imageName = (String)selectedItem.getUserData().get("image");
                        URL imageURL = getClass().getResource(imageName);

                        // If the image has not been added to the resource cache yet,
                        // add it
                        Image image = (Image)ApplicationContext.getResourceCache().get(imageURL);

                        if (image == null) {
                            try {
                                image = Image.load(imageURL);
                            } catch (TaskExecutionException exception) {
                                throw new RuntimeException(exception);
                            }

                            ApplicationContext.getResourceCache().put(imageURL, image);
                        }

                        // Update the image
                        menuImageView.setImage(image);
                    }
                });

                BXMLSerializer bxmlSerializer = new BXMLSerializer();
                try {
                    component = (Component)bxmlSerializer.readObject(KitchenSink.class, "menus.bxml");
                } catch(IOException exception) {
                    throw new RuntimeException(exception);
                } catch(SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                menuImageView = (ImageView)bxmlSerializer.getNamespace().get("menuImageView");
                helpAboutMenuItem  = (Menu.Item)bxmlSerializer.getNamespace().get("helpAboutMenuItem");

                rollup.setContent(component);

                try {
                    menuSection = (Menu.Section)bxmlSerializer.readObject(KitchenSink.class, "menu_section.bxml");
                    imageMenuGroup = (ButtonGroup)bxmlSerializer.getNamespace().get("imageMenuGroup");
                } catch(IOException exception) {
                    throw new RuntimeException(exception);
                } catch(SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                menuImageView.setMenuHandler(new MenuHandler.Adapter() {
                    @Override
                    public boolean configureContextMenu(Component component, Menu menu, int x, int y) {
                        menu.getSections().add(menuSection);
                        return false;
                    }
                });

                helpAboutMenuItem.getButtonPressListeners().add(new ButtonPressListener() {
                    @Override
                    public void buttonPressed(Button button) {
                        aboutRequested();
                    }
                });
            }

            return Vote.APPROVE;
        }
    }

    private class MetersRollupStateHandler extends RollupStateHandler {
        private Component component = null;
        private ActivityIndicator activityIndicator1 = null;
        private ActivityIndicator activityIndicator2 = null;
        private ActivityIndicator activityIndicator3 = null;

        @Override
        public Vote previewExpandedChange(Rollup rollup) {
            if (component == null) {
                BXMLSerializer bxmlSerializer = new BXMLSerializer();
                try {
                    component = (Component)bxmlSerializer.readObject(KitchenSink.class, "meters.bxml");
                } catch(IOException exception) {
                    throw new RuntimeException(exception);
                } catch(SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                activityIndicator1 = (ActivityIndicator)bxmlSerializer.getNamespace().get("activityIndicator1");
                activityIndicator2 = (ActivityIndicator)bxmlSerializer.getNamespace().get("activityIndicator2");
                activityIndicator3 = (ActivityIndicator)bxmlSerializer.getNamespace().get("activityIndicator3");

                rollup.setContent(component);

                metersRollup.getRollupStateListeners().add(new RollupStateListener() {
                    @Override
                    public Vote previewExpandedChange(Rollup rollup) {
                        return Vote.APPROVE;
                    }

                    @Override
                    public void expandedChangeVetoed(Rollup rollup, Vote reason) {
                        // No-op
                    }

                    @Override
                    public void expandedChanged(Rollup rollup) {
                        activityIndicator1.setActive(rollup.isExpanded());
                        activityIndicator2.setActive(rollup.isExpanded());
                        activityIndicator3.setActive(rollup.isExpanded());
                    }
                });
}

            return Vote.APPROVE;
        }
    }

    private class SpinnersRollupStateHandler extends RollupStateHandler {
        private Component component = null;

        private Spinner numericSpinner = null;
        private Spinner dateSpinner = null;

        private Slider redSlider = null;
        private Slider greenSlider = null;
        private Slider blueSlider = null;
        private Border colorBorder = null;

        @Override
        public Vote previewExpandedChange(Rollup rollup) {
            if (component == null) {
                BXMLSerializer bxmlSerializer = new BXMLSerializer();
                try {
                    component = (Component)bxmlSerializer.readObject(KitchenSink.class, "spinners.bxml");
                } catch(IOException exception) {
                    throw new RuntimeException(exception);
                } catch(SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                numericSpinner = (Spinner)bxmlSerializer.getNamespace().get("numericSpinner");
                dateSpinner = (Spinner)bxmlSerializer.getNamespace().get("dateSpinner");

                redSlider = (Slider)bxmlSerializer.getNamespace().get("redSlider");
                greenSlider = (Slider)bxmlSerializer.getNamespace().get("greenSlider");
                blueSlider = (Slider)bxmlSerializer.getNamespace().get("blueSlider");
                colorBorder = (Border)bxmlSerializer.getNamespace().get("colorBorder");

                rollup.setContent(component);

                initializeNumericSpinner(numericSpinner);
                initializeDateSpinner(dateSpinner);

                SliderValueListener sliderValueListener = new SliderValueListener() {
                    @Override
                    public void valueChanged(Slider slider, int previousValue) {
                        Color color = new Color(redSlider.getValue(), greenSlider.getValue(),
                            blueSlider.getValue());
                        colorBorder.getStyles().put("backgroundColor", color);
                    }
                };

                redSlider.getSliderValueListeners().add(sliderValueListener);
                greenSlider.getSliderValueListeners().add(sliderValueListener);
                blueSlider.getSliderValueListeners().add(sliderValueListener);

                Color color = new Color(redSlider.getValue(), greenSlider.getValue(),
                    blueSlider.getValue());
                colorBorder.getStyles().put("backgroundColor", color);
            }

            return Vote.APPROVE;
        }

        private void initializeNumericSpinner(Spinner numericSpinner) {
            NumericSpinnerData numericSpinnerData = new NumericSpinnerData(0, 256, 4);
            numericSpinner.setSpinnerData(numericSpinnerData);
            numericSpinner.setSelectedIndex(0);
        }

        private void initializeDateSpinner(Spinner dateSpinner) {
            CalendarDate lowerBound = new CalendarDate(2008, 0, 0);
            CalendarDate upperBound = new CalendarDate(2019, 11, 30);
            CalendarDateSpinnerData spinnerData = new CalendarDateSpinnerData(lowerBound, upperBound);

            CalendarDate today = new CalendarDate();
            dateSpinner.setSpinnerData(spinnerData);
            dateSpinner.setSelectedItem(today);
        }
    }

    private class TablesRollupStateHandler extends RollupStateHandler {
        private Component component = null;
        private TableView sortableTableView = null;
        private TableView customTableView = null;

        @Override
        public Vote previewExpandedChange(Rollup rollup) {
            if (component == null) {
                BXMLSerializer bxmlSerializer = new BXMLSerializer();
                try {
                    component = (Component)bxmlSerializer.readObject(KitchenSink.class, "tables.bxml");
                } catch(IOException exception) {
                    throw new RuntimeException(exception);
                } catch(SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                sortableTableView = (TableView)bxmlSerializer.getNamespace().get("sortableTableView");
                customTableView = (TableView)bxmlSerializer.getNamespace().get("customTableView");

                rollup.setContent(component);

                // Set table header data
                TableView.ColumnSequence columns = sortableTableView.getColumns();
                columns.get(0).setHeaderData(new TableViewHeaderData("#"));
                columns.get(1).setHeaderData(new TableViewHeaderData("A"));
                columns.get(2).setHeaderData(new TableViewHeaderData("B"));
                columns.get(3).setHeaderData(new TableViewHeaderData("C"));
                columns.get(4).setHeaderData(new TableViewHeaderData("D"));

                // Populate table
                ArrayList<Object> tableData = new ArrayList<Object>(10000);

                for (int i = 0, n = tableData.getCapacity(); i < n; i++) {
                    HashMap<String, Integer> tableRow = new HashMap<String, Integer>();

                    tableRow.put("i", i);
                    tableRow.put("a", (int)Math.round(Math.random() * 10));
                    tableRow.put("b", (int)Math.round(Math.random() * 100));
                    tableRow.put("c", (int)Math.round(Math.random() * 1000));
                    tableRow.put("d", (int)Math.round(Math.random() * 10000));

                    tableData.add(tableRow);
                }

                sortableTableView.setTableData(tableData);
                sortableTableView.getTableViewSortListeners().add(new TableViewSortListener() {
                    @Override
                    public void sortAdded(TableView tableView, String columnName) {
                        resort(tableView);
                    }

                    @Override
                    public void sortUpdated(TableView tableView, String columnName,
                        SortDirection previousSortDirection) {
                        resort(tableView);
                    }

                    @Override
                    public void sortRemoved(TableView tableView, String columnName,
                        SortDirection sortDirection) {
                        resort(tableView);
                    }

                    @Override
                    public void sortChanged(TableView tableView) {
                        resort(tableView);
                    }

                    @SuppressWarnings("unchecked")
                    private void resort(TableView tableView) {
                        List<Object> tableData = (List<Object>)tableView.getTableData();
                        tableData.setComparator(new TableViewRowComparator(tableView));
                    }
                });

                customTableView.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener.Adapter() {
                    @Override
                    @SuppressWarnings("unchecked")
                    public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
                       if (button == Mouse.Button.LEFT) {
                           List<CustomTableRow> customTableData =
                               (List<CustomTableRow>)customTableView.getTableData();

                          int columnIndex = customTableView.getColumnAt(x);
                          if (columnIndex == 0) {
                             int rowIndex = customTableView.getRowAt(y);
                             CustomTableRow row = customTableData.get(rowIndex);

                             row.setA(!row.getA());
                             customTableData.update(rowIndex, row);
                          }
                       }

                       return false;
                    }
                });
            }

            return Vote.APPROVE;
        }
    }

    private class TreesRollupStateHandler extends RollupStateHandler {
        private Component component = null;
        private TreeView editableTreeView = null;
        private TreeView checkTreeView = null;

        @Override
        public Vote previewExpandedChange(Rollup rollup) {
            if (component == null) {
                BXMLSerializer bxmlSerializer = new BXMLSerializer();
                try {
                    component = (Component)bxmlSerializer.readObject(KitchenSink.class, "trees.bxml");
                } catch(IOException exception) {
                    throw new RuntimeException(exception);
                } catch(SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                editableTreeView = (TreeView)bxmlSerializer.getNamespace().get("editableTreeView");
                checkTreeView = (TreeView)bxmlSerializer.getNamespace().get("checkTreeView");

                rollup.setContent(component);

                TreeBranch treeData = (TreeBranch)editableTreeView.getTreeData();
                treeData.setComparator(new TreeNodeComparator());

                checkTreeView.setDisabledNodeFilter(new Filter<TreeNode>() {
                    @Override
                    public boolean include(TreeNode treeNode) {
                        boolean include = false;

                        if (!(treeNode instanceof TreeBranch)) {
                            String text = treeNode.getText();

                            if (text != null) {
                                char firstCharacter = Character.toLowerCase(text.charAt(0));
                                include = (firstCharacter % 2 == 0);
                            }
                        }

                        return include;
                    }
                });

                checkTreeView.setDisabledCheckmarkFilter(new Filter<TreeNode>() {
                    @Override
                    public boolean include(TreeNode treeNode) {
                        return (treeNode instanceof TreeBranch);
                    }
                });
            }

            return Vote.APPROVE;
        }
    }

    private class DragDropRollupStateHandler extends RollupStateHandler {
        private Component component = null;
        private ImageView imageView1 = null;
        private ImageView imageView2 = null;
        private ImageView imageView3 = null;

        @Override
        public Vote previewExpandedChange(Rollup rollup) {
            if (component == null) {
                BXMLSerializer bxmlSerializer = new BXMLSerializer();
                try {
                    component = (Component)bxmlSerializer.readObject(KitchenSink.class, "dragdrop.bxml");
                } catch(IOException exception) {
                    throw new RuntimeException(exception);
                } catch(SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                imageView1 = (ImageView)bxmlSerializer.getNamespace().get("imageView1");
                imageView2 = (ImageView)bxmlSerializer.getNamespace().get("imageView2");
                imageView3 = (ImageView)bxmlSerializer.getNamespace().get("imageView3");

                rollup.setContent(component);

                DragSource imageDragSource = new DragSource() {
                    private Image image = null;
                    private Point offset = null;
                    private JavaAwtLocalManifest content = null;

                    @Override
                    public boolean beginDrag(Component component, int x, int y) {
                        ImageView imageView = (ImageView)component;
                        image = imageView.getImage();

                        if (image != null) {
                            imageView.setImage((Image)null);
                            content = new JavaAwtLocalManifest();
                            content.putImage(image);
                            offset = new Point(x - (imageView.getWidth() - image.getWidth()) / 2,
                                y - (imageView.getHeight() - image.getHeight()) / 2);
                        }

                        return (image != null);
                    }

                    @Override
                    public void endDrag(Component component, DropAction dropAction) {
                        if (dropAction == null) {
                            ImageView imageView = (ImageView)component;
                            imageView.setImage(image);
                        }

                        image = null;
                        offset = null;
                        content = null;
                    }

                    @Override
                    public boolean isNative() {
                        return false;
                    }

                    @Override
                    public JavaAwtLocalManifest getContent() {
                        return content;
                    }

                    @Override
                    public Visual getRepresentation() {
                        return image;
                    }

                    @Override
                    public Point getOffset() {
                        return offset;
                    }

                    @Override
                    public int getSupportedDropActions() {
                        return DropAction.MOVE.getMask();
                    }
                };

                DropTarget imageDropTarget = new DropTarget() {
                    @Override
                    public DropAction dragEnter(Component component, Manifest dragContent,
                        int supportedDropActions, DropAction userDropAction) {
                        DropAction dropAction = null;

                        ImageView imageView = (ImageView)component;
                        if (imageView.getImage() == null
                            && dragContent.containsImage()
                            && DropAction.MOVE.isSelected(supportedDropActions)) {
                            dropAction = DropAction.MOVE;
                            component.getStyles().put("backgroundColor", "#f0e68c");
                        }

                        return dropAction;
                    }

                    @Override
                    public void dragExit(Component component) {
                        component.getStyles().put("backgroundColor", null);
                    }

                    @Override
                    public DropAction dragMove(Component component, Manifest dragContent,
                        int supportedDropActions, int x, int y, DropAction userDropAction) {
                        ImageView imageView = (ImageView)component;
                        return (imageView.getImage() == null
                            && dragContent.containsImage() ? DropAction.MOVE : null);
                    }

                    @Override
                    public DropAction userDropActionChange(Component component, Manifest dragContent,
                        int supportedDropActions, int x, int y, DropAction userDropAction) {
                        ImageView imageView = (ImageView)component;
                        return (imageView.getImage() == null
                            && dragContent.containsImage() ? DropAction.MOVE : null);
                    }

                    @Override
                    public DropAction drop(Component component, Manifest dragContent,
                        int supportedDropActions, int x, int y, DropAction userDropAction) {
                        DropAction dropAction = null;

                        ImageView imageView = (ImageView)component;
                        if (imageView.getImage() == null
                            && dragContent.containsImage()) {
                            try {
                                imageView.setImage(dragContent.getImage());
                                dropAction = DropAction.MOVE;
                            } catch(IOException exception) {
                                System.err.println(exception);
                            }
                        }

                        dragExit(component);

                        return dropAction;
                    }
                };

                imageView1.setDragSource(imageDragSource);
                imageView1.setDropTarget(imageDropTarget);

                imageView2.setDragSource(imageDragSource);
                imageView2.setDropTarget(imageDropTarget);

                imageView3.setDragSource(imageDragSource);
                imageView3.setDropTarget(imageDropTarget);
            }

            return Vote.APPROVE;
        }
    }

    private class AlertsRollupStateHandler extends RollupStateHandler {
        private Component component = null;
        private PushButton alertButton = null;
        private PushButton promptButton = null;
        private ButtonGroup messageTypeGroup = null;

        @Override
        public Vote previewExpandedChange(Rollup rollup) {
            if (component == null) {
                BXMLSerializer bxmlSerializer = new BXMLSerializer();
                try {
                    component = (Component)bxmlSerializer.readObject(KitchenSink.class, "alerts.bxml");
                } catch(IOException exception) {
                    throw new RuntimeException(exception);
                } catch(SerializationException exception) {
                    throw new RuntimeException(exception);
                }

                alertButton = (PushButton)bxmlSerializer.getNamespace().get("alertButton");
                promptButton = (PushButton)bxmlSerializer.getNamespace().get("promptButton");
                messageTypeGroup = (ButtonGroup)bxmlSerializer.getNamespace().get("messageTypeGroup");

                rollup.setContent(component);

                alertButton.getButtonPressListeners().add(new ButtonPressListener() {
                    @Override
                    public void buttonPressed(Button button) {
                        Button selection = messageTypeGroup.getSelection();

                        Map<String, ?> userData;
                        try {
                            userData = JSONSerializer.parseMap((String)selection.getUserData().get("messageInfo"));
                        } catch (SerializationException exception) {
                            throw new RuntimeException(exception);
                        }

                        String messageType = (String)userData.get("messageType");

                        if (messageType == null) {
                            ArrayList<String> options = new ArrayList<String>();
                            options.add("OK");
                            options.add("Cancel");

                            Component body = null;
                            BXMLSerializer bxmlSerializer = new BXMLSerializer();
                            try {
                                body = (Component)bxmlSerializer.readObject(KitchenSink.class, "alert.bxml");
                            } catch(Exception exception) {
                                System.err.println(exception);
                            }

                            Alert alert = new Alert(MessageType.QUESTION, "Please select your favorite icon:",
                                options, body);
                            alert.setTitle("Select Icon");
                            alert.setSelectedOptionIndex(0);
                            alert.getDecorators().update(0, new ReflectionDecorator());
                            alert.open(window);
                        } else {
                            String message = (String)userData.get("message");
                            Alert.alert(MessageType.valueOf(messageType.toUpperCase()), message, window);
                        }
                    }
                });

                promptButton.getButtonPressListeners().add(new ButtonPressListener() {
                    @Override
                    public void buttonPressed(Button button) {
                        Button selection = messageTypeGroup.getSelection();

                        Map<String, ?> userData;
                        try {
                            userData = JSONSerializer.parseMap((String)selection.getUserData().get("messageInfo"));
                        } catch (SerializationException exception) {
                            throw new RuntimeException(exception);
                        }

                        String messageType = (String)userData.get("messageType");

                        if (messageType == null) {
                            ArrayList<String> options = new ArrayList<String>();
                            options.add("OK");
                            options.add("Cancel");

                            Component body = null;
                            BXMLSerializer bxmlSerializer = new BXMLSerializer();
                            try {
                                body = (Component)bxmlSerializer.readObject(KitchenSink.class, "alert.bxml");
                            } catch(Exception exception) {
                                System.err.println(exception);
                            }

                            Prompt prompt = new Prompt(MessageType.QUESTION, "Please select your favorite icon:",
                                options, body);
                            prompt.setTitle("Select Icon");
                            prompt.setSelectedOptionIndex(0);
                            prompt.getDecorators().update(0, new ReflectionDecorator());
                            prompt.open(window);
                        } else {
                            String message = (String)userData.get("message");
                            Prompt.prompt(MessageType.valueOf(messageType.toUpperCase()), message, window);
                        }
                    }
                });
            }

            return Vote.APPROVE;
        }
    }

    private Window window = null;
    private Rollup buttonsRollup;
    private Rollup listsRollup;
    private Rollup textRollup;
    private Rollup calendarsRollup;
    private Rollup colorChoosersRollup;
    private Rollup navigationRollup;
    private Rollup splittersRollup;
    private Rollup menusRollup;
    private Rollup metersRollup;
    private Rollup spinnersRollup;
    private Rollup tablesRollup;
    private Rollup treesRollup;
    private Rollup dragDropRollup;
    private Rollup alertsRollup;

    public static void main(String[] args) {
        DesktopApplicationContext.main(KitchenSink.class, args);
    }

    @Override
    public void startup(Display display, Map<String, String> properties) throws Exception {
        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        window = (Window)bxmlSerializer.readObject(KitchenSink.class, "kitchen_sink.bxml");
        bxmlSerializer.bind(this, KitchenSink.class);

        buttonsRollup = (Rollup)bxmlSerializer.getNamespace().get("buttonsRollup");
        buttonsRollup.getRollupStateListeners().add(new ButtonsRollupStateHandler());

        listsRollup = (Rollup)bxmlSerializer.getNamespace().get("listsRollup");
        listsRollup.getRollupStateListeners().add(new ListsRollupStateHandler());

        textRollup = (Rollup)bxmlSerializer.getNamespace().get("textRollup");
        textRollup.getRollupStateListeners().add(new TextRollupStateHandler());

        calendarsRollup = (Rollup)bxmlSerializer.getNamespace().get("calendarsRollup");
        calendarsRollup.getRollupStateListeners().add(new CalendarsRollupStateHandler());

        colorChoosersRollup = (Rollup)bxmlSerializer.getNamespace().get("colorChoosersRollup");
        colorChoosersRollup.getRollupStateListeners().add(new ColorChoosersRollupStateHandler());

        navigationRollup = (Rollup)bxmlSerializer.getNamespace().get("navigationRollup");
        navigationRollup.getRollupStateListeners().add(new NavigationRollupStateHandler());

        splittersRollup = (Rollup)bxmlSerializer.getNamespace().get("splittersRollup");
        splittersRollup.getRollupStateListeners().add(new SplittersRollupStateHandler());

        menusRollup = (Rollup)bxmlSerializer.getNamespace().get("menusRollup");
        menusRollup.getRollupStateListeners().add(new MenusRollupStateHandler());

        metersRollup = (Rollup)bxmlSerializer.getNamespace().get("metersRollup");
        metersRollup.getRollupStateListeners().add(new MetersRollupStateHandler());

        spinnersRollup = (Rollup)bxmlSerializer.getNamespace().get("spinnersRollup");
        spinnersRollup.getRollupStateListeners().add(new SpinnersRollupStateHandler());

        tablesRollup = (Rollup)bxmlSerializer.getNamespace().get("tablesRollup");
        tablesRollup.getRollupStateListeners().add(new TablesRollupStateHandler());

        treesRollup = (Rollup)bxmlSerializer.getNamespace().get("treesRollup");
        treesRollup.getRollupStateListeners().add(new TreesRollupStateHandler());

        dragDropRollup = (Rollup)bxmlSerializer.getNamespace().get("dragDropRollup");
        dragDropRollup.getRollupStateListeners().add(new DragDropRollupStateHandler());

        alertsRollup = (Rollup)bxmlSerializer.getNamespace().get("alertsRollup");
        alertsRollup.getRollupStateListeners().add(new AlertsRollupStateHandler());

        window.open(display);

        // Start with the "Buttons" rollup expanded
        ApplicationContext.scheduleCallback(new Runnable() {
            @Override
            public void run() {
                buttonsRollup.setExpanded(true);
            }
        }, 0);
    }

    @Override
    public boolean shutdown(boolean optional) throws Exception {
        if (window != null) {
            window.close();
        }

        return false;
    }

    @Override
    public void suspend() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void aboutRequested() {
        String about = "Origin: " + ApplicationContext.getOrigin()
            + "; JVM version: " + ApplicationContext.getJVMVersion();

        Prompt.prompt(about, window);
    }
}
