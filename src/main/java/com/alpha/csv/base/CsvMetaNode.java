package com.alpha.csv.base;

public class CsvMetaNode implements Comparable<CsvMetaNode>{

    private CsvMetaInfo csvMetaInfo;
    
    private CsvMetaNode parent;
    
    private CsvMetaNode[] childs;
    
    private CsvMetaNode[] path;
    
    private int depth;

    public CsvMetaNode(CsvMetaInfo csvMetaInfo, CsvMetaNode parent, CsvMetaNode[] childs) {
        super();
        this.csvMetaInfo = csvMetaInfo;
        this.parent = parent;
        this.childs = childs;
        this.depth = 0;
    }

    public CsvMetaInfo getCsvMetaInfo() {
        return csvMetaInfo;
    }

    public void setCsvMetaInfo(CsvMetaInfo csvMetaInfo) {
        this.csvMetaInfo = csvMetaInfo;
    }

    public CsvMetaNode getParent() {
        return parent;
    }

    public void setParent(CsvMetaNode parent) {
        this.parent = parent;
    }

    public CsvMetaNode[] getChilds() {
        return childs;
    }

    public void setChilds(CsvMetaNode[] childs) {
        this.childs = childs;
    }

    public CsvMetaNode[] getPath() {
        return path;
    }

    public void setPath(CsvMetaNode[] path) {
        this.path = path;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    @Override
    public int compareTo(CsvMetaNode o) {
        return this.csvMetaInfo.compareTo(o.getCsvMetaInfo());
    }
    
}
