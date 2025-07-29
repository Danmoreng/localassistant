#include <jni.h>
#include <string>
#include <vector>
#include <android/log.h>
#include <thread>
#include "llama.h"
#include "common.h"

#define TAG "LlamaJni"

// Global state
static llama_model *g_model = nullptr;
static llama_context *g_ctx = nullptr;
static llama_sampler *g_sampler = nullptr;
static int32_t g_n_past = 0;

// Log callback
void llama_log_callback(ggml_log_level level, const char * text, void * user_data) {
    switch (level) {
        case GGML_LOG_LEVEL_ERROR: __android_log_print(ANDROID_LOG_ERROR, "llama.cpp", "%s", text); break;
        case GGML_LOG_LEVEL_WARN:  __android_log_print(ANDROID_LOG_WARN,  "llama.cpp", "%s", text); break;
        case GGML_LOG_LEVEL_INFO:  __android_log_print(ANDROID_LOG_INFO,  "llama.cpp", "%s", text); break;
        case GGML_LOG_LEVEL_DEBUG: __android_log_print(ANDROID_LOG_DEBUG, "llama.cpp", "%s", text); break;
        default: break;
    }
}

// JNI init
extern "C" JNIEXPORT void JNICALL
Java_com_example_localassistant_llamacpp_Llama_init(JNIEnv *env, jobject thiz) {
    llama_log_set(llama_log_callback, nullptr);
    llama_backend_init();
}

// Create context
extern "C" JNIEXPORT jlong JNICALL
Java_com_example_localassistant_llamacpp_Llama_newContext(JNIEnv *env, jobject thiz, jstring model_path) {
    // Clean up previous instances
    if (g_sampler) { llama_sampler_free(g_sampler); g_sampler = nullptr; }
    if (g_ctx) { llama_free(g_ctx); g_ctx = nullptr; }
    if (g_model) { llama_model_free(g_model); g_model = nullptr; }

    const char *path = env->GetStringUTFChars(model_path, 0);
    llama_model_params model_params = llama_model_default_params();
    g_model = llama_model_load_from_file(path, model_params);
    env->ReleaseStringUTFChars(model_path, path);

    if (!g_model) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "Failed to load model");
        return 0;
    }

    llama_context_params ctx_params = llama_context_default_params();
    ctx_params.n_ctx = 2048;
    ctx_params.n_threads = std::max(1u, std::thread::hardware_concurrency() - 2);
    ctx_params.n_threads_batch = ctx_params.n_threads;
    g_ctx = llama_init_from_model(g_model, ctx_params);

    if (!g_ctx) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "Failed to create context");
        llama_model_free(g_model);
        g_model = nullptr;
        return 0;
    }
    
    // Create a simple greedy sampler
    g_sampler = llama_sampler_init_greedy();

    g_n_past = 0;
    return reinterpret_cast<jlong>(g_ctx);
}

// Free context
extern "C" JNIEXPORT void JNICALL
Java_com_example_localassistant_llamacpp_Llama_freeContext(JNIEnv *env, jobject thiz, jlong context_ptr) {
    if (g_sampler) { llama_sampler_free(g_sampler); g_sampler = nullptr; }
    if (g_ctx) { llama_free(g_ctx); g_ctx = nullptr; }
    if (g_model) { llama_model_free(g_model); g_model = nullptr; }
    llama_backend_free();
}

// Tokenize
extern "C" JNIEXPORT jintArray JNICALL
Java_com_example_localassistant_llamacpp_Llama_tokenize(JNIEnv *env, jobject thiz, jstring text, jboolean add_bos) {
    if (!g_model) return env->NewIntArray(0);
    auto vocab = llama_model_get_vocab(g_model);

    const char *text_chars = env->GetStringUTFChars(text, 0);
    int n_text = strlen(text_chars);
    
    std::vector<llama_token> tokens(n_text + (add_bos ? 1 : 0));
    int n_tokens = llama_tokenize(vocab, text_chars, n_text, tokens.data(), tokens.size(), add_bos, false);
    
    env->ReleaseStringUTFChars(text, text_chars);

    if (n_tokens < 0) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "Tokenization failed.");
        return env->NewIntArray(0);
    }

    jintArray result = env->NewIntArray(n_tokens);
    env->SetIntArrayRegion(result, 0, n_tokens, tokens.data());
    return result;
}

// Evaluate
extern "C" JNIEXPORT jint JNICALL
Java_com_example_localassistant_llamacpp_Llama_evalTokens(JNIEnv *env, jobject thiz, jlong context_ptr, jintArray token_ids) {
    if (!g_ctx) return -1;
    auto context = reinterpret_cast<llama_context *>(context_ptr);

    jint *tokens = env->GetIntArrayElements(token_ids, nullptr);
    int n_tokens = env->GetArrayLength(token_ids);

    llama_batch batch = llama_batch_init(n_tokens, 0, 1);
    for (int i = 0; i < n_tokens; ++i) {
        batch.token[i]    = tokens[i];
        batch.pos[i]      = g_n_past + i;
        batch.n_seq_id[i] = 1;
        batch.seq_id[i][0] = 0;
        batch.logits[i]   = false;
    }
    batch.logits[batch.n_tokens - 1] = true;

    if (llama_decode(context, batch) != 0) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "llama_decode failed");
        llama_batch_free(batch);
        return -1;
    }
    
    g_n_past += n_tokens;
    llama_batch_free(batch);
    env->ReleaseIntArrayElements(token_ids, tokens, JNI_ABORT);
    return 0;
}

// Sample
extern "C" JNIEXPORT jint JNICALL
Java_com_example_localassistant_llamacpp_Llama_sample(JNIEnv *env, jobject thiz, jlong context_ptr) {
    if (!g_ctx || !g_sampler) return -1;
    return llama_sampler_sample(g_sampler, g_ctx, -1);
}

// Token to Piece
extern "C" JNIEXPORT jstring JNICALL
Java_com_example_localassistant_llamacpp_Llama_tokenToPiece(JNIEnv *env, jobject thiz, jlong context_ptr, jint token_id) {
    if (!g_model) return env->NewStringUTF("");
    auto vocab = llama_model_get_vocab(g_model);

    std::vector<char> result(8, 0);
    const int n_chars = llama_token_to_piece(vocab, token_id, result.data(), result.size(), 0, false);
    if (n_chars < 0) {
        result.resize(-n_chars);
        llama_token_to_piece(vocab, token_id, result.data(), result.size(), 0, false);
    } else {
        result.resize(n_chars);
    }
    return env->NewStringUTF(std::string(result.begin(), result.end()).c_str());
}

// Token EOS
extern "C" JNIEXPORT jint JNICALL
Java_com_example_localassistant_llamacpp_Llama_tokenEOS(JNIEnv *env, jobject thiz) {
    if (!g_model) return -1;
    return llama_vocab_eos(llama_model_get_vocab(g_model));
}