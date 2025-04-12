#include <jni.h>
#include <string>
#include <iostream>

#include "llama.cpp/llama.h"
// Placeholder for llama.cpp includes and functionality
// You will need to replace these with actual llama.cpp includes and code.
// #include "llama.h" 

extern "C" {

struct ModelContext {
    llama_model *model;
    llama_context *ctx;
};

JNIEXPORT jlong JNICALL
Java_com_example_localassistant_inference_Phi4LlamaCppInference_loadModel(JNIEnv *env, jobject thiz, jstring modelPath) {
    const char *modelPathStr = env->GetStringUTFChars(modelPath, nullptr);
    if (modelPathStr == nullptr) {
        return 0; // Error handling: Return 0 to indicate failure
    }

    llama_model_params model_params = llama_model_default_params();
    llama_model *model = llama_load_model_from_file(modelPathStr, model_params);
    env->ReleaseStringUTFChars(modelPath, modelPathStr);
    if (model == nullptr) {
        return 0;
    }
    llama_context_params ctx_params = llama_context_default_params();
    ctx_params.seed = 1234;
    ctx_params.n_ctx = 2048;
    llama_context *ctx = llama_new_context_with_model(model, ctx_params);    
    if (ctx == nullptr) {
        llama_free_model(model);
        return 0;
    }
    
    ModelContext *model_context = new ModelContext;
    model_context->model = model;
    model_context->ctx = ctx;

    return (jlong) model_context;
}


JNIEXPORT jstring JNICALL
Java_com_example_localassistant_inference_Phi4LlamaCppInference_runInference(JNIEnv *env, jobject thiz, jlong modelContext, jstring prompt) {
    ModelContext *model_context_ptr = reinterpret_cast<ModelContext *>(modelContext);
    if (model_context_ptr == nullptr || model_context_ptr->ctx == nullptr) {
        return env->NewStringUTF("");
    }

    llama_context *ctx = model_context_ptr->ctx;

    const char *user_prompt = env->GetStringUTFChars(prompt, nullptr);
    if (user_prompt == nullptr) return env->NewStringUTF("");

    std::vector<llama_token> tokens = llama_tokenize(ctx, user_prompt, false, true);
    env->ReleaseStringUTFChars(prompt, user_prompt);

    if (tokens.empty()) return env->NewStringUTF("");

    llama_eval(ctx, tokens.data(), tokens.size(), 0, 1);

    std::string result;
    const int max_tokens = 128;
    for (int i = 0; i < max_tokens; ++i) {
        llama_token token = llama_sample_token(ctx, nullptr);
        if (token == llama_token_eos(ctx)) break;
        result += llama_detokenize(ctx, &token, 1);
        llama_eval(ctx, &token, 1, tokens.size() + i, 1);
    }

    return env->NewStringUTF(result.c_str());
}

JNIEXPORT void JNICALL
Java_com_example_localassistant_inference_Phi4LlamaCppInference_unloadModel(JNIEnv *env, jobject thiz, jlong modelContext) {
    ModelContext *model_context_ptr = reinterpret_cast<ModelContext *>(modelContext);
    if (model_context_ptr != nullptr) {
        if (model_context_ptr->ctx != nullptr) {
            llama_free(model_context_ptr->ctx);
        }
        if (model_context_ptr->model != nullptr) {
            llama_free_model(model_context_ptr->model);
        }
        delete model_context_ptr;
    } else {
        std::cerr << "Error: Invalid model context in unloadModel" << std::endl;
        // You might want to throw a Java exception here for better error handling.
    }
}

}