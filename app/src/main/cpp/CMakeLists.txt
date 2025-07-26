cmake_minimum_required(VERSION 3.18.1)
project("MyTapGameServer")

set(CMAKE_CXX_STANDARD 17)
add_compile_options(-Werror=format -fdata-sections -ffunction-sections -fno-exceptions -fno-rtti -fno-threadsafe-statics)

# Находим системную библиотеку логирования
find_library(log-lib log)

# Цель для tap_starter
add_executable(tap_starter
        starter.cpp
        misc.cpp
        selinux.cpp
        cgroup.cpp
        android.cpp
        adb_pairing.cpp)
# --- ФИНАЛЬНОЕ ИСПРАВЛЕНИЕ ---
# Связываем его только с системным логгером. libc++ подключится автоматически.
target_link_libraries(tap_starter PRIVATE ${log-lib})
