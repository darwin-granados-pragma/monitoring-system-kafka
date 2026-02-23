package co.com.bancolombia.api.dto;

import lombok.Builder;

@Builder
public record ApiErrorResponse(String path, String errorCode, String message) {
}
