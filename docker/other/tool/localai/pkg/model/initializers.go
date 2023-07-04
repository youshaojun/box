package model

import (
	"fmt"
	"path/filepath"
	"strings"

	rwkv "github.com/donomii/go-rwkv.cpp"
	whisper "github.com/ggerganov/whisper.cpp/bindings/go/pkg/whisper"
	"github.com/go-skynet/LocalAI/pkg/langchain"
	"github.com/go-skynet/LocalAI/pkg/stablediffusion"
	"github.com/go-skynet/LocalAI/pkg/tts"
	bloomz "github.com/go-skynet/bloomz.cpp"
	bert "github.com/go-skynet/go-bert.cpp"
	transformers "github.com/go-skynet/go-ggml-transformers.cpp"
	llama "github.com/go-skynet/go-llama.cpp"
	"github.com/hashicorp/go-multierror"
	gpt4all "github.com/nomic-ai/gpt4all/gpt4all-bindings/golang"
	"github.com/rs/zerolog/log"
)

const tokenizerSuffix = ".tokenizer.json"

const (
	LlamaBackend           = "llama"
	BloomzBackend          = "bloomz"
	StarcoderBackend       = "starcoder"
	GPTJBackend            = "gptj"
	DollyBackend           = "dolly"
	MPTBackend             = "mpt"
	GPTNeoXBackend         = "gptneox"
	ReplitBackend          = "replit"
	Gpt2Backend            = "gpt2"
	Gpt4AllLlamaBackend    = "gpt4all-llama"
	Gpt4AllMptBackend      = "gpt4all-mpt"
	Gpt4AllJBackend        = "gpt4all-j"
	Gpt4All                = "gpt4all"
	FalconBackend          = "falcon"
	BertEmbeddingsBackend  = "bert-embeddings"
	RwkvBackend            = "rwkv"
	WhisperBackend         = "whisper"
	StableDiffusionBackend = "stablediffusion"
	PiperBackend           = "piper"
	LCHuggingFaceBackend   = "langchain-huggingface"
)

var autoLoadBackends []string = []string{
	LlamaBackend,
	Gpt4All,
	RwkvBackend,
	GPTNeoXBackend,
	WhisperBackend,
	BertEmbeddingsBackend,
	GPTJBackend,
	Gpt2Backend,
	DollyBackend,
	FalconBackend,
	MPTBackend,
	ReplitBackend,
	StarcoderBackend,
	BloomzBackend,
}

var starCoder = func(modelFile string) (interface{}, error) {
	return transformers.NewStarcoder(modelFile)
}

var mpt = func(modelFile string) (interface{}, error) {
	return transformers.NewMPT(modelFile)
}

var dolly = func(modelFile string) (interface{}, error) {
	return transformers.NewDolly(modelFile)
}

var gptNeoX = func(modelFile string) (interface{}, error) {
	return transformers.NewGPTNeoX(modelFile)
}

var replit = func(modelFile string) (interface{}, error) {
	return transformers.NewReplit(modelFile)
}

var gptJ = func(modelFile string) (interface{}, error) {
	return transformers.NewGPTJ(modelFile)
}

var falcon = func(modelFile string) (interface{}, error) {
	return transformers.NewFalcon(modelFile)
}

var bertEmbeddings = func(modelFile string) (interface{}, error) {
	return bert.New(modelFile)
}

var bloomzLM = func(modelFile string) (interface{}, error) {
	return bloomz.New(modelFile)
}

var transformersLM = func(modelFile string) (interface{}, error) {
	return transformers.New(modelFile)
}

var stableDiffusion = func(assetDir string) (interface{}, error) {
	return stablediffusion.New(assetDir)
}

func piperTTS(assetDir string) func(s string) (interface{}, error) {
	return func(s string) (interface{}, error) {
		return tts.New(assetDir)
	}
}

var whisperModel = func(modelFile string) (interface{}, error) {
	return whisper.New(modelFile)
}

var lcHuggingFace = func(repoId string) (interface{}, error) {
	return langchain.NewHuggingFace(repoId)
}

func llamaLM(opts ...llama.ModelOption) func(string) (interface{}, error) {
	return func(s string) (interface{}, error) {
		return llama.New(s, opts...)
	}
}

func gpt4allLM(opts ...gpt4all.ModelOption) func(string) (interface{}, error) {
	return func(s string) (interface{}, error) {
		return gpt4all.New(s, opts...)
	}
}

func rwkvLM(tokenFile string, threads uint32) func(string) (interface{}, error) {
	return func(s string) (interface{}, error) {
		log.Debug().Msgf("Loading RWKV", s, tokenFile)

		model := rwkv.LoadFiles(s, tokenFile, threads)
		if model == nil {
			return nil, fmt.Errorf("could not load model")
		}
		return model, nil
	}
}

func (ml *ModelLoader) BackendLoader(backendString string, modelFile string, llamaOpts []llama.ModelOption, threads uint32, assetDir string) (model interface{}, err error) {
	log.Debug().Msgf("Loading model %s from %s", backendString, modelFile)
	switch strings.ToLower(backendString) {
	case LlamaBackend:
		return ml.LoadModel(modelFile, llamaLM(llamaOpts...))
	case BloomzBackend:
		return ml.LoadModel(modelFile, bloomzLM)
	case GPTJBackend:
		return ml.LoadModel(modelFile, gptJ)
	case DollyBackend:
		return ml.LoadModel(modelFile, dolly)
	case MPTBackend:
		return ml.LoadModel(modelFile, mpt)
	case Gpt2Backend:
		return ml.LoadModel(modelFile, transformersLM)
	case FalconBackend:
		return ml.LoadModel(modelFile, falcon)
	case GPTNeoXBackend:
		return ml.LoadModel(modelFile, gptNeoX)
	case ReplitBackend:
		return ml.LoadModel(modelFile, replit)
	case StableDiffusionBackend:
		return ml.LoadModel(modelFile, stableDiffusion)
	case PiperBackend:
		return ml.LoadModel(modelFile, piperTTS(filepath.Join(assetDir, "backend-assets", "espeak-ng-data")))
	case StarcoderBackend:
		return ml.LoadModel(modelFile, starCoder)
	case Gpt4AllLlamaBackend, Gpt4AllMptBackend, Gpt4AllJBackend, Gpt4All:
		return ml.LoadModel(modelFile, gpt4allLM(gpt4all.SetThreads(int(threads)), gpt4all.SetLibrarySearchPath(filepath.Join(assetDir, "backend-assets", "gpt4all"))))
	case BertEmbeddingsBackend:
		return ml.LoadModel(modelFile, bertEmbeddings)
	case RwkvBackend:
		return ml.LoadModel(modelFile, rwkvLM(filepath.Join(ml.ModelPath, modelFile+tokenizerSuffix), threads))
	case WhisperBackend:
		return ml.LoadModel(modelFile, whisperModel)
	case LCHuggingFaceBackend:
		return ml.LoadModel(modelFile, lcHuggingFace)
	default:
		return nil, fmt.Errorf("backend unsupported: %s", backendString)
	}
}

func (ml *ModelLoader) GreedyLoader(modelFile string, llamaOpts []llama.ModelOption, threads uint32, assetDir string) (interface{}, error) {
	log.Debug().Msgf("Loading model '%s' greedly", modelFile)

	ml.mu.Lock()
	m, exists := ml.models[modelFile]
	if exists {
		log.Debug().Msgf("Model '%s' already loaded", modelFile)
		ml.mu.Unlock()
		return m, nil
	}
	ml.mu.Unlock()
	var err error

	for _, b := range autoLoadBackends {
		if b == BloomzBackend || b == WhisperBackend || b == RwkvBackend { // do not autoload bloomz/whisper/rwkv
			continue
		}
		log.Debug().Msgf("[%s] Attempting to load", b)
		model, modelerr := ml.BackendLoader(b, modelFile, llamaOpts, threads, assetDir)
		if modelerr == nil && model != nil {
			log.Debug().Msgf("[%s] Loads OK", b)
			return model, nil
		} else if modelerr != nil {
			err = multierror.Append(err, modelerr)
			log.Debug().Msgf("[%s] Fails: %s", b, modelerr.Error())
		}
	}

	return nil, fmt.Errorf("could not load model - all backends returned error: %s", err.Error())
}
