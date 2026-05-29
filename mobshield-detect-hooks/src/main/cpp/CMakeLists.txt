cmake_minimum_required(VERSION 3.22.1)
project(mobshieldhooks)

set(MOBSHIELD_HOOKS_GENERATED_INCLUDE_DIR "" CACHE STRING "Generated hooks headers")
if (MOBSHIELD_HOOKS_GENERATED_INCLUDE_DIR STREQUAL "")
    set(MOBSHIELD_HOOKS_GENERATED_INCLUDE_DIR "${CMAKE_CURRENT_SOURCE_DIR}")
endif()

add_library(mobshieldhooks SHARED
    hooks_jni.cpp
    proc_maps_scan.cpp
    function_prologue_inspect.cpp
    frida_port_probe.cpp
    thread_name_scan.cpp
    art_inspection.cpp
)

target_include_directories(mobshieldhooks PRIVATE
    ${CMAKE_CURRENT_SOURCE_DIR}
    ${MOBSHIELD_HOOKS_GENERATED_INCLUDE_DIR}
)

target_compile_features(mobshieldhooks PRIVATE cxx_std_17)

find_library(log-lib log)
target_link_libraries(mobshieldhooks ${log-lib} dl)
