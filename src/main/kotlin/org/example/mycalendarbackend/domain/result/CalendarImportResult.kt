package org.example.mycalendarbackend.domain.result

import org.springframework.http.HttpStatus

abstract class CalendarImportResult(
    val message: String,
    val httpStatus: HttpStatus
) {
    object EmptyFile : CalendarImportResult(
        message = "File is empty",
        httpStatus = HttpStatus.BAD_REQUEST
    )

    object InvalidFileType : CalendarImportResult(
        message = "Invalid file type",
        httpStatus = HttpStatus.BAD_REQUEST
    )

    object ProcessingError : CalendarImportResult(
        message = "Error processing file",
        httpStatus = HttpStatus.INTERNAL_SERVER_ERROR
    )

    object Success : CalendarImportResult(
        message = "File processed successfully",
        httpStatus = HttpStatus.OK
    )
}
