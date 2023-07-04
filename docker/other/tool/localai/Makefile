GOCMD=go
GOTEST=$(GOCMD) test
GOVET=$(GOCMD) vet
BINARY_NAME=local-ai

GOLLAMA_VERSION?=9470597ae75ad8b5f17cfab73805ee4a6685d199
GPT4ALL_REPO?=https://github.com/nomic-ai/gpt4all
GPT4ALL_VERSION?=3417a37c5472fb5111a7bd0ed747c8df749c595e
GOGGMLTRANSFORMERS_VERSION?=a459d2726792132541152c981ed9fbfe28f4fd20
RWKV_REPO?=https://github.com/donomii/go-rwkv.cpp
RWKV_VERSION?=f5a8c45396741470583f59b916a2a7641e63bcd0
WHISPER_CPP_VERSION?=3f7a03ebe3b65be0792849e300a122f6a050e3f8
BERT_VERSION?=6069103f54b9969c02e789d0fb12a23bd614285f
PIPER_VERSION?=56b8a81b4760a6fbee1a82e62f007ae7e8f010a7
BLOOMZ_VERSION?=1834e77b83faafe912ad4092ccf7f77937349e2f
export BUILD_TYPE?=
CGO_LDFLAGS?=
CUDA_LIBPATH?=/usr/local/cuda/lib64/
STABLEDIFFUSION_VERSION?=d89260f598afb809279bc72aa0107b4292587632
GO_TAGS?=
BUILD_ID?=git

VERSION?=$(shell git describe --always --tags --dirty || echo "dev" )
# go tool nm ./local-ai | grep Commit
LD_FLAGS?=
override LD_FLAGS += -X "github.com/go-skynet/LocalAI/internal.Version=$(VERSION)"
override LD_FLAGS += -X "github.com/go-skynet/LocalAI/internal.Commit=$(shell git rev-parse HEAD)"

OPTIONAL_TARGETS?=
ESPEAK_DATA?=

OS := $(shell uname -s)
ARCH := $(shell uname -m)
GREEN  := $(shell tput -Txterm setaf 2)
YELLOW := $(shell tput -Txterm setaf 3)
WHITE  := $(shell tput -Txterm setaf 7)
CYAN   := $(shell tput -Txterm setaf 6)
RESET  := $(shell tput -Txterm sgr0)

C_INCLUDE_PATH=$(shell pwd)/go-llama:$(shell pwd)/go-stable-diffusion/:$(shell pwd)/gpt4all/gpt4all-bindings/golang/:$(shell pwd)/go-ggml-transformers:$(shell pwd)/go-rwkv:$(shell pwd)/whisper.cpp:$(shell pwd)/go-bert:$(shell pwd)/bloomz
LIBRARY_PATH=$(shell pwd)/go-piper:$(shell pwd)/go-llama:$(shell pwd)/go-stable-diffusion/:$(shell pwd)/gpt4all/gpt4all-bindings/golang/:$(shell pwd)/go-ggml-transformers:$(shell pwd)/go-rwkv:$(shell pwd)/whisper.cpp:$(shell pwd)/go-bert:$(shell pwd)/bloomz

ifeq ($(BUILD_TYPE),openblas)
	CGO_LDFLAGS+=-lopenblas
endif

ifeq ($(BUILD_TYPE),cublas)
	CGO_LDFLAGS+=-lcublas -lcudart -L$(CUDA_LIBPATH)
	export LLAMA_CUBLAS=1
endif

ifeq ($(BUILD_TYPE),metal)
	CGO_LDFLAGS+=-framework Foundation -framework Metal -framework MetalKit -framework MetalPerformanceShaders
	export LLAMA_METAL=1
endif

ifeq ($(BUILD_TYPE),clblas)
	CGO_LDFLAGS+=-lOpenCL -lclblast
endif

# glibc-static or glibc-devel-static required
ifeq ($(STATIC),true)
	LD_FLAGS=-linkmode external -extldflags -static
endif

ifeq ($(findstring stablediffusion,$(GO_TAGS)),stablediffusion)
	OPTIONAL_TARGETS+=go-stable-diffusion/libstablediffusion.a
endif

ifeq ($(findstring tts,$(GO_TAGS)),tts)
	OPTIONAL_TARGETS+=go-piper/libpiper_binding.a
	OPTIONAL_TARGETS+=backend-assets/espeak-ng-data
endif

.PHONY: all test build vendor

all: help

## GPT4ALL
gpt4all:
	git clone --recurse-submodules $(GPT4ALL_REPO) gpt4all
	cd gpt4all && git checkout -b build $(GPT4ALL_VERSION) && git submodule update --init --recursive --depth 1
	# This is hackish, but needed as both go-llama and go-gpt4allj have their own version of ggml..
	@find ./gpt4all -type f -name "*.c" -exec sed -i'' -e 's/ggml_/ggml_gpt4all_/g' {} +
	@find ./gpt4all -type f -name "*.cpp" -exec sed -i'' -e 's/ggml_/ggml_gpt4all_/g' {} +
	@find ./gpt4all -type f -name "*.m" -exec sed -i'' -e 's/ggml_/ggml_gpt4all_/g' {} +
	@find ./gpt4all -type f -name "*.h" -exec sed -i'' -e 's/ggml_/ggml_gpt4all_/g' {} +
	@find ./gpt4all -type f -name "*.c" -exec sed -i'' -e 's/llama_/llama_gpt4all_/g' {} +
	@find ./gpt4all -type f -name "*.cpp" -exec sed -i'' -e 's/llama_/llama_gpt4all_/g' {} +
	@find ./gpt4all -type f -name "*.h" -exec sed -i'' -e 's/llama_/llama_gpt4all_/g' {} +
	@find ./gpt4all/gpt4all-backend -type f -name "llama_util.h" -execdir mv {} "llama_gpt4all_util.h" \;
	@find ./gpt4all -type f -name "*.cmake" -exec sed -i'' -e 's/llama_util/llama_gpt4all_util/g' {} +
	@find ./gpt4all -type f -name "*.txt" -exec sed -i'' -e 's/llama_util/llama_gpt4all_util/g' {} +
	@find ./gpt4all/gpt4all-bindings/golang -type f -name "*.cpp" -exec sed -i'' -e 's/load_model/load_gpt4all_model/g' {} +
	@find ./gpt4all/gpt4all-bindings/golang -type f -name "*.go" -exec sed -i'' -e 's/load_model/load_gpt4all_model/g' {} +
	@find ./gpt4all/gpt4all-bindings/golang -type f -name "*.h" -exec sed -i'' -e 's/load_model/load_gpt4all_model/g' {} +

## go-piper
go-piper:
	git clone --recurse-submodules https://github.com/mudler/go-piper go-piper
	cd go-piper && git checkout -b build $(PIPER_VERSION) && git submodule update --init --recursive --depth 1

## BERT embeddings
go-bert:
	git clone --recurse-submodules https://github.com/go-skynet/go-bert.cpp go-bert
	cd go-bert && git checkout -b build $(BERT_VERSION) && git submodule update --init --recursive --depth 1
	@find ./go-bert -type f -name "*.c" -exec sed -i'' -e 's/ggml_/ggml_bert_/g' {} +
	@find ./go-bert -type f -name "*.cpp" -exec sed -i'' -e 's/ggml_/ggml_bert_/g' {} +
	@find ./go-bert -type f -name "*.h" -exec sed -i'' -e 's/ggml_/ggml_bert_/g' {} +

## stable diffusion
go-stable-diffusion:
	git clone --recurse-submodules https://github.com/mudler/go-stable-diffusion go-stable-diffusion
	cd go-stable-diffusion && git checkout -b build $(STABLEDIFFUSION_VERSION) && git submodule update --init --recursive --depth 1

go-stable-diffusion/libstablediffusion.a:
	$(MAKE) -C go-stable-diffusion libstablediffusion.a

## RWKV
go-rwkv:
	git clone --recurse-submodules $(RWKV_REPO) go-rwkv
	cd go-rwkv && git checkout -b build $(RWKV_VERSION) && git submodule update --init --recursive --depth 1
	@find ./go-rwkv -type f -name "*.c" -exec sed -i'' -e 's/ggml_/ggml_rwkv_/g' {} +
	@find ./go-rwkv -type f -name "*.cpp" -exec sed -i'' -e 's/ggml_/ggml_rwkv_/g' {} +
	@find ./go-rwkv -type f -name "*.h" -exec sed -i'' -e 's/ggml_/ggml_rwkv_/g' {} +

go-rwkv/librwkv.a: go-rwkv
	cd go-rwkv && cd rwkv.cpp &&	cmake . -DRWKV_BUILD_SHARED_LIBRARY=OFF &&	cmake --build . && 	cp librwkv.a ..

## bloomz
bloomz:
	git clone --recurse-submodules https://github.com/go-skynet/bloomz.cpp bloomz
	@find ./bloomz -type f -name "*.c" -exec sed -i'' -e 's/ggml_/ggml_bloomz_/g' {} +
	@find ./bloomz -type f -name "*.cpp" -exec sed -i'' -e 's/ggml_/ggml_bloomz_/g' {} +
	@find ./bloomz -type f -name "*.h" -exec sed -i'' -e 's/ggml_/ggml_bloomz_/g' {} +
	@find ./bloomz -type f -name "*.cpp" -exec sed -i'' -e 's/gpt_/gpt_bloomz_/g' {} +
	@find ./bloomz -type f -name "*.h" -exec sed -i'' -e 's/gpt_/gpt_bloomz_/g' {} +
	@find ./bloomz -type f -name "*.cpp" -exec sed -i'' -e 's/void replace/void json_bloomz_replace/g' {} +
	@find ./bloomz -type f -name "*.cpp" -exec sed -i'' -e 's/::replace/::json_bloomz_replace/g' {} +

bloomz/libbloomz.a: bloomz
	cd bloomz && make libbloomz.a

go-bert/libgobert.a: go-bert
	$(MAKE) -C go-bert libgobert.a

backend-assets/gpt4all: gpt4all/gpt4all-bindings/golang/libgpt4all.a
	mkdir -p backend-assets/gpt4all
	@cp gpt4all/gpt4all-bindings/golang/buildllm/*.so backend-assets/gpt4all/ || true
	@cp gpt4all/gpt4all-bindings/golang/buildllm/*.dylib backend-assets/gpt4all/ || true
	@cp gpt4all/gpt4all-bindings/golang/buildllm/*.dll backend-assets/gpt4all/ || true

backend-assets/espeak-ng-data:
	mkdir -p backend-assets/espeak-ng-data
ifdef ESPEAK_DATA
	@cp -rf $(ESPEAK_DATA)/. backend-assets/espeak-ng-data
else
	@touch backend-assets/espeak-ng-data/keep
endif

gpt4all/gpt4all-bindings/golang/libgpt4all.a: gpt4all
	$(MAKE) -C gpt4all/gpt4all-bindings/golang/ libgpt4all.a

## CEREBRAS GPT
go-ggml-transformers:
	git clone --recurse-submodules https://github.com/go-skynet/go-ggml-transformers.cpp go-ggml-transformers
	cd go-ggml-transformers && git checkout -b build $(GOGPT2_VERSION) && git submodule update --init --recursive --depth 1
	# This is hackish, but needed as both go-llama and go-gpt4allj have their own version of ggml..
	@find ./go-ggml-transformers -type f -name "*.c" -exec sed -i'' -e 's/ggml_/ggml_gpt2_/g' {} +
	@find ./go-ggml-transformers -type f -name "*.cpp" -exec sed -i'' -e 's/ggml_/ggml_gpt2_/g' {} +
	@find ./go-ggml-transformers -type f -name "*.h" -exec sed -i'' -e 's/ggml_/ggml_gpt2_/g' {} +
	@find ./go-ggml-transformers -type f -name "*.cpp" -exec sed -i'' -e 's/gpt_print_usage/gpt2_print_usage/g' {} +
	@find ./go-ggml-transformers -type f -name "*.h" -exec sed -i'' -e 's/gpt_print_usage/gpt2_print_usage/g' {} +
	@find ./go-ggml-transformers -type f -name "*.cpp" -exec sed -i'' -e 's/gpt_params_parse/gpt2_params_parse/g' {} +
	@find ./go-ggml-transformers -type f -name "*.h" -exec sed -i'' -e 's/gpt_params_parse/gpt2_params_parse/g' {} +
	@find ./go-ggml-transformers -type f -name "*.cpp" -exec sed -i'' -e 's/gpt_random_prompt/gpt2_random_prompt/g' {} +
	@find ./go-ggml-transformers -type f -name "*.h" -exec sed -i'' -e 's/gpt_random_prompt/gpt2_random_prompt/g' {} +
	@find ./go-ggml-transformers -type f -name "*.cpp" -exec sed -i'' -e 's/json_/json_gpt2_/g' {} +

go-ggml-transformers/libtransformers.a: go-ggml-transformers
	$(MAKE) -C go-ggml-transformers libtransformers.a

whisper.cpp:
	git clone https://github.com/ggerganov/whisper.cpp.git
	cd whisper.cpp && git checkout -b build $(WHISPER_CPP_VERSION) && git submodule update --init --recursive --depth 1
	@find ./whisper.cpp -type f -name "*.c" -exec sed -i'' -e 's/ggml_/ggml_whisper_/g' {} +
	@find ./whisper.cpp -type f -name "*.cpp" -exec sed -i'' -e 's/ggml_/ggml_whisper_/g' {} +
	@find ./whisper.cpp -type f -name "*.h" -exec sed -i'' -e 's/ggml_/ggml_whisper_/g' {} +

whisper.cpp/libwhisper.a: whisper.cpp
	cd whisper.cpp && make libwhisper.a

go-llama:
	git clone --recurse-submodules https://github.com/go-skynet/go-llama.cpp go-llama
	cd go-llama && git checkout -b build $(GOLLAMA_VERSION) && git submodule update --init --recursive --depth 1

go-llama/libbinding.a: go-llama
	$(MAKE) -C go-llama BUILD_TYPE=$(BUILD_TYPE) libbinding.a

go-piper/libpiper_binding.a:
	$(MAKE) -C go-piper libpiper_binding.a example/main

get-sources: go-llama go-ggml-transformers gpt4all go-piper go-rwkv whisper.cpp go-bert bloomz go-stable-diffusion
	touch $@

replace:
	$(GOCMD) mod edit -replace github.com/go-skynet/go-llama.cpp=$(shell pwd)/go-llama
	$(GOCMD) mod edit -replace github.com/nomic-ai/gpt4all/gpt4all-bindings/golang=$(shell pwd)/gpt4all/gpt4all-bindings/golang
	$(GOCMD) mod edit -replace github.com/go-skynet/go-ggml-transformers.cpp=$(shell pwd)/go-ggml-transformers
	$(GOCMD) mod edit -replace github.com/donomii/go-rwkv.cpp=$(shell pwd)/go-rwkv
	$(GOCMD) mod edit -replace github.com/ggerganov/whisper.cpp=$(shell pwd)/whisper.cpp
	$(GOCMD) mod edit -replace github.com/go-skynet/go-bert.cpp=$(shell pwd)/go-bert
	$(GOCMD) mod edit -replace github.com/go-skynet/bloomz.cpp=$(shell pwd)/bloomz
	$(GOCMD) mod edit -replace github.com/mudler/go-stable-diffusion=$(shell pwd)/go-stable-diffusion
	$(GOCMD) mod edit -replace github.com/mudler/go-piper=$(shell pwd)/go-piper

prepare-sources: get-sources replace
	$(GOCMD) mod download

## GENERIC
rebuild: ## Rebuilds the project
	$(MAKE) -C go-llama clean
	$(MAKE) -C gpt4all/gpt4all-bindings/golang/ clean
	$(MAKE) -C go-ggml-transformers clean
	$(MAKE) -C go-rwkv clean
	$(MAKE) -C whisper.cpp clean
	$(MAKE) -C go-stable-diffusion clean
	$(MAKE) -C go-bert clean
	$(MAKE) -C bloomz clean
	$(MAKE) -C go-piper clean
	$(MAKE) build

prepare: prepare-sources backend-assets/gpt4all $(OPTIONAL_TARGETS) go-llama/libbinding.a go-bert/libgobert.a go-ggml-transformers/libtransformers.a go-rwkv/librwkv.a whisper.cpp/libwhisper.a bloomz/libbloomz.a  ## Prepares for building
	touch $@

clean: ## Remove build related file
	rm -fr ./go-llama
	rm -rf ./gpt4all
	rm -rf ./go-gpt2
	rm -rf ./go-stable-diffusion
	rm -rf ./go-ggml-transformers
	rm -rf ./backend-assets
	rm -rf ./go-rwkv
	rm -rf ./go-bert
	rm -rf ./bloomz
	rm -rf ./whisper.cpp
	rm -rf ./go-piper
	rm -rf $(BINARY_NAME)
	rm -rf release/

## Build:

build: prepare ## Build the project
	$(info ${GREEN}I local-ai build info:${RESET})
	$(info ${GREEN}I BUILD_TYPE: ${YELLOW}$(BUILD_TYPE)${RESET})
	$(info ${GREEN}I GO_TAGS: ${YELLOW}$(GO_TAGS)${RESET})
	$(info ${GREEN}I LD_FLAGS: ${YELLOW}$(LD_FLAGS)${RESET})

	CGO_LDFLAGS="$(CGO_LDFLAGS)" C_INCLUDE_PATH=${C_INCLUDE_PATH} LIBRARY_PATH=${LIBRARY_PATH} $(GOCMD) build -ldflags "$(LD_FLAGS)" -tags "$(GO_TAGS)" -o $(BINARY_NAME) ./
ifeq ($(BUILD_TYPE),metal)
	cp go-llama/build/bin/ggml-metal.metal .
endif

dist: build
	mkdir -p release
	cp $(BINARY_NAME) release/$(BINARY_NAME)-$(BUILD_ID)-$(OS)-$(ARCH)

generic-build: ## Build the project using generic
	BUILD_TYPE="generic" $(MAKE) build

## Run
run: prepare ## run local-ai
	CGO_LDFLAGS="$(CGO_LDFLAGS)" C_INCLUDE_PATH=${C_INCLUDE_PATH} LIBRARY_PATH=${LIBRARY_PATH} $(GOCMD) run ./

test-models/testmodel:
	mkdir test-models
	mkdir test-dir
	wget https://huggingface.co/nnakasato/ggml-model-test/resolve/main/ggml-model-q4.bin -O test-models/testmodel
	wget https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-base.en.bin -O test-models/whisper-en
	wget https://huggingface.co/skeskinen/ggml/resolve/main/all-MiniLM-L6-v2/ggml-model-q4_0.bin -O test-models/bert
	wget https://cdn.openai.com/whisper/draft-20220913a/micro-machines.wav -O test-dir/audio.wav
	wget https://huggingface.co/mudler/rwkv-4-raven-1.5B-ggml/resolve/main/RWKV-4-Raven-1B5-v11-Eng99%2525-Other1%2525-20230425-ctx4096_Q4_0.bin -O test-models/rwkv
	wget https://raw.githubusercontent.com/saharNooby/rwkv.cpp/5eb8f09c146ea8124633ab041d9ea0b1f1db4459/rwkv/20B_tokenizer.json -O test-models/rwkv.tokenizer.json
	cp tests/models_fixtures/* test-models

test: prepare test-models/testmodel
	cp -r backend-assets api
	cp tests/models_fixtures/* test-models
	C_INCLUDE_PATH=${C_INCLUDE_PATH} LIBRARY_PATH=${LIBRARY_PATH} TEST_DIR=$(abspath ./)/test-dir/ FIXTURES=$(abspath ./)/tests/fixtures CONFIG_FILE=$(abspath ./)/test-models/config.yaml MODELS_PATH=$(abspath ./)/test-models $(GOCMD) run github.com/onsi/ginkgo/v2/ginkgo --label-filter="!gpt4all && !llama" --flake-attempts 5 -v -r ./api ./pkg
	C_INCLUDE_PATH=${C_INCLUDE_PATH} LIBRARY_PATH=${LIBRARY_PATH} TEST_DIR=$(abspath ./)/test-dir/ FIXTURES=$(abspath ./)/tests/fixtures CONFIG_FILE=$(abspath ./)/test-models/config.yaml MODELS_PATH=$(abspath ./)/test-models $(GOCMD) run github.com/onsi/ginkgo/v2/ginkgo --label-filter="gpt4all" --flake-attempts 5 -v -r ./api ./pkg
	C_INCLUDE_PATH=${C_INCLUDE_PATH} LIBRARY_PATH=${LIBRARY_PATH} TEST_DIR=$(abspath ./)/test-dir/ FIXTURES=$(abspath ./)/tests/fixtures CONFIG_FILE=$(abspath ./)/test-models/config.yaml MODELS_PATH=$(abspath ./)/test-models $(GOCMD) run github.com/onsi/ginkgo/v2/ginkgo --label-filter="llama" --flake-attempts 5 -v -r ./api ./pkg

## Help:
help: ## Show this help.
	@echo ''
	@echo 'Usage:'
	@echo '  ${YELLOW}make${RESET} ${GREEN}<target>${RESET}'
	@echo ''
	@echo 'Targets:'
	@awk 'BEGIN {FS = ":.*?## "} { \
		if (/^[a-zA-Z_-]+:.*?##.*$$/) {printf "    ${YELLOW}%-20s${GREEN}%s${RESET}\n", $$1, $$2} \
		else if (/^## .*$$/) {printf "  ${CYAN}%s${RESET}\n", substr($$1,4)} \
		}' $(MAKEFILE_LIST)
