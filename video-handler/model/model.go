package model

type Video struct {
	ID            int64  `json:"id"`
	TransactionID string `json:"transactionId"`
	Path          string `json:"path"`
	IsUrl         bool   `json:"isUrl"`
	OutputPath    string `json:"outputPath"`
	ProcessType   string `json:"processType"`
	Resolution    string `json:"resolution"`
	PathType      string `json:"pathType"`
	StartTime     int    `json:"startTime"`
	EndTime       int    `json:"endTime"`
	Status        string `json:"status"`
	CreatedAt     string `json:"createdAt"`
}

type VideoErrorLog struct {
	TransactionID string `json:"transactionId"`
	Message       string `json:"message"`
}
