package com.teambind.bind_android.data.api

sealed class NetworkError : Exception() {
    object NotFoundURL : NetworkError() {
        private fun readResolve(): Any = NotFoundURL
        override val message: String = "URL을 찾을 수 없습니다."
    }

    object InvalidURL : NetworkError() {
        private fun readResolve(): Any = InvalidURL
        override val message: String = "유효하지 않은 URL입니다."
    }

    object NotFoundToken : NetworkError() {
        private fun readResolve(): Any = NotFoundToken
        override val message: String = "토큰을 찾을 수 없습니다."
    }

    data class InvalidToken(val statusCode: Int) : NetworkError() {
        override val message: String = "유효하지 않은 토큰입니다. (status: $statusCode)"
    }

    object RequestCancelled : NetworkError() {
        private fun readResolve(): Any = RequestCancelled
        override val message: String = "네트워크 요청이 취소되었습니다."
    }

    data class ServerError(val statusCode: Int, val errorMessage: String) : NetworkError() {
        override val message: String = errorMessage
    }

    data class DecodingFailed(val statusCode: Int, val errorMessage: String) : NetworkError() {
        override val message: String = errorMessage
    }

    object ConnectionFailed : NetworkError() {
        private fun readResolve(): Any = ConnectionFailed
        override val message: String = "네트워크에 연결할 수 없습니다."
    }

    data class UnknownError(val errorMessage: String) : NetworkError() {
        override val message: String = errorMessage
    }

    object RetryLimitExceeded : NetworkError() {
        private fun readResolve(): Any = RetryLimitExceeded
        override val message: String = "네트워크 요청 재시도 제한 횟수를 초과했습니다."
    }
}
