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

import java.io.Serializable;
import java.util.Comparator;

import org.apache.pivot.wtk.content.TreeNode;

/**
 * Orders TreeNode instances by their name using string comparison.
 */
public class TreeNodeComparator implements Comparator<TreeNode>, Serializable {
    private static final long serialVersionUID = 1L;

    @Override
    public int compare(TreeNode treeNode1, TreeNode treeNode2) {
        String text1 = treeNode1.getText();
        String text2 = treeNode2.getText();

        int result;

        if (text1 == null && text2 == null) {
            result = 0;
        } else if (text1 == null) {
            result = -1;
        } else if (text2 == null) {
            result = 1;
        } else {
            result = text1.compareToIgnoreCase(text2);
        }

        return result;
    }
}
