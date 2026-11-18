package kh.edu.istad.platform.iam.application.port.out;

public interface PasswordResetNotifier {
    void notifyPasswordReset(String email, String plainToken);
}
