cmake_minimum_required(VERSION 3.22.1)
project(mobshielddebug)

add_library(mobshielddebug SHARED
    debug_jni.cpp
    tracerpid_check.cpp
    ptrace_self.cpp
    native_debugger_timing.cpp
)

target_include_directories(mobshielddebug PRIVATE ${CMAKE_CURRENT_SOURCE_DIR})
target_compile_features(mobshielddebug PRIVATE cxx_std_17)

find_library(log-lib log)
target_link_libraries(mobshielddebug ${log-lib})
