package com.github.lahahana.csv.base;

/**
 * An node which contains {@link CsvMetaInfo}, and it's location in {@link CsvMetaTreeBuilder.CsvMetaTree}
 *  
 * @author Lahahana
 * */

public class CsvMetaNode<T> implements Comparable<CsvMetaNode<T>>{

    private CsvMetaInfo<T> csvMetaInfo;
    
    private CsvMetaNode<?> parent;
    
    private CsvMetaNode<?>[] childs;
    
    private CsvMetaNode<?>[] path;
    
    private int depth;

    public CsvMetaNode(CsvMetaInfo<T> csvMetaInfo, CsvMetaNode<T> parent, CsvMetaNode<T>[] childs) {
        super();
        this.csvMetaInfo = csvMetaInfo;
        this.parent = parent;
        this.childs = childs;
        this.depth = 0;
    }
    
    public CsvMetaNode(CsvMetaInfo<T> csvMetaInfo, CsvMetaNode<?> parent, CsvMetaNode<?>[] childs, int depth) {
		super();
		this.csvMetaInfo = csvMetaInfo;
		this.parent = parent;
		this.childs = childs;
		this.depth = depth;
	}

	public CsvMetaInfo<T> getCsvMetaInfo() {
        return csvMetaInfo;
    }

    public void setCsvMetaInfo(CsvMetaInfo<T> csvMetaInfo) {
        this.csvMetaInfo = csvMetaInfo;
    }

    public CsvMetaNode<?> getParent() {
        return parent;
    }

    public void setParent(CsvMetaNode<T> parent) {
        this.parent = parent;
    }

    public CsvMetaNode<?>[] getChilds() {
        return childs;
    }

    public void setChilds(CsvMetaNode<?>[] childs) {
        this.childs = childs;
    }

    public CsvMetaNode<?>[] getPath() {
        return path;
    }

    public void setPath(CsvMetaNode<?>[] path) {
        this.path = path;
    }

    public int getDepth() {
        return depth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    @Override
    public int compareTo(CsvMetaNode<T> o) {
        return this.csvMetaInfo.compareTo(o.getCsvMetaInfo());
    }
    
}
