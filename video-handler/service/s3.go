package service

import (
	"bytes"
	"context"
	"errors"
	"io"
	"log"
	"os"
	"time"
	"video-handler/util"

	"github.com/aws/aws-sdk-go-v2/aws"
	"github.com/aws/aws-sdk-go-v2/config"
	"github.com/aws/aws-sdk-go-v2/credentials"
	"github.com/aws/aws-sdk-go-v2/service/s3"
	"github.com/aws/aws-sdk-go-v2/service/s3/types"
)

type S3Service struct {
	Client   *s3.Client
	ctx      context.Context
	S3Config S3Config
}
type S3Config struct {
	Endpoint  string
	AccessKey string
	SecretKey string
	Region    string
}

func NewS3Service() *S3Service {
	s3Config := S3Config{
		Endpoint:  util.GetEnvKey("S3_ENDPOINT", "OBJECT_STORAGE_URI"),
		AccessKey: util.GetEnvKey("S3_ACCESS_KEY", "OBJECT_STORAGE_ROOT_USER"),
		SecretKey: util.GetEnvKey("S3_SECRET_KEY", "OBJECT_STORAGE_ROOT_USER"),
		Region:    os.Getenv("S3_REGION"),
	}

	s3Service := &S3Service{S3Config: s3Config}
	s3Service.BuildS3Config(s3Config)
	return s3Service
}

func (s3Service *S3Service) GetFile(filePath string) ([]byte, error) {
	bucket, key, _ := util.ParsePath(filePath)

	resp, err := s3Service.Client.GetObject(s3Service.ctx, &s3.GetObjectInput{
		Bucket: aws.String(bucket),
		Key:    aws.String(key),
	})
	if err != nil {
		return nil, util.NewError("Download error: %v", err)
	}
	body, err := io.ReadAll(resp.Body)
	if err != nil {

		return nil, util.NewError("Read error: %v", err)
	}
	_ = resp.Body.Close()
	return body, nil
}

func (s3Service *S3Service) PutFile(filePath string, body []byte, expires ...*time.Time) error {
	bucket, key, _ := util.ParsePath(filePath)

	err := s3Service.ensureBucketExists(bucket)
	if err != nil {
		return err
	}

	s3Opt := &s3.PutObjectInput{
		Bucket: aws.String(bucket),
		Body:   bytes.NewReader(body),
		Key:    aws.String(key),
	}

	if len(expires) > 0 && expires[0] != nil {
		s3Opt.Expires = expires[0]
	}

	_, err = s3Service.Client.PutObject(s3Service.ctx, s3Opt)

	if err != nil {
		return util.NewError("Upload error: %v", err)
	}

	return nil
}

func (s3Service *S3Service) BuildS3Config(s3Config S3Config) {
	s3Service.ctx = context.Background()

	cfg, err := config.LoadDefaultConfig(s3Service.ctx,
		config.WithRegion(s3Config.Region),
		config.WithCredentialsProvider(
			credentials.NewStaticCredentialsProvider(s3Config.AccessKey, s3Config.SecretKey, ""),
		),
	)
	if err != nil {
		log.Fatalf("S3 config error: %v", err)
	}

	s3Client := s3.NewFromConfig(cfg, func(o *s3.Options) {
		o.UsePathStyle = true
		o.BaseEndpoint = aws.String(s3Config.Endpoint)
	})

	s3Service.Client = s3Client
}

func (s3Service *S3Service) ensureBucketExists(bucketName string) error {
	_, err := s3Service.Client.HeadBucket(s3Service.ctx, &s3.HeadBucketInput{
		Bucket: aws.String(bucketName),
	})

	if err == nil {
		return nil
	}

	var nfe *types.NotFound
	if !errors.As(err, &nfe) {
		return util.NewError("Head bucket error: %v", err)
	}

	_, err = s3Service.Client.CreateBucket(s3Service.ctx, &s3.CreateBucketInput{
		Bucket: aws.String(bucketName),
		CreateBucketConfiguration: &types.CreateBucketConfiguration{
			LocationConstraint: types.BucketLocationConstraint(s3Service.S3Config.Region),
		},
	})

	var nfe2 *types.NotFound
	if err != nil && !errors.As(err, &nfe2) {
		return util.NewError("Create bucket error: %v", err)
	}
	log.Println("Created bucket:", bucketName)

	return nil
}
