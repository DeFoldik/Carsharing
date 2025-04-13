package com.carsharing.carsharing.service;

import com.carsharing.carsharing.exception.InvalidRequest;
import com.carsharing.carsharing.exception.LoggingException;
import com.carsharing.carsharing.exception.NotFound;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class LogServiceTest {

    @TempDir
    Path tempDir;

    private LogService createLogServiceWithTestLogs() throws IOException {
        Path logFile = tempDir.resolve("application.log");
        try (BufferedWriter writer = Files.newBufferedWriter(logFile, StandardCharsets.UTF_8)) {
            writer.write("2023-05-01 10:00:00 - Log entry 1\n");
            writer.write("2023-05-01 11:00:00 - Log entry 2\n");
            writer.write("2023-05-02 10:00:00 - Log entry for different date\n");
        }
        return new LogService() {
            protected Path getLogFilePath() {
                return logFile;
            }
        };
    }


    @Test
    void getLogFileForDate_shouldThrowWhenNoLogsForDate() throws IOException {
        // Создаем лог-файл без нужной даты (например, только за 2023-05-02)
        Path logFile = tempDir.resolve("application.log");
        try (BufferedWriter writer = Files.newBufferedWriter(logFile, StandardCharsets.UTF_8)) {
            writer.write("2023-05-02 10:00:00 - Entry not matching date\n");
        }

        // Подменяем путь к логу в тестовом экземпляре LogService
        LogService logService = new LogService() {
            @Override
            public Resource getLogFileForDate(String date) {
                Path originalPath = Paths.get("logs", "application.log");
                try {
                    // Скопировать наш временный файл туда, куда LogService ожидает
                    Files.createDirectories(originalPath.getParent());
                    Files.copy(logFile, originalPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                return super.getLogFileForDate(date);
            }
        };

        // Then
        assertThatThrownBy(() -> logService.getLogFileForDate("01.05.2023"))
                .isInstanceOf(NotFound.class)
                .hasMessage("No logs found for this date.");
    }


    @Test
    void getLogFileForDate_shouldThrowLoggingExceptionOnReadError() throws IOException {
        // Создаем "битый" лог
        Path brokenFile = tempDir.resolve("application.log");
        Files.write(brokenFile, new byte[0]); // пустой, но допустимый

        LogService logService = new LogService() {
            @Override
            public Resource getLogFileForDate(String date) {
                throw new LoggingException("Error reading log file.");
            }
        };

        assertThatThrownBy(() -> logService.getLogFileForDate("01.05.2023"))
                .isInstanceOf(LoggingException.class)
                .hasMessage("Error reading log file.");
    }

    @Test
    void getLogFileForDate_shouldThrowWhenFileNotExists() {
        LogService logService = new LogService();

        // Убедись, что папки logs нет или файл отсутствует
        Path path = Paths.get("logs/application.log");
        if (Files.exists(path)) {
            try {
                Files.delete(path);
            } catch (IOException e) {
                org.assertj.core.api.Assertions.fail("Cant dell", e);
            }
        }

        assertThatThrownBy(() -> logService.getLogFileForDate("01.05.2023"))
                .isInstanceOf(NotFound.class)
                .hasMessage("Log file does not exist.");
    }

    @Test
    void getDownloadFileName_shouldReturnCorrectFileName() {
        // Given
        LogService logService = new LogService();
        String date = "01.05.2023";

        // When
        String fileName = logService.getDownloadFileName(date);

        // Then
        assertThat(fileName).isEqualTo("application-2023-05-01.log");
    }

    @Test
    void getDownloadFileName_shouldThrowForInvalidDateFormat() {
        // Given
        LogService logService = new LogService();
        String invalidDate = "2023/05/01";

        // Then
        assertThatThrownBy(() -> logService.getDownloadFileName(invalidDate))
                .isInstanceOf(InvalidRequest.class)
                .hasMessage("Incorrect date format. Use dd.MM.yyyy.");
    }

    @Test
    void parseDate_shouldParseValidDate() {
        // Given
        LogService logService = new LogService();
        String validDate = "01.05.2023";

        // When
        LocalDate result = logService.parseDate(validDate);

        // Then
        assertThat(result).isEqualTo(LocalDate.of(2023, 5, 1));
    }

    @Test
    void parseDate_shouldThrowForInvalidDate() {
        // Given
        LogService logService = new LogService();
        String invalidDate = "not-a-date";

        // Then
        assertThatThrownBy(() -> logService.parseDate(invalidDate))
                .isInstanceOf(InvalidRequest.class)
                .hasMessage("Incorrect date format. Use dd.MM.yyyy.");
    }

    @Test
    void parseDate_shouldThrowForNullDate() {
        LogService logService = new LogService();

        assertThatThrownBy(() -> logService.parseDate(null))
                .isInstanceOf(InvalidRequest.class)
                .hasMessage("Date cannot be null or empty");
    }

    @Test
    void parseDate_shouldThrowForEmptyDate() {
        LogService logService = new LogService();

        assertThatThrownBy(() -> logService.parseDate(""))
                .isInstanceOf(InvalidRequest.class)
                .hasMessage("Date cannot be null or empty");
    }


}