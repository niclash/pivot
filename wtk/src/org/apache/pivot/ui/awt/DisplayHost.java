package org.apache.pivot.ui.awt;

import java.awt.AWTEvent;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.DragGestureRecognizer;
import java.awt.dnd.DragSourceContext;
import java.awt.dnd.DragSourceDragEvent;
import java.awt.dnd.DragSourceDropEvent;
import java.awt.dnd.DragSourceEvent;
import java.awt.dnd.DragSourceListener;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;
import java.awt.image.VolatileImage;
import java.util.Random;
import org.apache.pivot.wtk.Application;
import org.apache.pivot.wtk.ApplicationContext;
import org.apache.pivot.wtk.Component;
import org.apache.pivot.wtk.ComponentClassListener;
import org.apache.pivot.wtk.Container;
import org.apache.pivot.wtk.Display;
import org.apache.pivot.wtk.DragSource;
import org.apache.pivot.wtk.DropAction;
import org.apache.pivot.wtk.DropTarget;
import org.apache.pivot.wtk.Keyboard;
import org.apache.pivot.wtk.Manifest;
import org.apache.pivot.wtk.Menu;
import org.apache.pivot.wtk.MenuHandler;
import org.apache.pivot.wtk.MenuPopup;
import org.apache.pivot.wtk.Mouse;
import org.apache.pivot.wtk.Platform;
import org.apache.pivot.wtk.Point;
import org.apache.pivot.wtk.Visual;
import org.apache.pivot.wtk.Window;
import org.apache.pivot.wtk.WindowStateListener;
import org.apache.pivot.wtk.effects.Decorator;
import org.apache.pivot.wtk.effects.ShadeDecorator;
import org.apache.pivot.wtk.graphics.ColorFactory;

/**
 * Native display host.
 */
public class DisplayHost extends java.awt.Component
{
    private static final long serialVersionUID = -815713849595314026L;

    private transient Display display = new Display( this );
    private AWTEvent currentAWTEvent = null;

    private Component focusedComponent = null;

    private Point dragLocation = null;
    private Component dragDescendant = null;
    private transient Manifest dragManifest = null;
    private DropAction userDropAction = null;
    private Component dropDescendant = null;

    private MenuPopup menuPopup = null;

    private double scale = 1;

    private boolean paintPending = false;
    private boolean disableVolatileBuffer = false;
    private boolean debugPaint = false;
    private VolatileImage volatileImage = null;
    private GraphicsConfiguration volatileImageGC = null;

    private Random random = null;

    private transient DropTargetListener dropTargetListener = new DropTargetListener()
    {
        @Override
        public void dragEnter( DropTargetDragEvent event )
        {
            if( dragDescendant != null )
            {
                throw new IllegalStateException( "Local drag already in progress." );
            }

            java.awt.Point location = event.getLocation();
            dragLocation = new Point( location.x, location.y );

            // Initialize drag state
            dragManifest = new JavaAwtRemoteManifest( event.getTransferable() );

            // Initialize drop state
            userDropAction = getDropAction( event.getDropAction() );

            // Notify drop target
            dropDescendant = getDropDescendant( location.x, location.y );

            DropAction dropAction = null;

            if( dropDescendant != null )
            {
                DropTarget dropTarget = dropDescendant.getDropTarget();
                dropAction = dropTarget.dragEnter( dropDescendant, dragManifest,
                                                   getSupportedDropActions( event.getSourceActions() ), userDropAction );
            }

            if( dropAction == null )
            {
                event.rejectDrag();
            }
            else
            {
                event.acceptDrag( getNativeDropAction( dropAction ) );
            }

            display.validate();
        }

        @Override
        public void dragExit( DropTargetEvent event )
        {
            // Clear drag location and state
            dragLocation = null;
            dragManifest = null;

            // Clear drop state
            userDropAction = null;

            if( dropDescendant != null )
            {
                DropTarget dropTarget = dropDescendant.getDropTarget();
                dropTarget.dragExit( dropDescendant );
            }

            dropDescendant = null;

            display.validate();
        }

        @Override
        public void dragOver( DropTargetDragEvent event )
        {
            java.awt.Point location = event.getLocation();

            // Get the previous and current drop descendant and call
            // move or exit/enter as appropriate
            Component previousDropDescendant = dropDescendant;
            dropDescendant = getDropDescendant( location.x, location.y );

            DropAction dropAction = null;

            if( previousDropDescendant == dropDescendant )
            {
                if( dropDescendant != null )
                {
                    DropTarget dropTarget = dropDescendant.getDropTarget();

                    Point dropLocation = dropDescendant.mapPointFromAncestor( display,
                                                                              location.x, location.y );
                    dropAction = dropTarget.dragMove( dropDescendant, dragManifest,
                                                      getSupportedDropActions( event.getSourceActions() ),
                                                      dropLocation.x, dropLocation.y, userDropAction );
                }
            }
            else
            {
                if( previousDropDescendant != null )
                {
                    DropTarget previousDropTarget = previousDropDescendant.getDropTarget();
                    previousDropTarget.dragExit( previousDropDescendant );
                }

                if( dropDescendant != null )
                {
                    DropTarget dropTarget = dropDescendant.getDropTarget();
                    dropAction = dropTarget.dragEnter( dropDescendant, dragManifest,
                                                       getSupportedDropActions( event.getSourceActions() ),
                                                       userDropAction );
                }
            }

            // Update cursor
            setCursor( getDropCursor( dropAction ) );

            if( dropAction == null )
            {
                event.rejectDrag();
            }
            else
            {
                event.acceptDrag( getNativeDropAction( dropAction ) );
            }

            display.validate();
        }

        @Override
        public void dropActionChanged( DropTargetDragEvent event )
        {
            userDropAction = getDropAction( event.getDropAction() );

            DropAction dropAction = null;

            if( dropDescendant != null )
            {
                java.awt.Point location = event.getLocation();
                Point dropLocation = dropDescendant.mapPointFromAncestor( display,
                                                                          location.x, location.y );

                DropTarget dropTarget = dropDescendant.getDropTarget();
                dropAction = dropTarget.userDropActionChange( dropDescendant, dragManifest,
                                                              getSupportedDropActions( event.getSourceActions() ), dropLocation.x, dropLocation.y,
                                                              userDropAction );
            }

            if( dropAction == null )
            {
                event.rejectDrag();
            }
            else
            {
                event.acceptDrag( getNativeDropAction( dropAction ) );
            }

            display.validate();
        }

        @Override
        public void drop( DropTargetDropEvent event )
        {
            java.awt.Point location = event.getLocation();
            dropDescendant = getDropDescendant( location.x, location.y );

            DropAction dropAction = null;

            if( dropDescendant != null )
            {
                Point dropLocation = dropDescendant.mapPointFromAncestor( display,
                                                                          location.x, location.y );
                DropTarget dropTarget = dropDescendant.getDropTarget();

                // Simulate a user drop action change to get the current drop action
                int supportedDropActions = getSupportedDropActions( event.getSourceActions() );

                dropAction = dropTarget.userDropActionChange( dropDescendant, dragManifest,
                                                              supportedDropActions, dropLocation.x, dropLocation.y, userDropAction );

                if( dropAction != null )
                {
                    // Perform the drop
                    event.acceptDrop( getNativeDropAction( dropAction ) );
                    dropTarget.drop( dropDescendant, dragManifest,
                                     supportedDropActions, dropLocation.x, dropLocation.y, userDropAction );
                }
            }

            if( dropAction == null )
            {
                event.rejectDrop();
            }

            event.dropComplete( true );

            // Restore the cursor to the default
            setCursor( java.awt.Cursor.getDefaultCursor() );

            // Clear drag state
            dragManifest = null;
            dragLocation = null;

            // Clear drop state
            dropDescendant = null;

            display.validate();
        }
    };

    public DisplayHost()
    {
        enableEvents( AWTEvent.COMPONENT_EVENT_MASK
                      | AWTEvent.FOCUS_EVENT_MASK
                      | AWTEvent.MOUSE_EVENT_MASK
                      | AWTEvent.MOUSE_MOTION_EVENT_MASK
                      | AWTEvent.MOUSE_WHEEL_EVENT_MASK
                      | AWTEvent.KEY_EVENT_MASK );

        try
        {
            System.setProperty( "sun.awt.noerasebackground", "true" );
            System.setProperty( "sun.awt.erasebackgroundonresize", "false" );
        }
        catch( SecurityException exception )
        {
            // No-op
        }

        try
        {
            disableVolatileBuffer = Boolean.parseBoolean( System.getProperty( "org.apache.pivot.wtk.disablevolatilebuffer" ) );
        }
        catch( SecurityException ex )
        {
            // No-op
        }

        try
        {
            debugPaint = Boolean.parseBoolean( System.getProperty( "org.apache.pivot.wtk.debugpaint" ) );
            if( debugPaint == true )
            {
                random = new Random();
            }
        }
        catch( SecurityException ex )
        {
            // No-op
        }

        try
        {
            boolean debugFocus = Boolean.parseBoolean( System.getProperty( "org.apache.pivot.wtk.debugfocus" ) );

            if( debugFocus )
            {
                final Decorator focusDecorator = new ShadeDecorator( 0.2f, ColorFactory.RED );

                ComponentClassListener focusChangeListener = new ComponentClassListener()
                {
                    @Override
                    public void focusedComponentChanged( Component previousFocusedComponent )
                    {
                        if( previousFocusedComponent != null
                            && previousFocusedComponent.getDecorators().indexOf( focusDecorator ) > -1 )
                        {
                            previousFocusedComponent.getDecorators().remove( focusDecorator );
                        }

                        Component focusedComponent = Component.getFocusedComponent();

                        if( focusedComponent != null
                            && focusedComponent.getDecorators().indexOf( focusDecorator ) == -1 )
                        {
                            focusedComponent.getDecorators().add( focusDecorator );
                        }

                        System.out.println( "focusedComponentChanged():\n  from = " + previousFocusedComponent
                                            + "\n  to = " + focusedComponent );
                    }
                };

                Component.getComponentClassListeners().add( focusChangeListener );
            }
        }
        catch( SecurityException ex )
        {
            // No-op
        }

        // Add native drop support
        new java.awt.dnd.DropTarget( this, dropTargetListener );

        setFocusTraversalKeysEnabled( false );
    }

    private static DropAction getUserDropAction( InputEvent event )
    {
        DropAction userDropAction;

        if( ( event.isControlDown() && event.isShiftDown() )
            || ( event.isAltDown() && event.isMetaDown() ) )
        {
            userDropAction = DropAction.LINK;
        }
        else if( event.isControlDown()
                 || ( event.isAltDown() ) )
        {
            userDropAction = DropAction.COPY;
        }
        else if( event.isShiftDown() )
        {
            userDropAction = DropAction.MOVE;
        }
        else
        {
            userDropAction = null;
        }

        return userDropAction;
    }

    private static DropAction getDropAction( int nativeDropAction )
    {
        DropAction dropAction = null;

        switch( nativeDropAction )
        {
        case DnDConstants.ACTION_COPY:
        {
            dropAction = DropAction.COPY;
            break;
        }

        case DnDConstants.ACTION_MOVE:
        {
            dropAction = DropAction.MOVE;
            break;
        }

        case DnDConstants.ACTION_LINK:
        {
            dropAction = DropAction.LINK;
            break;
        }
        }

        return dropAction;
    }

    private static int getSupportedDropActions( int sourceActions )
    {
        int dropActions = 0;

        if( ( sourceActions & DnDConstants.ACTION_COPY ) > 0 )
        {
            dropActions |= DropAction.COPY.getMask();
        }

        if( ( sourceActions & DnDConstants.ACTION_MOVE ) > 0 )
        {
            dropActions |= DropAction.MOVE.getMask();
        }

        if( ( sourceActions & DnDConstants.ACTION_LINK ) > 0 )
        {
            dropActions |= DropAction.LINK.getMask();
        }

        return dropActions;
    }

    private static int getNativeDropAction( DropAction dropAction )
    {
        int nativeDropAction = 0;

        if( dropAction != null )
        {
            switch( dropAction )
            {
            case COPY:
            {
                nativeDropAction = DnDConstants.ACTION_COPY;
                break;
            }

            case MOVE:
            {
                nativeDropAction = DnDConstants.ACTION_MOVE;
                break;
            }

            case LINK:
            {
                nativeDropAction = DnDConstants.ACTION_LINK;
                break;
            }
            }
        }

        return nativeDropAction;
    }

    private static java.awt.Cursor getDropCursor( DropAction dropAction )
    {
        // Update the drop cursor
        java.awt.Cursor cursor = java.awt.Cursor.getDefaultCursor();

        if( dropAction != null )
        {
            // Show the cursor for the drop action returned by the
            // drop target
            switch( dropAction )
            {
            case COPY:
            {
                cursor = java.awt.dnd.DragSource.DefaultCopyDrop;
                break;
            }

            case MOVE:
            {
                cursor = java.awt.dnd.DragSource.DefaultMoveDrop;
                break;
            }

            case LINK:
            {
                cursor = java.awt.dnd.DragSource.DefaultLinkDrop;
                break;
            }
            }
        }

        return cursor;
    }

    public Display getDisplay()
    {
        return display;
    }

    public AWTEvent getCurrentAWTEvent()
    {
        return currentAWTEvent;
    }

    public double getScale()
    {
        return scale;
    }

    public void setScale( double scale )
    {
        if( scale != this.scale )
        {
            this.scale = scale;
            display.setSize( Math.max( (int) Math.ceil( getWidth() / scale ), 0 ),
                             Math.max( (int) Math.ceil( getHeight() / scale ), 0 ) );
            display.repaint();
        }
    }

    public void scaleUp()
    {
        double newScale;

        if( scale < 1 )
        {
            newScale = 1;
        }
        else if( scale < 1.25 )
        {
            newScale = 1.25;
        }
        else if( scale < 1.5 )
        {
            newScale = 1.5;
        }
        else if( scale < 2 )
        {
            newScale = 2;
        }
        else
        {
            newScale = Math.min( Math.floor( scale ) + 1, 12 );
        }

        setScale( newScale );
    }

    public void scaleDown()
    {
        double newScale;

        if( scale <= 1.25 )
        {
            newScale = 1;
        }
        else if( scale <= 1.5 )
        {
            newScale = 1.25;
        }
        else if( scale <= 2 )
        {
            newScale = 1.5;
        }
        else
        {
            newScale = Math.ceil( scale ) - 1;
        }

        setScale( newScale );
    }

    @Override
    public void repaint( int x, int y, int width, int height )
    {
        // Ensure that the repaint call is properly bounded (some
        // implementations of AWT do not properly clip the repaint call
        // when x or y is negative: the negative value is converted to 0,
        // but the width/height is not adjusted)
        if( x < 0 )
        {
            width = Math.max( width + x, 0 );
            x = 0;
        }

        if( y < 0 )
        {
            height = Math.max( height + y, 0 );
            y = 0;
        }

        if( width > 0
            && height > 0 )
        {
            if( scale == 1 )
            {
                super.repaint( x, y, width, height );
            }
            else
            {
                super.repaint( (int) Math.floor( x * scale ), (int) Math.floor( y * scale ),
                               (int) Math.ceil( width * scale ) + 1, (int) Math.ceil( height * scale ) + 1 );
            }

            paintPending = true;
        }
    }

    @Override
    public void paint( Graphics graphics )
    {
        // Intersect the clip region with the bounds of this component
        // (for some reason, AWT does not do this automatically)
        graphics.clipRect( 0, 0, getWidth(), getHeight() );

        java.awt.Rectangle clipBounds = graphics.getClipBounds();
        if( clipBounds != null && !clipBounds.isEmpty() )
        {
            try
            {
                if( disableVolatileBuffer || !paintVolatileBuffered( (Graphics2D) graphics ) )
                {
                    if( !paintBuffered( (Graphics2D) graphics ) )
                    {
                        paintDisplay( (Graphics2D) graphics );
                    }
                }

                if( debugPaint )
                {
                    graphics.setColor( new java.awt.Color( random.nextInt( 256 ),
                                                           random.nextInt( 256 ), random.nextInt( 256 ), 75 ) );
                    graphics.fillRect( 0, 0, getWidth(), getHeight() );
                }
            }
            catch( RuntimeException exception )
            {
                System.err.println( "Exception thrown during paint(): " + exception );
                throw exception;
            }
        }

        paintPending = false;
    }

    @Override
    public void update( Graphics graphics )
    {
        paint( graphics );
    }

    /**
     * Attempts to paint the display using an offscreen buffer.
     *
     * @param graphics The source graphics context.
     *
     * @return <tt>true</tt> if the display was painted using the offscreen
     *         buffer; <tt>false</tt>, otherwise.
     */
    private boolean paintBuffered( Graphics2D graphics )
    {
        boolean painted = false;

        // Paint the display into an offscreen buffer
        GraphicsConfiguration gc = graphics.getDeviceConfiguration();
        Rectangle clipBounds = graphics.getClipBounds();
        BufferedImage bufferedImage =
            gc.createCompatibleImage( clipBounds.width, clipBounds.height,
                                      Transparency.OPAQUE );

        if( bufferedImage != null )
        {
            Graphics2D bufferedImageGraphics = (Graphics2D) bufferedImage.getGraphics();
            bufferedImageGraphics.setClip( 0, 0, clipBounds.width, clipBounds.height );
            bufferedImageGraphics.translate( -clipBounds.x, -clipBounds.y );

            try
            {
                paintDisplay( bufferedImageGraphics );
                graphics.drawImage( bufferedImage, clipBounds.x, clipBounds.y, this );
            }
            finally
            {
                bufferedImageGraphics.dispose();
            }

            painted = true;
        }

        return painted;
    }

    /**
     * Attempts to paint the display using a volatile offscreen buffer.
     *
     * @param graphics The source graphics context.
     *
     * @return <tt>true</tt> if the display was painted using the offscreen
     *         buffer; <tt>false</tt>, otherwise.
     */
    private boolean paintVolatileBuffered( Graphics2D graphics )
    {
        boolean painted = false;

        // Paint the display into a volatile offscreen buffer
        GraphicsConfiguration gc = graphics.getDeviceConfiguration();
        java.awt.Rectangle gcBounds = gc.getBounds();
        if( volatileImage == null || volatileImageGC != gc )
        {
            if( volatileImage != null )
            {
                volatileImage.flush();
            }
            volatileImage = gc.createCompatibleVolatileImage( gcBounds.width, gcBounds.height,
                                                              Transparency.OPAQUE );
            // we need to create a new volatile if the GC changes
            volatileImageGC = gc;
        }

        // If we have a valid volatile image, attempt to paint the
        // display to it
        int valid = volatileImage.validate( gc );

        if( valid == java.awt.image.VolatileImage.IMAGE_OK
            || valid == java.awt.image.VolatileImage.IMAGE_RESTORED )
        {
            java.awt.Rectangle clipBounds = graphics.getClipBounds();
            Graphics2D volatileImageGraphics = volatileImage.createGraphics();
            volatileImageGraphics.setClip( clipBounds.x, clipBounds.y, clipBounds.width, clipBounds.height );

            try
            {
                paintDisplay( volatileImageGraphics );
                // this drawImage method doesn't use width and height
                int x2 = clipBounds.x + clipBounds.width;
                int y2 = clipBounds.y + clipBounds.height;
                graphics.drawImage( volatileImage,
                                    clipBounds.x, clipBounds.y, x2, y2,
                                    clipBounds.x, clipBounds.y, x2, y2,
                                    this );
            }
            finally
            {
                volatileImageGraphics.dispose();
            }

            painted = !volatileImage.contentsLost();
        }
        else
        {
            volatileImage.flush();
            volatileImage = null;
        }

        return painted;
    }

    /**
     * Paints the display including any decorators.
     *
     * @param awtGraphics
     */
    private void paintDisplay( Graphics2D awtGraphics )
    {
        if( scale != 1 )
        {
            awtGraphics.scale( scale, scale );
        }

        org.apache.pivot.wtk.graphics.Graphics2D graphics = new JavaAwtGraphics( awtGraphics );
        org.apache.pivot.wtk.graphics.Graphics2D decoratedGraphics = graphics;

        Component.DecoratorSequence decorators = display.getDecorators();
        int n = decorators.getLength();
        for( int i = n - 1; i >= 0; i-- )
        {
            Decorator decorator = decorators.get( i );
            decoratedGraphics = decorator.prepare( display, decoratedGraphics );
        }

        awtGraphics.setRenderingHint( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED );
        display.paint( graphics );

        for( int i = 0; i < n; i++ )
        {
            Decorator decorator = decorators.get( i );
            decorator.update();
        }

        // Paint the drag visual
        if( dragDescendant != null )
        {
            DragSource dragSource = dragDescendant.getDragSource();
            Visual dragRepresentation = dragSource.getRepresentation();

            if( dragRepresentation != null )
            {
                Point dragOffset = dragSource.getOffset();
                int tx = dragLocation.x - dragOffset.x;
                int ty = dragLocation.y - dragOffset.y;

                graphics.translate( tx, ty );
                dragRepresentation.paint( graphics );
            }
        }
    }

    private void repaintDragRepresentation()
    {
        DragSource dragSource = dragDescendant.getDragSource();
        Visual dragRepresentation = dragSource.getRepresentation();

        if( dragRepresentation != null )
        {
            Point dragOffset = dragSource.getOffset();

            repaint( dragLocation.x - dragOffset.x, dragLocation.y - dragOffset.y,
                     dragRepresentation.getWidth(), dragRepresentation.getHeight() );
        }
    }

    private Component getDropDescendant( int x, int y )
    {
        Component dropDescendant = display.getDescendantAt( x, y );

        while( dropDescendant != null
               && dropDescendant.getDropTarget() == null )
        {
            dropDescendant = dropDescendant.getParent();
        }

        if( dropDescendant != null
            && dropDescendant.isBlocked() )
        {
            dropDescendant = null;
        }

        return dropDescendant;
    }

    private void startNativeDrag( final DragSource dragSource, final Component dragDescendant,
                                  final MouseEvent mouseEvent
    )
    {
        java.awt.dnd.DragSource awtDragSource = java.awt.dnd.DragSource.getDefaultDragSource();

        final int supportedDropActions = dragSource.getSupportedDropActions();

        DragGestureRecognizer dragGestureRecognizer =
            new DragGestureRecognizer( java.awt.dnd.DragSource.getDefaultDragSource(), DisplayHost.this )
            {
                private static final long serialVersionUID = -3204487375572082596L;

                {
                    appendEvent( mouseEvent );
                }

                @Override
                public synchronized int getSourceActions()
                {
                    int awtSourceActions = 0;

                    if( DropAction.COPY.isSelected( supportedDropActions ) )
                    {
                        awtSourceActions |= DnDConstants.ACTION_COPY;
                    }

                    if( DropAction.MOVE.isSelected( supportedDropActions ) )
                    {
                        awtSourceActions |= DnDConstants.ACTION_MOVE;
                    }

                    if( DropAction.LINK.isSelected( supportedDropActions ) )
                    {
                        awtSourceActions |= DnDConstants.ACTION_LINK;
                    }

                    return awtSourceActions;
                }

                @Override
                protected void registerListeners()
                {
                    // No-op
                }

                @Override
                protected void unregisterListeners()
                {
                    // No-op
                }
            };

        java.util.List<InputEvent> inputEvents = new java.util.ArrayList<InputEvent>();
        inputEvents.add( mouseEvent );

        // TODO If current user drop action is supported by drag source, use it
        // as initial action - otherwise, select MOVE, COPY, LINK in that order
        java.awt.Point location = new java.awt.Point( mouseEvent.getX(), mouseEvent.getY() );
        DragGestureEvent trigger = new DragGestureEvent( dragGestureRecognizer,
                                                         DnDConstants.ACTION_MOVE, location, inputEvents );

        JavaAwtLocalManifest dragContent = dragSource.getContent();
        JavaAwtLocalManifestAdapter localManifestAdapter = new JavaAwtLocalManifestAdapter( dragContent );

        awtDragSource.startDrag( trigger, java.awt.Cursor.getDefaultCursor(),
                                 null, null, localManifestAdapter, new DragSourceListener()
        {
            @Override
            public void dragEnter( DragSourceDragEvent event )
            {
                DragSourceContext context = event.getDragSourceContext();
                context.setCursor( getDropCursor( getDropAction( event.getDropAction() ) ) );
            }

            @Override
            public void dragExit( DragSourceEvent event )
            {
                DragSourceContext context = event.getDragSourceContext();
                context.setCursor( java.awt.Cursor.getDefaultCursor() );
            }

            @Override
            public void dragOver( DragSourceDragEvent event )
            {
                DragSourceContext context = event.getDragSourceContext();
                context.setCursor( getDropCursor( getDropAction( event.getDropAction() ) ) );
            }

            @Override
            public void dropActionChanged( DragSourceDragEvent event )
            {
                DragSourceContext context = event.getDragSourceContext();
                context.setCursor( getDropCursor( getDropAction( event.getDropAction() ) ) );
            }

            @Override
            public void dragDropEnd( DragSourceDropEvent event )
            {
                DragSourceContext context = event.getDragSourceContext();
                context.setCursor( java.awt.Cursor.getDefaultCursor() );
                dragSource.endDrag( dragDescendant, getDropAction( event.getDropAction() ) );
            }
        } );
    }

    @Override
    protected void processEvent( AWTEvent event )
    {
        currentAWTEvent = event;

        super.processEvent( event );

        currentAWTEvent = null;

        display.validate();
    }

    @Override
    protected void processComponentEvent( ComponentEvent event )
    {
        super.processComponentEvent( event );

        switch( event.getID() )
        {
        case ComponentEvent.COMPONENT_RESIZED:
        {
            if( scale == 1 )
            {
                display.setSize( Math.max( getWidth(), 0 ), Math.max( getHeight(), 0 ) );
            }
            else
            {
                display.setSize( Math.max( (int) Math.ceil( getWidth() / scale ), 0 ),
                                 Math.max( (int) Math.ceil( getHeight() / scale ), 0 ) );
            }

            break;
        }
        }
    }

    @Override
    protected void processFocusEvent( FocusEvent event )
    {
        super.processFocusEvent( event );

        switch( event.getID() )
        {
        case FocusEvent.FOCUS_GAINED:
        {
            if( focusedComponent != null
                && focusedComponent.isShowing()
                && !focusedComponent.isBlocked() )
            {
                focusedComponent.requestFocus();
            }

            break;
        }

        case FocusEvent.FOCUS_LOST:
        {
            focusedComponent = Component.getFocusedComponent();
            Component.clearFocus();

            break;
        }
        }
    }

    @Override
    protected void processMouseEvent( MouseEvent event )
    {
        super.processMouseEvent( event );

        int x = (int) Math.round( event.getX() / scale );
        int y = (int) Math.round( event.getY() / scale );

        // Set the mouse button state
        int mouseButtons = 0x00;

        int modifiersEx = event.getModifiersEx();
        if( ( modifiersEx & MouseEvent.BUTTON1_DOWN_MASK ) > 0 )
        {
            mouseButtons |= Mouse.Button.LEFT.getMask();
        }

        if( ( modifiersEx & MouseEvent.BUTTON2_DOWN_MASK ) > 0 )
        {
            mouseButtons |= Mouse.Button.MIDDLE.getMask();
        }

        if( ( modifiersEx & MouseEvent.BUTTON3_DOWN_MASK ) > 0 )
        {
            mouseButtons |= Mouse.Button.RIGHT.getMask();
        }

        Mouse.setButtons( mouseButtons );

        // Get the button associated with this event
        Mouse.Button button = null;
        switch( event.getButton() )
        {
        case MouseEvent.BUTTON1:
        {
            button = Mouse.Button.LEFT;
            break;
        }

        case MouseEvent.BUTTON2:
        {
            button = Mouse.Button.MIDDLE;
            break;
        }

        case MouseEvent.BUTTON3:
        {
            button = Mouse.Button.RIGHT;
            break;
        }
        }

        // Process the event
        int eventID = event.getID();
        if( eventID == MouseEvent.MOUSE_ENTERED
            || eventID == MouseEvent.MOUSE_EXITED )
        {
            try
            {
                switch( eventID )
                {
                case MouseEvent.MOUSE_ENTERED:
                {
                    display.mouseOver();
                    break;
                }

                case MouseEvent.MOUSE_EXITED:
                {
                    display.mouseOut();
                    break;
                }
                }
            }
            catch( Exception exception )
            {
                ApplicationContext.handleUncaughtException( exception );
            }
        }
        else
        {
            // Determine the mouse owner
            Component mouseOwner;
            Component mouseCapturer = Mouse.getCapturer();
            if( mouseCapturer == null )
            {
                mouseOwner = display;
            }
            else
            {
                mouseOwner = mouseCapturer;
                Point location = mouseOwner.mapPointFromAncestor( display, x, y );
                x = location.x;
                y = location.y;
            }

            // Delegate the event to the owner
            try
            {
                switch( eventID )
                {
                case MouseEvent.MOUSE_PRESSED:
                {
                    requestFocusInWindow();

                    boolean consumed = mouseOwner.mouseDown( button, x, y );

                    if( button == Mouse.Button.LEFT )
                    {
                        dragLocation = new Point( x, y );
                    }
                    else if( menuPopup == null
                             && button == Mouse.Button.RIGHT
                             && !consumed )
                    {
                        // Instantiate a context menu
                        Menu menu = new Menu();

                        // Allow menu handlers to configure the menu
                        Component component = mouseOwner;
                        int componentX = x;
                        int componentY = y;

                        do
                        {
                            MenuHandler menuHandler = component.getMenuHandler();
                            if( menuHandler != null )
                            {
                                if( menuHandler.configureContextMenu( component,
                                                                      menu, componentX, componentY ) )
                                {
                                    // Stop propagation
                                    break;
                                }
                            }

                            if( component instanceof Container )
                            {
                                Container container = (Container) component;
                                component = container.getComponentAt( componentX,
                                                                      componentY );

                                if( component != null )
                                {
                                    componentX -= component.getX();
                                    componentY -= component.getY();
                                }
                            }
                            else
                            {
                                component = null;
                            }
                        }
                        while( component != null && component.isEnabled() );

                        // Show the context menu if it contains any sections
                        if( menu.getSections().getLength() > 0 )
                        {
                            menuPopup = new MenuPopup( menu );

                            menuPopup.getWindowStateListeners().add( new WindowStateListener.Adapter()
                            {
                                @Override
                                public void windowClosed( Window window, Display display, Window owner )
                                {
                                    menuPopup.getMenu().getSections().clear();
                                    menuPopup = null;
                                    window.getWindowStateListeners().remove( this );
                                }
                            } );

                            Window window = null;
                            if( mouseOwner == display )
                            {
                                window = (Window) display.getComponentAt( x, y );
                            }
                            else
                            {
                                window = mouseOwner.getWindow();
                            }

                            Display display = window.getDisplay();
                            Point location = mouseOwner.mapPointToAncestor( display, x, y );
                            menuPopup.open( window, location );
                        }
                    }

                    if( consumed )
                    {
                        event.consume();
                    }

                    break;
                }

                case MouseEvent.MOUSE_RELEASED:
                {
                    if( dragDescendant == null )
                    {
                        boolean consumed = mouseOwner.mouseUp( button, x, y );

                        if( consumed )
                        {
                            event.consume();
                        }
                    }
                    else
                    {
                        DragSource dragSource = dragDescendant.getDragSource();

                        repaintDragRepresentation();

                        if( dropDescendant == null )
                        {
                            dragSource.endDrag( dragDescendant, null );
                        }
                        else
                        {
                            DropTarget dropTarget = dropDescendant.getDropTarget();
                            DropAction dropAction = dropTarget.drop( dropDescendant,
                                                                     dragManifest, dragSource.getSupportedDropActions(), x, y,
                                                                     getUserDropAction( event ) );
                            dragSource.endDrag( dragDescendant, dropAction );
                        }

                        setCursor( java.awt.Cursor.getDefaultCursor() );

                        // Clear the drag state
                        dragDescendant = null;
                        dragManifest = null;

                        // Clear the drop state
                        userDropAction = null;
                        dropDescendant = null;
                    }

                    // Clear the drag location
                    dragLocation = null;

                    break;
                }
                }
            }
            catch( Exception exception )
            {
                ApplicationContext.handleUncaughtException( exception );
            }
        }
    }

    @Override
    protected void processMouseMotionEvent( MouseEvent event )
    {
        super.processMouseMotionEvent( event );

        if( !paintPending )
        {
            int x = (int) Math.round( event.getX() / scale );
            int y = (int) Math.round( event.getY() / scale );

            // Process the event
            try
            {
                switch( event.getID() )
                {
                case MouseEvent.MOUSE_MOVED:
                case MouseEvent.MOUSE_DRAGGED:
                {
                    if( dragDescendant == null )
                    {
                        // A drag is not active
                        Component mouseCapturer = Mouse.getCapturer();

                        if( mouseCapturer == null )
                        {
                            // The mouse is not captured, so propagate the event to the display
                            if( !display.isMouseOver() )
                            {
                                display.mouseOver();
                            }

                            display.mouseMove( x, y );

                            int dragThreshold = Platform.getInstalled().getDragThreshold();

                            if( dragLocation != null
                                && ( Math.abs( x - dragLocation.x ) > dragThreshold
                                     || Math.abs( y - dragLocation.y ) > dragThreshold ) )
                            {
                                // The user has dragged the mouse past the drag threshold; try
                                // to find a drag source
                                dragDescendant = display.getDescendantAt( dragLocation.x,
                                                                          dragLocation.y );

                                while( dragDescendant != null
                                       && dragDescendant.getDragSource() == null )
                                {
                                    dragDescendant = dragDescendant.getParent();
                                }

                                if( dragDescendant == null
                                    || dragDescendant.isBlocked() )
                                {
                                    // There was nothing to drag, so clear the drag location
                                    dragDescendant = null;
                                    dragLocation = null;
                                }
                                else
                                {
                                    DragSource dragSource = dragDescendant.getDragSource();
                                    dragLocation = dragDescendant.mapPointFromAncestor( display, x, y );

                                    if( dragSource.beginDrag( dragDescendant,
                                                              dragLocation.x, dragLocation.y ) )
                                    {
                                        // A drag has started
                                        if( dragSource.isNative() )
                                        {
                                            startNativeDrag( dragSource, dragDescendant,
                                                             event );

                                            // Clear the drag state since it is not used for
                                            // native drags
                                            dragDescendant = null;
                                            dragLocation = null;
                                        }
                                        else
                                        {
                                            if( dragSource.getRepresentation() != null
                                                && dragSource.getOffset() == null )
                                            {
                                                throw new IllegalStateException( "Drag offset is required when a "
                                                                                 + " respresentation is specified." );
                                            }

                                            if( display.isMouseOver() )
                                            {
                                                display.mouseOut();
                                            }

                                            // Get the drag content
                                            dragManifest = dragSource.getContent();

                                            // Get the initial user drop action
                                            userDropAction = getUserDropAction( event );

                                            // Repaint the drag visual
                                            dragLocation = new Point( x, y );
                                            repaintDragRepresentation();
                                        }
                                    }
                                    else
                                    {
                                        // Clear the drag state
                                        dragDescendant = null;
                                        dragLocation = null;
                                    }
                                }
                            }
                        }
                        else
                        {
                            // Delegate the event to the capturer
                            Point location = mouseCapturer.mapPointFromAncestor( display, x, y );
                            boolean consumed = mouseCapturer.mouseMove( location.x, location.y );

                            if( consumed )
                            {
                                event.consume();
                            }
                        }
                    }
                    else
                    {
                        if( dragLocation != null )
                        {
                            DragSource dragSource = dragDescendant.getDragSource();

                            // Get the previous and current drop descendant and call
                            // move or exit/enter as appropriate
                            Component previousDropDescendant = dropDescendant;
                            dropDescendant = getDropDescendant( x, y );

                            DropAction dropAction = null;

                            if( previousDropDescendant == dropDescendant )
                            {
                                if( dropDescendant != null )
                                {
                                    DropTarget dropTarget = dropDescendant.getDropTarget();

                                    Point dropLocation = dropDescendant.mapPointFromAncestor( display, x, y );
                                    dropAction = dropTarget.dragMove( dropDescendant, dragManifest,
                                                                      dragSource.getSupportedDropActions(),
                                                                      dropLocation.x, dropLocation.y, userDropAction );
                                }
                            }
                            else
                            {
                                if( previousDropDescendant != null )
                                {
                                    DropTarget previousDropTarget = previousDropDescendant.getDropTarget();
                                    previousDropTarget.dragExit( previousDropDescendant );
                                }

                                if( dropDescendant != null )
                                {
                                    DropTarget dropTarget = dropDescendant.getDropTarget();
                                    dropAction = dropTarget.dragEnter( dropDescendant, dragManifest,
                                                                       dragSource.getSupportedDropActions(), userDropAction );
                                }
                            }

                            // Update cursor
                            setCursor( getDropCursor( dropAction ) );

                            // Repaint the drag visual
                            repaintDragRepresentation();

                            dragLocation = new Point( x, y );
                            repaintDragRepresentation();
                        }
                    }

                    break;
                }
                }
            }
            catch( Exception exception )
            {
                ApplicationContext.handleUncaughtException( exception );
            }
        }
    }

    @Override
    protected void processMouseWheelEvent( MouseWheelEvent event )
    {
        super.processMouseWheelEvent( event );

        // Get the event coordinates
        int x = (int) Math.round( event.getX() / scale );
        int y = (int) Math.round( event.getY() / scale );

        // Get the scroll type
        Mouse.ScrollType scrollType = null;
        switch( event.getScrollType() )
        {
        case MouseWheelEvent.WHEEL_BLOCK_SCROLL:
        {
            scrollType = Mouse.ScrollType.BLOCK;
            break;
        }

        case MouseWheelEvent.WHEEL_UNIT_SCROLL:
        {
            scrollType = Mouse.ScrollType.UNIT;
            break;
        }
        }

        // Process the event
        try
        {
            switch( event.getID() )
            {
            case MouseEvent.MOUSE_WHEEL:
            {
                if( Keyboard.isPressed( Keyboard.Modifier.CTRL )
                    && Keyboard.isPressed( Keyboard.Modifier.SHIFT ) )
                {
                    // Mouse wheel scaling
                    if( event.getWheelRotation() < 0 )
                    {
                        scaleUp();
                    }
                    else
                    {
                        scaleDown();
                    }
                }
                else if( dragDescendant == null )
                {
                    // Determine the mouse owner
                    Component mouseOwner;
                    Component mouseCapturer = Mouse.getCapturer();
                    if( mouseCapturer == null )
                    {
                        mouseOwner = display;
                    }
                    else
                    {
                        mouseOwner = mouseCapturer;
                        Point location = mouseOwner.mapPointFromAncestor( display, x, y );
                        x = location.x;
                        y = location.y;
                    }

                    // Delegate the event to the owner
                    boolean consumed = mouseOwner.mouseWheel( scrollType, event.getScrollAmount(),
                                                              event.getWheelRotation(), x, y );

                    if( consumed )
                    {
                        event.consume();
                    }
                }
                break;
            }
            }
        }
        catch( Exception exception )
        {
            ApplicationContext.handleUncaughtException( exception );
        }
    }

    @Override
    protected void processKeyEvent( KeyEvent event )
    {
        super.processKeyEvent( event );

        int modifiersEx = event.getModifiersEx();
        int awtKeyLocation = event.getKeyLocation();

        // Set the keyboard modifier state
        int keyboardModifiers = 0;
        if( ( modifiersEx & KeyEvent.SHIFT_DOWN_MASK ) > 0 )
        {
            keyboardModifiers |= Keyboard.Modifier.SHIFT.getMask();
        }

        // Ignore Control when Alt-Graphics is pressed
        if( ( modifiersEx & KeyEvent.CTRL_DOWN_MASK ) > 0
            && ( ( modifiersEx & KeyEvent.ALT_DOWN_MASK ) == 0
                 || awtKeyLocation == KeyEvent.KEY_LOCATION_RIGHT ) )
        {
            keyboardModifiers |= Keyboard.Modifier.CTRL.getMask();
        }

        if( ( modifiersEx & KeyEvent.ALT_DOWN_MASK ) > 0 )
        {
            keyboardModifiers |= Keyboard.Modifier.ALT.getMask();
        }

        if( ( modifiersEx & KeyEvent.META_DOWN_MASK ) > 0 )
        {
            keyboardModifiers |= Keyboard.Modifier.META.getMask();
        }

        Keyboard.setModifiers( keyboardModifiers );

        // Get the key location
        Keyboard.KeyLocation keyLocation = null;
        switch( awtKeyLocation )
        {
        case KeyEvent.KEY_LOCATION_STANDARD:
        {
            keyLocation = Keyboard.KeyLocation.STANDARD;
            break;
        }

        case KeyEvent.KEY_LOCATION_LEFT:
        {
            keyLocation = Keyboard.KeyLocation.LEFT;
            break;
        }

        case KeyEvent.KEY_LOCATION_RIGHT:
        {
            keyLocation = Keyboard.KeyLocation.RIGHT;
            break;
        }

        case KeyEvent.KEY_LOCATION_NUMPAD:
        {
            keyLocation = Keyboard.KeyLocation.KEYPAD;
            break;
        }
        }

        if( dragDescendant == null )
        {
            // Process the event
            Component focusedComponent = Component.getFocusedComponent();

            switch( event.getID() )
            {
            case KeyEvent.KEY_PRESSED:
            {
                boolean consumed = false;

                int keyCode = event.getKeyCode();

                if( Keyboard.isPressed( Keyboard.Modifier.CTRL )
                    && Keyboard.isPressed( Keyboard.Modifier.SHIFT ) )
                {
                    if( keyCode == JavaAwtKeyCode.PLUS
                        || keyCode == JavaAwtKeyCode.EQUALS
                        || keyCode == JavaAwtKeyCode.ADD )
                    {
                        scaleUp();
                    }
                    else if( keyCode == JavaAwtKeyCode.MINUS
                             || keyCode == JavaAwtKeyCode.SUBTRACT )
                    {
                        scaleDown();
                    }
                }

                try
                {
                    if( focusedComponent == null )
                    {
                        for( Application application : ApplicationContext.applications )
                        {
                            if( application instanceof Application.UnprocessedKeyHandler )
                            {
                                Application.UnprocessedKeyHandler unprocessedKeyHandler =
                                    (Application.UnprocessedKeyHandler) application;
                                unprocessedKeyHandler.keyPressed( keyCode, keyLocation );
                            }
                        }
                    }
                    else
                    {
                        if( !focusedComponent.isBlocked() )
                        {
                            consumed = focusedComponent.keyPressed( keyCode, keyLocation );
                        }
                    }
                }
                catch( Exception exception )
                {
                    ApplicationContext.handleUncaughtException( exception );
                }

                if( consumed )
                {
                    event.consume();
                }

                break;
            }

            case KeyEvent.KEY_RELEASED:
            {
                boolean consumed = false;

                int keyCode = event.getKeyCode();

                try
                {
                    if( focusedComponent == null )
                    {
                        for( Application application : ApplicationContext.applications )
                        {
                            if( application instanceof Application.UnprocessedKeyHandler )
                            {
                                Application.UnprocessedKeyHandler unprocessedKeyHandler =
                                    (Application.UnprocessedKeyHandler) application;
                                unprocessedKeyHandler.keyReleased( keyCode, keyLocation );
                            }
                        }
                    }
                    else
                    {
                        if( !focusedComponent.isBlocked() )
                        {
                            consumed = focusedComponent.keyReleased( keyCode, keyLocation );
                        }
                    }
                }
                catch( Exception exception )
                {
                    ApplicationContext.handleUncaughtException( exception );
                }

                if( consumed )
                {
                    event.consume();
                }

                break;
            }

            case KeyEvent.KEY_TYPED:
            {
                boolean consumed = false;

                char keyChar = event.getKeyChar();

                try
                {
                    if( focusedComponent == null )
                    {
                        for( Application application : ApplicationContext.applications )
                        {
                            if( application instanceof Application.UnprocessedKeyHandler )
                            {
                                Application.UnprocessedKeyHandler unprocessedKeyHandler =
                                    (Application.UnprocessedKeyHandler) application;
                                unprocessedKeyHandler.keyTyped( keyChar );
                            }
                        }
                    }
                    else
                    {
                        if( !focusedComponent.isBlocked() )
                        {
                            consumed = focusedComponent.keyTyped( keyChar );
                        }
                    }
                }
                catch( Exception exception )
                {
                    ApplicationContext.handleUncaughtException( exception );
                }

                if( consumed )
                {
                    event.consume();
                }

                break;
            }
            }
        }
        else
        {
            DragSource dragSource = dragDescendant.getDragSource();

            // If the user drop action changed, notify the drop descendant
            if( dropDescendant != null )
            {
                DropAction previousUserDropAction = userDropAction;
                userDropAction = getUserDropAction( event );

                if( previousUserDropAction != userDropAction )
                {
                    DropTarget dropTarget = dropDescendant.getDropTarget();

                    Point dropLocation = dragLocation;
                    if( dropLocation == null )
                    {
                        dropLocation = display.getMouseLocation();
                    }

                    dropLocation = dropDescendant.mapPointFromAncestor( display,
                                                                        dropLocation.x, dropLocation.y );
                    dropTarget.userDropActionChange( dropDescendant, dragManifest,
                                                     dragSource.getSupportedDropActions(),
                                                     dropLocation.x, dropLocation.y, userDropAction );
                }
            }
        }
    }

    public void immediateRepaint( int x, int y, int width, int height )
    {
        Graphics graphics = getGraphics();

        // If the display host has been made non-displayable (as will
        // happen when the native peer closes), graphics will be null
        if( graphics != null )
        {
            double scale = getScale();
            if( scale == 1 )
            {
                graphics.clipRect( x, y, width, height );
            }
            else
            {
                graphics.clipRect( (int) Math.floor( x * scale ), (int) Math.floor( y * scale ),
                                   (int) Math.ceil( width * scale ) + 1, (int) Math.ceil( height * scale ) + 1 );
            }
            paint( graphics );
            graphics.dispose();
        }
    }
}
