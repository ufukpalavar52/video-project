package util

import (
	"fmt"
)

func NewError(format string, params ...any) error {
	return fmt.Errorf(format, params...)
}
