cmake
cmake_minimum_required(VERSION 3.18)
project(LocalAssistantLlamaCpp)

set(CMAKE_CXX_STANDARD 17)

# Option 1: llama.cpp as a subdirectory (source)
# add_subdirectory(llama.cpp) 
# set(LLAMACPP_LIB llama)

# Option 2: llama.cpp as a pre-built library
# Replace with the actual path to your pre-built llama.cpp library
# set(LLAMACPP_LIB /path/to/your/llama.cpp/libllama.a)  

find_path(JNI_INCLUDE jni.h REQUIRED)

add_library(llamacpp_jni SHARED
    llama.cpp_wrapper.cpp
)

target_include_directories(llamacpp_jni PRIVATE
    ${JNI_INCLUDE}
    # Add any other necessary include directories for llama.cpp here
)

target_link_libraries(llamacpp_jni 
    ${LLAMACPP_LIB}
    # Add other necessary libraries if needed
)