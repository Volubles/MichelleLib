package io.voluble.michellelib.text.sanitize;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Pattern;

/**
 * Removes a curated set of "dangerous" MiniMessage tags from an input string.
 *
 * <p>This is meant for user-controlled descriptions / bios / etc. If you want true safety,
 * prefer parsing with a restricted MiniMessage allowlist (configure {@link io.voluble.michellelib.text.TextEngine#userMiniMessage()}).</p>
 */
public final class MiniMessageDangerousTagStripper {

    private MiniMessageDangerousTagStripper() {
    }

    // Matches:
    // - <tag>, <tag:arg>, <tag="arg"> (we treat everything after ':' as args), <tag/>, <tag:arg/>
    // - </tag>
    private static final @NotNull Pattern DANGEROUS_TAGS = Pattern.compile(
            "(?i)</?(?:"
                    + "click"
                    + "|hover"
                    + "|key"
                    + "|insert|insertion"
                    + "|lang|tr|translate|lang_or|tr_or|translate_or"
                    + "|selector|sel"
                    + "|score"
                    + "|nbt|data"
                    + "|newline|br"
                    + "|pride"
                    + "|sprite"
                    + "|head"
                    + "|font"
                    + "|shadow"
                    + ")(?::[^>]*)?/?>"
    );

    public static @Nullable String strip(final @Nullable String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        return DANGEROUS_TAGS.matcher(input).replaceAll("");
    }
}


