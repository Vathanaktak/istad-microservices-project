package kh.edu.istad.platform.iam.application.result;

import java.time.Instant;

public record SessionResult(
    String id,
    String device,
    boolean current,
    String browser,
    String os,
    String ipAddress,
    String location,
    Instant lastActiveAt,
    Instant createdAt
) {}
