cmake_minimum_required(VERSION 3.16)
project(cybertron_ppc_smoke LANGUAGES CXX)

set(CMAKE_CXX_STANDARD 17)
set(CMAKE_CXX_STANDARD_REQUIRED ON)
set(CMAKE_CXX_EXTENSIONS OFF)

add_library(cybertron_ppc STATIC ppc_core.cpp)
target_include_directories(cybertron_ppc PUBLIC ${CMAKE_CURRENT_SOURCE_DIR})

add_executable(ppc_smoke_test ppc_smoke_test.cpp)
target_link_libraries(ppc_smoke_test PRIVATE cybertron_ppc)
