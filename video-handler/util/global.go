package util

type VideoLog struct {
	Video struct {
		ID            int64  `json:"id"`
		TransactionID string `json:"transactionId"`
	} `json:"video"`
	Err string `json:"error"`
}
