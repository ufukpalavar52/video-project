package service

import (
	"context"
	"encoding/json"
	"fmt"
	"log"
	"video-handler/util"

	"github.com/segmentio/kafka-go"
)

const NumPartitions = 4
const ReplicationFactor = 1

type KafkaService struct {
	Servers []string
	Topic   string
	Group   string
}

func NewKafkaService(servers []string, groupId string) *KafkaService {
	return &KafkaService{
		Servers: servers,
		Group:   groupId,
	}
}

func (k *KafkaService) Produce(topic string, message []byte) error {
	ctx := context.Background()
	err := k.ensureTopicExists(topic, ctx)
	if err != nil {
		return err
	}

	w := &kafka.Writer{
		Addr:     kafka.TCP(k.Servers...),
		Topic:    topic,
		Balancer: &kafka.LeastBytes{},
	}
	defer func() {
		_ = w.Close()
	}()
	key := fmt.Sprintf("Key-%s", util.UUID())
	err = w.WriteMessages(ctx,
		kafka.Message{
			Key:       []byte(key),
			Value:     message,
			Partition: 1,
		},
	)

	if err != nil {
		return err
	}
	return nil
}

func (k *KafkaService) ProduceAny(topic string, message any) error {
	data, err := json.Marshal(message)
	if err != nil {
		return err
	}
	return k.Produce(topic, data)
}

func (k *KafkaService) Consume(topic string, callback func(message []byte)) {
	r := kafka.NewReader(kafka.ReaderConfig{
		Brokers:     k.Servers,
		Topic:       topic,
		GroupID:     k.Group,
		MinBytes:    10e3, // 10KB
		MaxBytes:    10e6, // 10MB
		StartOffset: kafka.FirstOffset,
	})
	defer func() {
		_ = r.Close()
	}()
	ctx := context.Background()

	for {
		m, err := r.ReadMessage(ctx)
		if err != nil {
			log.Fatalf("Error reading message: %v", err)
		}
		callback(m.Value)
	}
}

func (k *KafkaService) ensureTopicExists(topic string, ctx context.Context) error {
	conn, err := kafka.DialContext(ctx, "tcp", k.Servers[0])
	if err != nil {
		return util.NewError("Connecting to kafka: %v", err)
	}

	defer func() {
		_ = conn.Close()
	}()

	topicConfig := kafka.TopicConfig{
		Topic:             topic,
		NumPartitions:     NumPartitions,
		ReplicationFactor: ReplicationFactor,
	}

	_ = conn.CreateTopics(topicConfig)

	return nil
}
