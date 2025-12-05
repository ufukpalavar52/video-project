package util

import (
	"errors"
	"log"
	"os"
	"strings"
)

func ParsePath(pathStr string) (string, string, error) {
	pathArr := strings.Split(pathStr, "/")

	if len(pathArr) < 2 {
		return "", "", errors.New("invalid path")
	}

	fileName := pathArr[len(pathArr)-1]
	path := strings.Join(pathArr[:len(pathArr)-1], "/")
	return path, fileName, nil
}

func SaveFile(fullPath string, data []byte) error {
	file, err := os.Create(fullPath)
	if err != nil {
		return err
	}
	defer file.Close()

	_, err = file.Write(data)
	if err != nil {
		return err
	}

	return nil
}

func DeleteFile(filePath string) {
	err := os.Remove(filePath)
	if err != nil {
		log.Println("Delete File Error:", err)
	}
}
