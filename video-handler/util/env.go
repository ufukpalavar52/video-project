package util

import "os"

func Getenv(key string) string {
	return os.Getenv(key)
}

func GetEnvWithDefault(key, fallback string) string {
	value := Getenv(key)
	if value == "" {
		value = fallback
	}
	return value
}

func GetEnvKey(key, fallbackKey string) string {
	return GetEnvWithDefault(key, os.Getenv(fallbackKey))
}
