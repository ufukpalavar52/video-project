package service

import (
	"fmt"
	"log"
	"os"
	"os/exec"
	"strings"
	"time"
	"video-handler/util"
)

type UrlService struct {
	storage Storage
}

func NewUrlService(storage Storage) *UrlService {
	return &UrlService{storage}
}

func (u *UrlService) GetFile(fullUrl string) ([]byte, error) {
	return u.DownloadVideo(fullUrl)
}

func (u *UrlService) PutFile(fullPath string, data []byte, expires ...*time.Time) error {
	return u.storage.PutFile(fullPath, data)
}

func (u *UrlService) DownloadVideo(fullUrl string) ([]byte, error) {
	filename := fmt.Sprintf("%s.mp4", util.UUID())
	args := []string{
		"download",
		"-q",
		"medium",
		"-o",
		filename,
		fullUrl,
	}

	log.Printf("Downloading video[url:%s]...\n", fullUrl)
	cmd := exec.Command("youtubedr", args...)
	out, err := cmd.CombinedOutput()
	if err != nil {
		log.Println("Command: youtubedr " + strings.Join(args, " "))
		return nil, util.NewError("Download Error: %v, Output: %s", err, out)
	}

	defer util.DeleteFile(filename)
	log.Printf("Downloaded video[url:%s]\n", fullUrl)
	return os.ReadFile(filename)
}
