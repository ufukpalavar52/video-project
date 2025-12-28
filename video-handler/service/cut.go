package service

import (
	"os"
	"time"
	"video-handler/model"
	"video-handler/util"
)

type CutVideoService struct {
	video   *model.Video
	storage Storage
	expires *time.Time
}

func NewCutVideoService(video *model.Video, expires ...*time.Time) *CutVideoService {
	cropService := &CutVideoService{
		video:   video,
		storage: NewStorage(video.PathType, video.IsUrl),
	}
	if len(expires) > 0 {
		cropService.expires = expires[0]
	}
	return cropService
}

func (c *CutVideoService) Process() error {
	video, err := c.storage.GetFile(c.video.Path)
	if err != nil {
		return err
	}
	filename := util.UUID() + ".mp4"
	if !c.video.IsUrl {
		_, filename, _ = util.ParsePath(c.video.Path)
	}

	filePath, err := SaveFile(video, filename)

	if err != nil {
		return err
	}

	defer util.DeleteFile(filePath)
	ffs := NewFfmpegService(c.video.TransactionID)

	cutPath, err := ffs.CutVideo(filePath, c.video.StartTime, c.video.EndTime)

	if err != nil {
		return err
	}
	defer util.DeleteFile(cutPath)

	body, err := os.ReadFile(cutPath)
	if err != nil {
		return util.NewError("Error reading video file. Path:%s Err:%v", cutPath, err)
	}

	_, videoName, _ := util.ParsePath(cutPath)
	fullPath := os.Getenv("VIDEO_GIF_OUTPUT_PATH") + "/" + videoName
	err = c.storage.PutFile(fullPath, body, c.expires)

	c.video.OutputPath = fullPath
	return nil
}
