package service

import (
	"os"
	"strings"
	"time"
	"video-handler/model"
	"video-handler/util"
)

const MB = 1024 * 1024
const Video240Scale = 240
const Video360Scale = 360
const Video480Scale = 480
const GifFps = 15

type Storage interface {
	GetFile(string) ([]byte, error)
	PutFile(string, []byte, ...*time.Time) error
}

type VideoProcess interface {
	Process() error
}

func NewStorage(pathType string, isUrl bool) Storage {
	var storage Storage

	switch pathType {
	case "file":
		storage = NewFileService()
		break
	case "s3":
		storage = NewS3Service()
		break
	default:
		storage = NewFileService()
	}

	if isUrl {
		return NewUrlService(storage)
	}

	return storage
}

func BuildVideoProcess(video *model.Video, expires ...*time.Time) VideoProcess {
	processType := strings.ToUpper(video.ProcessType)
	if processType == "CUT" {
		return NewCutVideoService(video, expires...)
	}
	return NewGifService(video, expires...)
}

func SaveFile(data []byte, filename string) (string, error) {
	path := os.Getenv("TMP_VIDEO_PATH")
	fullPath := path + "/" + filename
	err := util.SaveFile(fullPath, data)
	if err != nil {
		return "", err
	}
	return fullPath, nil
}
