package com.ak93.holocron;

import android.support.annotation.Nullable;

import java.util.List;

/**
 * An object to hold response data for use with HolocronResponseHandler
 */

public class HolocronResponse<T> {

    private T mDataObject;
    private List<T> mDataObjectList;

    /**
     * Constructor
     * @param object A single data object associated with this response
     * @param objectList A List of data objects associated with this response
     */
    public HolocronResponse(@Nullable T object, @Nullable List<T> objectList){
        mDataObject = object;
        mDataObjectList = objectList;
    }

    /**
     * DEPRECATED Constructor
     * @param object A single data object associated with this response
     * @param objectList A List of data objects associated with this response
     * @param c A Class associated with this response
     */
    public HolocronResponse(@Nullable T object, @Nullable List<T> objectList, @Nullable Class c){
        mDataObject = object;
        mDataObjectList = objectList;
    }

    /**
     * @return A single data object held by this response
     */
    public T getDataObject() {
        return mDataObject;
    }

    /**
     * @return A List of data objects held by this response
     */
    public List<T> getDataObjectList() {
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
        if(mDataObject!=null){
            return mDataObject.getClass();
        }else if(mDataObjectList!=null && mDataObjectList.size()>0){
            return mDataObjectList.get(0).getClass();
        }
        return Object.class;
    }
}
