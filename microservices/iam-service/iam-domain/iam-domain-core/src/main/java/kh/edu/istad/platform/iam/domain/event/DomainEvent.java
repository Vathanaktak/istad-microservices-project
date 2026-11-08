package kh.edu.istad.platform.iam.domain.event;

/**
 * Marker interface for all IAM domain events.
 * <p>
 * Intentionally self-contained — {@code iam-domain-core} has no dependency on
 * {@code istad-common-domain}, keeping the core pure-Java with no framework coupling.
 *
 * @param <T> the aggregate root type that produced this event
 */
public interface DomainEvent<T> {
}
