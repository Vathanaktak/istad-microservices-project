package kh.edu.istad.platform.iam.application.result;

public record ClientCreatedResult(ClientSummaryResult client, String secret) {}
