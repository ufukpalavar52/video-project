package service

import (
	"fmt"
	"log"
	"os"
	"os/exec"
	"strings"
	"time"
	"video-handler/model"
	"video-handler/util"
)

const MB = 1024 * 1024
const Video240Scale = 240
const Video360Scale = 360
const Video480Scale = 480

type GifService struct {
	gif     *model.GifVideo
	storage Storage
	expires *time.Time
}

func NewGifService(gif *model.GifVideo, expires ...*time.Time) *GifService {
	gifService := &GifService{
		gif:     gif,
		storage: NewStorage(gif.PathType, gif.IsUrl),
	}
	if len(expires) > 0 {
		gifService.expires = expires[0]
	}

	return gifService
}

func (g *GifService) GiffProcess() error {
	video, err := g.storage.GetFile(g.gif.Path)
	if err != nil {
		return err
	}
	filename := util.UUID() + ".mp4"
	if !g.gif.IsUrl {
		_, filename, _ = util.ParsePath(g.gif.Path)
	}

	filePath, err := g.SaveFile(video, filename)

	if err != nil {
		return err
	}

	gifPath, err := g.FfmpegProcess(filePath)
	if err != nil {
		return err
	}

	defer util.DeleteFile(gifPath)

	body, err := os.ReadFile(gifPath)
	if err != nil {
		return util.NewError("Error reading gif file. Path:%s Err:%v", gifPath, err)
	}

	_, gifName, _ := util.ParsePath(gifPath)
	fullPath := os.Getenv("GIF_OUTPUT_PATH") + "/" + gifName
	err = g.storage.PutFile(fullPath, body, g.expires)

	g.gif.GifPath = fullPath

	return err
}

func (g *GifService) FfmpegProcess(filePath string) (string, error) {
	now := time.Now()
	inputFile, err := g.CutVideo(filePath)
	if err != nil {
		return "", err
	}

	giffName := util.UUID() + ".gif"
	outputGif := os.Getenv("TMP_VIDEO_PATH") + "/" + giffName
	videoScale := fmt.Sprintf("%d", g.GetVideoScale(filePath))
	args := []string{
		"-i", inputFile,
		"-filter_complex", "[0:v] fps=15,scale=" + videoScale + ":-1:flags=lanczos[x];[x]split[y][z];[y]palettegen[p];[z][p]paletteuse",
		"-loop", "0",
		outputGif,
	}
	log.Printf("Start[transactionId:%s] ffmpeg process\n", g.gif.TransactionID)
	defer func() {
		end := time.Now()
		log.Printf("End[transactionId:%s] ffmpeg process. Process Time:%f\n", g.gif.TransactionID, end.Sub(now).Seconds())
		util.DeleteFile(inputFile)
	}()
	cmd := exec.Command("ffmpeg", args...)

	out, err := cmd.CombinedOutput()
	if err != nil {
		log.Println("Command: ffmpeg " + strings.Join(args, " "))
		return "", util.NewError("Ffmpeg Error: %v, Output: %s", err, out)
	}

	return outputGif, nil
}

func (g *GifService) CutVideo(filePath string) (string, error) {
	cutFilename := util.UUID() + ".mp4"
	outputGif := os.Getenv("TMP_VIDEO_PATH") + "/" + cutFilename
	startSec := fmt.Sprintf("%d", g.gif.StartTime)
	duration := fmt.Sprintf("%d", g.gif.EndTime-g.gif.StartTime)
	args := []string{
		"-ss", startSec,
		"-i", filePath,
		"-t", duration,
		"-c:v", "libx264",
		"-preset", "veryfast",
		outputGif,
	}
	cmd := exec.Command("ffmpeg", args...)

	out, err := cmd.CombinedOutput()
	if err != nil {
		log.Println("Cut Command: ffmpeg " + strings.Join(args, " "))
		return "", util.NewError("Ffmpeg Error: %v, Output: %s", err, out)
	}

	defer util.DeleteFile(filePath)

	return outputGif, nil
}

func (g *GifService) SaveFile(data []byte, filename string) (string, error) {
	path := os.Getenv("TMP_VIDEO_PATH")
	fullPath := path + "/" + filename
	err := util.SaveFile(fullPath, data)
	if err != nil {
		return "", err
	}
	return fullPath, nil
}

func (g *GifService) GetVideoScale(fullPath string) int {
	info, err := os.Stat(fullPath)
	if err != nil {
		return Video360Scale
	}
	fileSize := float64(info.Size()) / MB
	if fileSize > 1024 {
		return Video360Scale
	}

	if fileSize > 4096 {
		return Video240Scale
	}

	return Video480Scale
}
