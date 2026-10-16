package com.example.blackbox.telemetry;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

@Service
public class TelemetryService {

    private static final Logger log = LoggerFactory.getLogger(TelemetryService.class);
    private final Tracer tracer;

    public TelemetryService() {
        Tracer localTracer;
        try {
            SdkTracerProvider tracerProvider = SdkTracerProvider.builder().build();
            OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
                    .setTracerProvider(tracerProvider)
                    .build();
            localTracer = openTelemetry.getTracer("com.example.blackbox", "1.0.0");
        } catch (Exception e) {
            log.warn("Could not initialize OpenTelemetry SDK, using default Global tracer: {}", e.getMessage());
            localTracer = GlobalOpenTelemetry.getTracer("com.example.blackbox", "1.0.0");
        }
        this.tracer = localTracer;
    }

    public Span startSpan(String spanName) {
        return tracer.spanBuilder(spanName).startSpan();
    }

    public <T> T trace(String spanName, Map<String, String> attributes, Callable<T> operation) throws Exception {
        Span span = tracer.spanBuilder(spanName).startSpan();
        if (attributes != null) {
            attributes.forEach(span::setAttribute);
        }
        try (Scope ignored = span.makeCurrent()) {
            T result = operation.call();
            span.setStatus(StatusCode.OK);
            return result;
        } catch (Exception ex) {
            span.setStatus(StatusCode.ERROR, ex.getMessage());
            span.recordException(ex);
            throw ex;
        } finally {
            span.end();
        }
    }

    public void traceRunnable(String spanName, Map<String, String> attributes, Runnable operation) {
        Span span = tracer.spanBuilder(spanName).startSpan();
        if (attributes != null) {
            attributes.forEach(span::setAttribute);
        }
        try (Scope ignored = span.makeCurrent()) {
            operation.run();
            span.setStatus(StatusCode.OK);
        } catch (Exception ex) {
            span.setStatus(StatusCode.ERROR, ex.getMessage());
            span.recordException(ex);
            throw ex;
        } finally {
            span.end();
        }
    }

    public String generateTraceId() {
        Span currentSpan = Span.current();
        if (currentSpan.getSpanContext().isValid()) {
            return currentSpan.getSpanContext().getTraceId();
        }
        // Generate valid W3C Trace ID format (32 lowercase hex characters)
        return UUID.randomUUID().toString().replace("-", "") +
                UUID.randomUUID().toString().replace("-", "").substring(0, 0);
    }

    public String generateSpanId() {
        Span currentSpan = Span.current();
        if (currentSpan.getSpanContext().isValid()) {
            return currentSpan.getSpanContext().getSpanId();
        }
        // Generate valid W3C Span ID format (16 lowercase hex characters)
        return UUID.randomUUID().toString().replace("-", "").substring(0, 16);
    }
}
