class ApiError<T> extends Error {
    body: T

    constructor(message: string, body: T) {
        super(message);
        this.body = body;
    }
}