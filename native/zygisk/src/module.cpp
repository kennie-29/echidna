#include "hooks/audio_hook_orchestrator.h"

#include <memory>

#include "runtime/profile_sync_server.h"
#include "state/shared_state.h"
#include "zygisk.hpp"

/**
 * @file module.cpp
 * @brief Zygisk module entrypoints - attach/detach hooks and ensure
 * the runtime orchestrator and profile sync server exist.
 */

namespace
{

    std::unique_ptr<echidna::hooks::AudioHookOrchestrator> g_audio_orchestrator;
    std::unique_ptr<echidna::runtime::ProfileSyncServer> g_profile_server;

} // namespace

/**
 * @brief Zygisk attach entrypoint called by the host to initialize
 * hooking machinery inside the process.
 */
extern "C" void echidna_module_attach()
{
    auto &state = echidna::state::SharedState::instance();
    state.refreshFromSharedMemory();
    state.setStatus(echidna::state::InternalStatus::kWaitingForAttach);

    if (!g_profile_server)
    {
        g_profile_server = std::make_unique<echidna::runtime::ProfileSyncServer>();
        g_profile_server->start();
    }

    if (!g_audio_orchestrator)
    {
        g_audio_orchestrator = std::make_unique<echidna::hooks::AudioHookOrchestrator>();
    }

    if (!g_audio_orchestrator->installHooks())
    {
        if (state.status() != static_cast<int>(echidna::state::InternalStatus::kDisabled))
        {
            state.setStatus(echidna::state::InternalStatus::kError);
        }
    }
}

#include "echidna/api.h"
#include <fcntl.h>
#include <unistd.h>

class EchidnaZygiskModule : public zygisk::ModuleBase {
public:
    void onLoad(zygisk::Api *api, JNIEnv *env) override {
        this->api = api;
        this->env = env;
    }

    void preAppSpecialize(zygisk::AppSpecializeArgs *args) override {
        preset_json = R"({
            "id": "my_voice", 
            "name": "Female", 
            "engine": {"latencyMode": "Balanced", "blockMs": 20}, 
            "modules": [
                {"id": "pitch", "enabled": true, "shiftCents": 300}, 
                {"id": "formant", "enabled": true, "shiftCents": 200, "width": 1.0}, 
                {"id": "mix", "wet": 100.0, "outGain": 0.0}
            ]
        })";
    }

    void postAppSpecialize(const zygisk::AppSpecializeArgs *args) override {
        echidna_module_attach();
        if (!preset_json.empty()) {
            echidna_set_profile(preset_json.c_str(), preset_json.length());
        }
    }

private:
    zygisk::Api *api;
    JNIEnv *env;
    std::string preset_json;
};

REGISTER_ZYGISK_MODULE(EchidnaZygiskModule)
