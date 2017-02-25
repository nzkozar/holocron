package com.ak93.holocron;

import java.util.List;

/**
 * Created by Anže Kožar on 25.2.2017.
 */

public class HolocronData {

    private Object mDataObject;
    private List<Object> mDataObjectList;
    private Class mDataClass;

    public HolocronData(Object object, List<Object> objectList,Class c){
        mDataObject = object;
        mDataObjectList = objectList;
        mDataClass = c;
    }

    public Object getDataObject() {
        return mDataObject;
    }

    public List<Object> getDataObjectList() {
        return mDataObjectList;
    }

    public boolean hasDataObject(){
        return mDataObject !=null;
    }

    public boolean hasDataObjectList(){
        return mDataObjectList !=null;
    }

    public Class getDataClass() {
        return mDataClass;
    }
}
