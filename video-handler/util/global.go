package util

type GifLog struct {
	Gif struct {
		ID            int64  `json:"id"`
		TransactionID string `json:"transactionId"`
	} `json:"gif"`
	Err string `json:"error"`
}
