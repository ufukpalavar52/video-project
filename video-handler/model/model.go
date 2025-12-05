package model

type GifVideo struct {
	ID            int64  `json:"id"`
	TransactionID string `json:"transactionId"`
	Path          string `json:"path"`
	IsUrl         bool   `json:"isUrl"`
	GifPath       string `json:"gifPath"`
	PathType      string `json:"pathType"`
	StartTime     int    `json:"startTime"`
	EndTime       int    `json:"endTime"`
	Status        string `json:"status"`
	CreatedAt     string `json:"createdAt"`
}

type GifVideoErrorLog struct {
	TransactionID string `json:"transactionId"`
	Message       string `json:"message"`
}
