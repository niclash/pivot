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
package org.apache.pivot.wtk;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import org.apache.pivot.collections.ArrayList;
import org.apache.pivot.collections.Dictionary;
import org.apache.pivot.collections.HashMap;
import org.apache.pivot.collections.Map;
import org.apache.pivot.json.JSONSerializer;
import org.apache.pivot.serialization.SerializationException;
import org.apache.pivot.util.Version;

/**
 * Base class for application contexts.
 */
public abstract class ApplicationContext
{

    /**
     * Resource cache dictionary implementation.
     */
    public static final class ResourceCacheDictionary
        implements Dictionary<URL, Object>, Iterable<URL>
    {
        private ResourceCacheDictionary()
        {
        }

        @Override
        public Object get( URL key )
        {
            try
            {
                return resourceCache.get( key.toURI() );
            }
            catch( URISyntaxException exception )
            {
                throw new RuntimeException( exception );
            }
        }

        @Override
        public Object put( URL key, Object value )
        {
            try
            {
                return resourceCache.put( key.toURI(), value );
            }
            catch( URISyntaxException exception )
            {
                throw new RuntimeException( exception );
            }
        }

        @Override
        public Object remove( URL key )
        {
            try
            {
                return resourceCache.remove( key.toURI() );
            }
            catch( URISyntaxException exception )
            {
                throw new RuntimeException( exception );
            }
        }

        @Override
        public boolean containsKey( URL key )
        {
            try
            {
                return resourceCache.containsKey( key.toURI() );
            }
            catch( URISyntaxException exception )
            {
                throw new RuntimeException( exception );
            }
        }

        @Override
        public Iterator<URL> iterator()
        {
            return new Iterator<URL>()
            {
                private Iterator<URI> iterator = resourceCache.iterator();

                @Override
                public boolean hasNext()
                {
                    return iterator.hasNext();
                }

                @Override
                public URL next()
                {
                    try
                    {
                        return iterator.next().toURL();
                    }
                    catch( MalformedURLException exception )
                    {
                        throw new RuntimeException( exception );
                    }
                }

                @Override
                public void remove()
                {
                    throw new UnsupportedOperationException();
                }
            };
        }
    }

    /**
     * Class representing a scheduled callback.
     */
    public static final class ScheduledCallback extends TimerTask
    {
        private Runnable callback;
        private QueuedCallback queuedCallback = null;

        private ScheduledCallback( Runnable callback )
        {
            this.callback = callback;
        }

        @Override
        public void run()
        {
            if( queuedCallback != null )
            {
                queuedCallback.cancel();
            }

            queuedCallback = queueCallback( callback );
        }

        @Override
        public boolean cancel()
        {
            if( queuedCallback != null )
            {
                queuedCallback.cancel();
            }

            return super.cancel();
        }
    }

    /**
     * Class representing a queued callback.
     */
    public static class QueuedCallback implements Runnable
    {
        private Runnable callback;
        private volatile boolean executed = false;
        private volatile boolean cancelled = false;

        private QueuedCallback( Runnable callback )
        {
            this.callback = callback;
        }

        public void run()
        {
            if( !cancelled )
            {
                try
                {
                    callback.run();
                }
                catch( Exception exception )
                {
                    exception.printStackTrace();

                    for( Application application : applications )
                    {
                        if( application instanceof Application.UncaughtExceptionHandler )
                        {
                            Application.UncaughtExceptionHandler uncaughtExceptionHandler =
                                (Application.UncaughtExceptionHandler) application;
                            uncaughtExceptionHandler.uncaughtExceptionThrown( exception );
                        }
                    }
                }

                for( Display display : displays )
                {
                    display.validate();
                }

                executed = true;
            }
        }

        public boolean cancel()
        {
            cancelled = true;
            return ( !executed );
        }
    }

    protected static URL origin = null;
    protected static ArrayList<Display> displays = new ArrayList<Display>();
    protected static ArrayList<Application> applications = new ArrayList<Application>();

    private static Timer timer = null;

    private static HashMap<URI, Object> resourceCache = new HashMap<URI, Object>();
    private static ResourceCacheDictionary resourceCacheDictionary = new ResourceCacheDictionary();

    private static Version jvmVersion = null;
    private static Version pivotVersion = null;

    static
    {
        // Get the JVM version
        jvmVersion = Version.decode( System.getProperty( "java.vm.version" ) );

        // Get the Pivot version
        String version = ApplicationContext.class.getPackage().getImplementationVersion();
        if( version == null )
        {
            pivotVersion = new Version( 0, 0, 0, 0 );
        }
        else
        {
            pivotVersion = Version.decode( version );
        }
    }

    /**
     * Returns this application's origin (the URL of it's originating server).
     *
     * @return The application's origin, or <tt>null</tt> if the origin cannot be determined.
     */
    public static URL getOrigin()
    {
        return origin;
    }

    /**
     * Resource properties accessor.
     */
    public static ResourceCacheDictionary getResourceCache()
    {
        return resourceCacheDictionary;
    }

    /**
     * Adds the styles from a named stylesheet to the named or typed style collections.
     *
     * @param resourceName
     */
    @SuppressWarnings( "unchecked" )
    public static void applyStylesheet( String resourceName )
    {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        URL stylesheetLocation = classLoader.getResource( resourceName.substring( 1 ) );
        if( stylesheetLocation == null )
        {
            throw new RuntimeException( "Unable to locate style sheet resource \"" + resourceName + "\"." );
        }

        try
        {
            InputStream inputStream = stylesheetLocation.openStream();

            try
            {
                JSONSerializer serializer = new JSONSerializer();
                Map<String, ?> stylesheet = (Map<String, ?>) serializer.readObject( inputStream );

                for( String name : stylesheet )
                {
                    Map<String, ?> styles = (Map<String, ?>) stylesheet.get( name );

                    int i = name.lastIndexOf( '.' ) + 1;
                    if( Character.isUpperCase( name.charAt( i ) ) )
                    {
                        // Assume the current package if none specified
                        if( !name.contains( "." ) )
                        {
                            name = ApplicationContext.class.getPackage().getName() + "." + name;
                        }

                        Class<?> type = null;
                        try
                        {
                            type = Class.forName( name );
                        }
                        catch( ClassNotFoundException exception )
                        {
                            // No-op
                        }

                        if( type != null
                            && Component.class.isAssignableFrom( type ) )
                        {
                            Component.getTypedStyles().put( (Class<? extends Component>) type, styles );
                        }
                    }
                    else
                    {
                        Component.getNamedStyles().put( name, styles );
                    }
                }
            }
            finally
            {
                inputStream.close();
            }
        }
        catch( IOException exception )
        {
            throw new RuntimeException( exception );
        }
        catch( SerializationException exception )
        {
            throw new RuntimeException( exception );
        }
    }

    /**
     * Returns the current JVM version.
     *
     * @return The current JVM version, or <tt>null</tt> if the version can't be
     *         determined.
     */
    public static Version getJVMVersion()
    {
        return jvmVersion;
    }

    /**
     * Returns the current Pivot version.
     *
     * @return The current Pivot version (determined at build time), or <tt>null</tt>
     *         if the version can't be determined.
     */
    public static Version getPivotVersion()
    {
        return pivotVersion;
    }

    /**
     * Schedules a task for one-time execution. The task will be executed on
     * the UI thread.
     *
     * @param callback The task to execute.
     * @param delay    The length of time to wait before executing the task.
     */
    public static ScheduledCallback scheduleCallback( Runnable callback, long delay )
    {
        ScheduledCallback scheduledCallback = new ScheduledCallback( callback );

        // TODO This is a workaround for a potential OS X bug; revisit
        try
        {
            try
            {
                timer.schedule( scheduledCallback, delay );
            }
            catch( IllegalStateException exception )
            {
                createTimer();
                timer.schedule( scheduledCallback, delay );
            }
        }
        catch( Throwable throwable )
        {
            System.err.println( "Unable to schedule callback: " + throwable );
        }

        return scheduledCallback;
    }

    /**
     * Schedules a task for repeated execution. The task will be executed on the
     * UI thread and will begin executing immediately.
     *
     * @param callback The task to execute.
     * @param period   The interval at which the task will be repeated.
     */
    public static ScheduledCallback scheduleRecurringCallback( Runnable callback, long period )
    {
        return scheduleRecurringCallback( callback, 0, period );
    }

    /**
     * Schedules a task for repeated execution. The task will be executed on the
     * UI thread.
     *
     * @param callback The task to execute.
     * @param delay    The length of time to wait before the first execution of the task
     * @param period   The interval at which the task will be repeated.
     */
    public static ScheduledCallback scheduleRecurringCallback( Runnable callback, long delay, long period )
    {
        ScheduledCallback scheduledCallback = new ScheduledCallback( callback );

        // TODO This is a workaround for a potential OS X bug; revisit
        try
        {
            try
            {
                timer.schedule( scheduledCallback, delay, period );
            }
            catch( IllegalStateException exception )
            {
                createTimer();
                timer.schedule( scheduledCallback, delay, period );
            }
        }
        catch( Throwable throwable )
        {
            System.err.println( "Unable to schedule callback: " + throwable );
        }

        return scheduledCallback;
    }

    /**
     * Queues a task to execute after all pending events have been processed and
     * returns without waiting for the task to complete.
     *
     * @param callback The task to execute.
     */
    public static QueuedCallback queueCallback( Runnable callback )
    {
        return queueCallback( callback, false );
    }

    /**
     * Queues a task to execute after all pending events have been processed and
     * optionally waits for the task to complete.
     *
     * @param callback The task to execute.
     * @param wait     If <tt>true</tt>, does not return until the task has executed.
     *                 Otherwise, returns immediately.
     */
    public static QueuedCallback queueCallback( Runnable callback, boolean wait )
    {
        QueuedCallback queuedCallback = new QueuedCallback( callback );

        // TODO This is a workaround for a potential OS X bug; revisit
        try
        {
            if( wait )
            {
                try
                {
                    java.awt.EventQueue.invokeAndWait( queuedCallback );
                }
                catch( InvocationTargetException exception )
                {
                    throw new RuntimeException( exception.getCause() );
                }
                catch( InterruptedException exception )
                {
                    throw new RuntimeException( exception );
                }
            }
            else
            {
                java.awt.EventQueue.invokeLater( queuedCallback );
            }
        }
        catch( Throwable throwable )
        {
            System.err.println( "Unable to queue callback: " + throwable );
        }

        return queuedCallback;
    }

    protected static void createTimer()
    {
        timer = new Timer();
    }

    protected static void destroyTimer()
    {
        timer.cancel();
        timer = null;
    }

    public static void invalidateDisplays()
    {
        for( Display display : displays )
        {
            display.invalidate();
        }
    }

    public static void handleUncaughtException( Exception exception )
    {
        int n = 0;
        for( Application application : applications )
        {
            if( application instanceof Application.UncaughtExceptionHandler )
            {
                Application.UncaughtExceptionHandler uncaughtExceptionHandler =
                    (Application.UncaughtExceptionHandler) application;
                uncaughtExceptionHandler.uncaughtExceptionThrown( exception );
                n++;
            }
        }

        if( n == 0 )
        {
            exception.printStackTrace();
        }
    }
}
