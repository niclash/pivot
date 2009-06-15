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
package pivot.wtk.skin.terra;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.GeneralPath;

import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.List;
import org.apache.pivot.collections.Sequence;
import org.apache.pivot.collections.Sequence.Tree.Path;

import pivot.wtk.Bounds;
import pivot.wtk.Button;
import pivot.wtk.Checkbox;
import pivot.wtk.Component;
import pivot.wtk.GraphicsUtilities;
import pivot.wtk.Keyboard;
import pivot.wtk.Mouse;
import pivot.wtk.Orientation;
import pivot.wtk.Theme;
import pivot.wtk.TreeView;
import pivot.wtk.TreeViewListener;
import pivot.wtk.TreeViewBranchListener;
import pivot.wtk.TreeViewNodeListener;
import pivot.wtk.TreeViewNodeStateListener;
import pivot.wtk.TreeViewSelectionListener;
import pivot.wtk.skin.ComponentSkin;

/**
 * Tree view skin.
 *
 * @author tvolkert
 */
public class TerraTreeViewSkin extends ComponentSkin implements TreeView.Skin,
    TreeViewListener, TreeViewBranchListener, TreeViewNodeListener,
    TreeViewNodeStateListener, TreeViewSelectionListener{

    /**
     * An internal data structure that keeps track of skin-related metadata
     * for a tree node. The justification for the existence of this class lies
     * in the <tt>visibleNodes</tt> data structure, which is a flat list of
     * nodes that are visible at any given time. In this context, visible means
     * that their parent hierarchy is expanded, <b>not</b> that they are being
     * painted. This list, combined with <tt>getNodeHeight()</tt>, enables us
     * to quickly determine which nodes to paint given a graphics clip rect.
     * It also enables us to quickly traverse the tree view when handling key
     * events.
     * <p>
     * NOTE: some of this data is managed by <tt>TreeView</tt> and cached here
     * to provide further optimizations during painting and user input.
     *
     * @author tvolkert
     */
    protected static class NodeInfo {
        // Core skin metadata
        private BranchInfo parent;

        protected Object data;
        protected int depth;

        // Cached fields. Note that this is maintained as a bitmask in favor of
        // separate properties because it allows us to easily clear any cached
        // field for all nodes in one common method. See #clearField(byte)
        protected byte fields = 0;

        public static final byte HIGHLIGHTED_MASK = 1 << 0;
        public static final byte SELECTED_MASK = 1 << 1;
        public static final byte DISABLED_MASK = 1 << 2;
        public static final byte CHECK_STATE_CHECKED_MASK = 1 << 3;
        public static final byte CHECK_STATE_MIXED_MASK = 1 << 4;

        public static final byte CHECK_STATE_MASK = CHECK_STATE_CHECKED_MASK | CHECK_STATE_MIXED_MASK;

        public NodeInfo(BranchInfo parent, Object data) {
            this.parent = parent;
            this.data = data;

            depth = (parent == null) ? 0 : parent.depth + 1;
        }

        @SuppressWarnings({"unchecked"})
        public static NodeInfo createNew(BranchInfo parent, Object data) {
            NodeInfo nodeInfo = null;

            if (data instanceof List<?>) {
                nodeInfo = new BranchInfo(parent, (List<Object>)data);
            } else {
                nodeInfo = new NodeInfo(parent, data);
            }

            return nodeInfo;
        }

        @SuppressWarnings("unchecked")
        public Path getPath() {
            Path path = new Path(depth);

            NodeInfo nodeInfo = this;

            while (nodeInfo.parent != null) {
                List<Object> parentData = (List<Object>)nodeInfo.parent.data;
                int index = parentData.indexOf(nodeInfo.data);
                path.insert(index, 0);

                nodeInfo = nodeInfo.parent;
            }

            return path;
        }

        public boolean isHighlighted() {
            return ((fields & HIGHLIGHTED_MASK) != 0);
        }

        public void setHighlighted(boolean highlighted) {
            if (highlighted) {
                fields |= HIGHLIGHTED_MASK;
            } else {
                fields &= ~HIGHLIGHTED_MASK;
            }
        }

        public boolean isSelected() {
            return ((fields & SELECTED_MASK) != 0);
        }

        public void setSelected(boolean selected) {
            if (selected) {
                fields |= SELECTED_MASK;
            } else {
                fields &= ~SELECTED_MASK;
            }
        }

        public boolean isDisabled() {
            return ((fields & DISABLED_MASK) != 0);
        }

        public void setDisabled(boolean disabled) {
            if (disabled) {
                fields |= DISABLED_MASK;
            } else {
                fields &= ~DISABLED_MASK;
            }
        }

        public TreeView.NodeCheckState getCheckState() {
            TreeView.NodeCheckState checkState;

            switch (fields & CHECK_STATE_MASK) {
            case CHECK_STATE_CHECKED_MASK:
                checkState = TreeView.NodeCheckState.CHECKED;
                break;
            case CHECK_STATE_MIXED_MASK:
                checkState = TreeView.NodeCheckState.MIXED;
                break;
            default:
                checkState = TreeView.NodeCheckState.UNCHECKED;
                break;
            }

            return checkState;
        }

        public boolean isChecked() {
            return ((fields & CHECK_STATE_CHECKED_MASK) != 0);
        }

        public void setCheckState(TreeView.NodeCheckState checkState) {
            fields &= ~CHECK_STATE_MASK;

            switch (checkState) {
            case CHECKED:
                fields |= CHECK_STATE_CHECKED_MASK;
                break;
            case MIXED:
                fields |= CHECK_STATE_MIXED_MASK;
                break;
            }
        }

        public void clearField(byte mask) {
            fields &= ~mask;
        }
    }

    /**
     * An internal data structure that keeps track of skin-related metadata
     * for a tree branch.
     *
     * @author tvolkert
     */
    protected static class BranchInfo extends NodeInfo {
        // Core skin metadata
        protected List<NodeInfo> children = null;

        public static final byte EXPANDED_MASK = 1 << 5;

        public BranchInfo(BranchInfo parent, List<Object> data) {
            super(parent, data);
        }

        /**
         * Loads this branch info's children. The children list is initialized
         * to <tt>null</tt> and loaded lazily to allow the skin to only create
         * <tt>NodeInfo</tt> objects for the nodes that it actually needs in
         * order to paint. Thus, it is the responsibility of the skin to check
         * if <tt>children</tt> is null and call <tt>loadChildren()</tt> if
         * necessary.
         */
        @SuppressWarnings("unchecked")
        public void loadChildren() {
            if (children == null) {
                List<Object> data = (List<Object>)this.data;
                int count = data.getLength();

                children = new ArrayList<NodeInfo>(count);

                for (int i = 0; i < count; i++) {
                    Object nodeData = data.get(i);
                    children.add(NodeInfo.createNew(this, nodeData));
                }
            }
        }

        public boolean isExpanded() {
            return ((fields & EXPANDED_MASK) != 0);
        }

        public void setExpanded(boolean expanded) {
            if (expanded) {
                fields |= EXPANDED_MASK;
            } else {
                fields &= ~EXPANDED_MASK;
            }
        }
    }

    private BranchInfo rootBranchInfo = null;
    private List<NodeInfo> visibleNodes = new ArrayList<NodeInfo>();

    private NodeInfo highlightedNode = null;
    private NodeInfo editNode = null;

    // Styles
    private Font font;
    private Color color;
    private Color disabledColor;
    private Color backgroundColor;
    private Color selectionColor;
    private Color selectionBackgroundColor;
    private Color inactiveSelectionColor;
    private Color inactiveSelectionBackgroundColor;
    private Color highlightColor;
    private Color highlightBackgroundColor;
    private int spacing;
    private int indent;
    private boolean showHighlight;
    private boolean showBranchControls;
    private Color branchControlColor;
    private Color branchControlDisabledColor;
    private Color branchControlSelectionColor;
    private Color branchControlInactiveSelectionColor;
    private Color gridColor;
    private boolean showGridLines;

    private static final int BRANCH_CONTROL_IMAGE_WIDTH = 8;
    private static final int BRANCH_CONTROL_IMAGE_HEIGHT = 8;
    private static final int VERTICAL_SPACING = 1;

    private static final Checkbox CHECKBOX = new Checkbox();
    private static final int CHECKBOX_VERTICAL_PADDING = 2;

    static {
        CHECKBOX.setSize(CHECKBOX.getPreferredSize());
        CHECKBOX.setTriState(true);
    }

    public TerraTreeViewSkin() {
        TerraTheme theme = (TerraTheme)Theme.getTheme();

        font = theme.getFont();
        color = theme.getColor(1);
        disabledColor = theme.getColor(7);
        backgroundColor = theme.getColor(4);
        selectionColor = theme.getColor(4);
        selectionBackgroundColor = theme.getColor(19);
        inactiveSelectionColor = theme.getColor(1);
        inactiveSelectionBackgroundColor = theme.getColor(10);
        highlightColor = theme.getColor(1);
        highlightBackgroundColor = theme.getColor(10);
        spacing = 6;
        indent = 16;
        showHighlight = true;
        showBranchControls = true;
        branchControlColor = theme.getColor(18);
        branchControlDisabledColor = theme.getColor(19);
        branchControlSelectionColor = theme.getColor(4);
        branchControlInactiveSelectionColor = theme.getColor(19);
        gridColor = theme.getColor(11);
        showGridLines = false;
    }

    public void install(Component component) {
        super.install(component);

        TreeView treeView = (TreeView)component;
        treeView.getTreeViewListeners().add(this);
        treeView.getTreeViewBranchListeners().add(this);
        treeView.getTreeViewNodeListeners().add(this);
        treeView.getTreeViewNodeStateListeners().add(this);
        treeView.getTreeViewSelectionListeners().add(this);

        treeDataChanged(treeView, null);
    }

    public void uninstall() {
        TreeView treeView = (TreeView)getComponent();
        treeView.getTreeViewListeners().remove(this);
        treeView.getTreeViewBranchListeners().remove(this);
        treeView.getTreeViewNodeListeners().remove(this);
        treeView.getTreeViewNodeStateListeners().remove(this);
        treeView.getTreeViewSelectionListeners().remove(this);

        super.uninstall();
    }

    public int getPreferredWidth(int height) {
        TreeView treeView = (TreeView)getComponent();
        TreeView.NodeRenderer nodeRenderer = treeView.getNodeRenderer();

        int preferredWidth = 0;

        for (int i = 0, n = visibleNodes.getLength(); i < n; i++) {
            NodeInfo nodeInfo = visibleNodes.get(i);

            int nodeWidth = (nodeInfo.depth - 1) * (indent + spacing);

            nodeRenderer.render(nodeInfo.data, treeView, false, false,
                TreeView.NodeCheckState.UNCHECKED, false, false);
            nodeWidth += nodeRenderer.getPreferredWidth(-1);

            preferredWidth = Math.max(preferredWidth, nodeWidth);
        }

        if (showBranchControls) {
            preferredWidth += indent + spacing;
        }

        if (treeView.getCheckmarksEnabled()) {
            preferredWidth += Math.max(CHECKBOX.getWidth(), indent) + spacing;
        }

        return preferredWidth;
    }

    public int getPreferredHeight(int width) {
        int nodeHeight = getNodeHeight();
        int visibleNodeCount = visibleNodes.getLength();

        int preferredHeight = nodeHeight * visibleNodeCount;

        if (visibleNodeCount > 1) {
            preferredHeight += VERTICAL_SPACING * (visibleNodeCount - 1);
        }

        return preferredHeight;
    }

    public void layout() {
        // No-op
    }

    public void paint(Graphics2D graphics) {
        TreeView treeView = (TreeView)getComponent();
        TreeView.NodeRenderer nodeRenderer = treeView.getNodeRenderer();

        int width = getWidth();
        int height = getHeight();

        int nodeHeight = getNodeHeight();

        // Paint the background
        graphics.setPaint(backgroundColor);
        graphics.fillRect(0, 0, width, height);

        // nodeStart and nodeEnd are both inclusive
        int nodeStart = 0;
        int nodeEnd = visibleNodes.getLength() - 1;

        // Ensure that we only paint items that are visible
        Rectangle clipBounds = graphics.getClipBounds();
        if (clipBounds != null) {
            nodeStart = Math.max(nodeStart, (int)(clipBounds.y
                / (double)(nodeHeight + VERTICAL_SPACING)));
            nodeEnd = Math.min(nodeEnd, (int)((clipBounds.y +
                clipBounds.height) / (double)(nodeHeight + VERTICAL_SPACING)));
        }

        int nodeY = nodeStart * (nodeHeight + VERTICAL_SPACING);

        for (int i = nodeStart; i <= nodeEnd; i++) {
            NodeInfo nodeInfo = visibleNodes.get(i);

            boolean expanded = false;
            boolean highlighted = nodeInfo.isHighlighted();
            boolean selected = nodeInfo.isSelected();
            boolean disabled = nodeInfo.isDisabled();

            int nodeX = (nodeInfo.depth - 1) * (indent + spacing);

            if (treeView.isEnabled()) {
                if (selected) {
                    // Paint the selection state
                    Color selectionBackgroundColor = treeView.isFocused() ?
                        this.selectionBackgroundColor : inactiveSelectionBackgroundColor;
                    graphics.setPaint(selectionBackgroundColor);
                    graphics.fillRect(0, nodeY, width, nodeHeight);
                } else if (highlighted && !disabled) {
                    // Paint the highlight state
                    graphics.setPaint(highlightBackgroundColor);
                    graphics.fillRect(0, nodeY, width, nodeHeight);
                }
            }

            // Paint the expand/collapse control
            if (showBranchControls) {
                if (nodeInfo instanceof BranchInfo) {
                    BranchInfo branchInfo = (BranchInfo)nodeInfo;
                    expanded = branchInfo.isExpanded();

                    Color branchControlColor;

                    if (treeView.isEnabled()
                        && !disabled) {
                        if (selected) {
                            if (treeView.isFocused()) {
                                branchControlColor = branchControlSelectionColor;
                            } else {
                                branchControlColor = branchControlInactiveSelectionColor;
                            }
                        } else {
                            branchControlColor = this.branchControlColor;
                        }
                    } else {
                        branchControlColor = branchControlDisabledColor;
                    }

                    GeneralPath shape = new GeneralPath();

                    int imageX = nodeX + (indent - BRANCH_CONTROL_IMAGE_WIDTH) / 2;
                    int imageY = nodeY + (nodeHeight - BRANCH_CONTROL_IMAGE_HEIGHT) / 2;

                    if (expanded) {
                        shape.moveTo(imageX, imageY + 1);
                        shape.lineTo(imageX + 8, imageY + 1);
                        shape.lineTo(imageX + 4, imageY + 7);
                    } else {
                        shape.moveTo(imageX + 1, imageY);
                        shape.lineTo(imageX + 7, imageY + 4);
                        shape.lineTo(imageX + 1, imageY + 8);
                    }

                    shape.closePath();

                    graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                    graphics.setPaint(branchControlColor);

                    graphics.fill(shape);
                }

                nodeX += indent + spacing;
            }

            // Paint the checkbox
            TreeView.NodeCheckState checkState = TreeView.NodeCheckState.UNCHECKED;
            if (treeView.getCheckmarksEnabled()) {
                checkState = nodeInfo.getCheckState();

                int checkboxWidth = CHECKBOX.getWidth();
                int checkboxHeight = CHECKBOX.getHeight();

                int checkboxX = Math.max(indent - checkboxWidth, 0) / 2;
                int checkboxY = (nodeHeight - checkboxHeight) / 2;
                Graphics2D checkboxGraphics = (Graphics2D)graphics.create(nodeX + checkboxX,
                    nodeY + checkboxY, checkboxWidth, checkboxHeight);

                Button.State state;
                switch (checkState) {
                case CHECKED:
                    state = Button.State.SELECTED;
                    break;
                case MIXED:
                    state = Button.State.MIXED;
                    break;
                default:
                    state = Button.State.UNSELECTED;
                    break;
                }

                CHECKBOX.setState(state);
                CHECKBOX.paint(checkboxGraphics);
                checkboxGraphics.dispose();

                nodeX += Math.max(indent, checkboxWidth) + spacing;
            }

            int nodeWidth = width - nodeX;

            // Paint the node data
            Graphics2D rendererGraphics = (Graphics2D)graphics.create(nodeX, nodeY,
                nodeWidth, nodeHeight);
            nodeRenderer.render(nodeInfo.data, treeView, expanded, selected,
                checkState, highlighted, disabled);
            nodeRenderer.setSize(nodeWidth, nodeHeight);
            nodeRenderer.paint(rendererGraphics);
            rendererGraphics.dispose();

            // Paint the grid line
            if (showGridLines) {
                graphics.setPaint(gridColor);

                GraphicsUtilities.drawLine(graphics, 0, nodeY + nodeHeight, width, Orientation.HORIZONTAL);
            }

            nodeY += nodeHeight + VERTICAL_SPACING;
        }
    }

    public Font getFont() {
        return font;
    }

    public void setFont(Font font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        this.font = font;
        invalidateComponent();
    }

    public void setFont(String font) {
        if (font == null) {
            throw new IllegalArgumentException("font is null.");
        }

        setFont(Font.decode(font));
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        this.color = color;
        repaintComponent();
    }

    public void setColor(String color) {
        if (color == null) {
            throw new IllegalArgumentException("color is null.");
        }

        setColor(GraphicsUtilities.decodeColor(color));
    }

    public final void setColor(int color) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setColor(theme.getColor(color));
    }

    public Color getDisabledColor() {
        return disabledColor;
    }

    public void setDisabledColor(Color disabledColor) {
        if (disabledColor == null) {
            throw new IllegalArgumentException("disabledColor is null.");
        }

        this.disabledColor = disabledColor;
        repaintComponent();
    }

    public void setDisabledColor(String disabledColor) {
        if (disabledColor == null) {
            throw new IllegalArgumentException("disabledColor is null.");
        }

        setDisabledColor(GraphicsUtilities.decodeColor(disabledColor));
    }

    public final void setDisabledColor(int disabledColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setDisabledColor(theme.getColor(disabledColor));
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Color backgroundColor) {
        if (backgroundColor == null) {
            throw new IllegalArgumentException("backgroundColor is null.");
        }

        this.backgroundColor = backgroundColor;
        repaintComponent();
    }

    public void setBackgroundColor(String backgroundColor) {
        if (backgroundColor == null) {
            throw new IllegalArgumentException("backgroundColor is null.");
        }

        setBackgroundColor(GraphicsUtilities.decodeColor(backgroundColor));
    }

    public final void setBackgroundColor(int backgroundColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setBackgroundColor(theme.getColor(backgroundColor));
    }

    public Color getSelectionColor() {
        return selectionColor;
    }

    public void setSelectionColor(Color selectionColor) {
        if (selectionColor == null) {
            throw new IllegalArgumentException("selectionColor is null.");
        }

        this.selectionColor = selectionColor;
        repaintComponent();
    }

    public void setSelectionColor(String selectionColor) {
        if (selectionColor == null) {
            throw new IllegalArgumentException("selectionColor is null.");
        }

        setSelectionColor(GraphicsUtilities.decodeColor(selectionColor));
    }

    public final void setSelectionColor(int selectionColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setSelectionColor(theme.getColor(selectionColor));
    }

    public Color getSelectionBackgroundColor() {
        return selectionBackgroundColor;
    }

    public void setSelectionBackgroundColor(Color selectionBackgroundColor) {
        if (selectionBackgroundColor == null) {
            throw new IllegalArgumentException("selectionBackgroundColor is null.");
        }

        this.selectionBackgroundColor = selectionBackgroundColor;
        repaintComponent();
    }

    public void setSelectionBackgroundColor(String selectionBackgroundColor) {
        if (selectionBackgroundColor == null) {
            throw new IllegalArgumentException("selectionBackgroundColor is null.");
        }

        setSelectionBackgroundColor(GraphicsUtilities.decodeColor(selectionBackgroundColor));
    }

    public final void setSelectionBackgroundColor(int selectionBackgroundColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setSelectionBackgroundColor(theme.getColor(selectionBackgroundColor));
    }

    public Color getInactiveSelectionColor() {
        return inactiveSelectionColor;
    }

    public void setInactiveSelectionColor(Color inactiveSelectionColor) {
        if (inactiveSelectionColor == null) {
            throw new IllegalArgumentException("inactiveSelectionColor is null.");
        }

        this.inactiveSelectionColor = inactiveSelectionColor;
        repaintComponent();
    }

    public void setInactiveSelectionColor(String inactiveSelectionColor) {
        if (inactiveSelectionColor == null) {
            throw new IllegalArgumentException("inactiveSelectionColor is null.");
        }

        setInactiveSelectionColor(GraphicsUtilities.decodeColor(inactiveSelectionColor));
    }

    public final void setInactiveSelectionColor(int inactiveSelectionColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setInactiveSelectionColor(theme.getColor(inactiveSelectionColor));
    }

    public Color getInactiveSelectionBackgroundColor() {
        return inactiveSelectionBackgroundColor;
    }

    public void setInactiveSelectionBackgroundColor(Color inactiveSelectionBackgroundColor) {
        if (inactiveSelectionBackgroundColor == null) {
            throw new IllegalArgumentException("inactiveSelectionBackgroundColor is null.");
        }

        this.inactiveSelectionBackgroundColor = inactiveSelectionBackgroundColor;
        repaintComponent();
    }

    public void setInactiveSelectionBackgroundColor(String inactiveSelectionBackgroundColor) {
        if (inactiveSelectionBackgroundColor == null) {
            throw new IllegalArgumentException("inactiveSelectionBackgroundColor is null.");
        }

        setInactiveSelectionBackgroundColor(GraphicsUtilities.decodeColor(inactiveSelectionBackgroundColor));
    }

    public final void setInactiveSelectionBackgroundColor(int inactiveSelectionBackgroundColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setInactiveSelectionBackgroundColor(theme.getColor(inactiveSelectionBackgroundColor));
    }

    public Color getHighlightColor() {
        return highlightColor;
    }

    public void setHighlightColor(Color highlightColor) {
        if (highlightColor == null) {
            throw new IllegalArgumentException("highlightColor is null.");
        }

        this.highlightColor = highlightColor;
        repaintComponent();
    }

    public void setHighlightColor(String highlightColor) {
        if (highlightColor == null) {
            throw new IllegalArgumentException("highlightColor is null.");
        }

        setHighlightColor(GraphicsUtilities.decodeColor(highlightColor));
    }

    public final void setHighlightColor(int highlightColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setHighlightColor(theme.getColor(highlightColor));
    }

    public Color getHighlightBackgroundColor() {
        return highlightBackgroundColor;
    }

    public void setHighlightBackgroundColor(Color highlightBackgroundColor) {
        if (highlightBackgroundColor == null) {
            throw new IllegalArgumentException("highlightBackgroundColor is null.");
        }

        this.highlightBackgroundColor = highlightBackgroundColor;
        repaintComponent();
    }

    public void setHighlightBackgroundColor(String highlightBackgroundColor) {
        if (highlightBackgroundColor == null) {
            throw new IllegalArgumentException("highlightBackgroundColor is null.");
        }

        setHighlightBackgroundColor(GraphicsUtilities.decodeColor(highlightBackgroundColor));
    }

    public final void setHighlightBackgroundColor(int highlightBackgroundColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setHighlightBackgroundColor(theme.getColor(highlightBackgroundColor));
    }

    public int getSpacing() {
        return spacing;
    }

    public void setSpacing(int spacing) {
        this.spacing = spacing;
        invalidateComponent();
    }

    public void setSpacing(Number spacing) {
        if (spacing == null) {
            throw new IllegalArgumentException("spacing is null.");
        }

        setSpacing(spacing.intValue());
    }

    public int getIndent() {
        return indent;
    }

    public void setIndent(int indent) {
        this.indent = indent;
        invalidateComponent();
    }

    public void setIndent(Number indent) {
        if (indent == null) {
            throw new IllegalArgumentException("indent is null.");
        }

        setIndent(indent.intValue());
    }

    public boolean getShowHighlight() {
        return showHighlight;
    }

    public void setShowHighlight(boolean showHighlight) {
        this.showHighlight = showHighlight;
        repaintComponent();
    }

    public boolean getShowBranchControls() {
        return showBranchControls;
    }

    public void setShowBranchControls(boolean showBranchControls) {
        this.showBranchControls = showBranchControls;
        invalidateComponent();
    }

    public Color getBranchControlColor() {
        return branchControlColor;
    }

    public void setBranchControlColor(Color branchControlColor) {
        if (branchControlColor == null) {
            throw new IllegalArgumentException("branchControlColor is null.");
        }

        this.branchControlColor = branchControlColor;
        repaintComponent();
    }

    public void setBranchControlColor(String branchControlColor) {
        if (branchControlColor == null) {
            throw new IllegalArgumentException("branchControlColor is null.");
        }

        setBranchControlColor(GraphicsUtilities.decodeColor(branchControlColor));
    }

    public final void setBranchControlColor(int branchControlColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setBranchControlColor(theme.getColor(branchControlColor));
    }

    public Color getBranchControlDisabledColor() {
        return branchControlDisabledColor;
    }

    public void setBranchControlDisabledColor(Color branchControlDisabledColor) {
        if (branchControlDisabledColor == null) {
            throw new IllegalArgumentException("branchControlDisabledColor is null.");
        }

        this.branchControlDisabledColor = branchControlDisabledColor;
        repaintComponent();
    }

    public void setBranchControlDisabledColor(String branchControlDisabledColor) {
        if (branchControlDisabledColor == null) {
            throw new IllegalArgumentException("branchControlDisabledColor is null.");
        }

        setBranchControlDisabledColor(GraphicsUtilities.decodeColor(branchControlDisabledColor));
    }

    public final void setBranchControlDisabledColor(int branchControlDisabledColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setBranchControlDisabledColor(theme.getColor(branchControlDisabledColor));
    }

    public Color getBranchControlSelectionColor() {
        return branchControlSelectionColor;
    }

    public void setBranchControlSelectionColor(Color branchControlSelectionColor) {
        if (branchControlSelectionColor == null) {
            throw new IllegalArgumentException("branchControlSelectionColor is null.");
        }

        this.branchControlSelectionColor = branchControlSelectionColor;
        repaintComponent();
    }

    public void setBranchControlSelectionColor(String branchControlSelectionColor) {
        if (branchControlSelectionColor == null) {
            throw new IllegalArgumentException("branchControlSelectionColor is null.");
        }

        setBranchControlSelectionColor(GraphicsUtilities.decodeColor(branchControlSelectionColor));
    }

    public final void setBranchControlSelectionColor(int branchControlSelectionColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setBranchControlSelectionColor(theme.getColor(branchControlSelectionColor));
    }

    public Color getBranchControlInactiveSelectionColor() {
        return branchControlInactiveSelectionColor;
    }

    public void setBranchControlInactiveSelectionColor(Color branchControlInactiveSelectionColor) {
        if (branchControlInactiveSelectionColor == null) {
            throw new IllegalArgumentException("branchControlInactiveSelectionColor is null.");
        }

        this.branchControlInactiveSelectionColor = branchControlInactiveSelectionColor;
        repaintComponent();
    }

    public void setBranchControlInactiveSelectionColor(String branchControlInactiveSelectionColor) {
        if (branchControlInactiveSelectionColor == null) {
            throw new IllegalArgumentException("branchControlInactiveSelectionColor is null.");
        }

        setBranchControlInactiveSelectionColor(GraphicsUtilities.decodeColor(branchControlInactiveSelectionColor));
    }

    public final void setBranchControlInactiveSelectionColor(int branchControlInactiveSelectionColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setBranchControlInactiveSelectionColor(theme.getColor(branchControlInactiveSelectionColor));
    }

    public Color getGridColor() {
        return gridColor;
    }

    public void setGridColor(Color gridColor) {
        if (gridColor == null) {
            throw new IllegalArgumentException("gridColor is null.");
        }

        this.gridColor = gridColor;
        repaintComponent();
    }

    public void setGridColor(String gridColor) {
        if (gridColor == null) {
            throw new IllegalArgumentException("gridColor is null.");
        }

        setGridColor(GraphicsUtilities.decodeColor(gridColor));
    }

    public final void setGridColor(int gridColor) {
        TerraTheme theme = (TerraTheme)Theme.getTheme();
        setGridColor(theme.getColor(gridColor));
    }

    public boolean getShowGridLines() {
        return showGridLines;
    }

    public void setShowGridLines(boolean showGridLines) {
        this.showGridLines = showGridLines;
        repaintComponent();
    }

    /**
     * Gets the fixed node height of this skin.
     */
    protected int getNodeHeight() {
        TreeView treeView = (TreeView)getComponent();
        TreeView.NodeRenderer nodeRenderer = treeView.getNodeRenderer();
        nodeRenderer.render(null, treeView, false, false,
            TreeView.NodeCheckState.UNCHECKED, false, false);

        int nodeHeight = nodeRenderer.getPreferredHeight(-1);
        if (treeView.getCheckmarksEnabled()) {
            nodeHeight = Math.max(CHECKBOX.getHeight() + (2 * CHECKBOX_VERTICAL_PADDING), nodeHeight);
        }

        return nodeHeight;
    }

    /**
     * Gets the metadata associated with the node found at the specified
     * y-coordinate, or <tt>null</tt> if there is no node at that location.
     */
    protected NodeInfo getNodeInfoAt(int y) {
        NodeInfo nodeInfo = null;

        int nodeHeight = getNodeHeight();
        int index = y / (nodeHeight + VERTICAL_SPACING);

        if (index >= 0
            && index < visibleNodes.getLength()) {
            nodeInfo = visibleNodes.get(index);
        }

        return nodeInfo;
    }

    /**
     * Gets the metadata associated with the node at the specified path.
     * The path must be valid.
     */
    protected NodeInfo getNodeInfoAt(Path path) {
        assert(path != null) : "Path is null";

        NodeInfo result = null;

        int n = path.getLength();

        if (n == 0) {
            result = rootBranchInfo;
        } else {
            BranchInfo branchInfo = rootBranchInfo;

            for (int i = 0; i < n - 1; i++) {
                branchInfo.loadChildren();
                NodeInfo nodeInfo = branchInfo.children.get(path.get(i));

                assert(nodeInfo instanceof BranchInfo) : "Invalid path";

                branchInfo = (BranchInfo)nodeInfo;
            }

            branchInfo.loadChildren();
            result = branchInfo.children.get(path.get(n - 1));
        }

        return result;
    }

    /**
     * Gets the bounding box defined by the specified node, or <tt>null</tt>
     * if the node is not currently visible.
     */
    protected Bounds getNodeBounds(NodeInfo nodeInfo) {
        Bounds bounds = null;

        int index = visibleNodes.indexOf(nodeInfo);

        if (index >= 0) {
            int nodeHeight = getNodeHeight();
            int nodeY = index * (nodeHeight + VERTICAL_SPACING);

            bounds = new Bounds(0, nodeY, getWidth(), nodeHeight);
        }

        return bounds;
    }

    /**
     * Adds all children of the specified branch to the visible node list.
     * Any children nodes that are expanded [branches] will also have their
     * children made visible, and so on. Invalidates the component only
     * if necessary.
     */
    private void addVisibleNodes(BranchInfo parentBranchInfo) {
        int insertIndex = -1;

        if (parentBranchInfo == rootBranchInfo) {
            // Bootstrap case since the root branch is implicitly expanded
            insertIndex = 0;
        } else {
            int branchIndex = visibleNodes.indexOf(parentBranchInfo);
            if (branchIndex >= 0) {
                insertIndex = branchIndex + 1;
            }
        }

        if (insertIndex >= 0) {
            Sequence<NodeInfo> nodes = new ArrayList<NodeInfo>();

            // The parent branch's children are the baseline nodes to make
            // visible
            parentBranchInfo.loadChildren();
            for (int i = 0, n = parentBranchInfo.children.getLength(); i < n; i++) {
                nodes.add(parentBranchInfo.children.get(i));
            }

            while (nodes.getLength() > 0) {
                NodeInfo nodeInfo = nodes.get(0);
                nodes.remove(0, 1);

                visibleNodes.insert(nodeInfo, insertIndex++);

                // If we encounter an expanded branch, we add that branch's
                // children to our list of nodes that are to become visible
                if (nodeInfo instanceof BranchInfo) {
                    BranchInfo branchInfo = (BranchInfo)nodeInfo;

                    if (branchInfo.isExpanded()) {
                        branchInfo.loadChildren();
                        for (int i = 0, n = branchInfo.children.getLength(); i < n; i++) {
                            nodes.insert(branchInfo.children.get(i), i);
                        }
                    }
                }
            }

            invalidateComponent();
        }
    }

    /**
     * Adds the specified child of the specified branch to the visible node
     * list. It is assumed that the child in question is not an expanded
     * branch. Invalidates the component only if necessary.
     *
     * @param parentBranchInfo
     * The branch info of the parent node.
     *
     * @param index
     * The index of the child within its parent.
     */
    private void addVisibleNode(BranchInfo parentBranchInfo, int index) {
        parentBranchInfo.loadChildren();

        assert(index >= 0) : "Index is too small";
        assert(index < parentBranchInfo.children.getLength()) : "Index is too large";

        int branchIndex = visibleNodes.indexOf(parentBranchInfo);

        if (parentBranchInfo == rootBranchInfo
            || (branchIndex >= 0 && parentBranchInfo.isExpanded())) {

            NodeInfo nodeInfo = parentBranchInfo.children.get(index);

            int insertIndex = branchIndex + index + 1;

            if (index > 0) {
                // Siblings of the node that lie before it may be expanded
                // branches, thus adding their own children to the
                // visible nodes list and pushing down our insert index
                NodeInfo youngerSibling = parentBranchInfo.children.get(index - 1);

                // Try to insert after our younger sibling
                insertIndex = visibleNodes.indexOf(youngerSibling) + 1;

                // Continue looking as long as the node at our insert index
                // has a greater depth than we do, which means that it's a
                // descendant of our younger sibling
                for (int n = visibleNodes.getLength(), nodeDepth = youngerSibling.depth;
                    insertIndex < n && visibleNodes.get(insertIndex).depth > nodeDepth;
                    insertIndex++);
            }

            visibleNodes.insert(nodeInfo, insertIndex);

            invalidateComponent();
        }
    }

    /**
     * Removes the specified children of the specified branch from the visible
     * node list if necessary. If they are not already in the visible node
     * list, nothing happens. Invalidates the component only if necessary.
     *
     * @param parentBranchInfo
     * The branch info of the parent node.
     *
     * @param index
     * The index of the first child node to remove from the visible nodes
     * sequence.
     *
     * @param count
     * The number of child nodes to remove, or <tt>-1</tt> to remove all
     * child nodes from the visible nodes sequence.
     */
    private void removeVisibleNodes(BranchInfo parentBranchInfo, int index, int count) {
        parentBranchInfo.loadChildren();

        if (count == -1) {
            assert(index == 0) : "Non-zero index with 'remove all' count";
            count = parentBranchInfo.children.getLength();
        }

        assert(index + count <= parentBranchInfo.children.getLength()) : "Value too big";

        if (count > 0) {
            NodeInfo first = parentBranchInfo.children.get(index);
            NodeInfo last = parentBranchInfo.children.get(index + count - 1);

            int rangeStart = visibleNodes.indexOf(first);

            if (rangeStart >= 0) {
                int rangeEnd = visibleNodes.indexOf(last) + 1;

                assert(rangeEnd > rangeStart) : "Invalid visible node structure";

                // Continue looking as long as the node at our endpoint has a
                // greater depth than the last child node, which means that
                // it's a descendant of the last child node
                for (int n = visibleNodes.getLength(), nodeDepth = last.depth;
                    rangeEnd < n && visibleNodes.get(rangeEnd).depth > nodeDepth;
                    rangeEnd++);

                visibleNodes.remove(rangeStart, rangeEnd - rangeStart);

                invalidateComponent();
            }
        }
    }

    /**
     * Repaints the region occupied by the specified node.
     */
    protected void repaintNode(NodeInfo nodeInfo) {
        Bounds bounds = getNodeBounds(nodeInfo);
        if (bounds != null) {
            repaintComponent(bounds);
        }
    }

    /**
     * Clears the highlighted node if one exists.
     */
    protected void clearHighlightedNode() {
        if (highlightedNode != null) {
            highlightedNode.setHighlighted(false);
            repaintNode(highlightedNode);

            highlightedNode = null;
        }
    }

    /**
     * Clears our <tt>NodeInfo</tt> hierarchy of the specified cached field.
     *
     * @param mask
     * The bitmask specifying which field to clear.
     */
    private void clearFields(byte mask) {
        Sequence<NodeInfo> nodes = new ArrayList<NodeInfo>();
        nodes.add(rootBranchInfo);

        while (nodes.getLength() > 0) {
            NodeInfo nodeInfo = nodes.get(0);
            nodes.remove(0, 1);

            nodeInfo.clearField(mask);

            if (nodeInfo instanceof BranchInfo) {
                BranchInfo branchInfo = (BranchInfo)nodeInfo;

                if (branchInfo.children != null) {
                    for (int i = 0, n = branchInfo.children.getLength(); i < n; i++) {
                        nodes.insert(branchInfo.children.get(i), i);
                    }
                }
            }
        }
    }

    @Override
    public boolean mouseMove(Component component, int x, int y) {
        boolean consumed = super.mouseMove(component, x, y);

        TreeView treeView = (TreeView)getComponent();

        if (showHighlight
            && treeView.getSelectMode() != TreeView.SelectMode.NONE) {
            NodeInfo previousHighlightedNode = highlightedNode;
            highlightedNode = getNodeInfoAt(y);

            if (highlightedNode != previousHighlightedNode) {
                if (previousHighlightedNode != null) {
                    previousHighlightedNode.setHighlighted(false);
                    repaintNode(previousHighlightedNode);
                }

                if (highlightedNode != null) {
                    highlightedNode.setHighlighted(true);
                    repaintNode(highlightedNode);
                }
            }
        }

        return consumed;
    }

    @Override
    public void mouseOut(Component component) {
        super.mouseOut(component);

        clearHighlightedNode();
        editNode = null;
    }

    @Override
    public boolean mouseDown(Component component, Mouse.Button button, int x, int y) {
        boolean consumed = super.mouseDown(component, button, x, y);

        if (!consumed) {
            TreeView treeView = (TreeView)getComponent();
            NodeInfo nodeInfo = getNodeInfoAt(y);

            if (nodeInfo != null
                && !nodeInfo.isDisabled()) {
                int nodeHeight = getNodeHeight();
                int baseNodeX = (nodeInfo.depth - 1) * (indent + spacing);

                int nodeX = baseNodeX + (showBranchControls ? indent + spacing : 0);
                int nodeY = (y / (nodeHeight + VERTICAL_SPACING)) * (nodeHeight + VERTICAL_SPACING);

                int checkboxWidth = CHECKBOX.getWidth();
                int checkboxHeight = CHECKBOX.getHeight();

                int checkboxX = Math.max(indent - checkboxWidth, 0) / 2;
                int checkboxY = (nodeHeight - checkboxHeight) / 2;

                // Only proceed if the user DIDN'T click on a checkbox
                if (!treeView.getCheckmarksEnabled()
                    || x < nodeX + checkboxX
                    || x >= nodeX + checkboxX + checkboxWidth
                    || y < nodeY + checkboxY
                    || y >= nodeY + checkboxY + checkboxHeight) {
                    Path path = nodeInfo.getPath();

                    // See if the user clicked on an expand/collapse control of
                    // a branch. If so, expand/collapse the branch
                    if (showBranchControls
                        && nodeInfo instanceof BranchInfo
                        && x >= baseNodeX
                        && x < baseNodeX + indent) {
                        BranchInfo branchInfo = (BranchInfo)nodeInfo;

                        treeView.setBranchExpanded(path, !branchInfo.isExpanded());
                        consumed = true;
                    }

                    // If we haven't consumed the event, then proceed to manage
                    // the selection state of the node
                    if (!consumed) {
                        TreeView.SelectMode selectMode = treeView.getSelectMode();

                        if (selectMode == TreeView.SelectMode.SINGLE) {
                            if (nodeInfo.isSelected()
                                && treeView.isFocused()) {
                                // Edit the node
                                editNode = nodeInfo;
                            } else {
                                // Select the node
                                treeView.setSelectedPath(path);
                            }
                        } else if (selectMode == TreeView.SelectMode.MULTI) {
                            if (Keyboard.isPressed(Keyboard.Modifier.CTRL)) {
                                if (nodeInfo.isSelected()) {
                                    treeView.removeSelectedPath(path);
                                } else {
                                    treeView.addSelectedPath(path);
                                }
                            } else {
                                // Replace the selection
                                treeView.setSelectedPath(path);
                            }
                        }
                    }
                }
            }

            if (treeView.isFocusable()) {
                treeView.requestFocus();
            }
        }

        return consumed;
    }

    @Override
    public boolean mouseClick(Component component, Mouse.Button button, int x, int y, int count) {
        boolean consumed = super.mouseClick(component, button, x, y, count);

        if (!consumed) {
            TreeView treeView = (TreeView)getComponent();

            NodeInfo nodeInfo = getNodeInfoAt(y);

            if (nodeInfo != null
                && !nodeInfo.isDisabled()) {
                int nodeHeight = getNodeHeight();
                int baseNodeX = (nodeInfo.depth - 1) * (indent + spacing);

                int nodeX = baseNodeX + (showBranchControls ? indent + spacing : 0);
                int nodeY = (y / (nodeHeight + VERTICAL_SPACING)) * (nodeHeight + VERTICAL_SPACING);

                int checkboxWidth = CHECKBOX.getWidth();
                int checkboxHeight = CHECKBOX.getHeight();

                int checkboxX = Math.max(indent - checkboxWidth, 0) / 2;
                int checkboxY = (nodeHeight - checkboxHeight) / 2;

                if (treeView.getCheckmarksEnabled()
                    && x >= nodeX + checkboxX
                    && x < nodeX + checkboxX + checkboxWidth
                    && y >= nodeY + checkboxY
                    && y < nodeY + checkboxY + checkboxHeight) {
                    Path path = nodeInfo.getPath();
                    treeView.setNodeChecked(path, !nodeInfo.isChecked());
                } else {
                    if (editNode != null
                        && count == 1) {
                        TreeView.NodeEditor nodeEditor = treeView.getNodeEditor();

                        if (nodeEditor != null) {
                            nodeEditor.edit(treeView, nodeInfo.getPath());
                        }
                    }

                    editNode = null;
                }
            }
        }

        return consumed;
    }

    @Override
    public boolean mouseWheel(Component component, Mouse.ScrollType scrollType, int scrollAmount,
        int wheelRotation, int x, int y) {
        boolean consumed = super.mouseWheel(component, scrollType, scrollAmount,
            wheelRotation, x, y);

        clearHighlightedNode();

        return consumed;
    }

    @Override
    public boolean keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        TreeView treeView = (TreeView)getComponent();
        TreeView.SelectMode selectMode = treeView.getSelectMode();

        switch (keyCode) {
        case Keyboard.KeyCode.UP: {
            if (selectMode != TreeView.SelectMode.NONE) {
                Path firstSelectedPath = treeView.getFirstSelectedPath();

                int index;
                if (firstSelectedPath != null) {
                    NodeInfo previousSelectedNode = getNodeInfoAt(firstSelectedPath);
                    index = visibleNodes.indexOf(previousSelectedNode);
                } else {
                    // Select the last visible node
                    index = visibleNodes.getLength();
                }

                NodeInfo newSelectedNode = null;
                do {
                    newSelectedNode = (--index >= 0) ? visibleNodes.get(index) : null;
                } while (newSelectedNode != null
                    && newSelectedNode.isDisabled());

                if (newSelectedNode != null) {
                    treeView.setSelectedPath(newSelectedNode.getPath());
                    treeView.scrollAreaToVisible(getNodeBounds(newSelectedNode));
                    consumed = true;
                }
            }

            break;
        }

        case Keyboard.KeyCode.DOWN: {
            if (selectMode != TreeView.SelectMode.NONE) {
                Path lastSelectedPath = treeView.getLastSelectedPath();

                int index;
                if (lastSelectedPath != null) {
                    NodeInfo previousSelectedNode = getNodeInfoAt(lastSelectedPath);
                    index = visibleNodes.indexOf(previousSelectedNode);
                } else {
                    // Select the first visible node
                    index = -1;
                }

                NodeInfo newSelectedNode = null;
                int n = visibleNodes.getLength();
                do {
                    newSelectedNode = (++index <= n - 1) ? visibleNodes.get(index) : null;
                } while (newSelectedNode != null
                    && newSelectedNode.isDisabled());

                if (newSelectedNode != null) {
                    treeView.setSelectedPath(newSelectedNode.getPath());
                    treeView.scrollAreaToVisible(getNodeBounds(newSelectedNode));
                    consumed = true;
                }
            }

            break;
        }

        case Keyboard.KeyCode.LEFT: {
            if (showBranchControls) {
                Sequence<Path> paths = treeView.getSelectedPaths();

                if (paths != null) {
                    Path path = paths.get(paths.getLength() - 1);
                    NodeInfo nodeInfo = getNodeInfoAt(path);

                    if (nodeInfo instanceof BranchInfo) {
                        BranchInfo branchInfo = (BranchInfo)nodeInfo;

                        if (branchInfo.isExpanded()) {
                            treeView.collapseBranch(branchInfo.getPath());
                        }
                    }

                    consumed = true;
                }
            }

            break;
        }

        case Keyboard.KeyCode.RIGHT: {
            if (showBranchControls) {
                Sequence<Path> paths = treeView.getSelectedPaths();

                if (paths != null) {
                    Path path = paths.get(paths.getLength() - 1);
                    NodeInfo nodeInfo = getNodeInfoAt(path);

                    if (nodeInfo instanceof BranchInfo) {
                        BranchInfo branchInfo = (BranchInfo)nodeInfo;

                        if (!branchInfo.isExpanded()) {
                            treeView.expandBranch(branchInfo.getPath());
                        }
                    }

                    consumed = true;
                }
            }

            break;
        }

        default:
            consumed = super.keyPressed(component, keyCode, keyLocation);
            break;
        }

        clearHighlightedNode();

        return consumed;
    }

    @Override
    public boolean keyReleased(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
        boolean consumed = false;

        TreeView treeView = (TreeView)getComponent();

        if (keyCode == Keyboard.KeyCode.SPACE) {
            if (treeView.getCheckmarksEnabled()
                && treeView.getSelectMode() == TreeView.SelectMode.SINGLE) {
                Path selectedPath = treeView.getSelectedPath();

                if (selectedPath != null) {
                    treeView.setNodeChecked(selectedPath, !treeView.isNodeChecked(selectedPath));
                }
            }
        } else {
            consumed = super.keyReleased(component, keyCode, keyLocation);
        }

        return consumed;
    }

    @Override
    public boolean isFocusable() {
        TreeView treeView = (TreeView)getComponent();
        return (treeView.getSelectMode() != TreeView.SelectMode.NONE);
    }

    // Component state events

    @Override
    public void enabledChanged(Component component) {
        super.enabledChanged(component);

        repaintComponent();
    }

    @Override
    public void focusedChanged(Component component, boolean temporary) {
        super.focusedChanged(component, temporary);

        repaintComponent(!component.isFocused());
    }

    // TreeView.Skin methods

    public Path getNodeAt(int y) {
        Path path = null;

        NodeInfo nodeInfo = getNodeInfoAt(y);

        if (nodeInfo != null) {
            path = nodeInfo.getPath();
        }

        return path;
    }

    public Bounds getNodeBounds(Path path) {
        NodeInfo nodeInfo = getNodeInfoAt(path);
        return getNodeBounds(nodeInfo);
    }

    public int getNodeIndent(int depth) {
        TreeView treeView = (TreeView)getComponent();

        int nodeIndent = (depth - 1) * (indent + spacing);

        if (showBranchControls) {
            nodeIndent += indent + spacing;
        }

        if (treeView.getCheckmarksEnabled()) {
            nodeIndent += Math.max(CHECKBOX.getWidth(), indent) + spacing;
        }

        return nodeIndent;
    }

    // TreeViewListener methods

    @SuppressWarnings("unchecked")
    public void treeDataChanged(TreeView treeView, List<?> previousTreeData) {
        List<Object> treeData = (List<Object>)treeView.getTreeData();

        visibleNodes.clear();

        if (treeData == null) {
            rootBranchInfo = null;
        } else {
            rootBranchInfo = new BranchInfo(null, treeData);
            addVisibleNodes(rootBranchInfo);
        }

        invalidateComponent();
    }

    public void nodeRendererChanged(TreeView treeView, TreeView.NodeRenderer previousNodeRenderer) {
        invalidateComponent();
    }

    public void nodeEditorChanged(TreeView treeView, TreeView.NodeEditor previousNodeEditor) {
        // No-op
    }

    public void selectModeChanged(TreeView treeView,
        TreeView.SelectMode previousSelectMode) {
        // The selection has implicitly been cleared
        clearFields(NodeInfo.SELECTED_MASK);

        repaintComponent();
    }

    public void checkmarksEnabledChanged(TreeView treeView) {
        // The check state of all nodes has implicitly been cleared
        clearFields(NodeInfo.CHECK_STATE_MASK);

        invalidateComponent();
    }

    public void showMixedCheckmarkStateChanged(TreeView treeView) {
        if (treeView.getCheckmarksEnabled()) {
            // The check state of all *branch* nodes may have changed, so we
            // need to update the cached check state of all BranchNode
            // instances in our hierarchy
            Sequence<NodeInfo> nodes = new ArrayList<NodeInfo>();
            nodes.add(rootBranchInfo);

            while (nodes.getLength() > 0) {
                NodeInfo nodeInfo = nodes.get(0);
                nodes.remove(0, 1);

                // Only branch nodes can be affected by this event
                if (nodeInfo instanceof BranchInfo) {
                    BranchInfo branchInfo = (BranchInfo)nodeInfo;

                    // Update the cached entry for this branch
                    Path path = branchInfo.getPath();
                    branchInfo.setCheckState(treeView.getNodeCheckState(path));

                    // Add the branch's children to the queue
                    if (branchInfo.children != null) {
                        for (int i = 0, n = branchInfo.children.getLength(); i < n; i++) {
                            nodes.insert(branchInfo.children.get(i), i);
                        }
                    }
                }
            }

            repaintComponent();
        }
    }

    // TreeViewBranchListener methods

    public void branchExpanded(TreeView treeView, Path path) {
        BranchInfo branchInfo = (BranchInfo)getNodeInfoAt(path);

        branchInfo.setExpanded(true);
        addVisibleNodes(branchInfo);

        repaintNode(branchInfo);
    }

    public void branchCollapsed(TreeView treeView, Path path) {
        BranchInfo branchInfo = (BranchInfo)getNodeInfoAt(path);

        branchInfo.setExpanded(false);
        removeVisibleNodes(branchInfo, 0, -1);

        repaintNode(branchInfo);
    }

    // TreeViewNodeListener methods

    @SuppressWarnings("unchecked")
    public void nodeInserted(TreeView treeView, Path path, int index) {
        BranchInfo branchInfo = (BranchInfo)getNodeInfoAt(path);
        List<Object> branchData = (List<Object>)branchInfo.data;

        // Update our internal branch info
        if (branchInfo.children != null) {
            NodeInfo nodeInfo = NodeInfo.createNew(branchInfo, branchData.get(index));
            branchInfo.children.insert(nodeInfo, index);
        }

        // Add the node to the visible nodes list
        addVisibleNode(branchInfo, index);
    }

    public void nodesRemoved(TreeView treeView, Path path, int index,
        int count) {
        BranchInfo branchInfo = (BranchInfo)getNodeInfoAt(path);

        // Remove the node from the visible nodes list
        removeVisibleNodes(branchInfo, index, count);

        // Update our internal branch info
        if (branchInfo.children != null) {
            branchInfo.children.remove(index, count);
        }
    }

    @SuppressWarnings("unchecked")
    public void nodeUpdated(TreeView treeView, Path path, int index) {
        BranchInfo branchInfo = (BranchInfo)getNodeInfoAt(path);
        List<Object> branchData = (List<Object>)branchInfo.data;

        branchInfo.loadChildren();
        NodeInfo nodeInfo = branchInfo.children.get(index);

        Object previousNodeData = nodeInfo.data;
        Object nodeData = branchData.get(index);

        if (previousNodeData != nodeData) {
            // Remove the old node from the visible nodes list
            removeVisibleNodes(branchInfo, index, 1);

            // Update our internal branch info
            nodeInfo = NodeInfo.createNew(branchInfo, nodeData);
            branchInfo.children.update(index, nodeInfo);

            // Add the new node to the visible nodes list
            addVisibleNode(branchInfo, index);
        } else if (visibleNodes.indexOf(nodeInfo) >= 0) {
            // The updated node data might affect our preferred width
            invalidateComponent();
        }
    }

    public void nodesCleared(TreeView treeView, Path path) {
        BranchInfo branchInfo = (BranchInfo)getNodeInfoAt(path);

        // Remove the node from the visible nodes list
        removeVisibleNodes(branchInfo, 0, -1);

        // Update our internal branch info
        if (branchInfo.children != null) {
            branchInfo.children.clear();
        }
    }

    public void nodesSorted(TreeView treeView, Path path) {
        BranchInfo branchInfo = (BranchInfo)getNodeInfoAt(path);

        // Remove the child nodes from the visible nodes list
        removeVisibleNodes(branchInfo, 0, -1);

        // Re-load the branch's children to get the correct sort order
        branchInfo.children = null;
        branchInfo.loadChildren();

        // Add the child nodes back to the visible nodes list
        addVisibleNodes(branchInfo);
    }

    // TreeViewNodeStateListener methods

    public void nodeDisabledChanged(TreeView treeView, Path path) {
        NodeInfo nodeInfo = getNodeInfoAt(path);

        nodeInfo.setDisabled(treeView.isNodeDisabled(path));

        repaintNode(nodeInfo);
    }

    public void nodeCheckStateChanged(TreeView treeView, Path path,
        TreeView.NodeCheckState previousCheckState) {
        NodeInfo nodeInfo = getNodeInfoAt(path);

        nodeInfo.setCheckState(treeView.getNodeCheckState(path));

        repaintNode(nodeInfo);
    }

    // TreeViewSelectionListener methods

    public void selectedPathAdded(TreeView treeView, Path path) {
        NodeInfo nodeInfo = getNodeInfoAt(path);

        nodeInfo.setSelected(true);
        repaintNode(nodeInfo);
    }

    public void selectedPathRemoved(TreeView treeView, Path path) {
        NodeInfo nodeInfo = getNodeInfoAt(path);

        nodeInfo.setSelected(false);
        repaintNode(nodeInfo);
    }

    public void selectedPathsChanged(TreeView treeView,
        Sequence<Path> previousSelectedPaths) {

        // Un-select the previous selected paths
        for (int i = 0, n = previousSelectedPaths.getLength(); i < n; i++) {
            NodeInfo previousSelectedNode = getNodeInfoAt(previousSelectedPaths.get(i));

            previousSelectedNode.setSelected(false);
            repaintNode(previousSelectedNode);
        }

        Sequence<Path> selectedPaths = treeView.getSelectedPaths();

        // Select the current selected paths
        for (int i = 0, n = selectedPaths.getLength(); i < n; i++) {
            NodeInfo selectedNode = getNodeInfoAt(selectedPaths.get(i));

            selectedNode.setSelected(true);
            repaintNode(selectedNode);
        }
    }
}
