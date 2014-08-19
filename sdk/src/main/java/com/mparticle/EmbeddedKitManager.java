package com.mparticle;

import android.app.Activity;
import android.content.Context;
import android.location.Location;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by sdozor on 3/13/14.
 */
class EmbeddedKitManager implements IEmbeddedKit, MPActivityCallbacks{
    private ConcurrentHashMap<Integer,EmbeddedProvider> providers = new ConcurrentHashMap<Integer, EmbeddedProvider>(0);

    Context context;

    EmbeddedKitManager(Context context){
        this.context = context;
    }

    void updateKits(JSONArray kitConfigs){
        for (int i = 0; i < kitConfigs.length(); i++){
            try {
                JSONObject current = kitConfigs.getJSONObject(i);
                int currentId = current.getInt(EmbeddedProvider.KEY_ID);
                if (!providers.containsKey(currentId)) {
                    providers.put(currentId, new EmbeddedKitFactory().createInstance(currentId, context));
                }
                providers.get(currentId).parseConfig(current).update();
                if (!providers.get(currentId).optedOut()) {
                    providers.get(currentId).setUserAttributes(MParticle.getInstance().mUserAttributes);
                    syncUserIdentities(providers.get(currentId));
                }
            }catch (JSONException jse){
                ConfigManager.log(MParticle.LogLevel.ERROR, "Exception while parsing embedded kit configuration: " + jse.getMessage());
            }catch (ClassNotFoundException cnfe){
                //this should already be logged in the EmbeddedProvider, but I want to bubble up the exception.
            }catch (Exception e){
                ConfigManager.log(MParticle.LogLevel.ERROR, "Exception while started embedded kit: " + e.getMessage());
            }
        }
    }

    private void syncUserIdentities(EmbeddedProvider provider) {
        JSONArray identities = MParticle.getInstance().mUserIdentities;
        if (identities != null) {
            for (int i = 0; i < identities.length(); i++) {
                try {
                    JSONObject identity = identities.getJSONObject(i);
                    MParticle.IdentityType type = MParticle.IdentityType.parseInt(identity.getInt(Constants.MessageKey.IDENTITY_NAME));
                    String id = identity.getString(Constants.MessageKey.IDENTITY_VALUE);
                    provider.setUserIdentity(id, type);
                }catch (JSONException jse){
                    //swallow
                }
            }
        }
    }

    @Override
    public void logEvent(MParticle.EventType type, String name, JSONObject eventAttributes) {
        for (EmbeddedProvider provider : providers.values()){
            try {
                if (!provider.optedOut()) {
                    provider.logEvent(type, name, eventAttributes);
                }
            } catch (Exception e) {
                ConfigManager.log(MParticle.LogLevel.WARNING, "Failed to call logEvent for embedded provider: " + provider.getName() + ": " + e.getMessage());
            }
        }
    }

    @Override
    public void logTransaction(MPProduct product) {
        for (EmbeddedProvider provider : providers.values()){
            try {
                if (!provider.optedOut()) {
                    provider.logTransaction(product);
                }
            } catch (Exception e) {
                ConfigManager.log(MParticle.LogLevel.WARNING, "Failed to call logTransaction for embedded provider: " + provider.getName() + ": " + e.getMessage());
            }
        }
    }

    @Override
    public void logScreen(String screenName, JSONObject eventAttributes) {
        for (EmbeddedProvider provider : providers.values()){
            try {
                if (!provider.optedOut()) {
                    provider.logScreen(screenName, eventAttributes);
                }
            } catch (Exception e) {
                ConfigManager.log(MParticle.LogLevel.WARNING, "Failed to call logScreen for embedded provider: " + provider.getName() + ": " + e.getMessage());
            }
        }
    }

    @Override
    public void setLocation(Location location) {
        for (EmbeddedProvider provider : providers.values()){
            try {
                if (!provider.optedOut()) {
                    provider.setLocation(location);
                }
            } catch (Exception e) {
                ConfigManager.log(MParticle.LogLevel.WARNING, "Failed to call setLocation for embedded provider: " + provider.getName() + ": " + e.getMessage());
            }
        }
    }

    @Override
    public void setUserAttributes(JSONObject mUserAttributes) {
        for (EmbeddedProvider provider : providers.values()){
            try {
                if (!provider.optedOut()) {
                    provider.setUserAttributes(mUserAttributes);
                }
            } catch (Exception e) {
                ConfigManager.log(MParticle.LogLevel.WARNING, "Failed to call setUserAttributes for embedded provider: " + provider.getName() + ": " + e.getMessage());
            }
        }
    }

    @Override
    public void removeUserAttribute(String key) {
        for (EmbeddedProvider provider : providers.values()){
            try {
                if (!provider.optedOut()) {
                    provider.removeUserAttribute(key);
                }
            } catch (Exception e) {
                ConfigManager.log(MParticle.LogLevel.WARNING, "Failed to call removeUserAttribute for embedded provider: " + provider.getName() + ": " + e.getMessage());
            }
        }
    }

    @Override
    public void setUserIdentity(String id, MParticle.IdentityType identityType) {
        for (EmbeddedProvider provider : providers.values()){
            try {
                if (!provider.optedOut()) {
                    provider.setUserIdentity(id, identityType);
                }
            } catch (Exception e) {
                ConfigManager.log(MParticle.LogLevel.WARNING, "Failed to call setUserIdentity for embedded provider: " + provider.getName() + ": " + e.getMessage());
            }
        }
    }

    @Override
    public void logout() {
        for (EmbeddedProvider provider : providers.values()){
            try {
                if (!provider.optedOut()) {
                    provider.logout();
                }
            } catch (Exception e) {
                ConfigManager.log(MParticle.LogLevel.WARNING, "Failed to call logout for embedded provider: " + provider.getName() + ": " + e.getMessage());
            }
        }
    }

    @Override
    public void removeUserIdentity(String id) {
        for (EmbeddedProvider provider : providers.values()){
            try {
                if (!provider.optedOut()) {
                    provider.removeUserIdentity(id);
                }
            } catch (Exception e) {
                ConfigManager.log(MParticle.LogLevel.WARNING, "Failed to call removeUserIdentity for embedded provider: " + provider.getName() + ": " + e.getMessage());
            }
        }
    }

    @Override
    public void onActivityCreated(Activity activity, int activityCount) {
        for (EmbeddedProvider provider : providers.values()){
            if (provider instanceof MPActivityCallbacks) {
                try {
                    if (!provider.optedOut()) {
                        ((MPActivityCallbacks) provider).onActivityCreated(activity, activityCount);
                    }
                } catch (Exception e) {
                    ConfigManager.log(MParticle.LogLevel.WARNING, "Failed to call onCreate for embedded provider: " + provider.getName() + ": " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void onActivityResumed(Activity activity, int currentCount) {
        for (EmbeddedProvider provider : providers.values()){
            if (provider instanceof MPActivityCallbacks) {
                try {
                    if (!provider.optedOut()) {
                        ((MPActivityCallbacks) provider).onActivityResumed(activity, currentCount);
                    }
                } catch (Exception e) {
                    ConfigManager.log(MParticle.LogLevel.WARNING, "Failed to call onResume for embedded provider: " + provider.getName() + ": " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void onActivityPaused(Activity activity, int activityCount) {

    }

    @Override
    public void onActivityStopped(Activity activity, int currentCount) {

    }

    @Override
    public void onActivityStarted(Activity activity, int currentCount) {

    }

    public boolean isEmbeddedKitUri(String url) {
        for (EmbeddedProvider provider : providers.values()){
            if (provider.isOriginator(url)){
                return true;
            }
        }
        return false;
    }

    public static class BaseEmbeddedKitFactory {
        private final static int MAT = 32;
        private final static int KOCHAVA = 37;
        protected EmbeddedProvider createInstance(int id, Context context) throws JSONException, ClassNotFoundException{
            switch (id){
                case MAT:
                    return new EmbeddedMAT(context);
                case KOCHAVA:
                    return new EmbeddedKochava(context);
                default:
                    return null;
            }
        }
    }
}
