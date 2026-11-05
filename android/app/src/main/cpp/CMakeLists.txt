cmake_minimum_required(VERSION 3.25)
project(cybertron_ppc_runtime LANGUAGES C CXX ASM)

add_library(ppc_runtime SHARED
    ppc_runtime.cpp
    ppc_core.cpp
)

target_compile_features(ppc_runtime PRIVATE cxx_std_23)
target_compile_options(ppc_runtime PRIVATE -Wall -Wextra -Wpedantic)
