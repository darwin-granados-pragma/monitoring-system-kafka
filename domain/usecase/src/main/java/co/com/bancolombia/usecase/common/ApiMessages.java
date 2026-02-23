package co.com.bancolombia.usecase.common;

public final class ApiMessages {
    public static final String EVENT_PUBLISHED = "Evento publicado correctamente";
    public static final String INTERNAL_ERROR = "Ocurrio un error interno procesando el evento";
    public static final String INVALID_BODY = "El body contiene campos invalidos";
    public static final String TRACE_ID_REQUIRED = "El header traceId es obligatorio";

    private ApiMessages() {
    }
}
