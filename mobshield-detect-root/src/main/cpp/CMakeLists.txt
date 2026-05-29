cmake_minimum_required(VERSION 3.22.1)
project(mobshieldroot)

add_library(mobshieldroot SHARED
    root_jni.cpp
    mount_namespace_check.cpp
    magisk_uds_probe.cpp
    overlayfs_check.cpp
    errno_deviation.cpp
    zygisk_loader_scan.cpp
    kernelsu_check.cpp
    system_property_read.cpp
)

target_include_directories(mobshieldroot PRIVATE ${CMAKE_CURRENT_SOURCE_DIR})
target_compile_features(mobshieldroot PRIVATE cxx_std_17)

find_library(log-lib log)
target_link_libraries(mobshieldroot ${log-lib})

include(${CMAKE_CURRENT_LIST_DIR}/../../../../native/cmake/MobShield16kPages.cmake)
mobshield_enable_16k_pages(mobshieldroot)
