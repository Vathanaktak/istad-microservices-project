package kh.edu.istad.platform.iam.application.command;

public record ResetPasswordWithTokenCommand(String token, String newPassword) {}
