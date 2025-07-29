cmake_minimum_required(VERSION 3.22.1)

project(rish LANGUAGES C CXX)

set(CMAKE_CXX_STANDARD 17)

set(C_FLAGS "-Werror=format -fdata-sections -ffunction-sections -fno-exceptions -fno-rtti -fno-threadsafe-statics")
set(LINKER_FLAGS "-Wl,--hash-style=both")

if (NOT CMAKE_BUILD_TYPE STREQUAL "Debug")
    message("Builing Release...")
    set(C_FLAGS "${C_FLAGS} -O2 -fvisibility=hidden -fvisibility-inlines-hidden")
    set(LINKER_FLAGS "${LINKER_FLAGS} -Wl,-exclude-libs,ALL -Wl,--gc-sections")
else ()
    message("Builing Debug...")
    add_definitions(-DDEBUG)
endif ()

set(CMAKE_C_FLAGS "${CMAKE_C_FLAGS} ${C_FLAGS}")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${C_FLAGS}")
set(CMAKE_SHARED_LINKER_FLAGS "${CMAKE_SHARED_LINKER_FLAGS} ${LINKER_FLAGS}")
set(CMAKE_MODULE_LINKER_FLAGS "${CMAKE_MODULE_LINKER_FLAGS} ${LINKER_FLAGS}")

list(APPEND CMAKE_PREFIX_PATH
    "${CMAKE_BINARY_DIR}/install"
    "${CMAKE_INSTALL_PREFIX}"
)
set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} "${CMAKE_SOURCE_DIR}/cmake")


include_directories(.)

add_executable(rish_starter
    starter.cpp
    misc.cpp
    selinux.cpp
    cgroup.cpp
    android.cpp
    helper.cpp
)
target_link_libraries(rish_starter PRIVATE log binder)

add_library(rish SHARED
        main.cpp
        pts.cpp
        rikka_rish_RishTerminal.cpp
        rikka_rish_RishHost.cpp)

target_link_libraries(rish log)

if (NOT CMAKE_BUILD_TYPE STREQUAL "Debug")
    add_custom_command(TARGET rish POST_BUILD
            COMMAND ${CMAKE_STRIP} --remove-section=.comment "${CMAKE_LIBRARY_OUTPUT_DIRECTORY}/librish.so")
endif ()
