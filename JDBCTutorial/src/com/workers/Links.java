package com.workers;
public class Links {
    protected Links al[]=null;
    public Object owner=null;
    final static int PREV=0,NEXT=1;
    public Links(int nLinks) {
        assert(nLinks > 0);
        al=new Links[nLinks*2];
    }
    public int getNumber() {
        return al.length/2;
    }
    public Links next() {
        return al[NEXT];
    }
    public Links next(int list) {
        return al[list*2+NEXT];
    }
    public Links previous() {
        return al[PREV];
    }
    public void remove() {
        if (al[PREV] != null)
          al[PREV].al[NEXT]=al[NEXT];
        if (al[NEXT] != null)
          al[NEXT].al[PREV]=al[PREV];
    }
    public Object get() {return owner;}
    public Object set(Object what) {
        Object x=owner;
        owner=what;
        return x;
    }
    public void addAfter(Links q) {
        q.al[PREV]=this;  //previous
        q.al[NEXT]=al[NEXT]; //next
        if (al[NEXT] != null) al[NEXT].al[PREV]=q;
        al[NEXT]=q;
    }
}
