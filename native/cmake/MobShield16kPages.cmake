# Google Play / Android 15+ require native libraries to support 16 KB memory pages.
# See https://developer.android.com/guide/practices/page-sizes
function(mobshield_enable_16k_pages target_name)
    if(NOT ANDROID)
        return()
    endif()
    target_link_options(${target_name} PRIVATE
        "-Wl,-z,max-page-size=16384"
        "-Wl,-z,common-page-size=16384"
    )
endfunction()
