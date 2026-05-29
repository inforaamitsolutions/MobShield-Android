cmake_minimum_required(VERSION 3.22.1)
project(mobshieldemu)

add_library(mobshieldemu SHARED
    emu_jni.cpp
    qemu_props.cpp
    qemu_devices.cpp
    cpu_features.cpp
)

target_include_directories(mobshieldemu PRIVATE ${CMAKE_CURRENT_SOURCE_DIR})
target_compile_features(mobshieldemu PRIVATE cxx_std_17)

find_library(log-lib log)
target_link_libraries(mobshieldemu ${log-lib})

include(${CMAKE_CURRENT_LIST_DIR}/../../../../native/cmake/MobShield16kPages.cmake)
mobshield_enable_16k_pages(mobshieldemu)
