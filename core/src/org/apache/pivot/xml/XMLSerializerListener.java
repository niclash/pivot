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
package org.apache.pivot.xml;

/**
 * XML serializer listener interface.
 */
public interface XMLSerializerListener {
    /**
     * XML serializer listener adapter.
     */
    public static class Adapter implements XMLSerializerListener {
        @Override
        public void beginElement(XMLSerializer xmlSerializer, Element element) {
        }

        @Override
        public void endElement(XMLSerializer xmlSerializer) {
        }

        @Override
        public void readTextNode(XMLSerializer xmlSerializer, TextNode textNode) {
        }
    }

    /**
     * Called when the serializer has begun reading an element.
     *
     * @param xmlSerializer
     * @param element
     */
    public void beginElement(XMLSerializer xmlSerializer, Element element);

    /**
     * Called when the serializer has finished reading an element.
     *
     * @param xmlSerializer
     */
    public void endElement(XMLSerializer xmlSerializer);

    /**
     * Called when the serializer has read a text node.
     *
     * @param xmlSerializer
     * @param textNode
     */
    public void readTextNode(XMLSerializer xmlSerializer, TextNode textNode);
}
