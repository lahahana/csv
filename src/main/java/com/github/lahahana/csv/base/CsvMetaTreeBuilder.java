package com.github.lahahana.csv.base;

import java.lang.reflect.Field;

import com.github.lahahana.csv.util.Utils;

/**
 * @author Lahahana
 * */

public class CsvMetaTreeBuilder {

    public static CsvMetaTree buildCsvMetaTree(Field[] fields) {
        CsvMetaTree csvMetaTree = new CsvMetaTree();
        csvMetaTree.root.setChilds(Utils.convertFieldsToCsvMetaNodes(csvMetaTree.root, fields));
        return csvMetaTree;
    }
    
    public static class CsvMetaTree {
        private final CsvMetaNode<Object> root;

        public CsvMetaTree() {
            super();
            this.root = new CsvMetaNode<Object>(null, null, null);
        }

        public CsvMetaNode<?> getRoot() {
            return root;
        }
        
        public int getLeafNodeCount(){
            return count(root, 1);
        }
        
        private int count(CsvMetaNode<?> csvMetaNode, int count) {
            CsvMetaNode<?>[] csvMetaNodes = csvMetaNode.getChilds();
            if(csvMetaNodes != null) {
                count += csvMetaNodes.length - 1;
                for (CsvMetaNode<?> node : csvMetaNodes) {
                    count = count(node, count);
                }
            }
            return count;
        }
        
    }
}
