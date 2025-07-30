#include <jni.h>
#include <cstdlib>
#include <string>
#include <vector>
#include <android/log.h>
#include <thread>
#include <mutex>
#include "llama.h"

#define TAG "LlamaJni"

struct llama_context_wrapper {
    llama_model *model = nullptr;
    llama_context *ctx = nullptr;
    llama_sampler *sampler = nullptr;
    int32_t n_past = 0;
};

static std::once_flag g_init_flag;

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

void init_llama() {
    llama_log_set(llama_log_callback, nullptr);
    llama_backend_init();
}

// Create context
extern "C" JNIEXPORT jlong JNICALL
Java_com_example_localassistant_llamacpp_Llama_newContext(JNIEnv *env, jobject, jstring model_path) {
    std::call_once(g_init_flag, init_llama);
    auto wrapper = new llama_context_wrapper;

    const char *path = env->GetStringUTFChars(model_path, 0);

    llama_model_params mparams = llama_model_default_params();
    wrapper->model = llama_model_load_from_file(path, mparams);
    env->ReleaseStringUTFChars(model_path, path);

    if (!wrapper->model) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "Failed to load model");
        delete wrapper;
        return 0;
    }

    llama_context_params cparams = llama_context_default_params();
    cparams.n_ctx           = 2048;
    cparams.n_threads       = std::max(1u, std::thread::hardware_concurrency() - 2);
    cparams.n_threads_batch = cparams.n_threads;
    wrapper->ctx = llama_init_from_model(wrapper->model, cparams);

    if (!wrapper->ctx) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "Failed to create context");
        llama_model_free(wrapper->model);
        delete wrapper;
        return 0;
    }

    llama_sampler_chain_params sparams = llama_sampler_chain_default_params();
    sparams.no_perf = true;
    wrapper->sampler = llama_sampler_chain_init(sparams);
    llama_sampler_chain_add(wrapper->sampler, llama_sampler_init_greedy());

    return reinterpret_cast<jlong>(wrapper);
}

// Free context
extern "C" JNIEXPORT void JNICALL
Java_com_example_localassistant_llamacpp_Llama_freeContext(JNIEnv *, jobject, jlong context_ptr) {
    auto wrapper = reinterpret_cast<llama_context_wrapper *>(context_ptr);
    if (wrapper->sampler) { llama_sampler_free(wrapper->sampler); }
    if (wrapper->ctx) { llama_free(wrapper->ctx); }
    if (wrapper->model) { llama_model_free(wrapper->model); }
    delete wrapper;
}

extern "C" JNIEXPORT void JNICALL
Java_com_example_localassistant_llamacpp_Llama_clearContext(JNIEnv *env, jobject, jlong context_ptr) {
    auto wrapper = reinterpret_cast<llama_context_wrapper *>(context_ptr);
    if (wrapper && wrapper->ctx) {
        llama_kv_self_clear(wrapper->ctx);
        wrapper->n_past = 0;
    }
}

// Tokenize
extern "C" JNIEXPORT jintArray JNICALL
Java_com_example_localassistant_llamacpp_Llama_tokenize(JNIEnv *env, jobject, jlong ctx_ptr,
                                                        jstring text, jboolean add_bos) {
    auto wrapper = reinterpret_cast<llama_context_wrapper *>(ctx_ptr);
    auto ctx = wrapper->ctx;
    const char *chars = env->GetStringUTFChars(text, 0);
    const int  len    = strlen(chars);
    auto model = llama_get_model(ctx);
    auto vocab = llama_model_get_vocab(model);

    std::vector<llama_token> tmp(len + 4);
    int n = llama_tokenize(vocab,
                           chars,
                           len,
                           tmp.data(),
                           tmp.size(),
                           add_bos == JNI_TRUE,
                           false);

    env->ReleaseStringUTFChars(text, chars);
    if (n < 0) {
        __android_log_print(ANDROID_LOG_ERROR, TAG, "Tokenization failed.");
        return env->NewIntArray(0);
    }

    jintArray result = env->NewIntArray(n);
    env->SetIntArrayRegion(result, 0, n, reinterpret_cast<jint*>(tmp.data()));
    return result;
}

// Evaluate
extern "C" JNIEXPORT jint JNICALL
Java_com_example_localassistant_llamacpp_Llama_evalTokens(
        JNIEnv *env, jobject, jlong ctx_ptr, jintArray token_ids) {
    auto wrapper = reinterpret_cast<llama_context_wrapper *>(ctx_ptr);
    auto ctx = wrapper->ctx;
    if (!ctx) return -1;

    const int n = env->GetArrayLength(token_ids);
    std::vector<llama_token> tok(n);
    env->GetIntArrayRegion(token_ids, 0, n, reinterpret_cast<jint*>(tok.data()));

    llama_batch batch = llama_batch_init(n, 0, 1);

    for (int i = 0; i < n; ++i) {
        batch.token[i]     = tok[i];
        batch.pos[i]       = wrapper->n_past + i;
        batch.n_seq_id[i]  = 1;
        batch.seq_id[i][0] = 0;
        batch.logits[i]    = false;
    }
    batch.n_tokens  = n;
    batch.logits[n-1] = true;

    int rc = llama_decode(ctx, batch);

    wrapper->n_past += n;
    llama_batch_free(batch);
    return rc;
}

// Sample
extern "C" JNIEXPORT jint JNICALL
Java_com_example_localassistant_llamacpp_Llama_sample(JNIEnv *, jobject, jlong ctx_ptr) {
    auto wrapper = reinterpret_cast<llama_context_wrapper *>(ctx_ptr);
    if (!wrapper->ctx || !wrapper->sampler) return -1;
    const int32_t id = llama_sampler_sample(wrapper->sampler, wrapper->ctx, -1);
    return id < 0 ? -1 : id;
}

extern "C" JNIEXPORT jbyteArray JNICALL
Java_com_example_localassistant_llamacpp_Llama_tokenToPiece(JNIEnv *env, jobject,
                                                            jlong ctx_ptr, jint token) {
    auto wrapper = reinterpret_cast<llama_context_wrapper *>(ctx_ptr);
    auto ctx   = wrapper->ctx;
    if (!ctx) {
        return env->NewByteArray(0);
    }

    auto model = llama_get_model(ctx);
    auto vocab = llama_model_get_vocab(model);

    int n = llama_token_to_piece(vocab, token, nullptr, 0, false, false);
    if (n >= 0) {
        return env->NewByteArray(0);
    }

    int required_size = -n;
    std::vector<char> out(required_size);

    int written_size = llama_token_to_piece(vocab, token, out.data(), required_size, false, false);
    if (written_size < 0) {
        return env->NewByteArray(0);
    }

    jbyteArray result = env->NewByteArray(written_size);
    env->SetByteArrayRegion(result, 0, written_size, reinterpret_cast<jbyte*>(out.data()));
    return result;
}

// Token EOS
extern "C" JNIEXPORT jint JNICALL
Java_com_example_localassistant_llamacpp_Llama_tokenEOS(JNIEnv *, jobject, jlong ctx_ptr) {
    auto wrapper = reinterpret_cast<llama_context_wrapper *>(ctx_ptr);
    if (!wrapper->ctx) return -1;
    auto model = llama_get_model(wrapper->ctx);
    auto vocab = llama_model_get_vocab(model);
    return llama_vocab_eos(vocab);
}