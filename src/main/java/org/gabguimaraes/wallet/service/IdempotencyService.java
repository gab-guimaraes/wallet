package org.gabguimaraes.wallet.service;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.gabguimaraes.wallet.exception.IdempotencyException;
import org.gabguimaraes.wallet.model.IdempotencyRequest;
import org.gabguimaraes.wallet.model.IdempotencyStatus;
import org.gabguimaraes.wallet.repository.IdempotencyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class IdempotencyService {

    private final IdempotencyRepository idempotencyRepository;

    public IdempotencyService(IdempotencyRepository idempotencyRepository) {
        this.idempotencyRepository = idempotencyRepository;
    }

    /**
     * Starts idempotent processing for the provided key.
     *
     * @param idempotencyKey the idempotency key
     */
    @Transactional
    public void startProcessing(UUID idempotencyKey) {
        OffsetDateTime now = OffsetDateTime.now();

        idempotencyRepository.findById(idempotencyKey).ifPresent(request -> {
            if (request.getStatus() == IdempotencyStatus.PROCESSING) {
                throw new IllegalStateException("Operation in progress");
            }
            if (request.getStatus() == IdempotencyStatus.COMPLETED) {
                throw new IllegalStateException("Operation already completed");
            }
        });

        // Se chegou aqui, ou não existe, ou estava FAILED -> Criar ou atualizar
        IdempotencyRequest request = idempotencyRepository.findById(idempotencyKey)
                .orElse(new IdempotencyRequest());

        request.setIdempotencyKey(idempotencyKey);
        request.setStatus(IdempotencyStatus.PROCESSING);
        request.setCreatedAt(request.getCreatedAt() == null ? now : request.getCreatedAt());
        request.setUpdatedAt(now);

        idempotencyRepository.save(request);
    }

    /**
     * Marks an idempotent request as completed and stores the response payload.
     *
     * @param idempotencyKey the idempotency key
     * @param responseBody the serialized response body
     */
    @Transactional
    public void complete(UUID idempotencyKey, String responseBody) {
        IdempotencyRequest request = idempotencyRepository.findById(idempotencyKey)
                .orElseThrow(() -> new IdempotencyException("Idempotency key not found"));

        request.setStatus(IdempotencyStatus.COMPLETED);
        request.setResponseBody(responseBody);
        request.setUpdatedAt(OffsetDateTime.now());
        idempotencyRepository.save(request);
    }

    /**
     * Marks an idempotent request as failed.
     *
     * @param idempotencyKey the idempotency key
     */
    @Transactional
    public void fail(UUID idempotencyKey) {
        IdempotencyRequest request = idempotencyRepository.findById(idempotencyKey)
                .orElseThrow(() -> new IdempotencyException("Idempotency key not found"));

        request.setStatus(IdempotencyStatus.FAILED);
        request.setUpdatedAt(OffsetDateTime.now());
        idempotencyRepository.save(request);
    }
}
