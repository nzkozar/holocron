package com.ak93.holocron;

import android.support.annotation.Nullable;

import java.util.List;

/**
 * Created by Anže Kožar on 25.2.2017.
 * An object to hold response data for use with HolocronResponseHandler
 */

public class HolocronResponse {

    private Object mDataObject;
    private List<Object> mDataObjectList;
    private Class mDataClass;

    /**
     * Constructor
     * @param object A single data object associated with this response
     * @param objectList A List of data objects associated with this response
     * @param c A Class associated with this response
     */
    public HolocronResponse(@Nullable Object object, @Nullable List<Object> objectList, @Nullable Class c){
        mDataObject = object;
        mDataObjectList = objectList;
        mDataClass = c;
    }

    /**
     * @return A single data object held by this response
     */
    public Object getDataObject() {
        return mDataObject;
    }

    /**
     * @return A List of data objects held by this response
     */
    public List<Object> getDataObjectList() {
        return mDataObjectList;
    }

    /**
     * @return True if this response is holding a data object
     */
    public boolean hasDataObject(){
        return mDataObject !=null;
    }

    /**
     * @return True if this response is holding a List of data objects
     */
    public boolean hasDataObjectList(){
        return mDataObjectList !=null;
    }

    /**
     * @return A Class associated with this response
     */
    public Class getDataClass() {
        return mDataClass;
    }
}
