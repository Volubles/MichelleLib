package io.voluble.michellelib.commands;

/**
 * A command module registers one or more commands using {@link CommandRegistration}.
 *
 * <p>Keep implementations stateless (or plugin-instance-scoped) to remain safe when shaded into
 * multiple plugins.</p>
 */
@FunctionalInterface
public interface CommandModule {
    void register(CommandRegistration registration);
}


