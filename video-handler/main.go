package main

import (
	"encoding/json"
	"log"
	"os"
	"strconv"
	"strings"
	"time"
	"video-handler/model"
	"video-handler/service"
	"video-handler/util"

	"github.com/joho/godotenv"
)

var kafkaService *service.KafkaService

func init() {
	err := godotenv.Load()
	if err != nil {
		log.Fatal("Error loading .env file")
	}
	kafkaServers := strings.Split(os.Getenv("KAFKA_BOOTSTRAP_SERVERS"), ",")
	kafkaService = service.NewKafkaService(
		kafkaServers,
		os.Getenv("KAFKA_GROUP_ID"),
	)
}

func main() {
	KafkaListen()
}

func KafkaListen() {
	limit := make(chan int, 10)
	log.Println("Kafka listening...")
	consumeTopic := os.Getenv("KAFKA_VIDEO_TOPIC")
	kafkaService.Consume(consumeTopic, func(message []byte) {
		var gif model.Video
		err := json.Unmarshal(message, &gif)
		if err != nil {
			log.Println("Error unmarshalling gif json", err)
			return
		}

		limit <- 1
		go func() {
			KafkaProcess(&gif)
			<-limit
		}()
	})
	close(limit)
}

func KafkaProcess(gif *model.Video) {
	now := time.Now()
	defer func() {
		finish := time.Now()
		log.Println("Process process took", finish.Sub(now).Seconds(), "seconds Gif", VideoLogData(gif, nil))
	}()
	produceTopic := os.Getenv("KAFKA_VIDEO_FINISH_TOPIC")
	produceErrorTopic := os.Getenv("KAFKA_VIDEO_ERROR_TOPIC")
	err := VideoProcess(gif)
	if err != nil {
		log.Println("Error processing video", VideoLogData(gif, err))
		err = kafkaService.ProduceAny(produceErrorTopic, model.VideoErrorLog{Message: err.Error(), TransactionID: gif.TransactionID})
		if err != nil {
			log.Println("Error producing video", VideoLogData(gif, err))
		}
		return
	}
	err = kafkaService.ProduceAny(produceTopic, gif)
	if err != nil {
		log.Println("Error producing message", VideoLogData(gif, err))
		return
	}
	log.Println("Processed video", VideoLogData(gif, err))
}

func VideoProcess(video *model.Video) error {
	log.Println("Video processing...", VideoLogData(video, nil))

	daysStr := os.Getenv("VIDEO_GIF_TIMEOUT_DAYS")
	var expires *time.Time = nil
	days, err := strconv.Atoi(daysStr)
	if err == nil && days > 0 {
		dur := time.Now().Add(time.Duration(days) * 24 * time.Hour)
		expires = &dur
	}

	vs := service.BuildVideoProcess(video, expires)
	err = vs.Process()

	if err != nil {
		return err
	}
	return nil
}

func VideoLogData(video *model.Video, err error) string {
	errMsg := ""
	if err != nil {
		errMsg = err.Error()
	}

	data := util.VideoLog{
		Err: errMsg,
	}

	data.Video.ID = video.ID
	data.Video.TransactionID = video.TransactionID
	jsonData, _ := json.Marshal(data)
	return string(jsonData)
}
