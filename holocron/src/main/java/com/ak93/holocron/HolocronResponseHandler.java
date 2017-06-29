package com.ak93.holocron;

/**
 *
 */

public interface HolocronResponseHandler<T>{
    void onHolocronResponse(int responseCode, HolocronResponse<T> data);
}
