cmake_minimum_required(VERSION 3.22)

project(echidna-zygisk LANGUAGES C CXX)

set(CMAKE_CXX_STANDARD 20)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_POSITION_INDEPENDENT_CODE ON)

# Configure Android ABIs when building via Gradle/ndk-build. When the generator
# does not expose ANDROID_ABI we fall back to building a generic host binary so
# the code can still be compiled in CI environments.
if(NOT ANDROID)
  message(WARNING "Building zygisk module without Android toolchain; using host platform for smoke testing.")
endif()

set(ECHIDNA_DSP_SOURCES
    ../dsp/src\n        /../dsp/src/api.cpp
    ../dsp/src\n        /../dsp/src/engine.cpp
    ../dsp/src\n        /../dsp/src/config/preset_loader.cpp
    ../dsp/src\n        /../dsp/src/runtime/block_queue.cpp
    ../dsp/src\n        /../dsp/src/runtime/simd.cpp
    ../dsp/src\n        /../dsp/src/effects/effect_base.cpp
    ../dsp/src\n        /../dsp/src/effects/gate_processor.cpp
    ../dsp/src\n        /../dsp/src/effects/parametric_eq.cpp
    ../dsp/src\n        /../dsp/src/effects/compressor.cpp
    ../dsp/src\n        /../dsp/src/effects/pitch_shifter.cpp
    ../dsp/src\n        /../dsp/src/effects/formant_shifter.cpp
    ../dsp/src\n        /../dsp/src/effects/auto_tune.cpp
    ../dsp/src\n        /../dsp/src/effects/reverb.cpp
    ../dsp/src\n        /../dsp/src/effects/mix_bus.cpp
    ../dsp/src\n        /../dsp/src/plugins/plugin_loader.cpp)

add_library(echidna SHARED
    ${ECHIDNA_DSP_SOURCES}
    src/api.cpp
    src/module.cpp
    src/state/shared_state.cpp
    src/runtime/inline_hook.cpp
    src/hooks/hook_manager.cpp
    src/hooks/aaudio_hook_manager.cpp
    src/hooks/opensl_hook_manager.cpp
    src/hooks/audiorecord_hook_manager.cpp
    src/hooks/audioflinger_hook_manager.cpp
    src/hooks/libc_read_hook_manager.cpp
    src/hooks/tinyalsa_hook_manager.cpp
    src/hooks/audiohal_hook_manager.cpp
    src/hooks/audio_hook_orchestrator.cpp
    src/utils/proc_maps_scanner.cpp
    src/utils/plt_resolver.cpp
    src/utils/api_level_probe.cpp
    src/utils/offset_probe.cpp
    src/utils/config_shared_memory.cpp
    src/utils/telemetry_shared_memory.cpp
    src/utils/process_utils.cpp
    src/jni/audio_bridge.cpp
    src/runtime/profile_sync_server.cpp)

set_target_properties(echidna PROPERTIES
    OUTPUT_NAME echidna
    LIBRARY_OUTPUT_DIRECTORY ${CMAKE_BINARY_DIR}/lib)

target_include_directories(echidna
    PUBLIC
        $<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}/include>
        $<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}/../include>
        $<BUILD_INTERFACE:${CMAKE_CURRENT_SOURCE_DIR}/../dsp/include>
        $<INSTALL_INTERFACE:include>
    PRIVATE
        ${CMAKE_CURRENT_SOURCE_DIR}/src\n        /../dsp/src)

find_library(LOG_LIB log)
find_library(ANDROID_LIB android)
if(LOG_LIB)
    target_link_libraries(echidna PRIVATE ${LOG_LIB} ${ANDROID_LIB})
endif()

install(TARGETS echidna
    LIBRARY DESTINATION lib)
install(DIRECTORY include/ DESTINATION include)
