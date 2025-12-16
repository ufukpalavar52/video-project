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

type FfmpegService struct {
	TransactionID string
}

func NewFfmpegService(transactionId string) *FfmpegService {
	return &FfmpegService{transactionId}
}

func (f *FfmpegService) GifProcess(filePath string, startTime, endTime int) (string, error) {
	now := time.Now()
	inputFile, err := f.CutGifVideo(filePath, startTime, endTime)
	if err != nil {
		return "", err
	}

	giffName := util.UUID() + ".gif"
	outputGif := os.Getenv("TMP_VIDEO_PATH") + "/" + giffName
	videoScale := fmt.Sprintf("%d", f.GetVideoScale(filePath))
	gifFps := fmt.Sprintf("%d", GifFps)
	args := []string{
		"-i", inputFile,
		"-filter_complex", "[0:v] fps=" + gifFps + ",scale=" + videoScale + ":-1:flags=lanczos[x];[x]split[y][z];[y]palettegen[p];[z][p]paletteuse",
		"-loop", "0",
		outputGif,
	}
	log.Printf("Start[transactionId:%s] ffmpeg process\n", f.TransactionID)
	defer func() {
		end := time.Now()
		log.Printf("End[transactionId:%s] ffmpeg process. Process Time:%f\n", f.TransactionID, end.Sub(now).Seconds())
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

func (f *FfmpegService) CutGifVideo(filePath string, startTime, endTime int) (string, error) {
	cutFilename := util.UUID() + ".mp4"
	outputVideo := os.Getenv("TMP_VIDEO_PATH") + "/" + cutFilename
	startSec := fmt.Sprintf("%d", startTime)
	duration := fmt.Sprintf("%d", endTime-startTime)
	args := []string{
		"-ss", startSec,
		"-i", filePath,
		"-t", duration,
		"-c:v", "libx264",
		"-preset", "veryfast",
		outputVideo,
	}
	cmd := exec.Command("ffmpeg", args...)

	out, err := cmd.CombinedOutput()
	if err != nil {
		log.Println("Cut Command: ffmpeg " + strings.Join(args, " "))
		return "", util.NewError("Ffmpeg Error: %v, Output: %s", err, out)
	}

	return outputVideo, nil
}

func (f *FfmpegService) CutVideo(filePath string, startTime, endTime int) (string, error) {
	cutFilename := util.UUID() + ".mp4"
	outputVideo := os.Getenv("TMP_VIDEO_PATH") + "/" + cutFilename
	startSec := fmt.Sprintf("%d", startTime)
	endSec := fmt.Sprintf("%d", endTime)
	// ffmpeg -ss 5 -to 10 -i input.mp4 -c:v copy -c:a aac output_sesli_trim.mp4
	args := []string{
		"-ss", startSec,
		"-to", endSec,
		"-i", filePath,
		"-c:v", "copy",
		"-c:a", "aac",
		outputVideo,
	}
	cmd := exec.Command("ffmpeg", args...)

	out, err := cmd.CombinedOutput()
	if err != nil {
		log.Println("Cut Command: ffmpeg " + strings.Join(args, " "))
		return "", util.NewError("Ffmpeg Error: %v, Output: %s", err, out)
	}

	return outputVideo, nil
}

func (f *FfmpegService) GetVideoScale(fullPath string) int {
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
