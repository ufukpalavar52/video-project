package service

import "time"

type Storage interface {
	GetFile(string) ([]byte, error)
	PutFile(string, []byte, ...*time.Time) error
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
