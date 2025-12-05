package service

import (
	"os"
	"time"
	"video-handler/util"
)

type FileService struct{}

func NewFileService() *FileService {
	return &FileService{}
}

func (*FileService) GetFile(fullPath string) ([]byte, error) {
	body, err := os.ReadFile(fullPath)
	if err != nil {
		return nil, err
	}
	return body, nil
}

func (*FileService) PutFile(fullPath string, data []byte, expires ...*time.Time) error {
	return util.SaveFile(fullPath, data)
}
