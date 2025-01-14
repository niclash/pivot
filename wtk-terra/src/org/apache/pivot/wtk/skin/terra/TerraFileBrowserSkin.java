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
package org.apache.pivot.wtk.skin.terra;

import org.apache.pivot.wtk.Platform;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.Comparator;
import java.util.Date;

import org.apache.pivot.beans.BXML;
import org.apache.pivot.beans.BXMLSerializer;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.text.FileSizeFormat;
import org.apache.pivot.util.Filter;
import org.apache.pivot.util.concurrent.Task;
import org.apache.pivot.util.concurrent.TaskExecutionException;
import org.apache.pivot.util.concurrent.TaskListener;
import org.apache.pivot.wtk.BoxPane;
import org.apache.pivot.wtk.Button;
import org.apache.pivot.wtk.ButtonPressListener;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentKeyListener;
import org.apache.pivot.wtk.ComponentMouseButtonListener;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.Dimensions;
import org.apache.pivot.wtk.FileBrowser;
import org.apache.pivot.wtk.FocusTraversalDirection;
import org.apache.pivot.wtk.HorizontalAlignment;
import org.apache.pivot.wtk.ImageView;
import org.apache.pivot.wtk.Insets;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Label;
import org.apache.pivot.wtk.ListButton;
import org.apache.pivot.wtk.ListButtonSelectionListener;
import org.apache.pivot.wtk.ListView;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Point;
import org.apache.pivot.wtk.PushButton;
import org.apache.pivot.wtk.ScrollPane;
import org.apache.pivot.wtk.SortDirection;
import org.apache.pivot.wtk.Span;
import org.apache.pivot.wtk.TableView;
import org.apache.pivot.wtk.TableViewSelectionListener;
import org.apache.pivot.wtk.TableViewSortListener;
import org.apache.pivot.wtk.TaskAdapter;
import org.apache.pivot.wtk.TextInput;
import org.apache.pivot.wtk.TextInputContentListener;
import org.apache.pivot.wtk.VerticalAlignment;
import org.apache.pivot.wtk.graphics.Color;
import org.apache.pivot.wtk.graphics.font.Font;
import org.apache.pivot.wtk.media.Image;
import org.apache.pivot.wtk.skin.FileBrowserSkin;

/**
 * Terra file browser skin.
 */
public class TerraFileBrowserSkin extends FileBrowserSkin {
    /**
     * Abstract renderer for displaying file system contents.
     */
    public static abstract class FileRenderer extends BoxPane {
        protected ImageView imageView = new ImageView();
        protected Label label = new Label();

        public static final int ICON_WIDTH = 16;
        public static final int ICON_HEIGHT = 16;

        public static final Image FOLDER_IMAGE;
        public static final Image HOME_FOLDER_IMAGE;
        public static final Image FILE_IMAGE;

        public static final File HOME_DIRECTORY;

        static {
            try {
                FOLDER_IMAGE = Image.load(FileRenderer.class.getResource("folder.png"));
                HOME_FOLDER_IMAGE = Image.load(FileRenderer.class.getResource("folder_home.png"));
                FILE_IMAGE = Image.load(FileRenderer.class.getResource("page_white.png"));

                HOME_DIRECTORY = new File(System.getProperty("user.home"));
            } catch (TaskExecutionException exception) {
                throw new RuntimeException(exception);
            }
        }

        public FileRenderer() {
            getStyles().put("verticalAlignment", VerticalAlignment.CENTER);

            add(imageView);
            add(label);

            imageView.setPreferredSize(ICON_WIDTH, ICON_HEIGHT);
            imageView.getStyles().put("backgroundColor", null);
        }

        @Override
        public void setSize(int width, int height) {
            super.setSize(width, height);

            // Since this component doesn't have a parent, it won't be validated
            // via layout; ensure that it is valid here
            validate();
        }

        /**
         * Obtains the icon to display for a given file.
         *
         * @param file
         */
        public static Image getIcon(File file) {
            Image icon;
            if (file.isDirectory()) {
                icon = file.equals(HOME_DIRECTORY) ? HOME_FOLDER_IMAGE : FOLDER_IMAGE;
            } else {
                icon = FILE_IMAGE;
            }

            return icon;
        }
    }

    /**
     * List button file renderer.
     */
    public static class ListButtonFileRenderer extends FileRenderer implements Button.DataRenderer {
        public ListButtonFileRenderer() {
            getStyles().put("horizontalAlignment", HorizontalAlignment.LEFT);
        }

        @Override
        public void render(Object data, Button button, boolean highlight) {
            if (data != null) {
                File file = (File)data;

                // Update the image view
                imageView.setImage(getIcon(file));
                imageView.getStyles().put("opacity", button.isEnabled() ? 1.0f : 0.5f);

                // Update the label
                String text = file.getName();
                if (text.length() == 0) {
                    text = System.getProperty("file.separator");
                }

                label.setText(text);
            }
        }

        @Override
        public String toString(Object item) {
            File file = (File)item;
            String text = file.getName();
            if (text.length() == 0) {
                text = System.getProperty("file.separator");
            }

            return text;
        }
    }

    /**
     * List view file renderer.
     */
    public static class ListViewFileRenderer extends FileRenderer implements ListView.ItemRenderer {
        public ListViewFileRenderer() {
            getStyles().put("horizontalAlignment", HorizontalAlignment.LEFT);
            getStyles().put("padding", new Insets(2, 3, 2, 3));
        }

        @Override
        public void render(Object item, int index, ListView listView, boolean selected,
            boolean checked, boolean highlighted, boolean disabled) {
            label.getStyles().put("font", listView.getStyles().get("font"));

            Object color = null;
            if (listView.isEnabled() && !disabled) {
                if (selected) {
                    if (listView.isFocused()) {
                        color = listView.getStyles().get("selectionColor");
                    } else {
                        color = listView.getStyles().get("inactiveSelectionColor");
                    }
                } else {
                    color = listView.getStyles().get("color");
                }
            } else {
                color = listView.getStyles().get("disabledColor");
            }

            label.getStyles().put("color", color);

            if (item != null) {
                File file = (File)item;

                // Update the image view
                imageView.setImage(getIcon(file));
                imageView.getStyles().put("opacity",
                    (listView.isEnabled() && !disabled) ? 1.0f : 0.5f);

                // Update the label
                String text = file.getName();
                if (text.length() == 0) {
                    text = System.getProperty("file.separator");
                }

                label.setText(text);
            }
        }

        @Override
        public String toString(Object item) {
            File file = (File)item;
            String text = file.getName();
            if (text.length() == 0) {
                text = System.getProperty("file.separator");
            }

            return text;
        }
    }

    /**
     * Table view file renderer.
     */
    public static class TableViewFileRenderer extends FileRenderer
        implements TableView.CellRenderer {
        public static final String NAME_KEY = "name";
        public static final String SIZE_KEY = "size";
        public static final String LAST_MODIFIED_KEY = "lastModified";

        public TableViewFileRenderer() {
            getStyles().put("horizontalAlignment", HorizontalAlignment.CENTER);
            getStyles().put("padding", new Insets(2));
        }

        @Override
        public void render(Object row, int rowIndex, int columnIndex,
            TableView tableView, String columnName,
            boolean selected, boolean highlighted, boolean disabled) {
            if (row != null) {
                File file = (File)row;

                String text = null;
                Image icon = null;

                if (columnName.equals(NAME_KEY)) {
                    text = file.getName();
                    icon = getIcon(file);
                    getStyles().put("horizontalAlignment", HorizontalAlignment.LEFT);
                } else if (columnName.equals(SIZE_KEY)) {
                    long length = file.length();
                    text = FileSizeFormat.getInstance().format(length);
                    getStyles().put("horizontalAlignment", HorizontalAlignment.RIGHT);
                } else if (columnName.equals(LAST_MODIFIED_KEY)) {
                    long lastModified = file.lastModified();
                    Date lastModifiedDate = new Date(lastModified);
                    text = DATE_FORMAT.format(lastModifiedDate);
                    getStyles().put("horizontalAlignment", HorizontalAlignment.RIGHT);
                } else {
                    System.err.println("Unexpected column name in " + getClass().getName()
                        + ": " + columnName);
                }

                label.setText(text);
                imageView.setImage(icon);
            }

            Font font = (Font)tableView.getStyles().get("font");
            label.getStyles().put("font", font);

            Color color;
            if (tableView.isEnabled() && !disabled) {
                if (selected) {
                    if (tableView.isFocused()) {
                        color = (Color)tableView.getStyles().get("selectionColor");
                    } else {
                        color = (Color)tableView.getStyles().get("inactiveSelectionColor");
                    }
                } else {
                    color = (Color)tableView.getStyles().get("color");
                }
            } else {
                color = (Color)tableView.getStyles().get("disabledColor");
            }

            label.getStyles().put("color", color);
        }

        public String toString(Object row, String columnName) {
            String string;

            File file = (File)row;
            if (columnName.equals(NAME_KEY)) {
                string = file.getName();
            } else if (columnName.equals(SIZE_KEY)) {
                long length = file.length();
                string = FileSizeFormat.getInstance().format(length);
            } else if (columnName.equals(LAST_MODIFIED_KEY)) {
                long lastModified = file.lastModified();
                Date lastModifiedDate = new Date(lastModified);
                string = DATE_FORMAT.format(lastModifiedDate);
            } else {
                System.err.println("Unexpected column name in " + getClass().getName()
                    + ": " + columnName);
                string = null;
            }

            return string;
        }
    }

    /**
     * Abstract base class for drive renderers.
     */
    public static abstract class DriveRenderer extends BoxPane {
        protected ImageView imageView = new ImageView();
        protected Label label = new Label();

        public static final int ICON_WIDTH = 16;
        public static final int ICON_HEIGHT = 16;

        public static final Image DRIVE_IMAGE;

        static {
            try {
                DRIVE_IMAGE = Image.load(FileRenderer.class.getResource("drive.png"));
            } catch (TaskExecutionException exception) {
                throw new RuntimeException(exception);
            }
        }

        public DriveRenderer() {
            getStyles().put("verticalAlignment", VerticalAlignment.CENTER);

            add(imageView);
            add(label);

            imageView.setPreferredSize(ICON_WIDTH, ICON_HEIGHT);
            imageView.getStyles().put("backgroundColor", null);
        }

        @Override
        public void setSize(int width, int height) {
            super.setSize(width, height);

            // Since this component doesn't have a parent, it won't be validated
            // via layout; ensure that it is valid here
            validate();
        }
    }

    /**
     * List button drive renderer.
     */
    public static class ListButtonDriveRenderer extends DriveRenderer
        implements Button.DataRenderer {
        public ListButtonDriveRenderer() {
            getStyles().put("horizontalAlignment", HorizontalAlignment.LEFT);
        }

        @Override
        public void render(Object data, Button button, boolean highlight) {
            if (data != null) {
                File file = (File)data;

                // Update the image view
                imageView.setImage(DRIVE_IMAGE);
                imageView.getStyles().put("opacity", button.isEnabled() ? 1.0f : 0.5f);

                // Update the label
                label.setText(file.toString());
            }
        }

        @Override
        public String toString(Object data) {
            return null;
        }
    }

    /**
     * List view drive renderer.
     */
    public static class ListViewDriveRenderer extends DriveRenderer
        implements ListView.ItemRenderer {
        public ListViewDriveRenderer() {
            getStyles().put("horizontalAlignment", HorizontalAlignment.LEFT);
            getStyles().put("padding", new Insets(2, 3, 2, 3));
        }

        @Override
        public void render(Object item, int index, ListView listView, boolean selected,
            boolean checked, boolean highlighted, boolean disabled) {
            label.getStyles().put("font", listView.getStyles().get("font"));

            Object color = null;
            if (listView.isEnabled() && !disabled) {
                if (selected) {
                    if (listView.isFocused()) {
                        color = listView.getStyles().get("selectionColor");
                    } else {
                        color = listView.getStyles().get("inactiveSelectionColor");
                    }
                } else {
                    color = listView.getStyles().get("color");
                }
            } else {
                color = listView.getStyles().get("disabledColor");
            }

            label.getStyles().put("color", color);

            if (item != null) {
                File file = (File)item;

                // Update the image view
                imageView.setImage(DRIVE_IMAGE);
                imageView.getStyles().put("opacity",
                    (listView.isEnabled() && !disabled) ? 1.0f : 0.5f);

                // Update the label
                label.setText(file.toString());
            }
        }

        @Override
        public String toString(Object item) {
            return null;
        }
    }

    /**
     * File comparator.
     */
    public static class FileComparator implements Comparator<File>, Serializable {
        private static final long serialVersionUID = 1L;

        private String columnName = null;
        private SortDirection sortDirection = null;

        public FileComparator(String columnName, SortDirection sortDirection) {
            this.columnName = columnName;
            this.sortDirection = sortDirection;
        }

        @Override
        public int compare(File file1, File file2) {
            int result;

            if (columnName.equals("name")) {
                boolean file1IsDirectory = file1.isDirectory();
                boolean file2IsDirectory = file2.isDirectory();

                if (file1IsDirectory && !file2IsDirectory) {
                    result = -1;
                } else if (!file1IsDirectory && file2IsDirectory) {
                    result = 1;
                } else {
                    result = file1.getName().compareToIgnoreCase(file2.getName());
                }
            } else if (columnName.equals("size")) {
                result = Long.signum(file1.length() - file2.length());
            } else if (columnName.equals("lastModified")) {
                result = Long.signum(file1.lastModified() - file2.lastModified());
            } else {
                throw new IllegalArgumentException();
            }

            result *= (sortDirection == SortDirection.ASCENDING) ? 1 : -1;

            return result;
        }
    }

    /**
     * Include file filter.
     */
    public static class IncludeFileFilter implements Filter<File> {
        private String match;
        private Filter<File> excludeFileFilter;

        public IncludeFileFilter() {
            this(null, null);
        }

        public IncludeFileFilter(String match, Filter<File> excludeFileFilter) {
            this.match = (match == null ? null : match.toLowerCase());
            this.excludeFileFilter = excludeFileFilter;
        }

        @Override
        public boolean include(File file) {
            String name = file.getName();
            name = name.toLowerCase();

            boolean include = true;

            if (match != null) {
                if (match.startsWith("*")) {
                    if (match.length() == 1) {
                        include = true;
                    } else {
                        include = name.contains(match.substring(1));
                    }
                } else {
                    include = name.startsWith(match);
                }
            }

            if (include
                && excludeFileFilter != null) {
                include = !excludeFileFilter.include(file);
            }

            return include;
        }
    }

    private class RefreshFileListTask extends Task<File[]> {
        @Override
        public File[] execute() {
            FileBrowser fileBrowser = (FileBrowser)getComponent();
            File rootDirectory = fileBrowser.getRootDirectory();
            File[] files = rootDirectory.listFiles(HIDDEN_FILE_FILTER);

            return files;
        }
    }

    private Component content = null;

    @BXML private ListButton driveListButton = null;
    @BXML private ListButton pathListButton = null;
    @BXML private PushButton goUpButton = null;
    @BXML private PushButton newFolderButton = null;
    @BXML private PushButton goHomeButton = null;
    @BXML private TextInput searchTextInput = null;

    @BXML private ScrollPane fileScrollPane = null;
    @BXML private TableView fileTableView = null;

    private boolean keyboardFolderTraversalEnabled = true;
    private boolean hideDisabledFiles = false;

    private boolean updatingSelection = false;

    private RefreshFileListTask refreshFileListTask = null;

    private static final FileFilter HIDDEN_FILE_FILTER = new FileFilter() {
        @Override
        public boolean accept(File file) {
            return !file.isHidden();
        }
    };

    private static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(
        DateFormat.SHORT, DateFormat.SHORT);


    @Override
    public void install(Component component) {
        super.install(component);

        final FileBrowser fileBrowser = (FileBrowser)component;

        BXMLSerializer bxmlSerializer = new BXMLSerializer();
        try {
            content = (Component)bxmlSerializer.readObject(TerraFileBrowserSkin.class,
                "terra_file_browser_skin.bxml", true);
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        } catch (SerializationException exception) {
            throw new RuntimeException(exception);
        }

        fileBrowser.add(content);

        bxmlSerializer.bind(this, TerraFileBrowserSkin.class);

        driveListButton.getListButtonSelectionListeners().add(new ListButtonSelectionListener.Adapter() {
            @Override
            public void selectedItemChanged(ListButton listButton, Object previousSelectedItem) {
                if (previousSelectedItem != null) {
                    File drive = (File)listButton.getSelectedItem();
                    fileBrowser.setRootDirectory(drive);
                }
            }
        });

        pathListButton.getListButtonSelectionListeners().add(new ListButtonSelectionListener.Adapter() {
            @Override
            public void selectedItemChanged(ListButton listButton, Object previousSelectedItem) {
                File ancestorDirectory = (File)listButton.getSelectedItem();

                if (ancestorDirectory != null) {
                    fileBrowser.setRootDirectory(ancestorDirectory);
                }
            }
        });

        goUpButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                File rootDirectory = fileBrowser.getRootDirectory();
                File parentDirectory = rootDirectory.getParentFile();
                fileBrowser.setRootDirectory(parentDirectory);
            }
        });

        newFolderButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                // TODO
            }
        });

        goHomeButton.getButtonPressListeners().add(new ButtonPressListener() {
            @Override
            public void buttonPressed(Button button) {
                fileBrowser.setRootDirectory(new File(System.getProperty("user.home")));
            }
        });

        /**
         * {@link Keyboard.Key#DOWN DOWN} Transfer focus to the file list and select
         * the first item.<br>
         * {@link Keyboard.Key#ESCAPE ESCAPE} Clear the search field.
         */
        searchTextInput.getComponentKeyListeners().add(new ComponentKeyListener.Adapter() {
            @Override
            public boolean keyPressed(Component component, Keyboard.Key keyCode, Keyboard.KeyLocation keyLocation) {
                boolean consumed = super.keyPressed(component, keyCode, keyLocation);

                if (keyCode == Keyboard.Key.ESCAPE) {
                    searchTextInput.setText("");
                    consumed = true;
                } else if (keyCode == Keyboard.Key.DOWN) {
                    if (fileTableView.getTableData().getLength() > 0) {
                        fileTableView.setSelectedIndex(0);
                        fileTableView.requestFocus();
                    }
                }

                return consumed;
            }
        });

        searchTextInput.getTextInputContentListeners().add(new TextInputContentListener.Adapter() {
            @Override
            public void textChanged(TextInput textInput) {
                refreshFileList();
            }
        });

        fileTableView.getTableViewSelectionListeners().add(new TableViewSelectionListener() {
            @Override
            @SuppressWarnings("unchecked")
            public void selectedRangeAdded(TableView tableView, int rangeStart, int rangeEnd) {
                updatingSelection = true;

                for (int i = rangeStart; i <= rangeEnd; i++) {
                    List<File> files = (List<File>)fileTableView.getTableData();
                    File file = files.get(i);
                    fileBrowser.addSelectedFile(file);
                }

                updatingSelection = false;
            }

            @Override
            @SuppressWarnings("unchecked")
            public void selectedRangeRemoved(TableView tableView, int rangeStart, int rangeEnd) {
                updatingSelection = true;

                for (int i = rangeStart; i <= rangeEnd; i++) {
                    List<File> files = (List<File>)fileTableView.getTableData();
                    File file = files.get(i);
                    fileBrowser.removeSelectedFile(file);
                }

                updatingSelection = false;
            }

            @Override
            @SuppressWarnings("unchecked")
            public void selectedRangesChanged(TableView tableView, Sequence<Span> previousSelectedRanges) {
                if (previousSelectedRanges != null) {
                    updatingSelection = true;

                    Sequence<File> files = (Sequence<File>)tableView.getSelectedRows();
                    for (int i = 0, n = files.getLength(); i < n; i++) {
                        File file = files.get(i);
                        files.update(i, file);
                    }

                    fileBrowser.setSelectedFiles(files);

                    updatingSelection = false;
                }
            }

            @Override
            public void selectedRowChanged(TableView tableView, Object previousSelectedRow) {
                // No-op
            }
        });

        fileTableView.getTableViewSortListeners().add(new TableViewSortListener.Adapter() {
            @Override
            @SuppressWarnings("unchecked")
            public void sortChanged(TableView tableView) {
                TableView.SortDictionary sort = fileTableView.getSort();

                if (!sort.isEmpty()) {
                    Dictionary.Pair<String, SortDirection> pair = fileTableView.getSort().get(0);
                    List<File> files = (List<File>)fileTableView.getTableData();
                    files.setComparator(new FileComparator(pair.key, pair.value));
                }
            }
        });

        fileTableView.getComponentMouseButtonListeners().add(new ComponentMouseButtonListener.Adapter() {
            private int index = -1;

            @Override
            public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
                boolean consumed = super.mouseClick(component, button, x, y, count);

                if (count == 1) {
                    index = fileTableView.getRowAt(y);
                } else if (count == 2) {
                    int index = fileTableView.getRowAt(y);
                    if (index != -1
                        && index == this.index
                        && fileTableView.isRowSelected(index)) {
                        File file = (File)fileTableView.getTableData().get(index);

                        if (file.isDirectory()) {
                            fileBrowser.setRootDirectory(file);
                            consumed = true;
                        }
                    }
                }

                return consumed;
            }
        });

        fileBrowser.setFocusTraversalPolicy(new IndexFocusTraversalPolicy() {
            @Override
            public Component getNextComponent(Container container, Component component,
                FocusTraversalDirection direction) {
                Component nextComponent;
                if (component == null) {
                    nextComponent = fileTableView;
                } else {
                    nextComponent = super.getNextComponent(container, component, direction);
                }

                return nextComponent;
            }
        });

        fileTableView.setSort(TableViewFileRenderer.NAME_KEY, SortDirection.ASCENDING);

        rootDirectoryChanged(fileBrowser, null);
        selectedFilesChanged(fileBrowser, null);
    }

    @Override
    public int getPreferredWidth(int height) {
        return content.getPreferredWidth(height);
    }

    @Override
    public int getPreferredHeight(int width) {
        return content.getPreferredHeight(width);
    }

    @Override
    public Dimensions getPreferredSize() {
        return content.getPreferredSize();
    }

    @Override
    public void layout() {
        int width = getWidth();
        int height = getHeight();

        content.setLocation(0, 0);
        content.setSize(width, height);
    }

    @Override
    public File getFileAt(int x, int y) {
        File file = null;

        FileBrowser fileBrowser = (FileBrowser)getComponent();
        Component component = fileBrowser.getDescendantAt(x, y);
        if (component == fileTableView) {
            Point location = fileTableView.mapPointFromAncestor(fileBrowser, x, y);

            int index = fileTableView.getRowAt(location.y);
            if (index != -1) {
                file = (File)fileTableView.getTableData().get(index);
            }
        }

        return file;
    }

    public boolean isKeyboardFolderTraversalEnabled() {
        return keyboardFolderTraversalEnabled;
    }

    public void setKeyboardFolderTraversalEnabled(boolean keyboardFolderTraversalEnabled) {
        this.keyboardFolderTraversalEnabled = keyboardFolderTraversalEnabled;
    }

    public boolean isHideDisabledFiles() {
        return hideDisabledFiles;
    }

    public void setHideDisabledFiles(boolean hideDisabledFiles) {
        this.hideDisabledFiles = hideDisabledFiles;
        refreshFileList();
    }

    /**
     * {@link Keyboard.Key#ENTER ENTER} Change into the selected directory if
     * {@link #keyboardFolderTraversalEnabled} is true.<br>
     * {@link Keyboard.Key#DELETE DELETE} or {@link Keyboard.Key#BACKSPACE BACKSPACE}
     * Change into the parent of the current directory.<br>
     * {@link Keyboard.Key#F5 F5} Refresh the file list.
     */
    @Override
    public boolean keyPressed(Component component, Keyboard.Key keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = super.keyPressed(component, keyCode, keyLocation);

        FileBrowser fileBrowser = (FileBrowser)getComponent();

        if (keyCode == Keyboard.Key.ENTER
            && keyboardFolderTraversalEnabled) {
            Sequence<File> selectedFiles = fileBrowser.getSelectedFiles();

            if (selectedFiles.getLength() == 1) {
                File selectedFile = selectedFiles.get(0);
                if (selectedFile.isDirectory()) {
                    fileBrowser.setRootDirectory(selectedFile);
                    consumed = true;
                }
            }
        } else if (keyCode == Keyboard.Key.DELETE
            || keyCode == Keyboard.Key.BACKSPACE) {
            File rootDirectory = fileBrowser.getRootDirectory();
            File parentDirectory = rootDirectory.getParentFile();
            if (parentDirectory != null) {
                fileBrowser.setRootDirectory(parentDirectory);
                consumed = true;
            }
        } else if (keyCode == Keyboard.Key.F5) {
            refreshFileList();
            consumed = true;
        }

        return consumed;
    }

    /**
     * CommandModifier + {@link Keyboard.Key#F F} Transfers focus to the search
     * TextInput.
     *
     * @see org.apache.pivot.ui.awt.JavaAwtPlatform#getCommandModifier()
     */
    @Override
    public boolean keyReleased(Component component, Keyboard.Key keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = super.keyReleased(component, keyCode, keyLocation);

        Keyboard.Modifier commandModifier = Platform.getInstalled().getCommandModifier();
        if (keyCode == Keyboard.Key.F
            && Keyboard.isPressed(commandModifier)) {
            searchTextInput.requestFocus();
            consumed = true;
        }

        return consumed;
    }

    @Override
    public void rootDirectoryChanged(FileBrowser fileBrowser, File previousRootDirectory) {
        ArrayList<File> path = new ArrayList<File>();

        File rootDirectory = fileBrowser.getRootDirectory();

        File ancestorDirectory = rootDirectory.getParentFile();
        while (ancestorDirectory != null) {
            path.add(ancestorDirectory);
            ancestorDirectory = ancestorDirectory.getParentFile();
        }

        File[] roots = File.listRoots();
        ArrayList<File> drives = new ArrayList<File>();
        for (int i = 0; i < roots.length; i++) {
            File root = roots[i];
            if (root.isDirectory()) {
                drives.add(root);
            }
        }

        driveListButton.setListData(drives);
        driveListButton.setVisible(drives.getLength() > 1);

        File drive;
        if (path.getLength() == 0) {
            drive = rootDirectory;
        } else {
            drive = path.get(path.getLength() - 1);
        }

        driveListButton.setSelectedItem(drive);

        pathListButton.setListData(path);
        pathListButton.setButtonData(rootDirectory);
        pathListButton.setEnabled(rootDirectory.getParentFile() != null);

        goUpButton.setEnabled(pathListButton.isEnabled());

        File homeDirectory = new File(System.getProperty("user.home"));
        goHomeButton.setEnabled(!rootDirectory.equals(homeDirectory));

        searchTextInput.setText("");

        fileScrollPane.setScrollTop(0);
        fileScrollPane.setScrollLeft(0);

        refreshFileList();

        fileTableView.requestFocus();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void selectedFileAdded(FileBrowser fileBrowser, File file) {
        if (!updatingSelection) {
            List<File> files = (List<File>)fileTableView.getTableData();
            int index = files.indexOf(file);
            if (index != -1) {
                fileTableView.addSelectedIndex(index);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void selectedFileRemoved(FileBrowser fileBrowser, File file) {
        if (!updatingSelection) {
            List<File> files = (List<File>)fileTableView.getTableData();
            int index = files.indexOf(file);
            if (index != -1) {
                fileTableView.removeSelectedIndex(index);
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void selectedFilesChanged(FileBrowser fileBrowser, Sequence<File> previousSelectedFiles) {
        if (!updatingSelection) {
            Sequence<File> selectedFiles = fileBrowser.getSelectedFiles();

            ArrayList<Span> selectedRanges = new ArrayList<Span>();
            for (int i = 0, n = selectedFiles.getLength(); i < n; i++) {
                File selectedFile = selectedFiles.get(i);

                List<File> files = (List<File>)fileTableView.getTableData();
                int index = files.indexOf(selectedFile);
                if (index != -1) {
                    selectedRanges.add(new Span(index, index));
                }
            }

            fileTableView.setSelectedRanges(selectedRanges);
        }
    }

    @Override
    public void multiSelectChanged(FileBrowser fileBrowser) {
        fileTableView.setSelectMode(fileBrowser.isMultiSelect() ? TableView.SelectMode.MULTI :
            TableView.SelectMode.SINGLE);
    }

    @Override
    public void disabledFileFilterChanged(FileBrowser fileBrowser, Filter<File> previousDisabledFileFilter) {
        fileTableView.setDisabledRowFilter(fileBrowser.getDisabledFileFilter());
        refreshFileList();
    }

    private void refreshFileList() {
        // Cancel any outstanding task
        if (refreshFileListTask != null) {
            refreshFileListTask.abort();
        }

        fileTableView.setTableData(new ArrayList<File>());

        refreshFileListTask = new RefreshFileListTask();
        refreshFileListTask.execute(new TaskAdapter<File[]>(new TaskListener<File[]>() {
            @SuppressWarnings("unchecked")
            @Override
            public void taskExecuted(Task<File[]> task) {
                if (task == refreshFileListTask) {
                    File files[] = task.getResult();

                    String text = searchTextInput.getText().trim();
                    IncludeFileFilter includeFileFilter = new IncludeFileFilter(text.length() == 0 ? null : text,
                        hideDisabledFiles ? ((FileBrowser) getComponent()).getDisabledFileFilter() : null);

                    TableView.SortDictionary sort = fileTableView.getSort();

                    final FileComparator fileComparator;
                    if (sort.isEmpty()) {
                        fileComparator = null;
                    } else {
                        Dictionary.Pair<String, SortDirection> pair = fileTableView.getSort().get(0);
                        fileComparator = new FileComparator(pair.key, pair.value);
                    }

                    for (int i = 0; i < files.length; i++) {
                        final File file = files[i];

                        if (includeFileFilter.include(file)) {
                            ArrayList<File> fileTableData = (ArrayList<File>)fileTableView.getTableData();

                            int index;
                            if (fileComparator == null) {
                                index = fileTableData.getLength();
                            } else {
                                index = ArrayList.binarySearch(fileTableData, file, fileComparator);
                                if (index < 0) {
                                    index = -(index + 1);
                                }
                            }

                            fileTableData.insert(file, index);
                        }
                    }

                    refreshFileListTask = null;
                }
            }

            @Override
            public void executeFailed(Task<File[]> task) {
                if (task == refreshFileListTask) {
                    refreshFileListTask = null;
                }
            }
        }));
    }
}
