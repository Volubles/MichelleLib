package io.voluble.michellelib.registry.validation;

import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.key.Key;
import org.bukkit.Keyed;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Utilities for validating registry keys from user input (configs, commands, etc.).
 */
public final class RegistryValidation {

    private RegistryValidation() {
    }

    /**
     * Result of validating a registry entry.
     */
    public static final class ValidationResult<T extends Keyed> {
        private final @NotNull String input;
        private final @NotNull Optional<T> value;
        private final @NotNull Optional<String> error;

        private ValidationResult(
                final @NotNull String input,
                final @NotNull Optional<T> value,
                final @NotNull Optional<String> error
        ) {
            this.input = input;
            this.value = value;
            this.error = error;
        }

        @NotNull
        public String input() {
            return this.input;
        }

        @NotNull
        public Optional<T> value() {
            return this.value;
        }

        @NotNull
        public Optional<String> error() {
            return this.error;
        }

        public boolean isValid() {
            return this.value.isPresent();
        }

        @NotNull
        public static <T extends Keyed> ValidationResult<T> valid(final @NotNull String input, final @NotNull T value) {
            return new ValidationResult<>(input, Optional.of(value), Optional.empty());
        }

        @NotNull
        public static <T extends Keyed> ValidationResult<T> invalid(final @NotNull String input, final @NotNull String error) {
            return new ValidationResult<>(input, Optional.empty(), Optional.of(error));
        }
    }

    /**
     * Validates a single registry key from a string input.
     *
     * @param registryKey the registry key
     * @param input the input string (e.g., "minecraft:diamond_sword" or "diamond_sword")
     * @param <T> the entry type
     * @return a validation result
     */
    @NotNull
    public static <T extends Keyed> ValidationResult<T> validate(
            final @NotNull RegistryKey<T> registryKey,
            final @NotNull String input
    ) {
        if (input == null || input.trim().isEmpty()) {
            return ValidationResult.invalid(input, "Input is empty");
        }

        try {
            final Key key = parseKey(input);
            final T value = io.voluble.michellelib.registry.Registries.registry(registryKey).get(key);
            
            if (value == null) {
                return ValidationResult.invalid(input, "No entry found for key '" + input + "' in registry " + registryKey);
            }
            
            return ValidationResult.valid(input, value);
        } catch (final IllegalArgumentException e) {
            return ValidationResult.invalid(input, "Invalid key format: " + e.getMessage());
        }
    }

    /**
     * Validates multiple registry keys from a list of strings.
     *
     * @param registryKey the registry key
     * @param inputs the list of input strings
     * @param <T> the entry type
     * @return a list of validation results
     */
    @NotNull
    public static <T extends Keyed> List<ValidationResult<T>> validateMultiple(
            final @NotNull RegistryKey<T> registryKey,
            final @NotNull List<String> inputs
    ) {
        final List<ValidationResult<T>> results = new ArrayList<>();
        for (final String input : inputs) {
            results.add(validate(registryKey, input));
        }
        return results;
    }

    /**
     * Validates multiple registry keys and returns only the valid entries.
     *
     * @param registryKey the registry key
     * @param inputs the list of input strings
     * @param <T> the entry type
     * @return a list of valid entries
     */
    @NotNull
    public static <T extends Keyed> List<T> validateAndCollect(
            final @NotNull RegistryKey<T> registryKey,
            final @NotNull List<String> inputs
    ) {
        return validateMultiple(registryKey, inputs).stream()
                .filter(ValidationResult::isValid)
                .map(result -> result.value().orElseThrow())
                .collect(Collectors.toList());
    }

    /**
     * Validates multiple registry keys and returns a report with all errors.
     *
     * @param registryKey the registry key
     * @param inputs the list of input strings
     * @param <T> the entry type
     * @return a validation report
     */
    @NotNull
    public static <T extends Keyed> ValidationReport<T> validateReport(
            final @NotNull RegistryKey<T> registryKey,
            final @NotNull List<String> inputs
    ) {
        final List<ValidationResult<T>> results = validateMultiple(registryKey, inputs);
        return new ValidationReport<>(results);
    }

    /**
     * Report containing validation results for multiple inputs.
     */
    public static final class ValidationReport<T extends Keyed> {
        private final @NotNull List<ValidationResult<T>> results;

        ValidationReport(final @NotNull List<ValidationResult<T>> results) {
            this.results = List.copyOf(results);
        }

        @NotNull
        public List<ValidationResult<T>> results() {
            return this.results;
        }

        @NotNull
        public List<T> validValues() {
            return this.results.stream()
                    .filter(ValidationResult::isValid)
                    .map(result -> result.value().orElseThrow())
                    .collect(Collectors.toList());
        }

        @NotNull
        public List<String> invalidInputs() {
            return this.results.stream()
                    .filter(result -> !result.isValid())
                    .map(ValidationResult::input)
                    .collect(Collectors.toList());
        }

        @NotNull
        public List<String> errors() {
            return this.results.stream()
                    .filter(result -> !result.isValid())
                    .map(result -> result.error().orElse("Unknown error"))
                    .collect(Collectors.toList());
        }

        public boolean isValid() {
            return this.results.stream().allMatch(ValidationResult::isValid);
        }

        @NotNull
        public String formatErrors() {
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < this.results.size(); i++) {
                final ValidationResult<T> result = this.results.get(i);
                if (!result.isValid()) {
                    sb.append(result.input()).append(": ").append(result.error().orElse("Unknown error"));
                    if (i < this.results.size() - 1) {
                        sb.append(", ");
                    }
                }
            }
            return sb.toString();
        }
    }

    private static @NotNull Key parseKey(final @NotNull String input) {
        if (input.contains(":")) {
            return Key.key(input);
        } else {
            return Key.key("minecraft", input);
        }
    }
}

