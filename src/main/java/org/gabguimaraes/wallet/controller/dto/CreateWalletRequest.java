package org.gabguimaraes.wallet.controller.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.UUID;
import jakarta.validation.constraints.NotNull;

public record CreateWalletRequest(
        @NotNull
        @JsonAlias("user_id")
        UUID userId
) {
}
