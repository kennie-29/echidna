package com.echidna.control.service;

import com.echidna.control.service.IEchidnaTelemetryListener;

interface IEchidnaControlService {
    void installModule(String archivePath);
    void uninstallModule();
    void refreshStatus();
    String getModuleStatus();
    void updateWhitelist(String processName, boolean enabled);
    void pushProfile(String profileId, String profileJson);
    String[] listProfiles();
    String getTelemetrySnapshot();
    boolean isTelemetryOptedIn();
    void registerTelemetryListener(IEchidnaTelemetryListener listener);
    void unregisterTelemetryListener(IEchidnaTelemetryListener listener);
    void setTelemetryOptIn(boolean enabled);
    String exportTelemetry(boolean includeTrends);
    void setProfile(String profile);
    void pushProfileSnapshot(String profileId, String profileJson);
    void setLatencyModeOverride(String profileId, String latencyMode);
    void setAppPresetBinding(String packageName, String presetId);
}
