package com.o3dr.android.dp.wear.lib.utils;

import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

/**
 * Created by fhuya on 12/27/14.
 */
public class WearUtils {

    private static final String TAG = WearUtils.class.getSimpleName();

    public static final String PACKAGE_NAME = "com.o3dr.android.dp.wear";

    private static final String ROOT_PATH = "/dp/wear";

    public static final String VEHICLE_DATA_PREFIX = ROOT_PATH + "/vehicle_data/";

    public static final String ACTION_PREFIX = ROOT_PATH + "/action";

    /* List of supported actions */

    /**
     * Used to request connection to the vehicle.
     */
    public static final String ACTION_CONNECT = ACTION_PREFIX + "/connect";

    /**
     * Used to request disconnection from the vehicle.
     */
    public static final String ACTION_DISCONNECT = ACTION_PREFIX + "/disconnect";

    public static final String ACTION_SHOW_CONTEXT_STREAM_NOTIFICATION = ACTION_PREFIX +
            "/show_context_stream_notification";

    public static final String ACTION_ARM = ACTION_PREFIX + "/arm";
    public static final String ACTION_TAKE_OFF = ACTION_PREFIX + "/take_off";
    public static final String ACTION_DISARM = ACTION_PREFIX + "/disarm";
    public static final String ACTION_OPEN_PHONE_APP = ACTION_PREFIX + "/open_phone_app";
    public static final String ACTION_OPEN_WEAR_APP = ACTION_PREFIX + "/open_wear_app";
    public static final String ACTION_CHANGE_VEHICLE_MODE = ACTION_PREFIX + "/change_vehicle_mode";

    /**
     * Asynchronously send a message using the Wearable.MessageApi api to connected wear nodes.
     *
     * @param apiClientMgr google api client manager
     * @param msgPath      non-null path for the message
     * @param msgData      optional message data
     * @return true if the message task was successfully queued.
     */
    public static boolean asyncSendMessage(GoogleApiClientManager apiClientMgr,
                                           final String msgPath, final byte[] msgData) {
        return apiClientMgr.addTaskToBackground(apiClientMgr.new GoogleApiClientTask() {

            @Override
            public void doRun() {
                final GoogleApiClient apiClient = getGoogleApiClient();

                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi
                        .getConnectedNodes(apiClient)
                        .await();

                for (Node node : nodes.getNodes()) {
                    Log.d(TAG, "Sending message to " + node.getDisplayName());
                    final MessageApi.SendMessageResult result = Wearable.MessageApi
                            .sendMessage(apiClient, node.getId(), msgPath, msgData)
                            .await();

                    final Status status = result.getStatus();
                    if (!status.isSuccess()) {
                        Log.e(TAG, "Failed to relay the data: " + status.getStatusCode());
                    }
                }
            }
        });
    }

    /**
     * Asynchronously push/update a data item using the Wearable.DataApi api to connected wear
     * nodes.
     * @param apiClientMgr google api client manager
     * @param path non-null path
     * @param dataMapBundle non-null data bundle
     * @return true if the task was successfully queued.
     */
    public static boolean asyncPutDataItem(GoogleApiClientManager apiClientMgr,
                                           final String path, final Bundle dataMapBundle) {
        return apiClientMgr.addTaskToBackground(apiClientMgr.new GoogleApiClientTask() {

            @Override
            public void doRun() {
                final PutDataMapRequest dataMap = PutDataMapRequest.create(path);
                dataMap.getDataMap().putAll(DataMap.fromBundle(dataMapBundle));
                PutDataRequest request = dataMap.asPutDataRequest();
                final DataApi.DataItemResult result = Wearable.DataApi
                        .putDataItem(getGoogleApiClient(), request)
                        .await();

                final Status status = result.getStatus();
                if (!status.isSuccess()) {
                    Log.e(TAG, "Failed to relay the data: " + status.getStatusCode());
                }
            }
        });
    }

    /**
     * Asynchronously push/update a data item using the Wearable.DataApi api to connected wear
     * nodes.
     * @param apiClientMgr google api client manager
     * @param path non-null path
     * @param data non-null data payload
     * @return true if the task was successfully queued.
     */
    public static boolean asyncPutDataItem(GoogleApiClientManager apiClientMgr,
                                           final String path, final byte[] data) {
        return apiClientMgr.addTaskToBackground(apiClientMgr.new GoogleApiClientTask() {

            @Override
            public void doRun() {
                final PutDataRequest request = PutDataRequest.create(path);
                request.setData(data);
                final DataApi.DataItemResult result = Wearable.DataApi
                        .putDataItem(getGoogleApiClient(), request)
                        .await();

                final Status status = result.getStatus();
                if (!status.isSuccess()) {
                    Log.e(TAG, "Failed to relay the data: " + status.getStatusCode());
                }
            }
        });
    }
}
