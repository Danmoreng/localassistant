cmake_minimum_required(VERSION 3.22)
project(llamacpp_jni CXX)

# llama.cpp
add_subdirectory(${PROJECT_SOURCE_DIR}/../../../../llama_cpp           libllama   EXCLUDE_FROM_ALL)

add_library(llamacpp_jni SHARED llamacpp_bridge.cpp)

find_library(log-lib log)

target_include_directories(llamacpp_jni PRIVATE
        ${PROJECT_SOURCE_DIR}/../../../../llama_cpp
        ${PROJECT_SOURCE_DIR}/../../../../llama_cpp/common)

target_link_libraries(llamacpp_jni
        PRIVATE
        llama
        ${log-lib})
