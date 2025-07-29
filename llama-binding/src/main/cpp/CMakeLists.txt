cmake_minimum_required(VERSION 3.18)

project(llamacpp_jni CXX)

add_subdirectory(${PROJECT_SOURCE_DIR}/../../../../llama_cpp libllama EXCLUDE_FROM_ALL)

add_library(llamacpp_jni SHARED llamacpp_bridge.cpp)

find_library(log-lib log)

# Add the common directory to the include path
target_include_directories(llamacpp_jni PRIVATE ${PROJECT_SOURCE_DIR}/../../../../llama_cpp/common)

target_link_libraries(llamacpp_jni
    PRIVATE
        llama
        ${log-lib}
)
