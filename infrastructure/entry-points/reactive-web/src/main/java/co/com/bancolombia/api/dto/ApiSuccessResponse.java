package co.com.bancolombia.api.dto;

import lombok.Builder;

@Builder
public record ApiSuccessResponse(Object data, boolean success, String message) {
}
