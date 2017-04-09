package com.github.lahahana.csv.base;

import java.lang.reflect.Field;

import com.github.lahahana.csv.util.Utils;

public class CsvMetaTreeBuilder {

    public static CsvMetaTree buildCsvMetaTree() {
        return new CsvMetaTree();
    }
    
    public static CsvMetaTree buildCsvMetaTree(Field[] fields) {
        CsvMetaTree csvMetaTree = new CsvMetaTree();
        csvMetaTree.root.setChilds(Utils.convertFieldsToCsvMetaNodes(csvMetaTree.root, fields));
        return csvMetaTree;
    }
    
    public static class CsvMetaTree {
        private final CsvMetaNode root;

        public CsvMetaTree() {
            super();
            this.root = new CsvMetaNode(null, null, null);
        }

        public CsvMetaNode getRoot() {
            return root;
        }
        
        public int getLeafNodeCount(){
            return count(root, 1);
        }
        
        private int count(CsvMetaNode csvMetaNode, int count) {
            CsvMetaNode[] childs = csvMetaNode.getChilds();
            if(childs != null) {
                count --;
                count += childs.length;
                for (CsvMetaNode node : childs) {
                    count = count(node, count);
                }
            }
            return count;
        }
        
    }
}
