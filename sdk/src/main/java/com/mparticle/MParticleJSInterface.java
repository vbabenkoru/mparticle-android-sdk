package com.mparticle;

import android.content.Context;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.mparticle.MParticle.EventType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Javascript interface to be used for {@code Webview} analytics.
 *
 */
public class MParticleJSInterface {
    private static final String TAG = Constants.LOG_TAG;
    private Context mContext;
    private MParticle mApiInstance;

    //the following keys are sent from the JS library as a part of each event
    private static final String JS_KEY_EVENT_NAME = "EventName";
    private static final String JS_KEY_EVENT_CATEGORY = "EventCategory";
    private static final String JS_KEY_EVENT_ATTRIBUTES = "EventAttributes";
    private static final String JS_KEY_EVENT_DATATYPE = "EventDataType";
    private static final String JS_KEY_OPTOUT = "OptOut";

    private static final int JS_MSG_TYPE_SS = 1;
    private static final  int JS_MSG_TYPE_SE = 2;
    private static final int JS_MSG_TYPE_PV = 3;
    private static final int JS_MSG_TYPE_PE = 4;
    private static final int JS_MSG_TYPE_CR = 5;
    private static final int JS_MSG_TYPE_OO = 6;

    public MParticleJSInterface(Context c, MParticle apiInstance) {
        mContext = c;
        mApiInstance = apiInstance;
    }

    @JavascriptInterface
    public void logEvent(String json) {
        try {
            JSONObject event = new JSONObject(json);

            String name = event.getString(JS_KEY_EVENT_NAME);
            EventType eventType = convertEventType(event.getInt(JS_KEY_EVENT_CATEGORY));
            Map<String, String> eventAttributes = convertToMap(event.getJSONObject(JS_KEY_EVENT_ATTRIBUTES));

            int messageType = event.getInt(JS_KEY_EVENT_DATATYPE);
            switch (messageType){
                case JS_MSG_TYPE_PE:
                    mApiInstance.logEvent(name,
                            eventType,
                            eventAttributes);
                    break;
                case JS_MSG_TYPE_PV:
                    mApiInstance.logScreen(name,
                            eventAttributes,
                            true);
                    break;
                case JS_MSG_TYPE_OO:
                    mApiInstance.setOptOut(event.optBoolean(JS_KEY_OPTOUT));
                    break;
                case JS_MSG_TYPE_CR:
                    mApiInstance.logError(name, eventAttributes);
                    break;
                case JS_MSG_TYPE_SE:
                case JS_MSG_TYPE_SS:
                    //swallow session start and end events, the native SDK will handle those.
                default:

            }

        } catch (JSONException e) {
            Log.w(TAG, "Error deserializing JSON data from WebView: " + e.getMessage());
        }
    }

    @JavascriptInterface
    public void setUserTag(String tagName){
        mApiInstance.setUserTag(tagName);
    }

    @JavascriptInterface
    public void removeUserTag(String tagName){
        mApiInstance.removeUserTag(tagName);
    }

    @JavascriptInterface
    public void setUserAttribute(String json){
        try {
            JSONObject attribute = new JSONObject(json);
            Iterator<?> keys = attribute.keys();

            while( keys.hasNext() ){
                String key = (String)keys.next();
                mApiInstance.setUserAttribute(key, attribute.getString(key));
            }

        } catch (JSONException e) {
            Log.w(TAG, "Error deserializing JSON data from WebView: " + e.getMessage());
        }
    }

    @JavascriptInterface
    public void removeUserAttribute(String attributeName){
        mApiInstance.removeUserAttribute(attributeName);
    }

    @JavascriptInterface
    public void setSessionAttribute(String json){
        try {
            JSONObject attribute = new JSONObject(json);
            Iterator<?> keys = attribute.keys();

            while( keys.hasNext() ){
                String key = (String)keys.next();
                mApiInstance.setSessionAttribute(key, attribute.getString(key));
            }

        } catch (JSONException e) {
            Log.w(TAG, "Error deserializing JSON data from WebView: " + e.getMessage());
        }
    }

    @JavascriptInterface
    public void setUserIdentity(String json){
        try {
            JSONObject attribute = new JSONObject(json);
            Iterator<?> keys = attribute.keys();

            while( keys.hasNext() ){
                String key = (String)keys.next();
                mApiInstance.setUserIdentity(key, convertIdentityType(attribute.getInt(key)));
            }

        } catch (JSONException e) {
            Log.w(TAG, "Error deserializing JSON data from WebView: " + e.getMessage());
        }
    }

    @JavascriptInterface
    public void removeUserIdentity(String id){
        mApiInstance.removeUserIdentity(id);
    }

    private Map<String, String> convertToMap(JSONObject attributes) {
        if (null != attributes) {
            Iterator keys = attributes.keys();

            Map<String, String> parsedAttributes = new HashMap<String, String>();

            while (keys.hasNext()) {
                String key = (String) keys.next();
                try {
                    parsedAttributes.put(key, attributes.getString(key));
                } catch (JSONException e) {
                    Log.w(TAG, "Could not parse event attribute value");
                }
            }

            return parsedAttributes;
        }

        return null;
    }

    private EventType convertEventType(int eventType) {
        switch (eventType) {
            case 1:
                return EventType.Navigation;
            case 2:
                return EventType.Location;
            case 3:
                return EventType.Search;
            case 4:
                return EventType.Transaction;
            case 5:
                return EventType.UserContent;
            case 6:
                return EventType.UserPreference;
            case 7:
                return EventType.Social;
            default:
                return EventType.Other;
        }
    }

    private MParticle.IdentityType convertIdentityType(int identityType) {
        switch (identityType) {
            case 2:
                return MParticle.IdentityType.CustomId;
            case 3:
                return MParticle.IdentityType.Facebook;
            case 4:
                return MParticle.IdentityType.Twitter;
            case 5:
                return  MParticle.IdentityType.Google;
            case 6:
                return  MParticle.IdentityType.Microsoft;
            case 7:
                return  MParticle.IdentityType.Yahoo;
            default:
                return  MParticle.IdentityType.Email;
        }
    }
}
