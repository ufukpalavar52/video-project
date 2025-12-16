package service

import (
	"os"
	"time"
	"video-handler/model"
	"video-handler/util"
)

type GifService struct {
	video   *model.Video
	storage Storage
	expires *time.Time
}

func NewGifService(video *model.Video, expires ...*time.Time) *GifService {
	gifService := &GifService{
		video:   video,
		storage: NewStorage(video.PathType, video.IsUrl),
	}
	if len(expires) > 0 {
		gifService.expires = expires[0]
	}

	return gifService
}

func (g *GifService) Process() error {
	video, err := g.storage.GetFile(g.video.Path)
	if err != nil {
		return err
	}
	filename := util.UUID() + ".mp4"
	if !g.video.IsUrl {
		_, filename, _ = util.ParsePath(g.video.Path)
	}

	filePath, err := SaveFile(video, filename)

	if err != nil {
		return err
	}

	defer util.DeleteFile(filePath)
	ffs := NewFfmpegService(g.video.TransactionID)

	gifPath, err := ffs.GifProcess(filePath, g.video.StartTime, g.video.EndTime)
	if err != nil {
		return err
	}

	defer util.DeleteFile(gifPath)

	body, err := os.ReadFile(gifPath)
	if err != nil {
		return util.NewError("Error reading video file. Path:%s Err:%v", gifPath, err)
	}

	_, gifName, _ := util.ParsePath(gifPath)
	fullPath := os.Getenv("VIDEO_GIF_OUTPUT_PATH") + "/" + gifName
	err = g.storage.PutFile(fullPath, body, g.expires)

	g.video.OutputPath = fullPath

	return err
}
